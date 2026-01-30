import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import LandingPage from './LandingPage';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', () => ({
    useNavigate: () => mockNavigate,
}));

describe('LandingPage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('navigates to login when get started is clicked and user is NOT logged in', () => {
        vi.spyOn(Storage.prototype, 'getItem').mockReturnValue(null);

        render(<LandingPage />);

        const button = screen.getByRole('button', { name: /Get Started/i });
        fireEvent.click(button);

        expect(mockNavigate).toHaveBeenCalledWith('/login');
    });

    it('navigates to recommendations when get started is clicked and user IS logged in', () => {
        const getItemSpy = vi.spyOn(Storage.prototype, 'getItem');
        getItemSpy.mockImplementation((key) => {
            if (key === 'authToken') return 'valid-token';
            if (key === 'userEmail') return 'test@example.com';
            return null;
        });

        render(<LandingPage />);

        const button = screen.getByRole('button', { name: /Get Started/i });
        fireEvent.click(button);

        expect(mockNavigate).toHaveBeenCalledWith('/recommendations', { state: { email: 'test@example.com' } });
    });
});
