import React, { useEffect, useState } from 'react';
import { Navigate, useLocation, useSearchParams } from 'react-router-dom';

const AuthGuard = ({ children }) => {
    const location = useLocation();
    const [searchParams] = useSearchParams();
    const [isAuthenticated, setIsAuthenticated] = useState(null); // null = loading

    useEffect(() => {
        const checkAuth = () => {
            const token = localStorage.getItem('authToken');
            const urlToken = searchParams.get('token');

            // If we have a stored token, or a magic link token in URL, we allow access
            // NOTE: The actual validation of the magic link happens in the target page (e.g. EpisodePage)
            // This guard just ensures we don't block magic links.
            if (token || urlToken) {
                setIsAuthenticated(true);
            } else {
                setIsAuthenticated(false);
            }
        };

        checkAuth();
    }, [searchParams]);

    if (isAuthenticated === null) {
        return null; // Or a loading spinner if preferred, avoiding flash of content
    }

    if (!isAuthenticated) {
        // Redirect to login, saving the current location they were trying to go to
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    return children;
};

export default AuthGuard;
