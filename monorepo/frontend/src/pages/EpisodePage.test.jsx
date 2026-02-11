import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import EpisodePage from './EpisodePage';
import api, { watchProgressApi, feedbackApi, mcqApi } from '../services/api';
import { vi } from 'vitest';

// Mock all API modules
vi.mock('../services/api', () => {
    const mockApi = {
        get: vi.fn(),
        post: vi.fn(),
    };
    return {
        default: mockApi,
        watchProgressApi: {
            isCompleted: vi.fn(),
            getContinueWatching: vi.fn(),
            saveProgress: vi.fn().mockResolvedValue({}),
            markComplete: vi.fn().mockResolvedValue({}),
        },
        feedbackApi: {
            saveFeedback: vi.fn().mockResolvedValue({}),
        },
        mcqApi: {
            getMCQ: vi.fn(),
            validateAnswer: vi.fn(),
        },
    };
});

// Mock child components
vi.mock('../components/VideoPlayer', () => ({
    default: ({ onEnded, title }) => (
        <div data-testid="video-player">
            Video Player: {title}
            <button onClick={onEnded} data-testid="trigger-ended">Trigger Ended</button>
        </div>
    ),
}));

vi.mock('../components/FeedbackModal', () => ({
    default: ({ isOpen, onClose, onSubmit }) => isOpen ? (
        <div data-testid="feedback-modal">
            Feedback Modal
            <button onClick={() => onSubmit(true)}>Helpful</button>
            <button onClick={() => onSubmit(false)}>Not Helpful</button>
            <button onClick={onClose}>Close</button>
        </div>
    ) : null,
}));

vi.mock('../components/MCQModal', () => ({
    default: ({ isOpen, onClose, onCorrect, onIncorrect }) => isOpen ? (
        <div data-testid="mcq-modal">
            MCQ Modal
            <button onClick={onCorrect}>Correct</button>
            <button onClick={() => onIncorrect(null)}>Incorrect</button>
            <button onClick={onClose}>Close</button>
        </div>
    ) : null,
}));

// Mock react-router hooks
const mockNavigate = vi.fn();
const mockParams = { seriesId: '1' };
const mockSearchParams = new URLSearchParams();

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useParams: () => mockParams,
        useNavigate: () => mockNavigate,
        useSearchParams: () => [mockSearchParams],
    };
});

describe('EpisodePage Auto-play', () => {
    const mockSeries = {
        id: '1',
        title: 'Test Series',
        thumbnailUrl: 'test.jpg',
    };

    const mockEpisodes = [
        { id: '101', title: 'Ep 1', sequenceNumber: 1, videoUrl: 'v1.mp4', durationSeconds: 600 },
        { id: '102', title: 'Ep 2', sequenceNumber: 2, videoUrl: 'v2.mp4', durationSeconds: 600 },
    ];

    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.setItem('userEmail', 'test@example.com');

        // Setup default API responses
        api.get.mockImplementation((url) => {
            if (url === '/api/series/1') return Promise.resolve({ data: mockSeries });
            if (url === '/api/series/1/episodes') return Promise.resolve({ data: mockEpisodes });
            return Promise.reject(new Error('Not found'));
        });

        watchProgressApi.isCompleted.mockResolvedValue({ data: { isCompleted: false } });
        watchProgressApi.getContinueWatching.mockResolvedValue({ data: {} });
        mcqApi.getMCQ.mockResolvedValue({ data: null }); // Default no MCQ
    });

    test('shows countdown after episode ends and no interactions needed', async () => {
        render(
            <BrowserRouter>
                <EpisodePage />
            </BrowserRouter>
        );

        await waitFor(() => expect(screen.getByText('Video Player: Ep 1')).toBeInTheDocument());

        // Trigger episode end
        fireEvent.click(screen.getByTestId('trigger-ended'));

        // Should show feedback modal first (simulating flow)
        await waitFor(() => expect(screen.getByTestId('feedback-modal')).toBeInTheDocument());

        // Close feedback (skip)
        fireEvent.click(screen.getByText('Close'));

        // Verify countdown appears using findByText which waits automatically
        expect(await screen.findByText(/Up Next/i)).toBeInTheDocument();
        expect(screen.getByText('5')).toBeInTheDocument();
    });

    test.skip('auto-navigates to next episode after countdown finishes', async () => {
        vi.useFakeTimers();

        render(
            <BrowserRouter>
                <EpisodePage />
            </BrowserRouter>
        );

        await waitFor(() => expect(screen.getByText('Video Player: Ep 1')).toBeInTheDocument());

        // End episode & skip feedback
        fireEvent.click(screen.getByTestId('trigger-ended'));
        await waitFor(() => expect(screen.getByTestId('feedback-modal')).toBeInTheDocument());
        fireEvent.click(screen.getByText('Close'));

        // Wait for countdown
        expect(await screen.findByText(/Up Next/i)).toBeInTheDocument();

        // Advance timer completely
        act(() => {
            vi.runAllTimers();
        });

        // Should load next episode
        await waitFor(() => expect(screen.getByText('Video Player: Ep 2')).toBeInTheDocument());

        vi.useRealTimers();
    });

    test.skip('"Play Now" button skips countdown', async () => {
        render(
            <BrowserRouter>
                <EpisodePage />
            </BrowserRouter>
        );

        await waitFor(() => expect(screen.getByText('Video Player: Ep 1')).toBeInTheDocument());

        // End episode & skip feedback
        fireEvent.click(screen.getByTestId('trigger-ended'));
        await waitFor(() => expect(screen.getByTestId('feedback-modal')).toBeInTheDocument());
        fireEvent.click(screen.getByText('Close'));

        // Wait for countdown
        const playNowBtn = await screen.findByText('Play Now');

        // Click Play Now
        fireEvent.click(playNowBtn);

        // Should immediately go to next episode
        await waitFor(() => expect(screen.getByText('Video Player: Ep 2')).toBeInTheDocument());
    });

    test.skip('last episode redirects to home after countdown', async () => {
        vi.useFakeTimers();

        // Mock only one episode
        api.get.mockImplementation((url) => {
            if (url === '/api/series/1') return Promise.resolve({ data: mockSeries });
            if (url === '/api/series/1/episodes') return Promise.resolve({ data: [mockEpisodes[0]] });
            return Promise.reject(new Error('Not found'));
        });

        render(
            <BrowserRouter>
                <EpisodePage />
            </BrowserRouter>
        );

        await waitFor(() => expect(screen.getByText('Video Player: Ep 1')).toBeInTheDocument());

        // End episode & skip feedback
        fireEvent.click(screen.getByTestId('trigger-ended'));
        await waitFor(() => expect(screen.getByTestId('feedback-modal')).toBeInTheDocument());
        fireEvent.click(screen.getByText('Close'));

        // Wait for countdown with "Home" text
        expect(await screen.findByText(/Redirecting to Home/i)).toBeInTheDocument();

        // Advance timer
        await act(async () => {
            vi.advanceTimersByTime(6000);
        });

        // Should navigate to home
        await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/'), { timeout: 3000 });

        vi.useRealTimers();
    });
});
