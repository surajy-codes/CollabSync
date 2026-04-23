import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../api/axios'

export default function Dashboard() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [teams, setTeams] = useState([])
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState({ name: '', description: '' })

  useEffect(() => {
    api.get('/teams').then(res => setTeams(res.data))
  }, [])

  const createTeam = async (e) => {
    e.preventDefault()
    const res = await api.post('/teams', form)
    setTeams([...teams, res.data])
    setForm({ name: '', description: '' })
    setShowCreate(false)
  }

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      {/* Navbar */}
      <div className="bg-gray-900 border-b border-gray-800 px-8 py-4 flex justify-between items-center">
        <h1 className="text-xl font-bold text-blue-400">CollabSync</h1>
        <div className="flex items-center gap-4">
          <span className="text-gray-400 text-sm">Hi, {user?.name}</span>
          <button onClick={logout} className="text-sm bg-gray-800 hover:bg-gray-700 px-3 py-1.5 rounded-lg transition">
            Logout
          </button>
        </div>
      </div>

      <div className="max-w-5xl mx-auto px-8 py-10">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-semibold">Your Teams</h2>
          <button
            onClick={() => setShowCreate(true)}
            className="bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg text-sm font-medium transition"
          >
            + New Team
          </button>
        </div>

        {/* Teams Grid */}
        {teams.length === 0 ? (
          <div className="text-center py-20 text-gray-500">
            <p className="text-lg">No teams yet.</p>
            <p className="text-sm mt-1">Create your first team to get started.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {teams.map(team => (
              <div
                key={team.id}
                onClick={() => navigate(`/teams/${team.id}`)}
                className="bg-gray-900 border border-gray-800 hover:border-blue-500 rounded-xl p-5 cursor-pointer transition"
              >
                <h3 className="text-lg font-semibold mb-1">{team.name}</h3>
                <p className="text-gray-400 text-sm">{team.description || 'No description'}</p>
                <p className="text-gray-600 text-xs mt-3">Created by {team.createdBy}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Create Team Modal */}
      {showCreate && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
          <div className="bg-gray-900 rounded-2xl p-6 w-full max-w-md">
            <h3 className="text-lg font-semibold mb-4">Create New Team</h3>
            <form onSubmit={createTeam} className="space-y-3">
              <input
                type="text"
                placeholder="Team name"
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
    </div>
  )
}