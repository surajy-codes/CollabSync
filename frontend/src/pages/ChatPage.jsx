import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'

export default function ChatPage() {
  const { projectId } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [connected, setConnected] = useState(false)
  const clientRef = useRef(null)
  const bottomRef = useRef(null)

  useEffect(() => {
    // Load chat history
    api.get(`/projects/${projectId}/messages`).then(res => {
      setMessages(res.data.content.reverse())
    })

    // Connect WebSocket
    const token = localStorage.getItem('accessToken')
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: { token },
      onConnect: () => {
        setConnected(true)
        client.subscribe(`/topic/projects/${projectId}/chat`, (msg) => {
          const body = JSON.parse(msg.body)
          setMessages(prev => [...prev, body])
        })
      },
      onDisconnect: () => setConnected(false),
    })

    client.activate()
    clientRef.current = client

    return () => client.deactivate()
  }, [projectId])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const sendMessage = () => {
    if (!input.trim() || !connected) return
    clientRef.current.publish({
      destination: `/app/projects/${projectId}/chat`,
      body: JSON.stringify({ content: input })
    })
    setInput('')
  }

  const handleKey = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  return (
    <div className="min-h-screen bg-gray-950 text-white flex flex-col">
      {/* Navbar */}
      <div className="bg-gray-900 border-b border-gray-800 px-8 py-4 flex justify-between items-center shrink-0">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate(-1)} className="text-gray-400 hover:text-white transition">
            ← Back
          </button>
          <span className="text-gray-600">/</span>
          <h1 className="text-lg font-semibold">Project Chat</h1>
        </div>
        <div className={`text-xs px-2 py-1 rounded-full ${connected ? 'bg-green-900 text-green-400' : 'bg-gray-800 text-gray-400'}`}>
          {connected ? '● Connected' : '○ Connecting...'}
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-6 py-4 space-y-3">
        {messages.length === 0 && (
          <p className="text-center text-gray-600 mt-10">No messages yet. Say hello!</p>
        )}
        {messages.map((msg, i) => {
          const isMe = msg.senderName === user?.name
          return (
            <div key={i} className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}>
              <div className={`max-w-sm rounded-2xl px-4 py-2.5 ${isMe ? 'bg-blue-600' : 'bg-gray-800'}`}>
                {!isMe && (
                  <p className="text-xs text-gray-400 mb-1">{msg.senderName}</p>
                )}
                <p className="text-sm">{msg.content}</p>
                <p className="text-xs text-right mt-1 opacity-50">
                  {new Date(msg.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </p>
              </div>
            </div>
          )
        })}
        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <div className="bg-gray-900 border-t border-gray-800 px-6 py-4 flex gap-3 shrink-0">
        <input
          type="text"
          placeholder="Type a message..."
          className="flex-1 bg-gray-800 text-white px-4 py-3 rounded-xl outline-none focus:ring-2 focus:ring-blue-500"
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={handleKey}
        />
        <button
          onClick={sendMessage}
          disabled={!connected || !input.trim()}
          className="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 px-5 py-3 rounded-xl font-medium transition"
        >
          Send
        </button>
      </div>
    </div>
  )
}