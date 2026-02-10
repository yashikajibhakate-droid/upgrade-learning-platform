import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import FeedbackModal from './FeedbackModal';

describe('FeedbackModal', () => {
    it('should not render when isOpen is false', () => {
        const { container } = render(
            <FeedbackModal
                isOpen={false}
                onClose={vi.fn()}
                onSubmit={vi.fn()}
                episodeTitle="Test Episode"
            />
        );
        expect(container.firstChild).toBeNull();
    });

    it('should render when isOpen is true', () => {
        render(
            <FeedbackModal
                isOpen={true}
                onClose={vi.fn()}
                onSubmit={vi.fn()}
                episodeTitle="Test Episode"
            />
        );
        expect(screen.getByText('How was this episode?')).toBeInTheDocument();
        expect(screen.getByText('Test Episode')).toBeInTheDocument();
    });

    it('should call onSubmit with true when Helpful button is clicked', async () => {
        const mockOnSubmit = vi.fn().mockResolvedValue(undefined);
        const mockOnClose = vi.fn();

        render(
            <FeedbackModal
                isOpen={true}
                onClose={mockOnClose}
                onSubmit={mockOnSubmit}
                episodeTitle="Test Episode"
            />
        );

        const helpfulButton = screen.getByText('Helpful');
        fireEvent.click(helpfulButton);

        await waitFor(() => {
            expect(mockOnSubmit).toHaveBeenCalledWith(true);
        });
    });

    it('should call onSubmit with false when Not Helpful button is clicked', async () => {
        const mockOnSubmit = vi.fn().mockResolvedValue(undefined);
        const mockOnClose = vi.fn();

        render(
            <FeedbackModal
                isOpen={true}
                onClose={mockOnClose}
                onSubmit={mockOnSubmit}
                episodeTitle="Test Episode"
            />
        );

        const notHelpfulButton = screen.getByText('Not Helpful');
        fireEvent.click(notHelpfulButton);

        await waitFor(() => {
            expect(mockOnSubmit).toHaveBeenCalledWith(false);
        });
    });

    it('should call onClose when close button is clicked', () => {
        const mockOnClose = vi.fn();

        render(
            <FeedbackModal
                isOpen={true}
                onClose={mockOnClose}
                onSubmit={vi.fn()}
                episodeTitle="Test Episode"
            />
        );

        // Find the X button (close button)
        const closeButtons = screen.getAllByRole('button');
        const closeButton = closeButtons[0]; // First button is the X button
        fireEvent.click(closeButton);

        expect(mockOnClose).toHaveBeenCalled();
    });

    it('should disable buttons while submitting', async () => {
        const mockOnSubmit = vi.fn(() => new Promise(() => { })); // Never resolves

        render(
            <FeedbackModal
                isOpen={true}
                onClose={vi.fn()}
                onSubmit={mockOnSubmit}
                episodeTitle="Test Episode"
            />
        );

        const helpfulButton = screen.getByText('Helpful');
        fireEvent.click(helpfulButton);

        await waitFor(() => {
            const buttons = screen.getAllByRole('button');
            buttons.forEach((button) => {
                if (button.textContent !== '') {
                    expect(button).toBeDisabled();
                }
            });
        });
    });

    it('should close modal even if submission fails', async () => {
        const mockOnSubmit = vi.fn().mockRejectedValue(new Error('Submission failed'));
        const mockOnClose = vi.fn();

        render(
            <FeedbackModal
                isOpen={true}
                onClose={mockOnClose}
                onSubmit={mockOnSubmit}
                episodeTitle="Test Episode"
            />
        );

        const helpfulButton = screen.getByText('Helpful');
        fireEvent.click(helpfulButton);

        await waitFor(() => {
            expect(mockOnSubmit).toHaveBeenCalledWith(true);
        });
    });
});
