import React from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './contexts/AuthProvider'
import LoginPage from './pages/LoginPage'
import BucketsPage from './pages/BucketsPage'
import FilesPage from './pages/FilesPage'
import AdminPage from './pages/AdminPage'
import type { JSX } from 'react/jsx-runtime'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import AdminRoute from './components/AdminRoute'

const PrivateRoute: React.FC<{ children: JSX.Element }> = ({ children }) => {
    const { token } = useAuth()
    return token ? children : <Navigate to="/login" replace />
}

const App: React.FC = () => (
    <BrowserRouter>
        <div className="min-h-screen flex flex-col">
            <Navbar />
            <main className="flex-grow">
                <Routes>
                    {/* Rutas Públicas */}
                    <Route path="/login" element={<LoginPage />} />

                    {/* Rutas Privadas */}
                    <Route
                        path="/buckets"
                        element={
                            <PrivateRoute>
                                <BucketsPage />
                            </PrivateRoute>
                        }
                    />
                    <Route
                        path="/files/:bucketKey"
                        element={
                            <PrivateRoute>
                                <FilesPage />
                            </PrivateRoute>
                        }
                    />

                    {/* Ruta de Admin */}
                    <Route
                        path="/admin"
                        element={
                            <AdminRoute>
                                <AdminPage />
                            </AdminRoute>
                        }
                    />

                    {/* Fallback */}
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </main>
            <Footer />
        </div>
    </BrowserRouter>
)

export default App
