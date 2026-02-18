import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import CommentList from './CommentList';
import { episodeCommentApi } from '../../services/api';

// Mock the API
vi.mock('../../services/api', () => ({
    episodeCommentApi: {
        getComments: vi.fn(),
        addComment: vi.fn(),
    },
}));

describe('CommentList', () => {
    const mockEpisodeId = '123';
    const mockComments = [
        {
            id: '1',
            userEmail: 'user1@example.com',
            content: 'Great episode!',
            createdAt: '2023-01-01T10:00:00',
            status: 'APPROVED'
        },
        {
            id: '2',
            userEmail: 'user2@example.com',
            content: 'Learned a lot.',
            createdAt: '2023-01-02T11:00:00',
            status: 'APPROVED'
        }
    ];

    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    it('renders comments after loading', async () => {
        episodeCommentApi.getComments.mockResolvedValue({ data: mockComments });

        render(<CommentList episodeId={mockEpisodeId} />);

        // Check loading state (optional, might be too fast)
        // await waitFor(() => expect(screen.getByRole('status')).toBeInTheDocument()); 

        // Wait for comments
        expect(await screen.findByText('Great episode!')).toBeInTheDocument();
        expect(screen.getByText('Learned a lot.')).toBeInTheDocument();
        expect(screen.getByText('user1')).toBeInTheDocument(); // Username from email
        expect(episodeCommentApi.getComments).toHaveBeenCalledWith(mockEpisodeId);
    });

    it('renders empty state when no comments', async () => {
        episodeCommentApi.getComments.mockResolvedValue({ data: [] });

        render(<CommentList episodeId={mockEpisodeId} />);

        expect(await screen.findByText(/No comments yet/i)).toBeInTheDocument();
    });

    it('allows submitting a comment if logged in', async () => {
        localStorage.setItem('userEmail', 'test@example.com');
        episodeCommentApi.getComments.mockResolvedValue({ data: [] });
        episodeCommentApi.addComment.mockResolvedValue({ data: { status: 'APPROVED' } });

        render(<CommentList episodeId={mockEpisodeId} />);

        // Wait for initial load
        await waitFor(() => expect(screen.queryByText(/loading/i)).not.toBeInTheDocument());

        // Find input
        const textarea = screen.getByPlaceholderText(/Share your thoughts/i);
        fireEvent.change(textarea, { target: { value: 'New comment' } });

        // Submit
        const submitBtn = screen.getByRole('button', { name: /Post Comment/i });
        fireEvent.click(submitBtn);

        // Verify API call
        await waitFor(() => expect(episodeCommentApi.addComment).toHaveBeenCalledWith(mockEpisodeId, 'New comment'));

        // Verify refetch
        expect(episodeCommentApi.getComments).toHaveBeenCalledTimes(2); // Initial + after submit
    });

    it('shows login message if not logged in', async () => {
        episodeCommentApi.getComments.mockResolvedValue({ data: [] });

        render(<CommentList episodeId={mockEpisodeId} />);

        expect(await screen.findByText(/Log in/i)).toBeInTheDocument();
        expect(screen.queryByPlaceholderText(/Share your thoughts/i)).not.toBeInTheDocument();
    });
});
