import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import CommentForm from './CommentForm';
import { episodeCommentApi } from '../../services/api';

// Mock the API
vi.mock('../../services/api', () => ({
    episodeCommentApi: {
        addComment: vi.fn(),
    },
}));

describe('CommentForm', () => {
    const mockEpisodeId = '123';
    const mockOnCommentAdded = vi.fn();

    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.setItem('userEmail', 'test@example.com');
    });

    it('displays moderation notice when comment is flagged', async () => {
        // Mock successful but flagged response
        episodeCommentApi.addComment.mockResolvedValue({
            data: { status: 'FLAGGED' }
        });

        render(<CommentForm episodeId={mockEpisodeId} onCommentAdded={mockOnCommentAdded} />);

        const textarea = screen.getByPlaceholderText(/Share your thoughts/i);
        fireEvent.change(textarea, { target: { value: 'Suspicious comment' } });

        const submitBtn = screen.getByRole('button', { name: /Post Comment/i });
        fireEvent.click(submitBtn);

        await waitFor(() => {
            expect(screen.getByText(/Your comment has been flagged for moderation/i)).toBeInTheDocument();
        });

        // Verify API was called
        expect(episodeCommentApi.addComment).toHaveBeenCalledWith(mockEpisodeId, 'Suspicious comment');

        // Callback should still be called (based on current implementation logic)
        // or check if it should NOT be called? The code calls setContent('') and onCommentAdded() regardless of status
        expect(mockOnCommentAdded).toHaveBeenCalled();
    });

    it('clears notice when user starts typing', async () => {
        episodeCommentApi.addComment.mockResolvedValue({
            data: { status: 'FLAGGED' }
        });

        render(<CommentForm episodeId={mockEpisodeId} onCommentAdded={mockOnCommentAdded} />);

        const textarea = screen.getByPlaceholderText(/Share your thoughts/i);
        fireEvent.change(textarea, { target: { value: 'Suspicious comment' } });
        fireEvent.click(screen.getByRole('button', { name: /Post Comment/i }));

        await waitFor(() => {
            expect(screen.getByText(/Your comment has been flagged/i)).toBeInTheDocument();
        });

        // Type again
        fireEvent.change(textarea, { target: { value: 'New text' } });

        // Notice should be gone
        await waitFor(() => {
            expect(screen.queryByText(/Your comment has been flagged/i)).not.toBeInTheDocument();
        });
    });
});
