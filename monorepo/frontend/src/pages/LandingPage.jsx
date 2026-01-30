import React from 'react';
import { useNavigate } from 'react-router-dom';
import { GraduationCap, Info, HelpCircle } from 'lucide-react';

const LandingPage = () => {
    const navigate = useNavigate();

    return (
        <div className="h-screen w-screen bg-white flex flex-col items-center justify-between p-4 md:p-6 overflow-hidden relative">
            {/* Top Right Info Icon */}
            <div className="absolute top-6 right-6 text-gray-400 hover:text-gray-600 cursor-pointer z-20">
                <Info size={24} />
            </div>

            {/* Main Content Area - Distribute vertical space */}
            <div className="flex-1 w-full max-w-6xl flex flex-col items-center justify-center min-h-0">

                {/* Header Section */}
                <div className="flex flex-col items-center text-center mt-4">
                    <div className="bg-gray-50 p-3 rounded-2xl mb-4">
                        <GraduationCap className="text-indigo-600" size={36} />
                    </div>
                    <h1 className="text-3xl md:text-5xl font-bold text-gray-900 mb-2 tracking-tight">
                        Welcome to LearnSphere
                    </h1>
                    <p className="text-gray-500 text-base md:text-lg max-w-2xl px-4 leading-relaxed line-clamp-2 md:line-clamp-none">
                        Your personalized journey to knowledge starts here. Dive into curated video series and master new skills in your own pace.
                    </p>
                </div>

                {/* Illustration - Flexible container that shrinks */}
                <div className="flex-1 w-full flex items-center justify-center min-h-0 my-4 px-4 overflow-hidden">
                    <img
                        src="/landing-illustration.png"
                        alt="Students studying together"
                        className="h-full max-h-[50vh] w-auto object-contain rounded-2xl shadow-sm"
                    />
                </div>

                {/* Button Section */}
                <div className="mb-8">
                    <button
                        onClick={() => {
                            const token = localStorage.getItem('authToken');
                            const email = localStorage.getItem('userEmail');
                            if (token && email) {
                                navigate('/recommendations', { state: { email } });
                            } else {
                                navigate('/login');
                            }
                        }}
                        className="bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-3 px-12 rounded-xl text-lg transition-all duration-200 shadow-lg hover:shadow-xl transform hover:-translate-y-1"
                    >
                        Get Started
                    </button>
                </div>
            </div>

            {/* Bottom Right Help - Fixed to bottom */}
            <div className="absolute bottom-6 right-6 flex items-center gap-2 bg-gray-100 text-indigo-600 px-4 py-2 rounded-full hover:bg-gray-200 cursor-pointer transition-colors z-20 font-medium text-sm">
                <HelpCircle size={18} />
                <span>Help</span>
            </div>
        </div>
    );
};

export default LandingPage;
