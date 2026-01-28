import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import VerifyOtpPage from './VerifyOtpPage';
import api from '../services/api';

const mockNavigate = vi.fn();

// Mock Router location state
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useLocation: () => ({
            state: { email: 'test@example.com' }
        }),
        useNavigate: () => mockNavigate,
    };
});

vi.mock('../services/api');

describe('VerifyOtpPage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        window.alert = vi.fn();
    });

    it('redirects to onboarding if users has no interests', async () => {
        api.post.mockResolvedValueOnce({
            status: 200,
            data: { hasInterests: false }
        });

        render(
            <BrowserRouter>
                <VerifyOtpPage />
            </BrowserRouter>
        );

        const inputs = screen.getAllByRole('textbox');
        inputs.forEach((input, index) => {
            fireEvent.change(input, { target: { value: String(index) } });
        });

        const verifyButton = screen.getByRole('button', { name: /Verify & Continue/i });
        fireEvent.click(verifyButton);

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/onboarding', { state: { email: 'test@example.com' } });
        });
    });

    it('redirects to recommendations if user has interests', async () => {
        api.post.mockResolvedValueOnce({
            status: 200,
            data: { hasInterests: true }
        });

        render(
            <BrowserRouter>
                <VerifyOtpPage />
            </BrowserRouter>
        );

        const inputs = screen.getAllByRole('textbox');
        inputs.forEach((input, index) => {
            fireEvent.change(input, { target: { value: String(index) } });
        });

        const verifyButton = screen.getByRole('button', { name: /Verify & Continue/i });
        fireEvent.click(verifyButton);

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/recommendations', { state: { email: 'test@example.com' } });
        });
    });
});
