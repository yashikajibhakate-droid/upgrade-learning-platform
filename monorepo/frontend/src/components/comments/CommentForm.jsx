import React, { useState } from 'react';
import { Send, AlertCircle } from 'lucide-react';
import { episodeCommentApi } from '../../services/api';

const CommentForm = ({ episodeId, onCommentAdded }) => {
    const [content, setContent] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const userEmail = localStorage.getItem('userEmail');

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!content.trim()) return;
        if (!userEmail) {
            setError('Please log in to comment.');
            return;
        }

        try {
            setLoading(true);
            setError(null);
            const response = await episodeCommentApi.addComment(episodeId, userEmail, content);

            // If the comment is flagged, the backend returns it with status FLAGGED
            if (response.data.status === 'FLAGGED') {
                alert('Your comment has been flagged for moderation and will be reviewed shortly.');
            }

            setContent('');
            if (onCommentAdded) {
                onCommentAdded();
            }
        } catch (err) {
            console.error('Failed to post comment:', err);
            setError('Failed to post comment. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    if (!userEmail) {
        return (
            <div className="bg-gray-800/30 p-4 rounded-xl border border-gray-700/30 text-center">
                <p className="text-sm text-gray-400">
                    <a href="/login" className="text-indigo-400 hover:text-indigo-300">Log in</a> to join the discussion.
                </p>
            </div>
        );
    }

    return (
        <form onSubmit={handleSubmit} className="space-y-3">
            <div className="relative">
                <textarea
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    placeholder="Share your thoughts on this episode..."
                    className="w-full bg-gray-800/50 text-gray-200 text-sm rounded-xl border border-gray-700/50 focus:border-indigo-500/50 focus:ring-1 focus:ring-indigo-500/50 p-4 min-h-[100px] resize-none outline-none transition-all placeholder:text-gray-500"
                    maxLength={1000}
                />
                <div className="absolute bottom-3 right-3 text-xs text-gray-500">
                    {content.length}/1000
                </div>
            </div>

            {error && (
                <div className="flex items-center gap-2 text-xs text-red-400 bg-red-400/10 p-3 rounded-lg">
                    <AlertCircle size={14} />
                    <span>{error}</span>
                </div>
            )}

            <div className="flex justify-end">
                <button
                    type="submit"
                    disabled={loading || !content.trim()}
                    className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-600/50 disabled:cursor-not-allowed text-white text-sm font-medium rounded-lg transition-all"
                >
                    {loading ? (
                        <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    ) : (
                        <Send size={16} />
                    )}
                    Post Comment
                </button>
            </div>
        </form>
    );
};

export default CommentForm;
