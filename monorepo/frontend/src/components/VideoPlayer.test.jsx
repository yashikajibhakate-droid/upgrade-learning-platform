import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import VideoPlayer from './VideoPlayer';

describe('VideoPlayer', () => {
    beforeEach(() => {
        vi.spyOn(window.HTMLMediaElement.prototype, 'play').mockImplementation(() => Promise.resolve());
        vi.spyOn(window.HTMLMediaElement.prototype, 'pause').mockImplementation(() => { });

        // Mock Fullscreen API on HTMLElement
        Element.prototype.requestFullscreen = vi.fn().mockResolvedValue();
        document.exitFullscreen = vi.fn().mockResolvedValue();
        // Reset fullscreen element
        Object.defineProperty(document, 'fullscreenElement', {
            configurable: true,
            value: null,
            writable: true
        });
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('renders video element with src', () => {
        const { container } = render(<VideoPlayer src="test.mp4" title="Test Video" />);
        const videoElement = container.querySelector('video');
        expect(videoElement).toHaveAttribute('src', 'test.mp4');
    });

    it('toggles play/pause on click', async () => {
        const { container } = render(<VideoPlayer src="test.mp4" title="Test Video" />);
        const videoElement = container.querySelector('video');

        // Mock play/pause state by overriding getters if needed, or just interactions
        Object.defineProperty(videoElement, 'paused', { value: true, writable: true });

        // Trigger click on video (which toggles play)
        fireEvent.click(videoElement);
        expect(window.HTMLMediaElement.prototype.play).toHaveBeenCalled();

        // Simulate browser firing 'play' event
        fireEvent.play(videoElement);
        // Simulate video.paused updating
        Object.defineProperty(videoElement, 'paused', { value: false, writable: true });

        // click again to pause
        fireEvent.click(videoElement);
        // Simulate browser firing 'pause' event
        fireEvent.pause(videoElement);
        Object.defineProperty(videoElement, 'paused', { value: true, writable: true });

        expect(window.HTMLMediaElement.prototype.play).toHaveBeenCalledTimes(1);
        expect(window.HTMLMediaElement.prototype.pause).toHaveBeenCalled();
    });

    it('shows error message when src fails', () => {
        const { container } = render(<VideoPlayer src="invalid.mp4" title="Test Video" />);
        const videoElement = container.querySelector('video');

        fireEvent.error(videoElement);

        expect(screen.getByText(/Error/i)).toBeInTheDocument();
        expect(screen.getByText(/Unable to load video/i)).toBeInTheDocument();
    });

    it('toggles fullscreen on click', async () => {
        const { container } = render(<VideoPlayer src="test.mp4" title="Test Video" />);
        const videoContainer = container.firstChild;
        const buttons = container.querySelectorAll('button');
        // Play, Mute, Volume, Fullscreen (Fullscreen is last)
        const fsButton = buttons[buttons.length - 1];

        // Click to enter fullscreen
        fireEvent.click(fsButton);
        expect(videoContainer.requestFullscreen).toHaveBeenCalled();

        // Simulate successful fullscreen entry
        Object.defineProperty(document, 'fullscreenElement', { value: videoContainer, writable: true });
        fireEvent(document, new Event('fullscreenchange'));

        // Now click to exit
        fireEvent.click(fsButton);
        expect(document.exitFullscreen).toHaveBeenCalled();

        // Simulate successful exit
        Object.defineProperty(document, 'fullscreenElement', { value: null, writable: true });
        fireEvent(document, new Event('fullscreenchange'));
    });
});
