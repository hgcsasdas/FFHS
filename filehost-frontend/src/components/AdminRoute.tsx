import React from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthProvider'
import type { JSX } from 'react/jsx-runtime'

const AdminRoute: React.FC<{ children: JSX.Element }> = ({ children }) => {
    const { token, role } = useAuth()

    if (!token) {
        return <Navigate to="/login" replace />
    }

    if (role !== 'ADMIN') {
        return <Navigate to="/buckets" replace />
    }

    return children
}

export default AdminRoute
