import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../api/axios'

const STATUSES = ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE']

const STATUS_LABELS = {
  TODO: 'To Do',
  IN_PROGRESS: 'In Progress',
  IN_REVIEW: 'In Review',
  DONE: 'Done'
}

const STATUS_COLORS = {
  TODO: 'border-t-gray-500',
  IN_PROGRESS: 'border-t-blue-500',
  IN_REVIEW: 'border-t-yellow-500',
  DONE: 'border-t-green-500'
}

const PRIORITY_BADGE = {
  LOW: 'bg-gray-700 text-gray-300',
  MEDIUM: 'bg-blue-900 text-blue-300',
  HIGH: 'bg-orange-900 text-orange-300',
  CRITICAL: 'bg-red-900 text-red-300'
}

export default function ProjectBoard() {
  const { projectId } = useParams()
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [project, setProject] = useState(null)
  const [tasks, setTasks] = useState([])
  const [showCreate, setShowCreate] = useState(false)
  const [selectedTask, setSelectedTask] = useState(null)
  const [form, setForm] = useState({ title: '', description: '', priority: 'MEDIUM', dueDate: '' })
  const [aiLoading, setAiLoading] = useState(false)
  const [weeklySummary, setWeeklySummary] = useState('')
  const [showSummary, setShowSummary] = useState(false)
  const [summaryLoading, setSummaryLoading] = useState(false)

  useEffect(() => {
    api.get(`/projects/${projectId}`).then(res => setProject(res.data))
    loadTasks()
  }, [projectId])

  const loadTasks = () => {
    api.get(`/projects/${projectId}/tasks?size=100`).then(res => setTasks(res.data.content))
  }

  const createTask = async (e) => {
    e.preventDefault()
    await api.post(`/projects/${projectId}/tasks`, form)
    setForm({ title: '', description: '', priority: 'MEDIUM', dueDate: '' })
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
    if (selectedTask?.id === task.id) {
      setSelectedTask({ ...selectedTask, status: newStatus })
    }
  }

  const generateDescription = async () => {
    if (!form.title) return
    setAiLoading(true)
    try {
      const res = await api.post('/ai/generate-description', {
        title: form.title,
        projectName: project?.name
      })
      setForm({ ...form, description: res.data.description })
    } finally {
      setAiLoading(false)
    }
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

  const tasksByStatus = (status) => tasks.filter(t => t.status === status)

  return (
    <div className="min-h-screen bg-gray-950 text-white flex flex-col">
      {/* Navbar */}
      <div className="bg-gray-900 border-b border-gray-800 px-6 py-3 flex justify-between items-center shrink-0">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate(-1)} className="text-gray-400 hover:text-white text-sm transition">
            ← Back
          </button>
          <span className="text-gray-600">/</span>
          <h1 className="text-base font-semibold">{project?.name}</h1>
          <span className="text-xs bg-green-900 text-green-400 px-2 py-0.5 rounded-full">{project?.status}</span>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={generateWeeklySummary}
            className="bg-purple-800 hover:bg-purple-700 px-3 py-1.5 rounded-lg text-xs transition"
          >
            ✨ Weekly Summary
          </button>
          <button
            onClick={() => navigate(`/projects/${projectId}/chat`)}
            className="bg-gray-800 hover:bg-gray-700 px-3 py-1.5 rounded-lg text-sm transition"
          >
            💬 Chat
          </button>
          <button
            onClick={() => setShowCreate(true)}
            className="bg-blue-600 hover:bg-blue-700 px-3 py-1.5 rounded-lg text-sm font-medium transition"
          >
            + New Task
          </button>
          <button onClick={logout} className="text-xs bg-gray-800 hover:bg-gray-700 px-3 py-1.5 rounded-lg transition">
            Logout
          </button>
        </div>
      </div>

      {/* Board */}
      <div className="flex gap-4 p-5 overflow-x-auto flex-1 items-start">
        {STATUSES.map(status => (
          <div key={status} className="flex-shrink-0 w-68">
            <div className={`border-t-4 ${STATUS_COLORS[status]} bg-gray-900 rounded-xl`}>
              <div className="flex justify-between items-center px-4 pt-4 pb-3">
                <h3 className="font-semibold text-sm tracking-wide">{STATUS_LABELS[status]}</h3>
                <span className="bg-gray-800 text-gray-400 text-xs px-2 py-0.5 rounded-full">
                  {tasksByStatus(status).length}
                </span>
              </div>
              <div className="px-3 pb-4 space-y-2 min-h-24">
                {tasksByStatus(status).map(task => (
                  <div
                    key={task.id}
                    onClick={() => setSelectedTask(task)}
                    className="bg-gray-800 hover:bg-gray-750 rounded-lg p-3 cursor-pointer border border-gray-700 hover:border-blue-500 transition group"
                  >
                    <p className="text-sm font-medium mb-2 group-hover:text-blue-300 transition">{task.title}</p>
                    {task.description && (
                      <p className="text-xs text-gray-500 mb-2 line-clamp-2">{task.description}</p>
                    )}
                    <div className="flex justify-between items-center">
                      <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${PRIORITY_BADGE[task.priority]}`}>
                        {task.priority}
                      </span>
                      {task.dueDate && (
                        <span className="text-xs text-gray-500">{task.dueDate}</span>
                      )}
                    </div>
                    {task.assignee && (
                      <p className="text-xs text-gray-500 mt-2">👤 {task.assignee}</p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Create Task Modal */}
      {showCreate && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4">
          <div className="bg-gray-900 rounded-2xl p-6 w-full max-w-lg border border-gray-700">
            <h3 className="text-lg font-semibold mb-4">Create New Task</h3>
            <form onSubmit={createTask} className="space-y-3">
              <input
                type="text"
                placeholder="Task title"
                className="w-full bg-gray-800 text-white px-4 py-3 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
                value={form.title}
                onChange={e => setForm({ ...form, title: e.target.value })}
                required
              />
              <div className="relative">
                <textarea
                  placeholder="Description (or use AI to generate)"
                  className="w-full bg-gray-800 text-white px-4 py-3 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 resize-none pr-28"
                  rows={4}
                  value={form.description}
                  onChange={e => setForm({ ...form, description: e.target.value })}
                />
                <button
                  type="button"
                  onClick={generateDescription}
                  disabled={aiLoading || !form.title}
                  className="absolute bottom-3 right-3 bg-purple-700 hover:bg-purple-600 disabled:opacity-40 text-xs px-2 py-1 rounded-lg transition"
                >
                  {aiLoading ? '...' : '✨ AI Generate'}
                </button>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <select
                  className="w-full bg-gray-800 text-white px-4 py-3 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
                  value={form.priority}
                  onChange={e => setForm({ ...form, priority: e.target.value })}
                >
                  <option value="LOW">Low Priority</option>
                  <option value="MEDIUM">Medium Priority</option>
                  <option value="HIGH">High Priority</option>
                  <option value="CRITICAL">Critical</option>
                </select>
                <input
                  type="date"
                  className="w-full bg-gray-800 text-white px-4 py-3 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
                  value={form.dueDate}
                  onChange={e => setForm({ ...form, dueDate: e.target.value })}
                />
              </div>
              <div className="flex gap-3 pt-1">
                <button type="submit" className="flex-1 bg-blue-600 hover:bg-blue-700 py-2.5 rounded-lg font-medium transition">
                  Create Task
                </button>
                <button type="button" onClick={() => setShowCreate(false)} className="flex-1 bg-gray-800 hover:bg-gray-700 py-2.5 rounded-lg transition">
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Task Detail Modal */}
      {selectedTask && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4">
          <div className="bg-gray-900 rounded-2xl p-6 w-full max-w-lg border border-gray-700">
            <div className="flex justify-between items-start mb-4">
              <h3 className="text-lg font-semibold pr-4">{selectedTask.title}</h3>
              <button onClick={() => setSelectedTask(null)} className="text-gray-400 hover:text-white text-xl leading-none">✕</button>
            </div>

            {selectedTask.description && (
              <p className="text-gray-400 text-sm mb-4 leading-relaxed">{selectedTask.description}</p>
            )}

            <div className="grid grid-cols-2 gap-3 mb-4">
              <div className="bg-gray-800 rounded-lg p-3">
                <p className="text-gray-500 text-xs mb-2">Move to</p>
                <div className="flex flex-col gap-1">
                  {STATUSES.filter(s => s !== selectedTask.status).map(s => (
                    <button
                      key={s}
                      onClick={() => updateStatus(selectedTask, s)}
                      className="text-xs text-left px-2 py-1.5 rounded bg-gray-700 hover:bg-blue-700 transition"
                    >
                      → {STATUS_LABELS[s]}
                    </button>
                  ))}
                </div>
              </div>

              <div className="space-y-3">
                <div className="bg-gray-800 rounded-lg p-3">
                  <p className="text-gray-500 text-xs mb-1">Current Status</p>
                  <p className="text-sm font-medium">{STATUS_LABELS[selectedTask.status]}</p>
                </div>
                <div className="bg-gray-800 rounded-lg p-3">
                  <p className="text-gray-500 text-xs mb-1">Priority</p>
                  <span className={`text-xs px-2 py-0.5 rounded-full ${PRIORITY_BADGE[selectedTask.priority]}`}>
                    {selectedTask.priority}
                  </span>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 mb-4 text-sm">
              <div className="bg-gray-800 rounded-lg p-3">
                <p className="text-gray-500 text-xs mb-1">Assignee</p>
                <p>{selectedTask.assignee || 'Unassigned'}</p>
              </div>
              <div className="bg-gray-800 rounded-lg p-3">
                <p className="text-gray-500 text-xs mb-1">Due Date</p>
                <p>{selectedTask.dueDate || '—'}</p>
              </div>
            </div>

            <button
              onClick={() => setSelectedTask(null)}
              className="w-full bg-gray-800 hover:bg-gray-700 py-2 rounded-lg text-sm transition"
            >
              Close
            </button>
          </div>
        </div>
      )}

      {/* Weekly Summary Modal */}
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