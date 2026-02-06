import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import HomePage from './HomePage';
import { vi, describe, it, expect, beforeEach } from 'vitest';

describe('HomePage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders LandingPage when no email is present in state', () => {
        render(
            <MemoryRouter initialEntries={[{ pathname: '/', state: {} }]}>
                <HomePage />
            </MemoryRouter>
        );

        expect(screen.getByText(/Welcome to LearnSphere/i)).toBeInTheDocument();
    });

    it('renders LandingPage when email is present but no token', () => {
        render(
            <MemoryRouter initialEntries={[{ pathname: '/', state: { email: 'test@example.com' } }]}>
                <HomePage />
            </MemoryRouter>
        );

        expect(screen.getByText(/Welcome to LearnSphere/i)).toBeInTheDocument();
    });

    it('redirects to /recommendations when both email and token are present', () => {
        Storage.prototype.getItem = vi.fn((key) => {
            if (key === 'authToken') return 'valid-token';
            return null;
        });

        render(
            <MemoryRouter initialEntries={[{ pathname: '/', state: { email: 'test@example.com' } }]}>
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/recommendations" element={<div>Recommendations Page</div>} />
                </Routes>
            </MemoryRouter>
        );

        expect(screen.getByText('Recommendations Page')).toBeInTheDocument();
    });
});
