import React, { useState, useEffect, useRef } from 'react';
import { CheckCircle, XCircle, X, Loader } from 'lucide-react';
import { mcqApi } from '../services/api';

const MCQModal = ({ isOpen, mcqData, onCorrect, onIncorrect, onClose }) => {
    const [selectedOption, setSelectedOption] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [validationResult, setValidationResult] = useState(null);
    const timeoutRef = useRef(null);

    // Reset state when modal opens or data changes
    useEffect(() => {
        if (isOpen) {
            setSelectedOption(null);
            setIsSubmitting(false);
            setValidationResult(null);
        }
    }, [isOpen, mcqData]);

    // Cleanup timeout on unmount
    useEffect(() => {
        return () => {
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, []);

    const handleClose = () => {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }
        onClose();
    };

    if (!isOpen || !mcqData) return null;

    const handleSubmit = async () => {
        if (!selectedOption) {
            alert('Please select an option');
            return;
        }

        setIsSubmitting(true);
        try {
            // Call backend to validate answer using mcqApi (includes auth token)
            const response = await mcqApi.validateAnswer(mcqData.id, selectedOption);
            const result = response.data;
            setValidationResult(result);

            // Wait a moment to show result, then trigger appropriate callback
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }

            timeoutRef.current = setTimeout(() => {
                if (result.correct) {
                    onCorrect();
                } else {
                    onIncorrect(result.refresherVideoUrl);
                }
            }, 1500);
        } catch (error) {
            console.error('Failed to validate MCQ answer:', error);
            // On error, proceed to next episode
            onCorrect();
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleOptionSelect = (optionId) => {
        if (validationResult) return; // Don't allow changing after submission
        setSelectedOption(optionId);
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm">
            <div className="relative bg-gray-900 rounded-2xl shadow-2xl p-8 max-w-2xl w-full mx-4 border border-gray-700">
                {/* Close Button */}
                <button
                    onClick={handleClose}
                    className="p-1 hover:bg-gray-800 rounded-full transition-colors"
                >
                    <X className="w-5 h-5 text-gray-400" />
                </button>

                {/* Content */}
                <div className="space-y-6">
                    <div className="text-center">
                        <h2 className="text-2xl font-bold text-white mb-2">
                            Quick Knowledge Check
                        </h2>
                        <p className="text-gray-400 text-sm">
                            Test your understanding of what you just learned
                        </p>
                    </div>

                    {/* Question */}
                    <div className="bg-gray-800 p-6 rounded-xl">
                        <p className="text-white text-lg leading-relaxed">
                            {mcqData.question}
                        </p>
                    </div>

                    {/* Options */}
                    <div className="space-y-3">
                        {mcqData.options.map((option) => {
                            const isSelected = selectedOption === option.id;
                            const showCorrect = validationResult?.correct && isSelected;
                            const showIncorrect = validationResult && !validationResult.correct && isSelected;

                            return (
                                <button
                                    key={option.id}
                                    onClick={() => handleOptionSelect(option.id)}
                                    disabled={isSubmitting || validationResult}
                                    className={`w-full text-left p-4 rounded-xl border-2 transition-all flex items-center gap-3 ${showCorrect
                                        ? 'bg-green-900/30 border-green-500'
                                        : showIncorrect
                                            ? 'bg-red-900/30 border-red-500'
                                            : isSelected
                                                ? 'bg-indigo-900/30 border-indigo-500'
                                                : 'bg-gray-800 border-gray-700 hover:border-gray-600'
                                        } ${validationResult || isSubmitting
                                            ? 'cursor-not-allowed'
                                            : 'cursor-pointer hover:scale-[1.02]'
                                        }`}
                                >
                                    {/* Radio indicator */}
                                    <div
                                        className={`w-5 h-5 rounded-full border-2 flex items-center justify-center flex-shrink-0 ${isSelected ? 'border-indigo-500 bg-indigo-500' : 'border-gray-500'
                                            }`}
                                    >
                                        {isSelected && (
                                            <div className="w-2 h-2 bg-white rounded-full" />
                                        )}
                                    </div>

                                    {/* Option text */}
                                    <span className="text-white flex-1">{option.optionText}</span>

                                    {/* Validation icons */}
                                    {showCorrect && (
                                        <CheckCircle className="text-green-500" size={24} />
                                    )}
                                    {showIncorrect && (
                                        <XCircle className="text-red-500" size={24} />
                                    )}
                                </button>
                            );
                        })}
                    </div>

                    {/* Submit Button */}
                    {!validationResult && (
                        <button
                            onClick={handleSubmit}
                            disabled={!selectedOption || isSubmitting}
                            className="w-full py-4 bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-700 disabled:cursor-not-allowed text-white font-semibold rounded-xl transition-all transform hover:scale-[1.02] active:scale-95 shadow-lg"
                        >
                            {isSubmitting ? (
                                <span className="flex items-center justify-center gap-2">
                                    <Loader className="animate-spin" size={20} />
                                    Checking...
                                </span>
                            ) : (
                                'Submit Answer'
                            )}
                        </button>
                    )}

                    {/* Validation Message */}
                    {validationResult && (
                        <div
                            className={`text-center p-4 rounded-xl ${validationResult.correct
                                ? 'bg-green-900/30 text-green-400'
                                : 'bg-red-900/30 text-red-400'
                                }`}
                        >
                            {validationResult.correct ? (
                                <p className="font-semibold">✅ Correct! Moving to next episode...</p>
                            ) : (
                                <p className="font-semibold">
                                    ❌ Not quite right. Watch the refresher video to learn more...
                                </p>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default MCQModal;
