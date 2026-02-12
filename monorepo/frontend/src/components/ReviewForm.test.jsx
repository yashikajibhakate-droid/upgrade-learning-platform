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
        expect(submitBtn.querySelector('svg.animate-spin')).toBeDefined();
    });
});
