import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import ReviewSection from './ReviewSection';
import { seriesReviewApi } from '../services/api';

// Mock the API
vi.mock('../services/api', () => ({
    seriesReviewApi: {
        getReviews: vi.fn(),
        submitReview: vi.fn(),
        updateReview: vi.fn(),
        deleteReview: vi.fn(),
    },
}));

describe('ReviewSection', () => {
    const mockSeriesId = '123';
    const mockReviews = [
        {
            id: '1',
            reviewerName: 'John',
            rating: 5,
            comment: 'Great!',
            createdAt: '2023-01-01',
            isVerified: true
        },
        {
            id: '2',
            reviewerName: 'Jane',
            rating: 4,
            comment: 'Good',
            createdAt: '2023-01-02',
            isVerified: false
        }
    ];

    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    it('renders reviews and default sort option', async () => {
        seriesReviewApi.getReviews.mockResolvedValue({ data: mockReviews });

        render(<ReviewSection seriesId={mockSeriesId} isLoggedIn={false} />);

        // Check if loading finishes
        await waitFor(() => expect(screen.queryByText(/loading/i)).not.toBeInTheDocument()); // Assuming no text "loading" but skeleton

        // Check reviews are rendered
        expect(await screen.findByText('Great!')).toBeInTheDocument();
        expect(screen.getByText('Good')).toBeInTheDocument();

        // Check default sort
        expect(seriesReviewApi.getReviews).toHaveBeenCalledWith(mockSeriesId, 'recent');
    });

    it('fetches reviews with "oldest" when sort is changed', async () => {
        seriesReviewApi.getReviews.mockResolvedValue({ data: mockReviews });

        render(<ReviewSection seriesId={mockSeriesId} isLoggedIn={false} />);

        await waitFor(() => expect(seriesReviewApi.getReviews).toHaveBeenCalledTimes(1));

        // Find sort dropdown
        const sortSelect = screen.getByRole('combobox');
        fireEvent.change(sortSelect, { target: { value: 'oldest' } });

        await waitFor(() => expect(seriesReviewApi.getReviews).toHaveBeenCalledWith(mockSeriesId, 'oldest'));
    });
});
