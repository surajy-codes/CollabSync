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
  TODO: 'border-gray-600',
  IN_PROGRESS: 'border-blue-500',
  IN_REVIEW: 'border-yellow-500',
  DONE: 'border-green-500'
}

const PRIORITY_COLORS = {
  LOW: 'text-gray-400',
  MEDIUM: 'text-blue-400',
  HIGH: 'text-orange-400',
  CRITICAL: 'text-red-400'
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

  const updateStatus = async (taskId, newStatus) => {
    const task = tasks.find(t => t.id === taskId)
    await api.put(`/tasks/${taskId}`, { ...task, status: newStatus })
    loadTasks()
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

  const tasksByStatus = (status) => tasks.filter(t => t.status === status)

  return (
    <div className="min-h-screen bg-gray-950 text-white flex flex-col">
      {/* Navbar */}
      <div className="bg-gray-900 border-b border-gray-800 px-8 py-4 flex justify-between items-center shrink-0">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate(-1)} className="text-gray-400 hover:text-white transition">
            ← Back
          </button>
          <span className="text-gray-600">/</span>
          <h1 className="text-lg font-semibold">{project?.name}</h1>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate(`/projects/${projectId}/chat`)}
            className="bg-gray-800 hover:bg-gray-700 px-3 py-1.5 rounded-lg text-sm transition"
          >
            💬 Chat
          </button>
          <button
            onClick={() => setShowCreate(true)}
            className="bg-blue-600 hover:bg-blue-700 px-4 py-1.5 rounded-lg text-sm font-medium transition"
          >
            + New Task
          </button>
          <button onClick={logout} className="text-sm bg-gray-800 hover:bg-gray-700 px-3 py-1.5 rounded-lg transition">
            Logout
          </button>
        </div>
      </div>

      {/* Kanban Board */}
      <div className="flex gap-4 p-6 overflow-x-auto flex-1">
        {STATUSES.map(status => (
          <div key={status} className="flex-shrink-0 w-72">
            <div className={`border-t-2 ${STATUS_COLORS[status]} bg-gray-900 rounded-xl p-4`}>
              <div className="flex justify-between items-center mb-4">
                <h3 className="font-semibold text-sm">{STATUS_LABELS[status]}</h3>
                <span className="bg-gray-800 text-gray-400 text-xs px-2 py-0.5 rounded-full">
                  {tasksByStatus(status).length}
                </span>
              </div>

              <div className="space-y-3">
                {tasksByStatus(status).map(task => (
                  <div
                    key={task.id}
                    onClick={() => setSelectedTask(task)}
                    className="bg-gray-800 hover:bg-gray-750 rounded-lg p-3 cursor-pointer border border-gray-700 hover:border-gray-500 transition"
                  >
                    <p className="text-sm font-medium mb-2">{task.title}</p>
                    <div className="flex justify-between items-center">
                      <span className={`text-xs font-medium ${PRIORITY_COLORS[task.priority]}`}>
                        {task.priority}
                      </span>
                      {task.dueDate && (
                        <span className="text-xs text-gray-500">{task.dueDate}</span>
                      )}
                    </div>
                    {task.assignee && (
                      <p className="text-xs text-gray-500 mt-1">👤 {task.assignee}</p>
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
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
          <div className="bg-gray-900 rounded-2xl p-6 w-full max-w-lg">
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
                  className="w-full bg-gray-800 text-white px-4 py-3 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                  rows={4}
                  value={form.description}
                  onChange={e => setForm({ ...form, description: e.target.value })}
                />
                <button
                  type="button"
                  onClick={generateDescription}
                  disabled={aiLoading || !form.title}
                  className="absolute bottom-3 right-3 bg-purple-700 hover:bg-purple-600 disabled:opacity-50 text-xs px-2 py-1 rounded transition"
                >
                  {aiLoading ? 'Generating...' : '✨ AI Generate'}
                </button>
              </div>
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
              <div className="flex gap-3 pt-1">
                <button type="submit" className="flex-1 bg-blue-600 hover:bg-blue-700 py-2.5 rounded-lg font-medium transition">
                  Create Task
                </button>
                <button type="button" onClick={() => setShowCreate(false)} className="flex-1 bg-gray-800 hover:bg-gray-700 py-2.5 rounded-lg font-medium transition">
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Task Detail Modal */}
      {selectedTask && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
          <div className="bg-gray-900 rounded-2xl p-6 w-full max-w-lg max-h-[80vh] overflow-y-auto">
            <div className="flex justify-between items-start mb-4">
              <h3 className="text-lg font-semibold">{selectedTask.title}</h3>
              <button onClick={() => setSelectedTask(null)} className="text-gray-400 hover:text-white">✕</button>
            </div>
            <p className="text-gray-400 text-sm mb-4">{selectedTask.description || 'No description'}</p>
            <div className="grid grid-cols-2 gap-3 mb-4 text-sm">
              <div className="bg-gray-800 rounded-lg p-3">
                <p className="text-gray-500 text-xs mb-1">Status</p>
                <select
                  className="bg-transparent text-white outline-none w-full"
                  value={selectedTask.status}
                  onChange={e => {
                    updateStatus(selectedTask.id, e.target.value)
                    setSelectedTask({ ...selectedTask, status: e.target.value })
                  }}
                >
                  {STATUSES.map(s => <option key={s} value={s}>{STATUS_LABELS[s]}</option>)}
                </select>
              </div>
              <div className="bg-gray-800 rounded-lg p-3">
                <p className="text-gray-500 text-xs mb-1">Priority</p>
                <p className={PRIORITY_COLORS[selectedTask.priority]}>{selectedTask.priority}</p>
              </div>
              <div className="bg-gray-800 rounded-lg p-3">
                <p className="text-gray-500 text-xs mb-1">Assignee</p>
                <p>{selectedTask.assignee || 'Unassigned'}</p>
              </div>
              <div className="bg-gray-800 rounded-lg p-3">
                <p className="text-gray-500 text-xs mb-1">Due Date</p>
                <p>{selectedTask.dueDate || 'No due date'}</p>
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
    </div>
  )
}