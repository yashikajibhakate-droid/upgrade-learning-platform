import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import OnboardingPage from './OnboardingPage';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import api from '../services/api';

// Mock API
vi.mock('../services/api', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn()
    }
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

describe('OnboardingPage', () => {
    const mockEmail = 'test@example.com';
    const mockInterests = ['Tech', 'Design', 'Business'];

    beforeEach(() => {
        vi.clearAllMocks();
        api.get.mockResolvedValue({ data: mockInterests });
    });

    it('redirects to login if email is missing', () => {
        render(
            <MemoryRouter initialEntries={['/onboarding']}>
                <Routes>
                    <Route path="/onboarding" element={<OnboardingPage />} />
                </Routes>
            </MemoryRouter>
        );

        expect(mockNavigate).toHaveBeenCalledWith('/login');
    });

    it('renders interests fetched from API', async () => {
        render(
            <MemoryRouter initialEntries={[{ pathname: '/onboarding', state: { email: mockEmail } }]}>
                <Routes>
                    <Route path="/onboarding" element={<OnboardingPage />} />
                </Routes>
            </MemoryRouter>
        );

        await waitFor(() => {
            mockInterests.forEach(interest => {
                expect(screen.getByText(interest)).toBeInTheDocument();
            });
        });
    });

    it('toggles interest selection and enables continue button', async () => {
        render(
            <MemoryRouter initialEntries={[{ pathname: '/onboarding', state: { email: mockEmail } }]}>
                <Routes>
                    <Route path="/onboarding" element={<OnboardingPage />} />
                </Routes>
            </MemoryRouter>
        );

        const continueButton = await screen.findByRole('button', { name: /continue/i });
        expect(continueButton).toBeDisabled();

        const techInterest = await screen.findByText('Tech');
        fireEvent.click(techInterest); // Select

        expect(continueButton).toBeEnabled();

        fireEvent.click(techInterest); // Deselect
        expect(continueButton).toBeDisabled();
    });

    it('submits preferences and redirects on continue', async () => {
        api.post.mockResolvedValue({ status: 200 });

        render(
            <MemoryRouter initialEntries={[{ pathname: '/onboarding', state: { email: mockEmail } }]}>
                <Routes>
                    <Route path="/onboarding" element={<OnboardingPage />} />
                </Routes>
            </MemoryRouter>
        );

        const techInterest = await screen.findByText('Tech');
        fireEvent.click(techInterest);

        const continueButton = screen.getByRole('button', { name: /continue/i });
        fireEvent.click(continueButton);

        await waitFor(() => {
            expect(api.post).toHaveBeenCalledWith('/api/users/preferences', {
                email: mockEmail,
                interests: ['Tech']
            });
            expect(mockNavigate).toHaveBeenCalledWith('/recommendations', { state: { email: mockEmail } });
        });
    });

    it('opens info modal when info button is clicked', async () => {
        render(
            <MemoryRouter initialEntries={[{ pathname: '/onboarding', state: { email: mockEmail } }]}>
                <Routes>
                    <Route path="/onboarding" element={<OnboardingPage />} />
                </Routes>
            </MemoryRouter>
        );

        const infoButton = await screen.findByLabelText(/Show information/i);
        fireEvent.click(infoButton);

        expect(screen.getByText('Why choose interests?')).toBeInTheDocument();
        expect(screen.getByRole('dialog')).toBeInTheDocument();
    });

    it('opens info modal when Enter key is pressed on info button', async () => {
        render(
            <MemoryRouter initialEntries={[{ pathname: '/onboarding', state: { email: mockEmail } }]}>
                <Routes>
                    <Route path="/onboarding" element={<OnboardingPage />} />
                </Routes>
            </MemoryRouter>
        );

        const infoButton = await screen.findByLabelText(/Show information/i);
        fireEvent.keyDown(infoButton, { key: 'Enter', code: 'Enter' });

        expect(screen.getByText('Why choose interests?')).toBeInTheDocument();
    });

    it('closes info modal when close button is clicked', async () => {
        render(
            <MemoryRouter initialEntries={[{ pathname: '/onboarding', state: { email: mockEmail } }]}>
                <Routes>
                    <Route path="/onboarding" element={<OnboardingPage />} />
                </Routes>
            </MemoryRouter>
        );

        // Open modal
        const infoButton = await screen.findByLabelText(/Show information/i);
        fireEvent.click(infoButton);
        expect(screen.getByRole('dialog')).toBeInTheDocument();

        // Close modal
        const closeButton = screen.getByLabelText(/Close modal/i);
        fireEvent.click(closeButton);

        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });
});
