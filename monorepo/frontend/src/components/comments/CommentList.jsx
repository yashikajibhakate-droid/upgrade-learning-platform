import React, { useState, useEffect } from 'react';
import { episodeCommentApi } from '../../services/api';
import CommentItem from './CommentItem';
import CommentForm from './CommentForm';
import { MessageCircle } from 'lucide-react';

const CommentList = ({ episodeId }) => {
    const [comments, setComments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchComments = async () => {
        try {
            setLoading(true);
            const response = await episodeCommentApi.getComments(episodeId);
            setComments(response.data);
            setError(null);
        } catch (err) {
            console.error('Failed to fetch comments:', err);
            setError('Failed to load comments.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (episodeId) {
            fetchComments();
        }
    }, [episodeId]);

    return (
        <div className="space-y-6">
            <div className="flex items-center gap-2 mb-6">
                <MessageCircle className="text-indigo-400" size={24} />
                <h3 className="text-xl font-bold text-white">Discussion</h3>
                <span className="bg-gray-800 px-2 py-0.5 rounded-full text-xs font-medium text-gray-400">
                    {comments.length}
                </span>
            </div>

            <CommentForm episodeId={episodeId} onCommentAdded={fetchComments} />

            <div className="space-y-4 mt-8">
                {loading ? (
                    <div className="space-y-4">
                        {[1, 2].map(i => (
                            <div key={i} className="h-24 bg-gray-800/30 rounded-xl animate-pulse" />
                        ))}
                    </div>
                ) : error ? (
                    <p className="text-sm text-red-400">{error}</p>
                ) : comments.length === 0 ? (
                    <div className="text-center py-8 bg-gray-800/20 rounded-xl border border-dashed border-gray-700">
                        <p className="text-gray-500 text-sm">No comments yet. Be the first to share your thoughts!</p>
                    </div>
                ) : (
                    comments.map(comment => (
                        <CommentItem key={comment.id} comment={comment} />
                    ))
                )}
            </div>
        </div>
    );
};

export default CommentList;
