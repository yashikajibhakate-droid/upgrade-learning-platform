import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Play } from 'lucide-react';

const ContinueWatchingSection = ({ data }) => {
    const navigate = useNavigate();

    if (!data) return null;

    const formatDuration = (seconds) => {
        if (!seconds) return '0 min';
        if (seconds < 60) return `${Math.floor(seconds)} sec`;
        return `${Math.floor(seconds / 60)} min`;
    };

    // Prevent division by zero
    const duration = data.episodeDurationSeconds || 0;
    const progress = data.progressSeconds || 0;
    const progressPercent = duration > 0 ? (progress / duration) * 100 : 0;

    const handleClick = () => {
        navigate(`/series/${data.seriesId}/watch?episodeId=${data.episodeId}`);
    };

    return (
        <section className="mb-12">
            <h2 className="text-2xl font-bold mb-6 flex items-center gap-2">
                Continue Watching
            </h2>

            <div
                onClick={handleClick}
                className="bg-white rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-all duration-300 border border-gray-100 group cursor-pointer max-w-4xl"
            >
                <div className="flex flex-col md:flex-row">
                    {/* Thumbnail */}
                    <div className="relative md:w-80 h-48 md:h-auto bg-gray-200 overflow-hidden flex-shrink-0">
                        <img
                            src={data.seriesThumbnailUrl || '/api/placeholder/400/320'}
                            alt={data.seriesTitle}
                            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                        />
                        <div className="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-colors duration-300 flex items-center justify-center">
                            <div className="w-16 h-16 bg-white/90 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transform scale-75 group-hover:scale-100 transition-all duration-300 shadow-lg">
                                <Play size={28} className="text-indigo-600 ml-1" fill="currentColor" />
                            </div>
                        </div>
                    </div>

                    {/* Content */}
                    <div className="p-6 flex-1 flex flex-col justify-between">
                        <div>
                            <div className="text-sm text-indigo-600 font-semibold mb-2">
                                {data.seriesCategory}
                            </div>
                            <h3 className="font-bold text-xl mb-2 text-gray-900 group-hover:text-indigo-600 transition-colors">
                                {data.seriesTitle}
                            </h3>
                            <p className="text-gray-600 text-sm mb-4">
                                {data.episodeTitle}
                            </p>
                        </div>

                        {/* Progress Bar */}
                        <div className="space-y-2">
                            <div className="flex justify-between text-xs text-gray-500">
                                <span>{formatDuration(progress)} watched</span>
                                <span>{formatDuration(duration)} total</span>
                            </div>
                            <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
                                <div
                                    className="bg-indigo-600 h-full rounded-full transition-all duration-300"
                                    style={{ width: `${Math.min(progressPercent, 100)}%` }}
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
};

export default ContinueWatchingSection;
