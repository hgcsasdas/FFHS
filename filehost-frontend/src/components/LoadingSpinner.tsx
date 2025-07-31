import React from 'react'

const LoadingSpinner: React.FC = () => (
  <div className="flex justify-center items-center p-6">
    <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-blue-500"></div>
  </div>
)

export default LoadingSpinner
