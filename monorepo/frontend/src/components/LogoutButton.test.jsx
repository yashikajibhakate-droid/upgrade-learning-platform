import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import LogoutButton from './LogoutButton';
import api from '../services/api';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', () => ({
    useNavigate: () => mockNavigate,
}));

vi.mock('../services/api');

describe('LogoutButton', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        // Mock localStorage
        vi.spyOn(Storage.prototype, 'removeItem').mockImplementation(() => { });
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('logs out successfully and redirects to login', async () => {
        api.post.mockResolvedValueOnce({ status: 200 });

        render(<LogoutButton />);

        const logoutBtn = screen.getByRole('button', { name: /Logout/i });
        fireEvent.click(logoutBtn);

        await waitFor(() => {
            expect(api.post).toHaveBeenCalledWith('/api/auth/logout');
            expect(localStorage.removeItem).toHaveBeenCalledWith('authToken');
            expect(localStorage.removeItem).toHaveBeenCalledWith('userEmail');
            expect(mockNavigate).toHaveBeenCalledWith('/login');
        });
    });

    it('handles api failure but still clears storage and redirects', async () => {
        api.post.mockRejectedValueOnce(new Error('Logout failed'));
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => { });

        render(<LogoutButton />);

        const logoutBtn = screen.getByRole('button', { name: /Logout/i });
        fireEvent.click(logoutBtn);

        await waitFor(() => {
            expect(api.post).toHaveBeenCalledWith('/api/auth/logout');
            expect(localStorage.removeItem).toHaveBeenCalledWith('authToken');
            expect(localStorage.removeItem).toHaveBeenCalledWith('userEmail');
            expect(mockNavigate).toHaveBeenCalledWith('/login');
        });

        consoleSpy.mockRestore();
    });
});
