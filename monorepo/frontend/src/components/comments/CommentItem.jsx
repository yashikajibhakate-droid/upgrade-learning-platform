import React from 'react';
import { User, Clock } from 'lucide-react';

const CommentItem = ({ comment }) => {
    const formatDate = (dateString) => {
        const options = { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' };
        return new Date(dateString).toLocaleDateString(undefined, options);
    };

    const getUsername = (email) => {
        if (!email) return 'Anonymous';
        return email.split('@')[0];
    };

    return (
        <div className="bg-gray-800/30 p-4 rounded-xl border border-gray-700/30">
            <div className="flex items-center gap-3 mb-2">
                <div className="bg-indigo-600/20 p-2 rounded-full">
                    <User size={16} className="text-indigo-400" />
                </div>
                <div>
                    <span className="text-sm font-semibold text-gray-200 block">
                        {getUsername(comment.userEmail)}
                    </span>
                    <div className="flex items-center gap-1 text-xs text-gray-500">
                        <Clock size={12} />
                        <span>{formatDate(comment.createdAt)}</span>
                    </div>
                </div>
            </div>
            <p className="text-gray-300 text-sm leading-relaxed pl-11">
                {comment.content}
            </p>
        </div>
    );
};

export default CommentItem;
