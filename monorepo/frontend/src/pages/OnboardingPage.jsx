import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Info, Code, BarChart, Palette, Megaphone, Server, Shield, Atom, Coins, Check } from 'lucide-react';
import api from '../services/api';

const OnboardingPage = () => {
    const [interests, setInterests] = useState([]);
    const [selectedInterests, setSelectedInterests] = useState(new Set());
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');

    const navigate = useNavigate();
    const location = useLocation();
    const email = location.state?.email;

    // Icon Mapping
    const iconMap = {
        "Python Programming": Code,
        "Data Science": BarChart,
        "UI/UX Design": Palette,
        "Digital Marketing": Megaphone,
        "Cloud Computing": Server,
        "Cybersecurity": Shield,
        "React Framework": Atom,
        "Personal Finance": Coins
    };

    useEffect(() => {
        if (!email) {
            navigate('/login');
            return;
        }

        const fetchInterests = async () => {
            try {
                const response = await api.get('/api/users/interests');
                setInterests(response.data);
            } catch (err) {
                console.error(err);
                setError('Failed to load interests. Please refresh the page.');
            } finally {
                setLoading(false);
            }
        };

        fetchInterests();
    }, [email, navigate]);

    const toggleInterest = (interest) => {
        const newSelected = new Set(selectedInterests);
        if (newSelected.has(interest)) {
            newSelected.delete(interest);
        } else {
            newSelected.add(interest);
        }
        setSelectedInterests(newSelected);
    };

    const handleContinue = async () => {
        if (selectedInterests.size === 0) return;

        setSaving(true);
        setError('');

        try {
            await api.post('/api/users/preferences', {
                email: email,
                interests: Array.from(selectedInterests)
            });
            navigate('/recommendations', { state: { email: email } }); // Redirect to RecommendationsPage with email
        } catch (err) {
            console.error(err);
            setError('Failed to save preferences. Please try again.');
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
    }

    return (
        <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4 relative font-sans text-gray-900">
            {/* Top Right Info Icon */}
            <div className="absolute top-6 right-6">
                <button
                    type="button"
                    aria-label="Show information"
                    className="bg-gray-800 text-white p-2 rounded-lg cursor-pointer hover:bg-gray-700 transition-colors"
                >
                    <Info size={24} />
                </button>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 md:p-12 max-w-5xl w-full text-center">
                <h1 className="text-3xl font-bold mb-4 text-gray-900">What do you want to learn?</h1>
                <p className="text-gray-500 mb-10">Select one or more topics to personalize your home feed.</p>

                {error && <p className="text-red-500 mb-4">{error}</p>}

                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-10">
                    {interests.map((interest) => {
                        const IconComponent = iconMap[interest] || Info;
                        const isSelected = selectedInterests.has(interest);

                        return (
                            <button
                                key={interest}
                                type="button"
                                onClick={() => toggleInterest(interest)}
                                className={`relative p-6 rounded-xl border-2 transition-all duration-200 flex flex-col items-center justify-center gap-4 h-40
                                    ${isSelected
                                        ? 'border-indigo-600 bg-indigo-50/10'
                                        : 'border-gray-100 bg-white hover:border-gray-200'}`}
                            >
                                {/* Radio Indicator */}
                                <div className={`absolute top-3 right-3 w-5 h-5 rounded-full border-2 flex items-center justify-center transition-colors
                                    ${isSelected ? 'bg-indigo-600 border-indigo-600' : 'border-gray-200'}`}>
                                    {isSelected && <Check size={12} className="text-white" />}
                                </div>

                                {/* Icon */}
                                <div className={`p-3 rounded-full ${isSelected ? 'bg-indigo-100 text-indigo-600' : 'bg-gray-100 text-gray-400'}`}>
                                    <IconComponent size={32} />
                                </div>

                                <span className={`text-sm font-semibold ${isSelected ? 'text-indigo-900' : 'text-gray-700'}`}>
                                    {interest}
                                </span>
                            </button>
                        );
                    })}
                </div>

                <button
                    onClick={handleContinue}
                    disabled={selectedInterests.size === 0 || saving}
                    className="bg-gray-500 text-white font-bold py-3.5 px-12 rounded-lg transition-colors duration-200 shadow-md 
                        disabled:opacity-50 disabled:cursor-not-allowed
                        enabled:bg-indigo-600 enabled:hover:bg-indigo-700"
                >
                    {saving ? 'Saving...' : 'Continue'}
                </button>
            </div>
        </div>
    );
};

export default OnboardingPage;
