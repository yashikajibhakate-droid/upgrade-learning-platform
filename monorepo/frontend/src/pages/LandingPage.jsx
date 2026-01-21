import React from 'react';
import { useNavigate } from 'react-router-dom';

const LandingPage = () => {
    const navigate = useNavigate();

    return (
        <div className="min-h-screen bg-gradient-to-br from-indigo-900 to-purple-800 flex flex-col items-center justify-center text-white p-4">
            <div className="max-w-md w-full text-center space-y-8">
                <h1 className="text-5xl font-extrabold tracking-tight">
                    Upgrade Learning
                </h1>
                <p className="text-xl text-indigo-200">
                    Master new skills with our advanced platform.
                </p>

                <div className="pt-8">
                    <button
                        onClick={() => navigate('/login')}
                        className="w-full bg-white text-indigo-900 font-bold py-4 px-8 rounded-xl text-lg shadow-lg hover:bg-gray-100 transform transition hover:-translate-y-1"
                    >
                        Start Learning
                    </button>
                </div>
            </div>
        </div>
    );
};

export default LandingPage;
