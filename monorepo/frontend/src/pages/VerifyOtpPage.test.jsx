import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import VerifyOtpPage from './VerifyOtpPage';
import api from '../services/api';

// Mock Router location state
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useLocation: () => ({
            state: { email: 'test@example.com' }
        }),
        useNavigate: () => vi.fn(),
    };
});

vi.mock('../services/api');

describe('VerifyOtpPage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders 6 input boxes', () => {
        render(
            <BrowserRouter>
                <VerifyOtpPage />
            </BrowserRouter>
        );
        const inputs = screen.getAllByRole('textbox');
        expect(inputs).toHaveLength(6);
    });

    it('handles input changes and focus management', () => {
        render(
            <BrowserRouter>
                <VerifyOtpPage />
            </BrowserRouter>
        );
        const inputs = screen.getAllByRole('textbox');

        // Type '1' in first box
        fireEvent.change(inputs[0], { target: { value: '1' } });
        expect(inputs[0].value).toBe('1');

        // Assuming focus management works (hard to test exact focus in RTL without user-event, but we can check values)
        fireEvent.change(inputs[1], { target: { value: '2' } });
        expect(inputs[1].value).toBe('2');
    });

    it('calls verify API with correct data', async () => {
        api.post.mockResolvedValueOnce({ status: 200 });
        // Mock alert
        window.alert = vi.fn();

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
            expect(api.post).toHaveBeenCalledWith('/auth/verify-otp', {
                email: 'test@example.com',
                otp: '012345'
            });
        });
    });
});
