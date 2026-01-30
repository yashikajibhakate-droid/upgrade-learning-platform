import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import VideoPlayer from '../components/VideoPlayer';
import { ArrowLeft, PlayCircle, Loader } from 'lucide-react';

const EpisodePage = () => {
    const { seriesId } = useParams();
    const navigate = useNavigate();
    const [series, setSeries] = useState(null);
    const [episodes, setEpisodes] = useState([]);
    const [currentEpisode, setCurrentEpisode] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                // Fetch Series Details
                const seriesRes = await api.get(`/api/series/${seriesId}`);
                setSeries(seriesRes.data);

                // Fetch Episodes
                const episodesRes = await api.get(`/api/series/${seriesId}/episodes`);
                setEpisodes(episodesRes.data);

                if (episodesRes.data.length > 0) {
                    setCurrentEpisode(episodesRes.data[0]);
                }
            } catch (err) {
                console.error("Failed to load series/episodes", err);
                setError("Failed to load content.");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [seriesId]);

    const handleEpisodeSelect = (episode) => {
        setCurrentEpisode(episode);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <Loader className="animate-spin text-indigo-600" size={48} />
            </div>
        );
    }

    if (error || !series) {
        return (
            <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center">
                <p className="text-red-500 mb-4">{error || "Series not found"}</p>
                <button
                    onClick={() => navigate('/recommendations')}
                    className="text-indigo-600 hover:underline flex items-center gap-2"
                >
                    <ArrowLeft size={16} /> Back to Recommendations
                </button>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-900 text-white">
            {/* Header / Nav */}
            <div className="p-4 border-b border-gray-800 flex items-center gap-4">
                <button
                    onClick={() => navigate('/recommendations')}
                    className="p-2 hover:bg-gray-800 rounded-full transition-colors"
                >
                    <ArrowLeft size={24} />
                </button>
                <div>
                    <h1 className="text-lg font-bold">{series.title}</h1>
                    <p className="text-sm text-gray-400">{currentEpisode ? currentEpisode.title : 'No Episodes'}</p>
                </div>
            </div>

            <div className="max-w-7xl mx-auto p-4 md:p-6 grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Main Content: Player */}
                <div className="lg:col-span-2 space-y-4">
                    {currentEpisode ? (
                        <>
                            <VideoPlayer
                                src={currentEpisode.videoUrl}
                                poster={series.thumbnailUrl}
                                title={currentEpisode.title}
                                onEnded={() => console.log('Episode ended')}
                            />
                            <div className="bg-gray-800 p-6 rounded-2xl">
                                <h2 className="text-2xl font-bold mb-2">{currentEpisode.title}</h2>
                                <p className="text-gray-400 leading-relaxed">
                                    Episode {currentEpisode.sequenceNumber} • {Math.floor(currentEpisode.durationSeconds / 60)} mins
                                </p>
                            </div>
                        </>
                    ) : (
                        <div className="aspect-video bg-gray-800 rounded-xl flex items-center justify-center">
                            <p className="text-gray-400">No episodes available</p>
                        </div>
                    )}
                </div>

                {/* Sidebar: Episode List */}
                <div className="space-y-4">
                    <h3 className="text-xl font-bold px-2">Episodes</h3>
                    <div className="space-y-2 max-h-[calc(100vh-200px)] overflow-y-auto pr-2 custom-scrollbar">
                        {episodes.map((ep) => (
                            <button
                                key={ep.id}
                                onClick={() => handleEpisodeSelect(ep)}
                                className={`w-full text-left p-4 rounded-xl flex items-start gap-3 transition-all ${currentEpisode?.id === ep.id
                                        ? 'bg-indigo-600 text-white shadow-lg'
                                        : 'bg-gray-800 text-gray-300 hover:bg-gray-700'
                                    }`}
                            >
                                <div className="mt-1">
                                    <PlayCircle size={20} className={currentEpisode?.id === ep.id ? 'text-white' : 'text-gray-500'} />
                                </div>
                                <div>
                                    <div className="font-medium line-clamp-1">
                                        {ep.sequenceNumber}. {ep.title}
                                    </div>
                                    <div className={`text-sm mt-1 ${currentEpisode?.id === ep.id ? 'text-indigo-200' : 'text-gray-500'}`}>
                                        {Math.floor(ep.durationSeconds / 60)} min
                                    </div>
                                </div>
                            </button>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default EpisodePage;
