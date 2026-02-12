import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import VerifyOtpPage from './VerifyOtpPage';
import api from '../services/api';

const mockNavigate = vi.fn();
// Defines a mutable mock for useLocation
let mockLocationState = { email: 'test@example.com' };

// Mock Router location state
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useLocation: () => ({
            state: mockLocationState
        }),
        useNavigate: () => mockNavigate,
    };
});

vi.mock('../services/api');

describe('VerifyOtpPage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        window.alert = vi.fn();
        mockLocationState = { email: 'test@example.com' }; // Reset state
    });

    it('redirects to onboarding if user has no interests', async () => {
        const setItemSpy = vi.spyOn(Storage.prototype, 'setItem');
        api.post.mockResolvedValueOnce({
            status: 200,
            data: { hasInterests: false, token: 'mock-token' }
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
            expect(setItemSpy).toHaveBeenCalledWith('authToken', 'mock-token');
            expect(setItemSpy).toHaveBeenCalledWith('userEmail', 'test@example.com');
            expect(mockNavigate).toHaveBeenCalledWith('/onboarding', { state: { email: 'test@example.com' } });
        });
        setItemSpy.mockRestore();
    });

    it('redirects to recommendations if user has interests', async () => {
        const setItemSpy = vi.spyOn(Storage.prototype, 'setItem');
        api.post.mockResolvedValueOnce({
            status: 200,
            data: { hasInterests: true, token: 'mock-token' }
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
            expect(setItemSpy).toHaveBeenCalledWith('authToken', 'mock-token');
            expect(setItemSpy).toHaveBeenCalledWith('userEmail', 'test@example.com');
            expect(mockNavigate).toHaveBeenCalledWith('/recommendations', { state: { email: 'test@example.com' } });
        });
        setItemSpy.mockRestore();
    });

    it('redirects to "from" location if present in state', async () => {
        // Setup state with "from"
        mockLocationState = { email: 'test@example.com', from: '/protected/route' };

        const setItemSpy = vi.spyOn(Storage.prototype, 'setItem');
        api.post.mockResolvedValueOnce({
            status: 200,
            data: { hasInterests: true, token: 'mock-token' }
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
            expect(setItemSpy).toHaveBeenCalledWith('authToken', 'mock-token');
            expect(setItemSpy).toHaveBeenCalledWith('userEmail', 'test@example.com');
            expect(mockNavigate).toHaveBeenCalledWith('/protected/route', { replace: true });
        });
        setItemSpy.mockRestore();
    });

    it('redirects to "from" location preserving query params and hash', async () => {
        // Setup state with "from" location object
        const fromLocation = {
            pathname: '/series/1/watch',
            search: '?episodeId=2&token=magic',
            hash: '#section'
        };
        mockLocationState = { email: 'test@example.com', from: fromLocation };

        const setItemSpy = vi.spyOn(Storage.prototype, 'setItem');
        api.post.mockResolvedValueOnce({
            status: 200,
            data: { hasInterests: true, token: 'mock-token' }
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
            expect(setItemSpy).toHaveBeenCalledWith('authToken', 'mock-token');
            expect(setItemSpy).toHaveBeenCalledWith('userEmail', 'test@example.com');
            // Navigate should be called with the location object or equivalent string
            expect(mockNavigate).toHaveBeenCalledWith(
                expect.objectContaining({
                    pathname: '/series/1/watch',
                    search: '?episodeId=2&token=magic',
                    hash: '#section'
                }),
                { replace: true }
            );
        });
        setItemSpy.mockRestore();
    });

    it('clears stale digits when pasting a shorter OTP', () => {
        render(
            <BrowserRouter>
                <VerifyOtpPage />
            </BrowserRouter>
        );

        const inputs = screen.getAllByRole('textbox');

        // 1. Enter initial 6 digits: '123456'
        inputs.forEach((input, index) => {
            fireEvent.change(input, { target: { value: String(index + 1) } });
        });
        expect(inputs[0].value).toBe('1');
        expect(inputs[5].value).toBe('6');

        // 2. Paste '99' into the first input
        // Create a mock clipboard event
        const pasteEvent = {
            clipboardData: {
                getData: () => '99',
            },
            preventDefault: vi.fn(),
        };

        fireEvent.paste(inputs[0], pasteEvent);

        // 3. Assert: '9', '9', '', '', '', ''
        expect(inputs[0].value).toBe('9');
        expect(inputs[1].value).toBe('9');
        expect(inputs[2].value).toBe('');
        expect(inputs[3].value).toBe('');
        expect(inputs[4].value).toBe('');
        expect(inputs[5].value).toBe('');
    });
});
