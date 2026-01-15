import React from 'react';
import HealthComponent from './components/HealthComponent';

function App() {
    return (
        <div className="min-h-screen bg-gray-100 p-8 font-sans text-gray-800">
            <div className="max-w-2xl mx-auto bg-white rounded-lg shadow-md p-6">
                <h1 className="text-3xl font-bold mb-4 text-blue-600">Monorepo App</h1>
                <p className="mb-6 text-gray-600">Frontend running on Vite + React + TailwindCSS</p>
                <HealthComponent />
            </div>
        </div>
    );
}

export default App;
