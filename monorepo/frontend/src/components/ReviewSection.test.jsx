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
        getRatingSummary: vi.fn(),
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
            isVerified: true,
            isOwnReview: false
        },
        {
            id: '2',
            reviewerName: 'Jane',
            rating: 4,
            comment: 'Good',
            createdAt: '2023-01-02',
            isVerified: false,
            isOwnReview: false
        }
    ];

    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    it('renders reviews and default sort option', async () => {
        seriesReviewApi.getReviews.mockResolvedValue({ data: mockReviews });
        seriesReviewApi.getRatingSummary.mockResolvedValue({ data: { averageRating: 4.5, totalReviews: 2 } });

        render(<ReviewSection seriesId={mockSeriesId} isLoggedIn={false} />);

        // Check if loading finishes
        await waitFor(() => expect(screen.queryByText(/loading/i)).not.toBeInTheDocument());

        // Check reviews are rendered
        expect(await screen.findByText('Great!')).toBeInTheDocument();
        expect(screen.getByText('Good')).toBeInTheDocument();

        // Check default sort
        expect(seriesReviewApi.getReviews).toHaveBeenCalledWith(mockSeriesId, 'recent');
    });

    it('displays rating summary', async () => {
        seriesReviewApi.getReviews.mockResolvedValue({ data: [] });
        seriesReviewApi.getRatingSummary.mockResolvedValue({
            data: { averageRating: 4.5, totalReviews: 10 }
        });

        render(<ReviewSection seriesId={mockSeriesId} isLoggedIn={false} />);

        // Check summary is rendered
        expect(await screen.findByText('4.5')).toBeInTheDocument();
        expect(screen.getByText(/Based on 10 reviews/i)).toBeInTheDocument();
        expect(seriesReviewApi.getRatingSummary).toHaveBeenCalledWith(mockSeriesId);
    });

    it('fetches reviews with "oldest" when sort is changed', async () => {
        seriesReviewApi.getReviews.mockResolvedValue({ data: mockReviews });
        seriesReviewApi.getRatingSummary.mockResolvedValue({ data: { averageRating: 4.5, totalReviews: 2 } });

        render(<ReviewSection seriesId={mockSeriesId} isLoggedIn={false} />);

        await waitFor(() => expect(seriesReviewApi.getReviews).toHaveBeenCalledTimes(1));

        // Find sort dropdown
        const sortSelect = screen.getByRole('combobox');
        fireEvent.change(sortSelect, { target: { value: 'oldest' } });

        await waitFor(() => expect(seriesReviewApi.getReviews).toHaveBeenCalledWith(mockSeriesId, 'oldest'));
    });
});
