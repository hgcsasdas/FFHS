import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import { QueryClientProvider } from '@tanstack/react-query'
import { queryClient } from './api/client'
import { AuthProvider } from './contexts/AuthProvider'
import 'react-toastify/dist/ReactToastify.css'
import './index.css'

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
    <React.StrictMode>
        <QueryClientProvider client={queryClient}>
            <AuthProvider>
                <App />
            </AuthProvider>
        </QueryClientProvider>
    </React.StrictMode>
)
