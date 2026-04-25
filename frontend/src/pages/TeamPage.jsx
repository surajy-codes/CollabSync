import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../api/axios'

export default function TeamPage() {
  const { teamId } = useParams()
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [team, setTeam] = useState(null)
  const [projects, setProjects] = useState([])
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState({ name: '', description: '' })
  const [showInvite, setShowInvite] = useState(false)
  const [inviteForm, setInviteForm] = useState({ email: '', role: 'MEMBER' })
  const [inviteMsg, setInviteMsg] = useState('')

  useEffect(() => {
    api.get(`/teams/${teamId}`).then(res => setTeam(res.data))
    api.get(`/teams/${teamId}/projects`).then(res => setProjects(res.data))
  }, [teamId])

  const createProject = async (e) => {
    e.preventDefault()
    const res = await api.post(`/teams/${teamId}/projects`, form)
    setProjects([...projects, res.data])
    setForm({ name: '', description: '' })
    setShowCreate(false)
  }

  const inviteMember = async (e) => {
  e.preventDefault()
  try {
    await api.post(`/teams/${teamId}/invite`, inviteForm)
    setInviteMsg('Member invited successfully!')
    setInviteForm({ email: '', role: 'MEMBER' })
  } catch {
    setInviteMsg('User not found or already a member.')
  }
}

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      {/* Navbar */}
      <div className="bg-gray-900 border-b border-gray-800 px-8 py-4 flex justify-between items-center">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate('/dashboard')} className="text-gray-400 hover:text-white transition">
            ← Dashboard
          </button>
          <span className="text-gray-600">/</span>
          <h1 className="text-lg font-semibold">{team?.name}</h1>
        </div>
        <div className="flex items-center gap-4">
          <span className="text-gray-400 text-sm">Hi, {user?.name}</span>
          <button onClick={logout} className="text-sm bg-gray-800 hover:bg-gray-700 px-3 py-1.5 rounded-lg transition">
            Logout
          </button>
        </div>
      </div>

      <div className="max-w-5xl mx-auto px-8 py-10">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-semibold">Projects</h2>
          <div className="flex gap-3">
            <button
              onClick={() => setShowInvite(true)}
              className="bg-gray-800 hover:bg-gray-700 px-4 py-2 rounded-lg text-sm font-medium transition"
            >
              + Invite Member
            </button>
            <button
              onClick={() => setShowCreate(true)}
              className="bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg text-sm font-medium transition"
            >
              + New Project
            </button>
          </div>
        </div>

        {projects.length === 0 ? (
          <div className="text-center py-20 text-gray-500">
            <p className="text-lg">No projects yet.</p>
            <p className="text-sm mt-1">Create your first project to get started.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {projects.map(project => (
              <div
                key={project.id}
                onClick={() => navigate(`/projects/${project.id}`)}
                className="bg-gray-900 border border-gray-800 hover:border-blue-500 rounded-xl p-5 cursor-pointer transition"
              >
                <div className="flex justify-between items-start mb-2">
                  <h3 className="text-lg font-semibold">{project.name}</h3>
                  <span className={`text-xs px-2 py-0.5 rounded-full ${
                    project.status === 'ACTIVE'
                      ? 'bg-green-900 text-green-400'
                      : 'bg-gray-800 text-gray-400'
                  }`}>
                    {project.status}
                  </span>
                </div>
                <p className="text-gray-400 text-sm">{project.description || 'No description'}</p>
                <p className="text-gray-600 text-xs mt-3">Created by {project.createdBy}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      {showCreate && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
          <div className="bg-gray-900 rounded-2xl p-6 w-full max-w-md">
            <h3 className="text-lg font-semibold mb-4">Create New Project</h3>
            <form onSubmit={createProject} className="space-y-3">
              <input
                type="text"
                placeholder="Project name"
                className="w-full bg-gray-800 text-white px-4 py-3 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
                value={form.name}
                onChange={e => setForm({ ...form, name: e.target.value })}
                required
              />
              <textarea
                placeholder="Description (optional)"
                className="w-full bg-gray-800 text-white px-4 py-3 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                rows={3}
                value={form.description}
                onChange={e => setForm({ ...form, description: e.target.value })}
              />
              <div className="flex gap-3 pt-1">
                <button type="submit" className="flex-1 bg-blue-600 hover:bg-blue-700 py-2.5 rounded-lg font-medium transition">
                  Create
                </button>
                <button type="button" onClick={() => setShowCreate(false)} className="flex-1 bg-gray-800 hover:bg-gray-700 py-2.5 rounded-lg font-medium transition">
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      
      {showInvite && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
          <div className="bg-gray-900 rounded-2xl p-6 w-full max-w-md">
            <h3 className="text-lg font-semibold mb-4">Invite Member</h3>
            {inviteMsg && (
              <p className={`text-sm mb-3 ${inviteMsg.includes('success') ? 'text-green-400' : 'text-red-400'}`}>
                {inviteMsg}
              </p>
            )}
            <form onSubmit={inviteMember} className="space-y-3">
              <input
                type="email"
                placeholder="Email address"
                className="w-full bg-gray-800 text-white px-4 py-3 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
                value={inviteForm.email}
                onChange={e => setInviteForm({ ...inviteForm, email: e.target.value })}
                required
              />
              <select
                className="w-full bg-gray-800 text-white px-4 py-3 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
                value={inviteForm.role}
                onChange={e => setInviteForm({ ...inviteForm, role: e.target.value })}
              >
                <option value="MEMBER">Member</option>
                <option value="VIEWER">Viewer</option>
              </select>
              <div className="flex gap-3 pt-1">
                <button type="submit" className="flex-1 bg-blue-600 hover:bg-blue-700 py-2.5 rounded-lg font-medium transition">
                  Send Invite
                </button>
                <button
                  type="button"
                  onClick={() => { setShowInvite(false); setInviteMsg('') }}
                  className="flex-1 bg-gray-800 hover:bg-gray-700 py-2.5 rounded-lg font-medium transition"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}