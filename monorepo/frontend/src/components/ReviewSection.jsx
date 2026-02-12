import React, { useState, useEffect } from 'react';
import { seriesReviewApi } from '../services/api';
import ReviewForm from './ReviewForm';
import SeriesReviewList from './SeriesReviewList';
import { MessageSquare } from 'lucide-react';

const ReviewSection = ({ seriesId, isLoggedIn }) => {
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [submitLoading, setSubmitLoading] = useState(false);
    const [error, setError] = useState(null);
    const [hasReviewed, setHasReviewed] = useState(false);
    const [userEmail, setUserEmail] = useState(localStorage.getItem('userEmail'));

    const fetchReviews = async () => {
        try {
            setLoading(true);
            const response = await seriesReviewApi.getReviews(seriesId);
            setReviews(response.data);

            // Check if current user already reviewed
            if (userEmail) {
                const reviewed = response.data.some(r => r.userEmail === userEmail);
                setHasReviewed(reviewed);
            }
        } catch (err) {
            console.error('Failed to fetch reviews:', err);
            setError('Failed to load reviews.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (isLoggedIn) {
            setUserEmail(localStorage.getItem('userEmail'));
        }
    }, [isLoggedIn]);

    useEffect(() => {
        if (seriesId) {
            fetchReviews();
        }
    }, [seriesId, userEmail]);

    const handleSubmitReview = async (reviewData) => {
        try {
            setSubmitLoading(true);
            await seriesReviewApi.submitReview(seriesId, reviewData);
            await fetchReviews();
            setError(null);
        } catch (err) {
            console.error('Failed to submit review:', err);
            const message = err.response?.data?.error || 'Failed to submit review. Please try again.';
            setError(message);
        } finally {
            setSubmitLoading(false);
        }
    };

    return (
        <div className="space-y-8 mt-12 pb-20">
            <div className="flex items-center gap-3 border-b border-gray-800 pb-4">
                <MessageSquare className="text-indigo-400" size={28} />
                <h3 className="text-2xl font-bold">Community Reviews</h3>
                <span className="bg-gray-800 px-3 py-1 rounded-full text-sm font-medium text-gray-400">
                    {reviews.length}
                </span>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-5 gap-12">
                <div className="lg:col-span-3 order-2 lg:order-1">
                    {loading ? (
                        <div className="space-y-4">
                            {[1, 2, 3].map(i => (
                                <div key={i} className="h-32 bg-gray-800/50 rounded-2xl animate-pulse" />
                            ))}
                        </div>
                    ) : (
                        <SeriesReviewList reviews={reviews} />
                    )}
                </div>

                <div className="lg:col-span-2 order-1 lg:order-2 space-y-6">
                    {isLoggedIn ? (
                        !hasReviewed ? (
                            <div className="sticky top-6">
                                <ReviewForm onSubmit={handleSubmitReview} loading={submitLoading} />
                                {error && (
                                    <p className="mt-4 text-sm text-red-500 bg-red-500/10 p-4 rounded-xl border border-red-500/20">
                                        {error}
                                    </p>
                                )}
                            </div>
                        ) : (
                            <div className="bg-indigo-600/10 p-6 rounded-2xl border border-indigo-500/20 text-center">
                                <p className="text-indigo-300 font-medium">
                                    You have already shared your feedback for this series. Thank you!
                                </p>
                            </div>
                        )
                    ) : (
                        <div className="bg-gray-800/50 p-8 rounded-2xl border border-gray-700/50 text-center space-y-4">
                            <p className="text-gray-400">Log in to share your thoughts and help other learners!</p>
                            <button
                                onClick={() => window.location.href = '/login'}
                                className="w-full py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-bold transition-all"
                            >
                                Log In to Review
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ReviewSection;
