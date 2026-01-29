import React from 'react';
import { useLocation, Navigate } from 'react-router-dom';
import LandingPage from './LandingPage';

const HomePage = () => {
    const location = useLocation();
    const email = location.state?.email || localStorage.getItem('userEmail');
    const token = localStorage.getItem('authToken');

    if (token && email) {
        return <Navigate to="/recommendations" state={{ email }} replace />;
    }

    if (location.state?.email) {
        return <Navigate to="/recommendations" state={{ email: location.state.email }} replace />;
    }

    return <LandingPage />;
};

export default HomePage;
