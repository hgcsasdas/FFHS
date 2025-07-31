import React from 'react'
import { useAuth } from '../contexts/AuthProvider'

const Footer: React.FC = () => {
  const { token } = useAuth()

  if (!token) return null // Ocultar footer si no está autenticado

  return (
    <footer className="bg-white border-t border-gray-200 p-4 text-center text-sm text-gray-500">
      © {new Date().getFullYear()} FileHost. Todos los derechos reservados.
    </footer>
  )
}

export default Footer
