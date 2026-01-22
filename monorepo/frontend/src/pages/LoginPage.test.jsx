import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, vi } from 'vitest';
import LoginPage from './LoginPage';
import api from '../services/api';

// Mock API and Navigation
vi.mock('../services/api');
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

describe('LoginPage', () => {
    it('renders login form correctly', () => {
        render(
            <BrowserRouter>
                <LoginPage />
            </BrowserRouter>
        );
        expect(screen.getByText(/Login or Sign Up/i)).toBeInTheDocument();
        expect(screen.getByPlaceholderText(/you@example.com/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Send OTP/i })).toBeInTheDocument();
    });

    it('updates email input value', () => {
        render(
            <BrowserRouter>
                <LoginPage />
            </BrowserRouter>
        );
        const input = screen.getByPlaceholderText(/you@example.com/i);
        fireEvent.change(input, { target: { value: 'test@example.com' } });
        expect(input.value).toBe('test@example.com');
    });

    it('calls API and navigates on successful submit', async () => {
        api.post.mockResolvedValueOnce({ data: { success: true } });

        render(
            <BrowserRouter>
                <LoginPage />
            </BrowserRouter>
        );

        const input = screen.getByPlaceholderText(/you@example.com/i);
        fireEvent.change(input, { target: { value: 'test@example.com' } });

        const button = screen.getByRole('button', { name: /Send OTP/i });
        fireEvent.click(button);

        expect(button).toBeDisabled(); // Loading state
        expect(button).toHaveTextContent('Sending...');

        await waitFor(() => {
            expect(api.post).toHaveBeenCalledWith('/api/auth/generate-otp', { email: 'test@example.com' });
            expect(mockNavigate).toHaveBeenCalledWith('/verify-otp', { state: { email: 'test@example.com' } });
        });
    });

    it('displays error message on API failure', async () => {
        api.post.mockRejectedValueOnce(new Error('Network Error'));

        render(
            <BrowserRouter>
                <LoginPage />
            </BrowserRouter>
        );

        const input = screen.getByPlaceholderText(/you@example.com/i);
        fireEvent.change(input, { target: { value: 'fail@example.com' } });
        fireEvent.click(screen.getByRole('button', { name: /Send OTP/i }));

        await waitFor(() => {
            expect(screen.getByText(/Failed to send OTP/i)).toBeInTheDocument();
        });
    });
});
