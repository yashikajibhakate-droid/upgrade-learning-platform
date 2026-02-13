import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import ReviewForm from './ReviewForm';

describe('ReviewForm', () => {
    it('renders all star buttons', () => {
        render(<ReviewForm onSubmit={() => { }} loading={false} />);
        const stars = screen.getAllByRole('button', { name: '' }).filter(b => b.querySelector('svg'));
        expect(stars).toHaveLength(5);
    });

    it('requires a rating to enable submit button', () => {
        render(<ReviewForm onSubmit={() => { }} loading={false} />);
        const submitBtn = screen.getByRole('button', { name: /submit review/i });
        expect(submitBtn).toBeDisabled();

        const stars = screen.getAllByRole('button');
        fireEvent.click(stars[2]); // 3-star rating

        expect(submitBtn).not.toBeDisabled();
    });

    it('calls onSubmit with correct data', () => {
        const handleSubmit = vi.fn();
        render(<ReviewForm onSubmit={handleSubmit} loading={false} />);

        const stars = screen.getAllByRole('button');
        fireEvent.click(stars[3]); // 4-star rating

        const textarea = screen.getByPlaceholderText(/share your thoughts/i);
        fireEvent.change(textarea, { target: { value: 'Amazing series!' } });

        const submitBtn = screen.getByRole('button', { name: /submit review/i });
        fireEvent.click(submitBtn);

        expect(handleSubmit).toHaveBeenCalledWith({
            rating: 4,
            comment: 'Amazing series!'
        });
    });

    it('shows loading state', () => {
        render(<ReviewForm onSubmit={() => { }} loading={true} />);
        const submitBtn = screen.getByRole('button', { name: /submit review/i });
        expect(submitBtn).toBeDisabled();
        expect(submitBtn.querySelector('svg.animate-spin')).not.toBeNull();
    });

    // Edit mode tests
    it('pre-populates rating and comment in edit mode', () => {
        render(
            <ReviewForm
                onSubmit={() => { }}
                loading={false}
                isEditMode={true}
                initialData={{ rating: 4, comment: 'Original comment' }}
            />
        );

        const textarea = screen.getByPlaceholderText(/share your thoughts/i);
        expect(textarea.value).toBe('Original comment');

        // Check the submit button text is "Update Review"
        expect(screen.getByRole('button', { name: /update review/i })).toBeInTheDocument();
    });

    it('shows "Update Review" button text in edit mode', () => {
        render(
            <ReviewForm
                onSubmit={() => { }}
                loading={false}
                isEditMode={true}
                initialData={{ rating: 3, comment: 'Test' }}
            />
        );

        expect(screen.getByRole('button', { name: /update review/i })).toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /submit review/i })).not.toBeInTheDocument();
    });

    it('shows "Edit Your Review" heading in edit mode', () => {
        render(
            <ReviewForm
                onSubmit={() => { }}
                loading={false}
                isEditMode={true}
                initialData={{ rating: 3, comment: 'Test' }}
            />
        );

        expect(screen.getByText('Edit Your Review')).toBeInTheDocument();
    });

    it('calls onSubmit with updated data in edit mode', () => {
        const handleSubmit = vi.fn();
        render(
            <ReviewForm
                onSubmit={handleSubmit}
                loading={false}
                isEditMode={true}
                initialData={{ rating: 3, comment: 'Original' }}
            />
        );

        const textarea = screen.getByPlaceholderText(/share your thoughts/i);
        fireEvent.change(textarea, { target: { value: 'Updated comment' } });

        const stars = screen.getAllByRole('button');
        fireEvent.click(stars[4]); // 5-star rating

        const submitBtn = screen.getByRole('button', { name: /update review/i });
        fireEvent.click(submitBtn);

        expect(handleSubmit).toHaveBeenCalledWith({
            rating: 5,
            comment: 'Updated comment'
        });
    });

    it('calls onCancel when cancel button is clicked in edit mode', () => {
        const handleCancel = vi.fn();
        render(
            <ReviewForm
                onSubmit={() => { }}
                loading={false}
                isEditMode={true}
                initialData={{ rating: 3, comment: 'Test' }}
                onCancel={handleCancel}
            />
        );

        const cancelBtn = screen.getByRole('button', { name: /cancel$/i });
        fireEvent.click(cancelBtn);

        expect(handleCancel).toHaveBeenCalledOnce();
    });
});
