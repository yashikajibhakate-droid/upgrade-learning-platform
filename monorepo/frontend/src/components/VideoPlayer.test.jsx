import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import VideoPlayer from './VideoPlayer';

describe('VideoPlayer', () => {
    beforeEach(() => {
        vi.spyOn(window.HTMLMediaElement.prototype, 'play').mockImplementation(() => Promise.resolve());
        vi.spyOn(window.HTMLMediaElement.prototype, 'pause').mockImplementation(() => { });
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('renders video element with src', () => {
        const { container } = render(<VideoPlayer src="test.mp4" title="Test Video" />);
        const videoElement = container.querySelector('video');
        expect(videoElement).toHaveAttribute('src', 'test.mp4');
    });

    it('toggles play/pause on click', () => {
        const { container } = render(<VideoPlayer src="test.mp4" title="Test Video" />);
        const videoElement = container.querySelector('video');

        // Mock play/pause state by overriding getters if needed, or just interactions
        Object.defineProperty(videoElement, 'paused', { value: true, writable: true });

        // Trigger click on video (which toggles play)
        fireEvent.click(videoElement);
        expect(window.HTMLMediaElement.prototype.play).toHaveBeenCalled();

        // click again to pause
        fireEvent.click(videoElement);
        // Note: Logic inside VideoPlayer depends on state 'isPlaying', which toggles on click. 
        // Real browser updates video.paused, JSDOM doesn't automatically.
        // We rely on 'isPlaying' state tracked in component.

        // Since play() is mocked, checking calls is correctly verifying interaction.
        expect(window.HTMLMediaElement.prototype.play).toHaveBeenCalledTimes(1);

        // Ideally we check pause too, but component logic calls pause() only if isPlaying is true.
        // isPlaying toggles on click.
        // 1st click: isPlaying false -> calls play(), sets isPlaying true.
        // 2nd click: isPlaying true -> calls pause(), sets isPlaying false.

        expect(window.HTMLMediaElement.prototype.pause).toHaveBeenCalled();
    });

    it('shows error message when src fails', () => {
        const { container } = render(<VideoPlayer src="invalid.mp4" title="Test Video" />);
        const videoElement = container.querySelector('video');

        fireEvent.error(videoElement);

        expect(screen.getByText(/Error/i)).toBeInTheDocument();
        expect(screen.getByText(/Unable to load video/i)).toBeInTheDocument();
    });
});
