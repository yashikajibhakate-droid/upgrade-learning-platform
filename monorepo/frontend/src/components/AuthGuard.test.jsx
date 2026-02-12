import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route, useLocation } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import AuthGuard from './AuthGuard';

// Helper component to display current location state
const LocationDisplay = () => {
    const location = useLocation();
    return (
        <div>
            <div data-testid="location-pathname">{location.pathname}</div>
            <div data-testid="location-state-from">{location.state?.from?.pathname}</div>
        </div>
    );
};

// Mock Protected Component
const ProtectedComponent = () => <div>Protected Content</div>;

describe('AuthGuard', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    it('renders children when authenticated (authToken present)', async () => {
        localStorage.setItem('authToken', 'valid-token');

        render(
            <MemoryRouter initialEntries={['/protected']}>
                <Routes>
                    <Route
                        path="/protected"
                        element={
                            <AuthGuard>
                                <ProtectedComponent />
                            </AuthGuard>
                        }
                    />
                </Routes>
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(screen.getByText('Protected Content')).toBeInTheDocument();
        });
    });

    it('renders children when magic link token is present in URL', async () => {
        render(
            <MemoryRouter initialEntries={['/protected?token=magic-token']}>
                <Routes>
                    <Route
                        path="/protected"
                        element={
                            <AuthGuard>
                                <ProtectedComponent />
                            </AuthGuard>
                        }
                    />
                </Routes>
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(screen.getByText('Protected Content')).toBeInTheDocument();
        });
    });

    it('redirects to /login when unauthenticated', async () => {
        render(
            <MemoryRouter initialEntries={['/protected']}>
                <Routes>
                    <Route
                        path="/protected"
                        element={
                            <AuthGuard>
                                <ProtectedComponent />
                            </AuthGuard>
                        }
                    />
                    <Route path="/login" element={<LocationDisplay />} />
                </Routes>
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(screen.getByTestId('location-pathname')).toHaveTextContent('/login');
            // Check if state.from was passed correctly
            expect(screen.getByTestId('location-state-from')).toHaveTextContent('/protected');
        });
    });
});
