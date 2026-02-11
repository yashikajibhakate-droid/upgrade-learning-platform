import React from 'react';
import { render, screen, act, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import EpisodePage from './EpisodePage';
import { watchProgressApi } from '../services/api';
import { vi } from 'vitest';

// Mock API
vi.mock('../services/api', () => {
    return {
        default: {
            get: vi.fn().mockImplementation((url) => {
                if (url.includes('/api/series/')) return Promise.resolve({ data: { id: '1', title: 'Test Series' } });
                return Promise.reject(new Error('Not found'));
            }),
        },
        watchProgressApi: {
            isCompleted: vi.fn().mockResolvedValue({ data: { isCompleted: false } }),
            getContinueWatching: vi.fn().mockResolvedValue({ data: {} }),
            saveProgress: vi.fn().mockResolvedValue({}),
            markComplete: vi.fn().mockResolvedValue({}),
        },
        feedbackApi: {
            saveFeedback: vi.fn().mockResolvedValue({}),
        },
        mcqApi: {
            getMCQ: vi.fn().mockResolvedValue({ data: null }),
        },
    };
});

// Mock VideoPlayer to expose onProgressUpdate
vi.mock('../components/VideoPlayer', () => ({
    default: ({ onProgressUpdate, title }) => (
        <div data-testid="video-player">
            Video Player: {title}
            <button
                data-testid="trigger-progress"
                onClick={() => onProgressUpdate(10)}
            >
                Trigger Progress 10s
            </button>
            <button
                data-testid="trigger-progress-20"
                onClick={() => onProgressUpdate(20)}
            >
                Trigger Progress 20s
            </button>
        </div>
    ),
}));

// Mock Router
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useParams: () => ({ seriesId: '1' }),
        useSearchParams: () => [new URLSearchParams()],
    };
});

describe('EpisodePage Session Resolution', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.useFakeTimers();
        // Define property for visibilityState if not exists
        Object.defineProperty(document, 'visibilityState', {
            value: 'visible',
            writable: true,
        });
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    test('saveProgress is called with timestamp', async () => {
        render(
            <BrowserRouter>
                <EpisodePage />
            </BrowserRouter>
        );

        // Wait for load
        await screen.findByText(/Video Player/);

        // Trigger progress update
        fireEvent.click(screen.getByTestId('trigger-progress'));

        // Advance time to bypass throttle check (initial save might happen or not depending on implementation?
        // My implementation: throttle 5s. if (now - lastSave > 5000). Init lastSave=0.
        // So first call should pass.)

        // Verify saveProgress called with 4 args
        expect(watchProgressApi.saveProgress).toHaveBeenCalledTimes(1);
        const args = watchProgressApi.saveProgress.mock.calls[0];
        expect(args[2]).toBe(10); // progress
        expect(typeof args[3]).toBe('number'); // timestamp
    });

    test('saveProgress is throttled', async () => {
        render(
            <BrowserRouter>
                <EpisodePage />
            </BrowserRouter>
        );

        await screen.findByText(/Video Player/);

        // First trigger (should save)
        fireEvent.click(screen.getByTestId('trigger-progress'));
        expect(watchProgressApi.saveProgress).toHaveBeenCalledTimes(1);

        // Advance time by 2 seconds (less than 5s throttle)
        vi.advanceTimersByTime(2000);

        // Second trigger (should NOT save)
        fireEvent.click(screen.getByTestId('trigger-progress-20'));
        expect(watchProgressApi.saveProgress).toHaveBeenCalledTimes(1);

        // Advance time by 4 seconds (total 6s > 5s)
        vi.advanceTimersByTime(4000);

        // Third trigger (should save)
        fireEvent.click(screen.getByTestId('trigger-progress-20'));
        expect(watchProgressApi.saveProgress).toHaveBeenCalledTimes(2);
        expect(watchProgressApi.saveProgress.mock.calls[1][2]).toBe(20);
    });

    test('interaction timestamp updates on visibility change', async () => {
        render(
            <BrowserRouter>
                <EpisodePage />
            </BrowserRouter>
        );

        await screen.findByText(/Video Player/);

        // Initial trigger
        const startTime = Date.now();
        fireEvent.click(screen.getByTestId('trigger-progress'));
        const initialTimestamp = watchProgressApi.saveProgress.mock.calls[0][3];

        // Advance time
        const advanceMs = 10000;
        vi.advanceTimersByTime(advanceMs); // Advance system time

        // Simulate leaving and returning (visibility change)
        // Note: My implementation updates timestamp on `visibilitychange` to `visible`

        // Hide
        Object.defineProperty(document, 'visibilityState', { value: 'hidden', writable: true });
        fireEvent(document, new Event('visibilitychange'));

        // Verify no update to timestamp yet? (Logic: on `visible` update)

        // Show
        Object.defineProperty(document, 'visibilityState', { value: 'visible', writable: true });
        fireEvent(document, new Event('visibilitychange'));

        // Trigger progress (throttle allowed)
        fireEvent.click(screen.getByTestId('trigger-progress-20'));

        const newTimestamp = watchProgressApi.saveProgress.mock.calls[1][3];

        // Verify new timestamp is greater than initial
        expect(newTimestamp).toBeGreaterThan(initialTimestamp);
        // It should differ by roughly advanceMs
        expect(newTimestamp - initialTimestamp).toBeGreaterThanOrEqual(advanceMs);
    });
});
