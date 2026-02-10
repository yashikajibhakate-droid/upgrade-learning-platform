import React, { useState } from 'react';
import { ThumbsUp, ThumbsDown, X, Loader } from 'lucide-react';

const FeedbackModal = ({ isOpen, onClose, onSubmit, episodeTitle }) => {
    const [isSubmitting, setIsSubmitting] = useState(false);

    if (!isOpen) return null;

    const handleFeedback = async (isHelpful) => {
        setIsSubmitting(true);
        try {
            await onSubmit(isHelpful);
        } catch (error) {
            console.error('Failed to submit feedback:', error);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm">
            <div className="relative bg-gray-900 rounded-2xl shadow-2xl p-8 max-w-md w-full mx-4 border border-gray-700">
                {/* Close Button */}
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 p-2 hover:bg-gray-800 rounded-full transition-colors"
                    disabled={isSubmitting}
                >
                    <X size={20} className="text-gray-400" />
                </button>

                {/* Content */}
                <div className="text-center space-y-6">
                    <div>
                        <h2 className="text-2xl font-bold text-white mb-2">
                            How was this episode?
                        </h2>
                        {episodeTitle && (
                            <p className="text-gray-400 text-sm">
                                {episodeTitle}
                            </p>
                        )}
                    </div>

                    <p className="text-gray-300">
                        Your feedback helps us improve your learning experience
                    </p>

                    {/* Feedback Buttons */}
                    <div className="flex gap-4 justify-center pt-4">
                        <button
                            onClick={() => handleFeedback(true)}
                            disabled={isSubmitting}
                            className="flex flex-col items-center gap-3 px-8 py-6 bg-green-600 hover:bg-green-700 disabled:bg-gray-700 disabled:cursor-not-allowed rounded-xl transition-all transform hover:scale-105 active:scale-95 shadow-lg"
                        >
                            {isSubmitting ? (
                                <Loader className="animate-spin text-white" size={32} />
                            ) : (
                                <>
                                    <ThumbsUp size={32} className="text-white" />
                                    <span className="text-white font-semibold">Helpful</span>
                                </>
                            )}
                        </button>

                        <button
                            onClick={() => handleFeedback(false)}
                            disabled={isSubmitting}
                            className="flex flex-col items-center gap-3 px-8 py-6 bg-red-600 hover:bg-red-700 disabled:bg-gray-700 disabled:cursor-not-allowed rounded-xl transition-all transform hover:scale-105 active:scale-95 shadow-lg"
                        >
                            {isSubmitting ? (
                                <Loader className="animate-spin text-white" size={32} />
                            ) : (
                                <>
                                    <ThumbsDown size={32} className="text-white" />
                                    <span className="text-white font-semibold">Not Helpful</span>
                                </>
                            )}
                        </button>
                    </div>

                    <p className="text-gray-500 text-xs pt-4">
                        You can skip this by closing the modal
                    </p>
                </div>
            </div>
        </div>
    );
};

export default FeedbackModal;
