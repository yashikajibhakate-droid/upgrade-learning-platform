import React, { useEffect, useState } from 'react';
import api from '../services/api';

const HealthComponent = () => {
    const [status, setStatus] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchHealth = async () => {
            try {
                const response = await api.get('/health');
                setStatus(response.data);
            } catch (err) {
                setError('Failed to connect to backend');
                console.error(err);
            }
        };

        fetchHealth();
    }, []);

    if (error) {
        return <div className="p-4 mb-4 text-sm text-red-800 rounded-lg bg-red-50" role="alert">Error: {error}</div>;
    }

    if (!status) {
        return <div className="text-gray-500 animate-pulse">Loading health status...</div>;
    }

    return (
        <div className="border border-green-200 rounded-md p-4 bg-green-50">
            <h2 className="text-xl font-semibold text-green-800 mb-2">Backend Health Status</h2>
            <pre className="bg-gray-800 text-gray-100 p-4 rounded text-sm overflow-auto">
                {JSON.stringify(status, null, 2)}
            </pre>
        </div>
    );
};

export default HealthComponent;
