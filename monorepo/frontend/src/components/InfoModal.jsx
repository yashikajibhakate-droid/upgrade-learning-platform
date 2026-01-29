import React from 'react';
import { X } from 'lucide-react';

const InfoModal = ({ isOpen, onClose }) => {
    if (!isOpen) return null;

    return (
        <div
            className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4 backdrop-blur-sm"
            onClick={onClose}
            aria-modal="true"
            role="dialog"
            id="info-modal"
        >
            <div
                className="bg-white rounded-2xl w-full max-w-lg p-6 relative shadow-xl transform transition-all animate-in fade-in zoom-in-95 duration-200"
                onClick={(e) => e.stopPropagation()}
            >
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 p-2 rounded-full hover:bg-gray-100 transition-colors text-gray-500"
                    aria-label="Close modal"
                >
                    <X size={20} />
                </button>

                <div className="mt-2 text-left">
                    <h3 className="text-xl font-bold text-gray-900 mb-4">
                        Why choose interests?
                    </h3>

                    <div className="space-y-4 text-gray-600">
                        <p>
                            Selecting interests helps us personalize your learning journey.
                            Based on your choices, we'll recommend:
                        </p>
                        <ul className="list-disc pl-5 space-y-2">
                            <li>Relevant courses and tutorials</li>
                            <li>Projects tailored to your skill level</li>
                            <li>Community discussions and events</li>
                            <li>Career paths matching your goals</li>
                        </ul>
                        <p className="text-sm text-gray-500 mt-6 pt-6 border-t border-gray-100">
                            You can always update these preferences later in your profile settings.
                        </p>
                    </div>

                    <button
                        onClick={onClose}
                        className="mt-8 w-full bg-indigo-600 text-white font-semibold py-3 px-6 rounded-lg hover:bg-indigo-700 transition-colors"
                    >
                        Got it, thanks!
                    </button>
                </div>
            </div>
        </div>
    );
};

export default InfoModal;
