import React from 'react';
import { Star, CheckCircle, User } from 'lucide-react';

const SeriesReviewList = ({ reviews }) => {
    if (!reviews || reviews.length === 0) {
        return (
            <div className="text-center py-12 bg-gray-800/30 rounded-2xl border border-gray-700/30">
                <p className="text-gray-500">No reviews yet. Be the first to share your thoughts!</p>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            {reviews.map((review) => (
                <div key={review.id} className="bg-gray-800/50 p-6 rounded-2xl border border-gray-700/50 space-y-3">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 bg-gray-700 rounded-full flex items-center justify-center text-gray-400">
                                <User size={20} />
                            </div>
                            <div>
                                <div className="font-semibold text-gray-200">
                                    {review.reviewerName}
                                </div>
                                <div className="text-xs text-gray-500">
                                    {new Date(review.createdAt).toLocaleDateString()}
                                </div>
                            </div>
                        </div>
                        <div className="flex items-center gap-1">
                            {[1, 2, 3, 4, 5].map((star) => (
                                <Star
                                    key={star}
                                    size={16}
                                    className={`${star <= review.rating
                                        ? 'fill-yellow-400 text-yellow-400'
                                        : 'text-gray-600'
                                        }`}
                                />
                            ))}
                        </div>
                    </div>

                    <p className="text-gray-300 leading-relaxed">
                        {review.comment}
                    </p>

                    <div className="flex items-center gap-4 pt-2">
                        {review.isVerified ? (
                            <div className="flex items-center gap-1.5 px-3 py-1 bg-green-500/10 text-green-400 text-xs font-bold rounded-full border border-green-500/20">
                                <CheckCircle size={14} />
                                Verified Watched Review
                            </div>
                        ) : (
                            <div className="text-xs text-gray-500 italic">
                                Progress: {Math.round(review.progressPercentage)}%
                            </div>
                        )}
                    </div>
                </div>
            ))}
        </div>
    );
};

export default SeriesReviewList;
