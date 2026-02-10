import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import ContinueWatchingSection from './ContinueWatchingSection';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

describe('ContinueWatchingSection', () => {
    const mockData = {
        seriesId: '123e4567-e89b-12d3-a456-426614174000',
        seriesTitle: 'Test Series',
        seriesThumbnailUrl: 'thumb.jpg',
        seriesCategory: 'Tech',
        episodeId: '123e4567-e89b-12d3-a456-426614174001',
        episodeTitle: 'Test Episode',
        episodeSequenceNumber: 2,
        episodeDurationSeconds: 600,
        progressSeconds: 120,
    };

    it('renders nothing when no data is provided', () => {
        const { container } = render(<ContinueWatchingSection data={null} />);
        expect(container.firstChild).toBeNull();
    });

    it('renders series and episode information correctly', () => {
        render(
            <BrowserRouter>
                <ContinueWatchingSection data={mockData} />
            </BrowserRouter>
        );

        expect(screen.getByText('Test Series')).toBeInTheDocument();
        expect(screen.getByText(/Test Episode/i)).toBeInTheDocument();
        expect(screen.getByText('Tech')).toBeInTheDocument();
        // 120s = 2 min, 600s = 10 min
        expect(screen.getByText('2 min watched')).toBeInTheDocument();
        expect(screen.getByText('10 min total')).toBeInTheDocument();
    });

    it('displays correct progress percentage', () => {
        const { container } = render(
            <BrowserRouter>
                <ContinueWatchingSection data={mockData} />
            </BrowserRouter>
        );

        const progressBar = container.querySelector('div[style*="width"]');
        // 120/600 = 20%
        expect(progressBar).toHaveStyle({ width: '20%' });
    });

    it('displays seconds for short durations', () => {
        const shortData = {
            ...mockData,
            episodeDurationSeconds: 45,
            progressSeconds: 15,
        };

        render(
            <BrowserRouter>
                <ContinueWatchingSection data={shortData} />
            </BrowserRouter>
        );

        expect(screen.getByText('15 sec watched')).toBeInTheDocument();
        expect(screen.getByText('45 sec total')).toBeInTheDocument();
    });

    it('navigates to episode on click', () => {
        const { container } = render(
            <BrowserRouter>
                <ContinueWatchingSection data={mockData} />
            </BrowserRouter>
        );

        const card = container.querySelector('.cursor-pointer');
        card?.click();

        expect(mockNavigate).toHaveBeenCalledWith(
            `/series/${mockData.seriesId}/watch?episodeId=${mockData.episodeId}`
        );
    });
});
