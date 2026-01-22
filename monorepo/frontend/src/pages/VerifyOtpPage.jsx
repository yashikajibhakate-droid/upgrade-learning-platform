import React, { useState, useRef, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Info, HelpCircle } from 'lucide-react';
import api from '../services/api';

const VerifyOtpPage = () => {
    const [otp, setOtp] = useState(['', '', '', '', '', '']);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const location = useLocation();
    const navigate = useNavigate();
    const email = location.state?.email || '';

    const inputRefs = useRef([]);

    useEffect(() => {
        // Focus first input on mount
        if (inputRefs.current[0]) {
            inputRefs.current[0].focus();
        }
    }, []);

    const handleChange = (index, value) => {
        // Allow only number
        if (isNaN(value)) return;

        const newOtp = [...otp];
        newOtp[index] = value.substring(value.length - 1); // Only take last char
        setOtp(newOtp);

        // Move to next input if value entered
        if (value && index < 5 && inputRefs.current[index + 1]) {
            inputRefs.current[index + 1].focus();
        }
    };

    const handleKeyDown = (index, e) => {
        // Move to prev input on Backspace if current empty
        if (e.key === 'Backspace' && !otp[index] && index > 0 && inputRefs.current[index - 1]) {
            inputRefs.current[index - 1].focus();
        }
    };

    const handlePaste = (e) => {
        e.preventDefault();
        const pastedData = e.clipboardData.getData('text').slice(0, 6).split('');
        if (pastedData.every(char => !isNaN(char))) {
            const newOtp = [...otp];
            pastedData.forEach((char, i) => {
                if (i < 6) newOtp[i] = char;
            });
            setOtp(newOtp);
            if (inputRefs.current[Math.min(pastedData.length, 5)]) {
                inputRefs.current[Math.min(pastedData.length, 5)].focus();
            }
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const otpCode = otp.join('');
        if (otpCode.length !== 6) {
            setError('Please enter the full 6-digit code');
            return;
        }

        setLoading(true);
        setError('');

        try {
            const response = await api.post('/auth/verify-otp', { email, otp: otpCode });
            if (response.status === 200) {
                alert('Login Successful!');
                navigate('/');
            }
        } catch (err) {
            console.error(err);
            setError('Invalid or expired OTP. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4 relative font-sans text-gray-900">
            {/* Top Right Info Icon */}
            <div className="absolute top-6 right-6">
                <div className="bg-gray-800 text-white p-2 rounded-lg cursor-pointer hover:bg-gray-700 transition-colors">
                    <Info size={24} />
                </div>
            </div>

            {/* Main Card */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 md:p-12 max-w-md w-full text-center">

                <h1 className="text-2xl font-bold mb-2 text-gray-900">Login or Sign Up</h1>

                <div className="space-y-1 mb-8">
                    <p className="text-gray-500 text-sm">
                        Enter your email to receive a login code.
                    </p>
                    <p className="text-gray-500 text-sm font-medium">
                        Enter the 6-digit code sent to your email.
                    </p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-8">
                    {/* OTP Inputs */}
                    <div className="flex justify-between gap-2">
                        {otp.map((digit, index) => (
                            <input
                                key={index}
                                ref={el => inputRefs.current[index] = el}
                                type="text"
                                maxLength={1}
                                value={digit}
                                onChange={(e) => handleChange(index, e.target.value)}
                                onKeyDown={(e) => handleKeyDown(index, e)}
                                onPaste={handlePaste}
                                className="w-10 h-10 md:w-12 md:h-12 border border-gray-300 rounded-lg text-center text-xl font-bold text-gray-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
                            />
                        ))}
                    </div>

                    {error && <p className="text-red-500 text-sm font-medium">{error}</p>}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-3.5 rounded-lg transition-colors duration-200 shadow-md disabled:opacity-70"
                    >
                        {loading ? 'Verifying...' : 'Verify & Continue'}
                    </button>

                    <div className="text-center">
                        <button
                            type="button"
                            onClick={() => navigate('/login')}
                            className="text-sm text-gray-500 hover:text-gray-700 font-medium transition-colors"
                        >
                            Back to email
                        </button>
                    </div>
                </form>
            </div>

            {/* Bottom Right Help */}
            <div className="absolute bottom-6 right-6">
                <div className="bg-indigo-600 text-white p-2 rounded-full cursor-pointer hover:bg-indigo-700 shadow-lg transition-colors">
                    <HelpCircle size={24} />
                </div>
            </div>
        </div>
    );
};

export default VerifyOtpPage;
