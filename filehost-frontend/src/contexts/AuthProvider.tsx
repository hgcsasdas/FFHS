import React, { createContext, useContext, useState, useEffect, type ReactNode } from 'react'
import { jwtDecode } from 'jwt-decode'
import { api } from '../api/client'
import type { UserRole } from '../types'

interface AuthContextType {
    user: string | null
    token: string | null
    role: UserRole | null
    login: (username: string, password: string) => Promise<void>
    logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

// Función para decodificar el rol del token
const getRoleFromToken = (token: string): UserRole | null => {
    try {
        const decoded: { scope: string } = jwtDecode(token)
        return decoded.scope.includes('ROLE_ADMIN') ? 'ADMIN' : 'USER'
    } catch (error) {
        console.error('Error decodificando el token:', error)
        return null
    }
}

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [token, setToken] = useState<string | null>(localStorage.getItem('token'))
    const [user, setUser] = useState<string | null>(localStorage.getItem('user'))
    const [role, setRole] = useState<UserRole | null>(token ? getRoleFromToken(token) : null)

    useEffect(() => {
        if (token && user) {
            localStorage.setItem('token', token)
            localStorage.setItem('user', user)
            setRole(getRoleFromToken(token))
        } else {
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            setRole(null)
        }
    }, [token, user])

    const login = async (username: string, password: string) => {
        const { data } = await api.post('/auth/login', { username, password })
        if (data.token) {
            setToken(data.token)
            setUser(username)
        }
    }

    const logout = () => {
        setToken(null)
        setUser(null)
    }

    return (
        <AuthContext.Provider value={{ user, token, role, login, logout }}>
            {children}
        </AuthContext.Provider>
    )
}

export const useAuth = (): AuthContextType => {
    const ctx = useContext(AuthContext)
    if (!ctx) throw new Error('useAuth must be inside AuthProvider')
    return ctx
}
