import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Info, HelpCircle } from 'lucide-react';
import api from '../services/api';

const LoginPage = () => {
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            await api.post('/auth/generate-otp', { email });
            navigate('/verify-otp', { state: { email } });
        } catch (err) {
            console.error(err);
            setError('Failed to send OTP. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4 relative font-sans text-gray-900">
            {/* Top Right Info Icon - Dark Square style from SS */}
            <div className="absolute top-6 right-6">
                <div className="bg-gray-800 text-white p-2 rounded-lg cursor-pointer hover:bg-gray-700 transition-colors">
                    <Info size={24} />
                </div>
            </div>

            {/* Main Card */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 md:p-10 max-w-md w-full text-center">

                <h1 className="text-2xl font-bold mb-2 text-gray-900">Login or Sign Up</h1>
                <p className="text-gray-500 text-sm mb-8">
                    Enter your email to receive a login code.
                </p>

                <form onSubmit={handleSubmit} className="text-left space-y-6">
                    <div>
                        <label htmlFor="email" className="block text-xs font-bold text-gray-700 uppercase tracking-wide mb-2">
                            Email Address
                        </label>
                        <input
                            id="email"
                            type="email"
                            required
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="block w-full px-4 py-3 border border-gray-200 rounded-lg text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
                            placeholder="you@example.com"
                        />
                    </div>

                    {error && <p className="text-red-500 text-sm text-center">{error}</p>}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-3.5 rounded-lg transition-colors duration-200 shadow-md disabled:opacity-70 disabled:cursor-not-allowed"
                    >
                        {loading ? 'Sending...' : 'Send OTP'}
                    </button>
                </form>
            </div>

            {/* Bottom Right Help */}
            <div className="absolute bottom-6 right-6">
                <div className="bg-indigo-600 text-white p-2 rounded-full cursor-pointer hover:bg-indigo-700 shadow-lg transition-colors">
                    <HelpCircle size={24} />
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
