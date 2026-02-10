import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import MCQModal from './MCQModal';
import { mcqApi } from '../services/api';

// Mock mcqApi
vi.mock('../services/api', () => ({
    mcqApi: {
        validateAnswer: vi.fn()
    }
}));

describe('MCQModal', () => {
    const mockMCQData = {
        id: 'mcq-123',
        question: 'What is the main concept?',
        options: [
            { id: 'option-1', optionText: 'Correct answer' },
            { id: 'option-2', optionText: 'Incorrect answer' }
        ],
        refresherVideoUrl: 'https://example.com/refresher.mp4'
    };

    const mockOnCorrect = vi.fn();
    const mockOnIncorrect = vi.fn();
    const mockOnClose = vi.fn();

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('should not render when isOpen is false', () => {
        const { container } = render(
            <MCQModal
                isOpen={false}
                mcqData={mockMCQData}
                onCorrect={mockOnCorrect}
                onIncorrect={mockOnIncorrect}
                onClose={mockOnClose}
            />
        );

        expect(container.firstChild).toBeNull();
    });

    it('should render when isOpen is true with MCQ data', () => {
        render(
            <MCQModal
                isOpen={true}
                mcqData={mockMCQData}
                onCorrect={mockOnCorrect}
                onIncorrect={mockOnIncorrect}
                onClose={mockOnClose}
            />
        );

        expect(screen.getByText('Quick Knowledge Check')).toBeInTheDocument();
        expect(screen.getByText('What is the main concept?')).toBeInTheDocument();
        expect(screen.getByText('Correct answer')).toBeInTheDocument();
        expect(screen.getByText('Incorrect answer')).toBeInTheDocument();
        expect(screen.getByText('Submit Answer')).toBeInTheDocument();
    });

    it('should allow selecting an option', () => {
        render(
            <MCQModal
                isOpen={true}
                mcqData={mockMCQData}
                onCorrect={mockOnCorrect}
                onIncorrect={mockOnIncorrect}
                onClose={mockOnClose}
            />
        );

        const option1 = screen.getByText('Correct answer').closest('button');
        fireEvent.click(option1);

        // Check that the option is selected (has the indigo border)
        expect(option1).toHaveClass('border-indigo-500');
    });

    it('should call onClose when close button is clicked', () => {
        render(
            <MCQModal
                isOpen={true}
                mcqData={mockMCQData}
                onCorrect={mockOnCorrect}
                onIncorrect={mockOnIncorrect}
                onClose={mockOnClose}
            />
        );

        const closeButton = screen.getAllByRole('button')[0]; // First button is close
        fireEvent.click(closeButton);

        expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('should validate correct answer and call onCorrect', async () => {
        mcqApi.validateAnswer.mockResolvedValue({
            data: { correct: true }
        });

        render(
            <MCQModal
                isOpen={true}
                mcqData={mockMCQData}
                onCorrect={mockOnCorrect}
                onIncorrect={mockOnIncorrect}
                onClose={mockOnClose}
            />
        );

        // Select option
        const option1 = screen.getByText('Correct answer').closest('button');
        fireEvent.click(option1);

        // Submit
        const submitButton = screen.getByText('Submit Answer');
        fireEvent.click(submitButton);

        // Wait for API call
        await waitFor(() => {
            expect(mcqApi.validateAnswer).toHaveBeenCalledWith('mcq-123', 'option-1');
        });

        // Wait for onCorrect to be called (after 1.5s delay)
        await waitFor(() => {
            expect(mockOnCorrect).toHaveBeenCalledTimes(1);
        }, { timeout: 2000 });
    });

    it('should validate incorrect answer and call onIncorrect with refresher URL', async () => {
        mcqApi.validateAnswer.mockResolvedValue({
            data: {
                correct: false,
                refresherVideoUrl: 'https://example.com/refresher.mp4'
            }
        });

        render(
            <MCQModal
                isOpen={true}
                mcqData={mockMCQData}
                onCorrect={mockOnCorrect}
                onIncorrect={mockOnIncorrect}
                onClose={mockOnClose}
            />
        );

        // Select option
        const option2 = screen.getByText('Incorrect answer').closest('button');
        fireEvent.click(option2);

        // Submit
        const submitButton = screen.getByText('Submit Answer');
        fireEvent.click(submitButton);

        // Wait for API call and callback
        await waitFor(() => {
            expect(mockOnIncorrect).toHaveBeenCalledWith('https://example.com/refresher.mp4');
        }, { timeout: 2000 });
    });

    it('should show loading state while submitting', async () => {
        mcqApi.validateAnswer.mockImplementationOnce(() =>
            new Promise(resolve => setTimeout(() => resolve({
                data: { correct: true }
            }), 100))
        );

        render(
            <MCQModal
                isOpen={true}
                mcqData={mockMCQData}
                onCorrect={mockOnCorrect}
                onIncorrect={mockOnIncorrect}
                onClose={mockOnClose}
            />
        );

        // Select and submit
        const option1 = screen.getByText('Correct answer').closest('button');
        fireEvent.click(option1);
        const submitButton = screen.getByText('Submit Answer');
        fireEvent.click(submitButton);

        // Check loading state
        expect(screen.getByText('Checking...')).toBeInTheDocument();
    });

    it('should handle API errors gracefully by calling onCorrect', async () => {
        mcqApi.validateAnswer.mockRejectedValue(new Error('Network error'));

        render(
            <MCQModal
                isOpen={true}
                mcqData={mockMCQData}
                onCorrect={mockOnCorrect}
                onIncorrect={mockOnIncorrect}
                onClose={mockOnClose}
            />
        );

        // Select and submit
        const option1 = screen.getByText('Correct answer').closest('button');
        fireEvent.click(option1);
        const submitButton = screen.getByText('Submit Answer');
        fireEvent.click(submitButton);

        // Should call onCorrect even on error
        await waitFor(() => {
            expect(mockOnCorrect).toHaveBeenCalledTimes(1);
        });
    });
});
