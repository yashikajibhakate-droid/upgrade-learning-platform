import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import VerifyOtpPage from './pages/VerifyOtpPage';
import HealthComponent from './components/HealthComponent'; // Keeping for debugging if needed

const App = () => {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<LandingPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/verify-otp" element={<VerifyOtpPage />} />
                <Route path="/health" element={<HealthComponent />} />
            </Routes>
        </Router>
    );
}

export default App;
