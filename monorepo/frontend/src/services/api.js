import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor to add auth token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('authToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor to handle 401s
api.interceptors.response.use(
    (response) => response,
    (error) => {
        const isVerifyOtp = error.config && error.config.url && error.config.url.includes('/api/auth/verify-otp');
        const isMagicLogin = error.config && error.config.url && error.config.url.includes('/api/auth/magic-login');

        if (error.response && error.response.status === 401 && !isVerifyOtp && !isMagicLogin) {
            localStorage.removeItem('authToken');
            localStorage.removeItem('userEmail');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

// Watch Progress API Methods
export const watchProgressApi = {
    getContinueWatching: (email) => api.get('/api/watch-progress/continue', { params: { email } }),
    saveProgress: (email, episodeId, progressSeconds) =>
        api.post('/api/watch-progress/save', { email, episodeId, progressSeconds }),
    markComplete: (email, episodeId) =>
        api.post('/api/watch-progress/complete', { email, episodeId }),
    isCompleted: (email, episodeId) =>
        api.get('/api/watch-progress/is-completed', { params: { email, episodeId } }),
};

// Feedback API Methods
export const feedbackApi = {
    saveFeedback: (episodeId, isHelpful) =>
        api.post('/api/feedback/save', { episodeId, isHelpful }),
    checkExists: (episodeId) =>
        api.post('/api/feedback/exists', { episodeId }),
};

// MCQ API Methods
export const mcqApi = {
    getMCQ: (episodeId) => api.get(`/api/mcq/${episodeId}`),
    validateAnswer: (mcqId, selectedOptionId) =>
        api.post('/api/mcq/validate', { mcqId, selectedOptionId }),
};

// Series Review API Methods
export const seriesReviewApi = {
    submitReview: (seriesId, reviewData) => api.post(`/api/series/${seriesId}/reviews`, reviewData),
    getReviews: (seriesId) => api.get(`/api/series/${seriesId}/reviews`),
};

export default api;
