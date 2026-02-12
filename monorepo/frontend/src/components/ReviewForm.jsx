import React, { useState } from 'react';
import { Star, Send, Loader } from 'lucide-react';

const ReviewForm = ({ onSubmit, loading }) => {
    const [rating, setRating] = useState(0);
    const [hoverRating, setHoverRating] = useState(0);
    const [comment, setComment] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        if (rating === 0) return;
        onSubmit({ rating, comment });
        setRating(0);
        setComment('');
    };

    return (
        <form onSubmit={handleSubmit} className="bg-gray-800/50 p-6 rounded-2xl border border-gray-700/50 space-y-4">
            <h4 className="text-lg font-semibold">Write a Review</h4>

            <div className="flex items-center gap-2">
                {[1, 2, 3, 4, 5].map((star) => (
                    <button
                        key={star}
                        type="button"
                        onClick={() => setRating(star)}
                        onMouseEnter={() => setHoverRating(star)}
                        onMouseLeave={() => setHoverRating(0)}
                        className="transition-transform hover:scale-110"
                    >
                        <Star
                            size={28}
                            className={`${star <= (hoverRating || rating)
                                    ? 'fill-yellow-400 text-yellow-400'
                                    : 'text-gray-500'
                                } transition-colors`}
                        />
                    </button>
                ))}
                <span className="ml-2 text-sm text-gray-400">
                    {rating > 0 ? `${rating} / 5 stars` : 'Select a rating'}
                </span>
            </div>

            <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder="Share your thoughts about this series..."
                className="w-full bg-gray-900 border border-gray-700 rounded-xl p-4 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 min-h-[100px] resize-none"
                required
            />

            <button
                type="submit"
                disabled={loading || rating === 0}
                className="w-full py-3 bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-700 disabled:text-gray-500 text-white rounded-xl font-bold transition-all flex items-center justify-center gap-2"
            >
                {loading ? <Loader className="animate-spin" size={20} /> : <Send size={20} />}
                Submit Review
            </button>
        </form>
    );
};

export default ReviewForm;
