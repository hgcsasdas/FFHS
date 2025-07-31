import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthProvider'
import { toast } from 'react-toastify'
import { FolderIcon, Lock, User } from 'lucide-react'

const LoginPage: React.FC = () => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      await login(username, password)
      toast.success('¡Bienvenido a HGC FFHS!')
      navigate('/buckets')
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Error al iniciar sesión')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-secondary p-4">
      <div className="absolute top-8 left-8 flex items-center space-x-2">
        <FolderIcon className="w-8 h-8 text-primary-foreground" />
        <h1 className="text-2xl font-bold text-primary-foreground">HGC FFHS</h1>
      </div>
      <form
        onSubmit={handleSubmit}
        className="bg-card p-8 rounded-2xl shadow-xl w-full max-w-sm space-y-6"
      >
        <h2 className="text-2xl font-bold text-primary text-center">
          Inicia Sesión
        </h2>

        <div className="space-y-2">
          <label className="relative block">
            <User className="absolute left-3 top-3 w-5 h-5 text-muted-foreground" />
            <input
              type="text"
              placeholder="Usuario"
              value={username}
              onChange={e => setUsername(e.target.value)}
              className="w-full pl-10 pr-3 py-2 border border-border rounded-lg bg-secondary"
              required
            />
          </label>

          <label className="relative block">
            <Lock className="absolute left-3 top-3 w-5 h-5 text-muted-foreground" />
            <input
              type="password"
              placeholder="Contraseña"
              value={password}
              onChange={e => setPassword(e.target.value)}
              className="w-full pl-10 pr-3 py-2 border border-border rounded-lg bg-secondary"
              required
            />
          </label>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full flex items-center justify-center space-x-2 py-2 rounded-lg bg-primary hover:bg-primary/90 text-primary-foreground font-semibold transition"
        >
          {loading && (
            <svg
              className="animate-spin h-5 w-5 text-primary-foreground"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
              />
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
              />
            </svg>
          )}
          <span>{loading ? 'Entrando…' : 'Entrar'}</span>
        </button>
      </form>
    </div>
  )
}

export default LoginPage
