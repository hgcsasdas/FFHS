import React, { createContext, useContext, useState, useEffect, type ReactNode } from 'react'
import { api } from '../api/client'

interface AuthContextType {
    user: string | null
    token: string | null
    login: (username: string, password: string) => Promise<void>
    logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [token, setToken] = useState<string | null>(localStorage.getItem('token'))
    const [user, setUser] = useState<string | null>(localStorage.getItem('user'))

    useEffect(() => {
        if (token) {
            localStorage.setItem('token', token)
            localStorage.setItem('user', user || '')
        } else {
            localStorage.removeItem('token')
            localStorage.removeItem('user')
        }
    }, [token, user])

    const login = async (username: string, password: string) => {
        const { data } = await api.post('/auth/login', { username, password })
        setToken(data.token)
        setUser(username)
    }

    const logout = () => {
        setToken(null)
        setUser(null)
    }

    return (
        <AuthContext.Provider value={{ user, token, login, logout }}>
            {children}
        </AuthContext.Provider>
    )
}

export const useAuth = (): AuthContextType => {
    const ctx = useContext(AuthContext)
    if (!ctx) throw new Error('useAuth must be inside AuthProvider')
    return ctx
}
