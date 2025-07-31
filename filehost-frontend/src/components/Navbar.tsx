import React from 'react'
import { useAuth } from '../contexts/AuthProvider'
import { useNavigate } from 'react-router-dom'

const Navbar: React.FC = () => {
  const { token, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    localStorage.clear()
    navigate('/login')
  }

  if (!token) return null // Ocultar navbar si no está autenticado

  return (
    <nav className="sticky top-0 z-50 bg-white border-b border-gray-200 shadow-sm p-4 flex justify-between items-center">
      <div className="text-xl font-semibold text-gray-800">FFHS</div>
      <button
        onClick={handleLogout}
        className="text-sm bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded"
      >
        Cerrar sesión
      </button>
    </nav>
  )
}

export default Navbar
