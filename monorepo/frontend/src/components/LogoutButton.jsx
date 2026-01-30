import React from 'react';
import { LogOut } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const LogoutButton = () => {
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await api.post('/api/auth/logout');
        } catch (error) {
            console.error('Logout failed:', error);
        } finally {
            localStorage.removeItem('authToken');
            localStorage.removeItem('userEmail');
            navigate('/login');
        }
    };

    return (
        <button
            onClick={handleLogout}
            className="flex items-center gap-2 text-gray-500 hover:text-red-600 transition-colors font-medium text-sm"
        >
            <LogOut size={18} />
            <span className="hidden md:inline">Logout</span>
        </button>
    );
};

export default LogoutButton;
