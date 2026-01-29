import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import VerifyOtpPage from './pages/VerifyOtpPage';
import RecommendationsPage from './pages/RecommendationsPage';
import OnboardingPage from './pages/OnboardingPage';
import EpisodePage from './pages/EpisodePage';
import HealthComponent from './components/HealthComponent'; // Keeping for debugging if needed

const App = () => {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/verify-otp" element={<VerifyOtpPage />} />
                <Route path="/recommendations" element={<RecommendationsPage />} />
                <Route path="/onboarding" element={<OnboardingPage />} />
                <Route path="/series/:seriesId/watch" element={<EpisodePage />} />
                <Route path="/health" element={<HealthComponent />} />
            </Routes>
        </Router>
    );
}

export default App;
