import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Clock, Play, Info } from 'lucide-react';
import api from '../services/api';

const RecommendationsPage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const email = location.state?.email;

    const [recommendations, setRecommendations] = useState([]);
    const [others, setOthers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        if (!email) {
            navigate('/login');
            return;
        }

        const fetchData = async () => {
            try {
                const response = await api.get(`/api/series/recommendations?email=${email}`);
                setRecommendations(response.data.recommended);
                setOthers(response.data.others);
            } catch (err) {
                console.error(err);
                setError('Failed to load recommendations.');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [email, navigate]);

    if (loading) {
        return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
    }

    return (
        <div className="min-h-screen bg-gray-50 font-sans text-gray-900 pb-20">
            {/* Header / Nav */}
            <div className="bg-white shadow-sm sticky top-0 z-10 px-6 py-4 flex justify-between items-center">
                <h1 className="text-xl font-bold text-indigo-600">LearnSphere</h1>
                <div className="flex items-center gap-4">
                    <span className="text-sm text-gray-500 hidden md:block">{email}</span>
                    <div className="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center text-indigo-600 font-bold">
                        {email[0].toUpperCase()}
                    </div>
                </div>
            </div>

            <div className="max-w-7xl mx-auto px-6 py-8">
                {error && <div className="bg-red-50 text-red-600 p-4 rounded-lg mb-6">{error}</div>}

                {/* Recommended Section */}
                <section className="mb-12">
                    <h2 className="text-2xl font-bold mb-6 flex items-center gap-2">
                        Recommended For You
                    </h2>

                    {recommendations.length > 0 ? (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {recommendations.map(series => (
                                <SeriesCard key={series.id} series={series} />
                            ))}
                        </div>
                    ) : (
                        <div className="bg-white p-8 rounded-xl border border-gray-100 text-center text-gray-500">
                            No specific recommendations yet. Explore our library below!
                        </div>
                    )}
                </section>

                {/* Explore More Section */}
                <section>
                    <h2 className="text-2xl font-bold mb-6 text-gray-800">Explore More</h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {others.map(series => (
                            <SeriesCard key={series.id} series={series} />
                        ))}
                    </div>
                </section>
            </div>
        </div>
    );
};

const SeriesCard = ({ series }) => {
    return (
        <div className="bg-white rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-all duration-300 border border-gray-100 group cursor-pointer group">
            {/* Thumbnail */}
            <div className="relative h-48 bg-gray-200 overflow-hidden">
                <img
                    src={series.thumbnailUrl || '/api/placeholder/400/320'}
                    alt={series.title}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                />
                <div className="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-colors duration-300 flex items-center justify-center">
                    <div className="w-12 h-12 bg-white/90 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transform scale-75 group-hover:scale-100 transition-all duration-300 shadow-lg">
                        <Play size={20} className="text-indigo-600 ml-1" fill="currentColor" />
                    </div>
                </div>
                <div className="absolute top-3 right-3 bg-white/90 backdrop-blur-sm px-2 py-1 rounded-md text-xs font-bold text-gray-700 shadow-sm">
                    {series.category}
                </div>
            </div>

            {/* Content */}
            <div className="p-5">
                <h3 className="font-bold text-lg mb-2 text-gray-900 line-clamp-1 group-hover:text-indigo-600 transition-colors">{series.title}</h3>
                <p className="text-gray-500 text-sm line-clamp-2 mb-4 h-10">{series.description}</p>

                <div className="flex items-center justify-between pt-4 border-t border-gray-50">
                    <div className="flex items-center text-xs text-gray-400 font-medium">
                        <Clock size={14} className="mr-1" />
                        <span>3 Episodes</span> {/* Placeholder since we don't have episode count in series DTO yet */}
                    </div>
                    <button className="text-indigo-600 text-sm font-bold hover:text-indigo-700">
                        Start Watching
                    </button>
                </div>
            </div>
        </div>
    );
};

export default RecommendationsPage;
