import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import api, { watchProgressApi, feedbackApi, mcqApi } from '../services/api';
import VideoPlayer from '../components/VideoPlayer';
import FeedbackModal from '../components/FeedbackModal';
import MCQModal from '../components/MCQModal';
import { ArrowLeft, PlayCircle, Loader } from 'lucide-react';

const EpisodePage = () => {
    const { seriesId } = useParams();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [series, setSeries] = useState(null);
    const [episodes, setEpisodes] = useState([]);
    const [currentEpisode, setCurrentEpisode] = useState(null);
    const [initialTime, setInitialTime] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showFeedbackModal, setShowFeedbackModal] = useState(false);
    const [showMCQModal, setShowMCQModal] = useState(false);
    const [mcqData, setMCQData] = useState(null);
    const [completedEpisodeId, setCompletedEpisodeId] = useState(null);
    const [playingRefresher, setPlayingRefresher] = useState(false);
    const [refresherVideoUrl, setRefresherVideoUrl] = useState(null);
    const email = localStorage.getItem('userEmail');
    const progressSaveTimerRef = useRef(null);
    const lastProgressRef = useRef(0); // Track current progress for cleanup

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

                // Check if specific episode ID is in URL params
                const episodeIdFromUrl = searchParams.get('episodeId');
                let episodeToLoad = null;

                if (episodeIdFromUrl) {
                    episodeToLoad = episodesRes.data.find(ep => ep.id === episodeIdFromUrl);
                }

                if (!episodeToLoad && episodesRes.data.length > 0) {
                    episodeToLoad = episodesRes.data[0];
                }

                if (episodeToLoad) {
                    let finalEpisode = episodeToLoad;

                    // Check if this episode is already completed
                    if (email) {
                        try {
                            const completionRes = await watchProgressApi.isCompleted(email, episodeToLoad.id);

                            if (completionRes.data.isCompleted) {
                                // Episode is completed, load next episode instead
                                const currentIndex = episodesRes.data.findIndex(ep => ep.id === episodeToLoad.id);
                                if (currentIndex >= 0 && currentIndex < episodesRes.data.length - 1) {
                                    finalEpisode = episodesRes.data[currentIndex + 1];
                                }
                                setInitialTime(0); // Start next episode from beginning
                            } else {
                                // Episode not completed, check for saved progress
                                try {
                                    const progressRes = await watchProgressApi.getContinueWatching(email);
                                    if (progressRes.data.episodeId === episodeToLoad.id) {
                                        setInitialTime(progressRes.data.progressSeconds || 0);
                                    } else {
                                        setInitialTime(0);
                                    }
                                } catch {
                                    setInitialTime(0);
                                }
                            }
                        } catch (err) {
                            // If check fails, just load the episode normally
                            setInitialTime(0);
                        }
                    }

                    setCurrentEpisode(finalEpisode);
                }
            } catch (err) {
                console.error("Failed to load series/episodes", err);
                setError("Failed to load content.");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [seriesId, searchParams, email]);

    // Save progress immediately when leaving the page
    useEffect(() => {
        return () => {
            if (progressSaveTimerRef.current) {
                clearTimeout(progressSaveTimerRef.current);
            }

            // Save final progress when unmounting
            if (email && currentEpisode && lastProgressRef.current > 0) {
                watchProgressApi.saveProgress(email, currentEpisode.id, Math.floor(lastProgressRef.current))
                    .catch(err => console.error('Failed to save final progress:', err));
            }
        };
    }, [email, currentEpisode]);

    const handleProgressUpdate = (currentTime) => {
        if (!email || !currentEpisode) return;

        // Store current progress for cleanup
        lastProgressRef.current = currentTime;

        // Debounce the save - only save every 3 seconds
        if (progressSaveTimerRef.current) {
            clearTimeout(progressSaveTimerRef.current);
        }

        progressSaveTimerRef.current = setTimeout(() => {
            watchProgressApi.saveProgress(email, currentEpisode.id, Math.floor(currentTime))
                .catch(err => console.error('Failed to save progress:', err));
        }, 3000); // Reduced from 5s to 3s for faster updates
    };

    const handleEpisodeEnded = async () => {
        if (!email || !currentEpisode) return;

        try {
            // Mark current episode as completed
            await watchProgressApi.markComplete(email, currentEpisode.id);

            // Check if feedback modal was already shown for this episode (page refresh scenario)
            const feedbackSessionKey = `feedback_shown_${currentEpisode.id}`;
            const feedbackShown = sessionStorage.getItem(feedbackSessionKey);

            if (feedbackShown) {
                // Skip feedback if page was refreshed during feedback flow
                sessionStorage.removeItem(feedbackSessionKey);
                proceedToNextEpisode();
            } else {
                // Mark that we're showing feedback modal (cleared after submission)
                sessionStorage.setItem(feedbackSessionKey, 'true');

                // Show feedback modal
                setCompletedEpisodeId(currentEpisode.id);
                setShowFeedbackModal(true);
            }
        } catch (err) {
            console.error('Failed to mark episode complete:', err);
            // Even if marking complete fails, proceed to next episode
            proceedToNextEpisode();
        }
    };

    const proceedToNextEpisode = () => {
        // Find and auto-play next episode
        const currentIndex = episodes.findIndex(ep => ep.id === currentEpisode.id);
        if (currentIndex >= 0 && currentIndex < episodes.length - 1) {
            const nextEpisode = episodes[currentIndex + 1];
            setCurrentEpisode(nextEpisode);
            setInitialTime(0); // Start from beginning
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    };

    const handleFeedbackSubmit = async (isHelpful) => {
        if (!completedEpisodeId) return;

        try {
            // Submit feedback - backend gets user email from auth token
            await feedbackApi.saveFeedback(completedEpisodeId, isHelpful);

            // Clear session storage key since feedback was submitted
            const feedbackSessionKey = `feedback_shown_${completedEpisodeId}`;
            sessionStorage.removeItem(feedbackSessionKey);

            // If feedback is helpful, check for MCQ
            if (isHelpful) {
                try {
                    const mcqResponse = await mcqApi.getMCQ(completedEpisodeId);
                    if (mcqResponse.data) {
                        // MCQ exists, show MCQ modal
                        setMCQData(mcqResponse.data);
                        setShowMCQModal(true);
                        // Don't proceed to next episode yet
                        return;
                    }
                } catch (err) {
                    // No MCQ exists or error fetching - proceed normally
                    console.log('No MCQ found for this episode or error:', err.message);
                }
            }
        } catch (err) {
            console.error('Failed to save feedback:', err);
        }
        // Note: FeedbackModal's onClose (handleFeedbackClose) handles modal closing and navigation
    };

    const handleFeedbackClose = () => {
        // User skipped feedback
        if (completedEpisodeId) {
            const feedbackSessionKey = `feedback_shown_${completedEpisodeId}`;
            sessionStorage.removeItem(feedbackSessionKey);
        }

        setShowFeedbackModal(false);
        proceedToNextEpisode();
    };

    const handleMCQCorrect = () => {
        // Close MCQ modal and proceed to next episode
        setShowMCQModal(false);
        setMCQData(null);
        proceedToNextEpisode();
    };

    const handleMCQIncorrect = (videoUrl) => {
        // Close MCQ modal and play refresher video
        setShowMCQModal(false);
        setMCQData(null);

        if (videoUrl) {
            // Play refresher video
            setRefresherVideoUrl(videoUrl);
            setPlayingRefresher(true);
        } else {
            // No refresher video, proceed to next episode
            proceedToNextEpisode();
        }
    };

    const handleRefresherEnded = () => {
        // Refresher video finished, proceed to next episode
        setPlayingRefresher(false);
        setRefresherVideoUrl(null);
        proceedToNextEpisode();
    };

    const handleMCQClose = () => {
        // User closed MCQ modal without answering
        setShowMCQModal(false);
        setMCQData(null);
        proceedToNextEpisode();
    };

    const handleEpisodeSelect = (episode) => {
        setCurrentEpisode(episode);
        setInitialTime(0); // Reset to start when manually selecting
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
                                src={playingRefresher ? refresherVideoUrl : currentEpisode.videoUrl}
                                poster={series.thumbnailUrl}
                                title={playingRefresher ? 'Refresher Video' : currentEpisode.title}
                                initialTime={playingRefresher ? 0 : initialTime}
                                onProgressUpdate={playingRefresher ? undefined : handleProgressUpdate}
                                onEnded={playingRefresher ? handleRefresherEnded : handleEpisodeEnded}
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

            {/* Feedback Modal */}
            <FeedbackModal
                isOpen={showFeedbackModal}
                onClose={handleFeedbackClose}
                onSubmit={handleFeedbackSubmit}
                episodeTitle={currentEpisode?.title}
            />

            {/* MCQ Modal */}
            <MCQModal
                isOpen={showMCQModal}
                mcqData={mcqData}
                onCorrect={handleMCQCorrect}
                onIncorrect={handleMCQIncorrect}
                onClose={handleMCQClose}
            />
        </div>
    );
};

export default EpisodePage;
