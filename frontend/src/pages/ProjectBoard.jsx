import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../api/axios'

const STATUSES = ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE']
const STATUS_LABELS = { TODO: 'To Do', IN_PROGRESS: 'In Progress', IN_REVIEW: 'In Review', DONE: 'Done' }
const STATUS_COLORS = { TODO: 'border-t-gray-500', IN_PROGRESS: 'border-t-blue-500', IN_REVIEW: 'border-t-yellow-500', DONE: 'border-t-green-500' }
const PRIORITY_BADGE = { LOW: 'bg-gray-700 text-gray-300', MEDIUM: 'bg-blue-900 text-blue-300', HIGH: 'bg-orange-900 text-orange-300', CRITICAL: 'bg-red-900 text-red-300' }

export default function ProjectBoard() {
  const { projectId } = useParams()
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const [project, setProject] = useState(null)
  const [tasks, setTasks] = useState([])
  const [members, setMembers] = useState([])
  const [showCreate, setShowCreate] = useState(false)
  const [selectedTask, setSelectedTask] = useState(null)
  const [form, setForm] = useState({ title: '', description: '', priority: 'MEDIUM', dueDate: '', assigneeId: '' })
  const [aiLoading, setAiLoading] = useState(false)

  // Task detail state
  const [comments, setComments] = useState([])
  const [attachments, setAttachments] = useState([])
  const [subtasks, setSubtasks] = useState([])
  const [newComment, setNewComment] = useState('')
  const [newSubtask, setNewSubtask] = useState('')
  const fileInputRef = useRef(null)
  const [weeklySummary, setWeeklySummary] = useState('')
  const [showSummary, setShowSummary] = useState(false)
  const [summaryLoading, setSummaryLoading] = useState(false)



  useEffect(() => {
    api.get(`/projects/${projectId}`).then(res => {
      setProject(res.data)
      // Load team members for assignment dropdown
      api.get(`/teams/${res.data.teamId}/members`).then(r => setMembers(r.data)).catch(() => {})
    })
    loadTasks()
  }, [projectId])

  useEffect(() => {
    if (selectedTask) {
      loadComments(selectedTask.id)
      loadAttachments(selectedTask.id)
      loadSubtasks(selectedTask.id)
    }
  }, [selectedTask?.id])

  const loadTasks = () => api.get(`/projects/${projectId}/tasks?size=100`).then(res => setTasks(res.data.content))
  const loadComments = (taskId) => api.get(`/tasks/${taskId}/comments`).then(res => setComments(res.data.content))
  const loadAttachments = (taskId) => api.get(`/tasks/${taskId}/attachments`).then(res => setAttachments(res.data))
  const loadSubtasks = (taskId) => {
    const task = tasks.find(t => t.id === taskId)
    if (task) setSubtasks(task.subtasks || [])
  }

  const createTask = async (e) => {
    e.preventDefault()
    const payload = { ...form, assigneeId: form.assigneeId ? Number(form.assigneeId) : null }
    await api.post(`/projects/${projectId}/tasks`, payload)
    setForm({ title: '', description: '', priority: 'MEDIUM', dueDate: '', assigneeId: '' })
    setShowCreate(false)
    loadTasks()
  }

  const updateStatus = async (task, newStatus) => {
    await api.put(`/tasks/${task.id}`, {
      title: task.title,
      description: task.description,
      priority: task.priority,
      status: newStatus,
      assigneeId: task.assigneeId,
      dueDate: task.dueDate
    })
    loadTasks()
    if (selectedTask?.id === task.id) setSelectedTask({ ...selectedTask, status: newStatus })
  }

  const generateDescription = async () => {
    if (!form.title) return
    setAiLoading(true)
    try {
      const res = await api.post('/ai/generate-description', { title: form.title, projectName: project?.name })
      setForm({ ...form, description: res.data.description })
    } finally { setAiLoading(false) }
  }

  const generateWeeklySummary = async () => {
  setSummaryLoading(true)
  setShowSummary(true)
  try {
    const res = await api.post(`/ai/weekly-summary/${projectId}`)
    setWeeklySummary(res.data.summary)
  } finally {
    setSummaryLoading(false)
  }
}

  const addComment = async (e) => {
    e.preventDefault()
    if (!newComment.trim()) return
    await api.post(`/tasks/${selectedTask.id}/comments`, { content: newComment })
    setNewComment('')
    loadComments(selectedTask.id)
  }

  const deleteComment = async (commentId) => {
    await api.delete(`/comments/${commentId}`)
    loadComments(selectedTask.id)
  }

  const uploadFile = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    const formData = new FormData()
    formData.append('file', file)
    await api.post(`/tasks/${selectedTask.id}/attachments`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    loadAttachments(selectedTask.id)
  }

  const downloadFile = async (attachmentId, fileName) => {
    const res = await api.get(`/attachments/${attachmentId}/download`)
    window.open(res.data.url, '_blank')
  }

  const deleteAttachment = async (attachmentId) => {
    await api.delete(`/attachments/${attachmentId}`)
    loadAttachments(selectedTask.id)
  }

  const addSubtask = async (e) => {
    e.preventDefault()
    if (!newSubtask.trim()) return
    await api.post(`/tasks/${selectedTask.id}/subtasks`, { title: newSubtask, completed: false })
    setNewSubtask('')
    loadTasks().then(() => {
      const updated = tasks.find(t => t.id === selectedTask.id)
      if (updated) setSubtasks(updated.subtasks || [])
    })
    // reload subtasks via task refresh
    api.get(`/projects/${projectId}/tasks?size=100`).then(res => {
      const updated = res.data.content.find(t => t.id === selectedTask.id)
      setTasks(res.data.content)
      if (updated) setSubtasks(updated.subtasks || [])
    })
  }

  const toggleSubtask = async (subtask) => {
    await api.put(`/tasks/${selectedTask.id}/subtasks/${subtask.id}`, {
      title: subtask.title,
      completed: !subtask.completed
    })
    api.get(`/projects/${projectId}/tasks?size=100`).then(res => {
      const updated = res.data.content.find(t => t.id === selectedTask.id)
      setTasks(res.data.content)
      if (updated) setSubtasks(updated.subtasks || [])
    })
  }

  const tasksByStatus = (status) => tasks.filter(t => t.status === status)

  return (
    <div className="min-h-screen bg-gray-950 text-white flex flex-col">
      {/* Navbar */}
      
      <div className="bg-gray-900 border-b border-gray-800 px-6 py-3 flex justify-between items-center shrink-0">

        

        <div className="flex items-center gap-3">
          <button onClick={() => navigate(-1)} className="text-gray-400 hover:text-white text-sm transition">← Back</button>
          <span className="text-gray-600">/</span>
          <h1 className="text-base font-semibold">{project?.name}</h1>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={generateWeeklySummary}
            className="bg-purple-800 hover:bg-purple-700 px-3 py-1.5 rounded-lg text-xs transition"
          >
            ✨ Weekly Summary
          </button>
          <button onClick={() => navigate(`/projects/${projectId}/chat`)} className="bg-gray-800 hover:bg-gray-700 px-3 py-1.5 rounded-lg text-sm transition">💬 Chat</button>
          <button onClick={() => setShowCreate(true)} className="bg-blue-600 hover:bg-blue-700 px-3 py-1.5 rounded-lg text-sm font-medium transition">+ New Task</button>
          <button onClick={logout} className="text-xs bg-gray-800 hover:bg-gray-700 px-3 py-1.5 rounded-lg transition">Logout</button>
        </div>
      </div>

      <div className="flex flex-1 overflow-hidden">
        {/* Kanban Board */}
        <div className={`flex gap-4 p-5 overflow-x-auto transition-all ${selectedTask ? 'w-1/2' : 'w-full'}`}>
          {STATUSES.map(status => (
            <div key={status} className="flex-shrink-0 w-64">
              <div className={`border-t-4 ${STATUS_COLORS[status]} bg-gray-900 rounded-xl`}>
                <div className="flex justify-between items-center px-4 pt-4 pb-3">
                  <h3 className="font-semibold text-sm">{STATUS_LABELS[status]}</h3>
                  <span className="bg-gray-800 text-gray-400 text-xs px-2 py-0.5 rounded-full">{tasksByStatus(status).length}</span>
                </div>
                <div className="px-3 pb-4 space-y-2 min-h-20">
                  {tasksByStatus(status).map(task => (
                    <div
                      key={task.id}
                      onClick={() => setSelectedTask(task)}
                      className={`bg-gray-800 rounded-lg p-3 cursor-pointer border transition ${selectedTask?.id === task.id ? 'border-blue-500' : 'border-gray-700 hover:border-gray-500'}`}
                    >
                      <p className="text-sm font-medium mb-2">{task.title}</p>
                      <div className="flex justify-between items-center">
                        <span className={`text-xs px-2 py-0.5 rounded-full ${PRIORITY_BADGE[task.priority]}`}>{task.priority}</span>
                        {task.dueDate && <span className="text-xs text-gray-500">{task.dueDate}</span>}
                      </div>
                      {task.assignee && <p className="text-xs text-gray-500 mt-1">👤 {task.assignee}</p>}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Task Detail Side Panel */}
        {selectedTask && (
          <div className="w-1/2 bg-gray-900 border-l border-gray-800 flex flex-col overflow-hidden">
            {/* Panel Header */}
            <div className="flex justify-between items-center px-5 py-4 border-b border-gray-800">
              <h2 className="font-semibold text-base truncate pr-4">{selectedTask.title}</h2>
              <div className="flex items-center gap-3">
                <button
                  onClick={async () => {
                    await api.delete(`/tasks/${selectedTask.id}`)
                    setSelectedTask(null)
                    loadTasks()
                  }}
                  className="text-xs text-red-400 hover:text-red-300 bg-gray-800 px-2 py-1 rounded transition"
                >
                  Delete Task
                </button>
                <button onClick={() => setSelectedTask(null)} className="text-gray-400 hover:text-white text-lg shrink-0">✕</button>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto px-5 py-4 space-y-5">

              {/* Status + Priority */}
              <div className="grid grid-cols-2 gap-3">
                <div className="bg-gray-800 rounded-lg p-3">
                  <p className="text-gray-500 text-xs mb-2">Move to</p>
                  <div className="space-y-1">
                    {STATUSES.filter(s => s !== selectedTask.status).map(s => (
                      <button key={s} onClick={() => updateStatus(selectedTask, s)}
                        className="w-full text-xs text-left px-2 py-1.5 rounded bg-gray-700 hover:bg-blue-700 transition">
                        → {STATUS_LABELS[s]}
                      </button>
                    ))}
                  </div>
                </div>
                <div className="space-y-2">
                  <div className="bg-gray-800 rounded-lg p-3">
                    <p className="text-gray-500 text-xs mb-1">Status</p>
                    <p className="text-sm">{STATUS_LABELS[selectedTask.status]}</p>
                  </div>
                  <div className="bg-gray-800 rounded-lg p-3">
                    <p className="text-gray-500 text-xs mb-1">Priority</p>
                    <span className={`text-xs px-2 py-0.5 rounded-full ${PRIORITY_BADGE[selectedTask.priority]}`}>{selectedTask.priority}</span>
                  </div>
                </div>
              </div>

              {/* Assignee + Due Date */}
              <div className="grid grid-cols-2 gap-3">
                <div className="bg-gray-800 rounded-lg p-3">
                  <p className="text-gray-500 text-xs mb-1">Assignee</p>
                  <p className="text-sm">{selectedTask.assignee || 'Unassigned'}</p>
                </div>
                <div className="bg-gray-800 rounded-lg p-3">
                  <p className="text-gray-500 text-xs mb-1">Due Date</p>
                  <p className="text-sm">{selectedTask.dueDate || '—'}</p>
                </div>
              </div>

              {/* Description */}
              {selectedTask.description && (
                <div className="bg-gray-800 rounded-lg p-3">
                  <p className="text-gray-500 text-xs mb-2">Description</p>
                  <p className="text-sm text-gray-300 whitespace-pre-wrap leading-relaxed">{selectedTask.description}</p>
                </div>
              )}

              {/* Subtasks */}
              <div>
                <p className="text-sm font-semibold mb-2">Subtasks</p>
                <div className="space-y-1 mb-2">
                  {subtasks.length === 0 && <p className="text-xs text-gray-600">No subtasks yet</p>}
                  {subtasks.map(st => (
                    <div key={st.id} className="flex items-center justify-between bg-gray-800 rounded-lg px-3 py-2">
                      <div className="flex items-center gap-2 flex-1">
                        <input type="checkbox" checked={st.completed} onChange={() => toggleSubtask(st)}
                          className="accent-blue-500 cursor-pointer" />
                        <span className={`text-sm flex-1 ${st.completed ? 'line-through text-gray-500' : ''}`}>{st.title}</span>
                      </div>
                      <button
                        onClick={async () => {
                          await api.delete(`/tasks/${selectedTask.id}/subtasks/${st.id}`)
                          api.get(`/projects/${projectId}/tasks?size=100`).then(res => {
                            const updated = res.data.content.find(t => t.id === selectedTask.id)
                            setTasks(res.data.content)
                            if (updated) setSubtasks(updated.subtasks || [])
                          })
                        }}
                        className="text-xs text-red-400 hover:text-red-300 ml-2"
                      >✕</button>
                    </div>
                  ))}
                </div>
                <form onSubmit={addSubtask} className="flex gap-2">
                  <input type="text" placeholder="Add subtask..." value={newSubtask}
                    onChange={e => setNewSubtask(e.target.value)}
                    className="flex-1 bg-gray-800 text-white text-sm px-3 py-2 rounded-lg outline-none focus:ring-1 focus:ring-blue-500" />
                  <button type="submit" className="bg-gray-700 hover:bg-gray-600 px-3 py-2 rounded-lg text-sm transition">Add</button>
                </form>
              </div>

              {/* Attachments */}
              <div>
                <div className="flex justify-between items-center mb-2">
                  <p className="text-sm font-semibold">Attachments</p>
                  <button onClick={() => fileInputRef.current.click()}
                    className="text-xs bg-gray-800 hover:bg-gray-700 px-2 py-1 rounded transition">
                    + Upload
                  </button>
                  <input ref={fileInputRef} type="file" className="hidden" onChange={uploadFile} />
                </div>
                <div className="space-y-1">
                  {attachments.length === 0 && <p className="text-xs text-gray-600">No attachments yet</p>}
                  {attachments.map(att => (
                    <div key={att.id} className="flex items-center justify-between bg-gray-800 rounded-lg px-3 py-2">
                      <div>
                        <p className="text-sm truncate max-w-48">{att.fileName}</p>
                        <p className="text-xs text-gray-500">{(att.fileSize / 1024).toFixed(1)} KB</p>
                      </div>
                      <div className="flex gap-2">
                        <button onClick={() => downloadFile(att.id, att.fileName)}
                          className="text-xs text-blue-400 hover:text-blue-300">↓</button>
                        <button onClick={() => deleteAttachment(att.id)}
                          className="text-xs text-red-400 hover:text-red-300">✕</button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Comments */}
              <div>
                <p className="text-sm font-semibold mb-2">Comments</p>
                <div className="space-y-2 mb-3">
                  {comments.length === 0 && <p className="text-xs text-gray-600">No comments yet</p>}
                  {comments.map(c => (
                    <div key={c.id} className="bg-gray-800 rounded-lg px-3 py-2">
                      <div className="flex justify-between items-start">
                        <p className="text-xs text-blue-400 font-medium mb-1">{c.authorName}</p>
                        {c.authorName === user?.name && (
                          <button onClick={() => deleteComment(c.id)} className="text-xs text-red-400 hover:text-red-300">✕</button>
                        )}
                      </div>
                      <p className="text-sm text-gray-300">{c.content}</p>
                      <p className="text-xs text-gray-600 mt-1">{new Date(c.createdAt).toLocaleString()}</p>
                    </div>
                  ))}
                </div>
                <form onSubmit={addComment} className="flex gap-2">
                  <input type="text" placeholder="Add a comment..." value={newComment}
                    onChange={e => setNewComment(e.target.value)}
                    className="flex-1 bg-gray-800 text-white text-sm px-3 py-2 rounded-lg outline-none focus:ring-1 focus:ring-blue-500" />
                  <button type="submit" className="bg-blue-600 hover:bg-blue-700 px-3 py-2 rounded-lg text-sm transition">Post</button>
                </form>
              </div>

            </div>
          </div>
        )}
      </div>

      {/* Create Task Modal */}
      {showCreate && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4">
          <div className="bg-gray-900 rounded-2xl p-6 w-full max-w-lg border border-gray-700">
            <h3 className="text-lg font-semibold mb-4">Create New Task</h3>
            <form onSubmit={createTask} className="space-y-3">
              <input type="text" placeholder="Task title"
                className="w-full bg-gray-800 text-white px-4 py-3 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
                value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} required />
              <div className="relative">
                <textarea placeholder="Description (or use AI to generate)"
                  className="w-full bg-gray-800 text-white px-4 py-3 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 resize-none pr-28"
                  rows={4} value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
                <button type="button" onClick={generateDescription} disabled={aiLoading || !form.title}
                  className="absolute bottom-3 right-3 bg-purple-700 hover:bg-purple-600 disabled:opacity-40 text-xs px-2 py-1 rounded-lg transition">
                  {aiLoading ? '...' : '✨ AI'}
                </button>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <select className="w-full bg-gray-800 text-white px-4 py-3 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
                  value={form.priority} onChange={e => setForm({ ...form, priority: e.target.value })}>
                  <option value="LOW">Low Priority</option>
                  <option value="MEDIUM">Medium Priority</option>
                  <option value="HIGH">High Priority</option>
                  <option value="CRITICAL">Critical</option>
                </select>
                <input type="date"
                  className="w-full bg-gray-800 text-white px-4 py-3 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
                  value={form.dueDate} onChange={e => setForm({ ...form, dueDate: e.target.value })} />
              </div>
              <select className="w-full bg-gray-800 text-white px-4 py-3 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
                value={form.assigneeId} onChange={e => setForm({ ...form, assigneeId: e.target.value })}>
                <option value="">Unassigned</option>
                {members.map(m => <option key={m.userId} value={m.userId}>{m.userName}</option>)}
              </select>
              <div className="flex gap-3 pt-1">
                <button type="submit" className="flex-1 bg-blue-600 hover:bg-blue-700 py-2.5 rounded-lg font-medium transition">Create Task</button>
                <button type="button" onClick={() => setShowCreate(false)} className="flex-1 bg-gray-800 hover:bg-gray-700 py-2.5 rounded-lg transition">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showSummary && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4">
          <div className="bg-gray-900 rounded-2xl p-6 w-full max-w-lg border border-gray-700 max-h-[80vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold">✨ Weekly Summary</h3>
              <button onClick={() => setShowSummary(false)} className="text-gray-400 hover:text-white text-xl">✕</button>
            </div>
            {summaryLoading ? (
              <p className="text-gray-400 text-sm">Generating summary...</p>
            ) : (
              <p className="text-gray-300 text-sm leading-relaxed whitespace-pre-wrap">{weeklySummary}</p>
            )}
          </div>
        </div>
      )}

    </div>
  )
}