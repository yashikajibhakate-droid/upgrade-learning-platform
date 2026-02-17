import React, { useState, useEffect } from 'react';
import { seriesReviewApi } from '../services/api';
import ReviewForm from './ReviewForm';
import SeriesReviewList from './SeriesReviewList';
import { MessageSquare, Pencil, Trash2 } from 'lucide-react';

const ReviewSection = ({ seriesId, isLoggedIn }) => {
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [submitLoading, setSubmitLoading] = useState(false);
    const [error, setError] = useState(null);
    const [hasReviewed, setHasReviewed] = useState(false);
    const [userReview, setUserReview] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [deleteLoading, setDeleteLoading] = useState(false);
    const [userEmail, setUserEmail] = useState(localStorage.getItem('userEmail'));

    const [sortBy, setSortBy] = useState('recent');

    const fetchReviews = async () => {
        try {
            setLoading(true);
            const response = await seriesReviewApi.getReviews(seriesId, sortBy);
            setReviews(response.data);

            // Find the current user's own review
            if (userEmail) {
                const ownReview = response.data.find(r => r.isOwnReview);
                if (ownReview) {
                    setHasReviewed(true);
                    setUserReview(ownReview);
                } else {
                    setHasReviewed(false);
                    setUserReview(null);
                }
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
    }, [seriesId, userEmail, sortBy]);

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

    const handleUpdateReview = async (reviewData) => {
        try {
            setSubmitLoading(true);
            await seriesReviewApi.updateReview(seriesId, reviewData);
            setIsEditing(false);
            await fetchReviews();
            setError(null);
        } catch (err) {
            console.error('Failed to update review:', err);
            const message = err.response?.data?.error || 'Failed to update review. Please try again.';
            setError(message);
        } finally {
            setSubmitLoading(false);
        }
    };

    const handleDeleteReview = async () => {
        if (!window.confirm('Are you sure you want to delete your review? This action cannot be undone and you will not be able to submit another review for this series.')) {
            return;
        }
        try {
            setDeleteLoading(true);
            await seriesReviewApi.deleteReview(seriesId);
            setHasReviewed(true);
            setUserReview(null);
            setIsEditing(false);
            setReviews(prev => prev.filter(r => !r.isOwnReview));
            setError(null);
        } catch (err) {
            console.error('Failed to delete review:', err);
            const message = err.response?.data?.error || 'Failed to delete review. Please try again.';
            setError(message);
        } finally {
            setDeleteLoading(false);
        }
    };

    const renderReviewPanel = () => {
        if (!isLoggedIn) {
            return (
                <div className="bg-gray-800/50 p-8 rounded-2xl border border-gray-700/50 text-center space-y-4">
                    <p className="text-gray-400">Log in to share your thoughts and help other learners!</p>
                    <button
                        onClick={() => window.location.href = '/login'}
                        className="w-full py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-bold transition-all"
                    >
                        Log In to Review
                    </button>
                </div>
            );
        }

        if (!hasReviewed) {
            return (
                <div className="sticky top-6">
                    <ReviewForm onSubmit={handleSubmitReview} loading={submitLoading} />
                    {error && (
                        <p className="mt-4 text-sm text-red-500 bg-red-500/10 p-4 rounded-xl border border-red-500/20">
                            {error}
                        </p>
                    )}
                </div>
            );
        }

        if (isEditing && userReview) {
            return (
                <div className="sticky top-6">
                    <ReviewForm
                        onSubmit={handleUpdateReview}
                        loading={submitLoading}
                        isEditMode={true}
                        initialData={{ rating: userReview.rating, comment: userReview.comment }}
                        onCancel={() => { setIsEditing(false); setError(null); }}
                    />
                    {error && (
                        <p className="mt-4 text-sm text-red-500 bg-red-500/10 p-4 rounded-xl border border-red-500/20">
                            {error}
                        </p>
                    )}
                </div>
            );
        }

        // User has reviewed
        return (
            <div className="bg-indigo-600/10 p-6 rounded-2xl border border-indigo-500/20 text-center space-y-3">
                <p className="text-indigo-300 font-medium">
                    You have already shared your feedback for this series. Thank you!
                </p>
                {userReview ? (
                    <div className="flex items-center gap-3 flex-wrap justify-center">
                        {userReview.editable && (
                            <button
                                onClick={() => { setIsEditing(true); setError(null); }}
                                className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-semibold transition-all text-sm"
                            >
                                <Pencil size={16} />
                                Edit Review
                            </button>
                        )}
                        {userReview.deletable && (
                            <button
                                onClick={handleDeleteReview}
                                disabled={deleteLoading}
                                className="inline-flex items-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 disabled:bg-gray-700 disabled:text-gray-500 text-white rounded-xl font-semibold transition-all text-sm"
                            >
                                <Trash2 size={16} />
                                {deleteLoading ? 'Deleting...' : 'Delete Review'}
                            </button>
                        )}
                        {!userReview.editable && !userReview.deletable && (
                            <p className="text-xs text-gray-500">
                                No actions available for this review.
                            </p>
                        )}
                    </div>
                ) : (
                    <p className="text-xs text-gray-500">
                        Your review has been deleted.
                    </p>
                )}
                {error && (
                    <p className="mt-4 text-sm text-red-500 bg-red-500/10 p-4 rounded-xl border border-red-500/20">
                        {error}
                    </p>
                )}
            </div>
        );
    };

    return (
        <div className="space-y-8 mt-12 pb-20">
            <div className="flex items-center justify-between border-b border-gray-800 pb-4">
                <div className="flex items-center gap-3">
                    <MessageSquare className="text-indigo-400" size={28} />
                    <h3 className="text-2xl font-bold">Community Reviews</h3>
                    <span className="bg-gray-800 px-3 py-1 rounded-full text-sm font-medium text-gray-400">
                        {reviews.length}
                    </span>
                </div>

                <div className="flex items-center gap-2">
                    <span className="text-sm text-gray-400">Sort by:</span>
                    <select
                        value={sortBy}
                        onChange={(e) => setSortBy(e.target.value)}
                        className="bg-gray-800 text-gray-300 text-sm rounded-lg border border-gray-700 focus:ring-indigo-500 focus:border-indigo-500 block p-2.5 outline-none"
                    >
                        <option value="recent">Most Recent</option>
                        <option value="oldest">Oldest First</option>
                    </select>
                </div>
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
                    {renderReviewPanel()}
                </div>
            </div>
        </div>
    );
};

export default ReviewSection;
