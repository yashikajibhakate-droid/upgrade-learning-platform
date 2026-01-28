import React from 'react';
import { useLocation, Navigate } from 'react-router-dom';
import LandingPage from './LandingPage';

const HomePage = () => {
    const location = useLocation();
    const email = location.state?.email;

    if (email) {
        return <Navigate to="/recommendations" state={{ email }} replace />;
    }

    return <LandingPage />;
};

export default HomePage;
