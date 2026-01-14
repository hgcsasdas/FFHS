import React from 'react';
import { useAuth } from '../contexts/AuthProvider';
import { useNavigate, Link } from 'react-router-dom';

const Navbar: React.FC = () => {
  const { token, role, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    localStorage.clear();
    navigate('/login');
  };

  if (!token) return null;

  return (
    <nav className="sticky top-0 z-50 bg-white border-b border-gray-200 shadow-sm p-4 flex justify-between items-center">
      <div className="text-xl font-semibold text-gray-800">
        <Link to="/buckets" className="text-gray-800 hover:text-gray-900">
            FFHS
        </Link>
      </div>
      <div className="flex items-center">
        {role === 'ADMIN' && (
          <Link to="/admin" className="text-sm text-gray-600 hover:text-gray-900 mr-4">
            Admin Panel
          </Link>
        )}
        <button
          onClick={handleLogout}
          className="text-sm bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded"
        >
          Cerrar sesión
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
