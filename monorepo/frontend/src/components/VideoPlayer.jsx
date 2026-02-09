import React, { useRef, useState, useEffect } from 'react';
import { Play, Pause, Volume2, VolumeX, Maximize, Minimize, SkipBack, SkipForward } from 'lucide-react';

const VideoPlayer = ({ src, poster, onEnded, onError, title, initialTime = 0, onProgressUpdate }) => {
    const videoRef = useRef(null);
    const containerRef = useRef(null);
    const progressUpdateTimerRef = useRef(null);
    const [isPlaying, setIsPlaying] = useState(false);
    const [currentTime, setCurrentTime] = useState(0);
    const [duration, setDuration] = useState(0);
    const [volume, setVolume] = useState(1);
    const [isMuted, setIsMuted] = useState(false);
    const [isFullscreen, setIsFullscreen] = useState(false);
    const [showControls, setShowControls] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        let timeout;
        const handleMouseMove = () => {
            setShowControls(true);
            clearTimeout(timeout);
            timeout = setTimeout(() => setShowControls(false), 3000);
        };

        const container = containerRef.current;
        if (container) {
            container.addEventListener('mousemove', handleMouseMove);
        }

        return () => {
            if (container) {
                container.removeEventListener('mousemove', handleMouseMove);
            }
            clearTimeout(timeout);
        };
    }, []);

    // Reset state when video source changes (for auto-next-episode)
    useEffect(() => {
        if (videoRef.current) {
            setCurrentTime(0);
            setIsPlaying(false);
            setError(null);
            // Reset completion flag for new video
            videoRef.current.hasTriggeredCompletion = false;
        }
    }, [src]);

    const togglePlay = () => {
        if (videoRef.current) {
            if (videoRef.current.paused) {
                videoRef.current.play().catch(e => console.error("Error playing video:", e));
            } else {
                videoRef.current.pause();
            }
        }
    };

    const handleTimeUpdate = () => {
        if (videoRef.current) {
            const time = videoRef.current.currentTime;
            const videoDuration = videoRef.current.duration;
            setCurrentTime(time);

            // Trigger completion at 95% to handle timing issues
            // This ensures onEnded is called even if the video doesn't naturally trigger it
            if (videoDuration > 0 && time >= videoDuration * 0.95 && onEnded) {
                // Only trigger once by checking if we're not already at the end
                if (time < videoDuration) {
                    // Mark as having triggered completion
                    if (!videoRef.current.hasTriggeredCompletion) {
                        videoRef.current.hasTriggeredCompletion = true;
                        onEnded();
                    }
                }
            }

            // Call onProgressUpdate if provided (debounced by parent component)
            if (onProgressUpdate && !videoRef.current.paused) {
                onProgressUpdate(time);
            }
        }
    };

    const handleLoadedMetadata = () => {
        if (videoRef.current) {
            setDuration(videoRef.current.duration);
            setError(null);

            // Seek to initial time if provided (for resume functionality)
            if (initialTime > 0 && initialTime < videoRef.current.duration) {
                videoRef.current.currentTime = initialTime;
                setCurrentTime(initialTime);
            }
        }
    };

    const handleSeek = (e) => {
        const time = parseFloat(e.target.value);
        if (videoRef.current) {
            videoRef.current.currentTime = time;
            setCurrentTime(time);
        }
    };

    const handleVolumeChange = (e) => {
        const val = parseFloat(e.target.value);
        if (videoRef.current) {
            videoRef.current.volume = val;
            setVolume(val);
            setIsMuted(val === 0);
        }
    };

    const toggleMute = () => {
        if (videoRef.current) {
            const newMuted = !isMuted;
            videoRef.current.muted = newMuted;
            setIsMuted(newMuted);
            // If unmute, restore volume (or default to 1)
            if (!newMuted && volume === 0) {
                setVolume(1);
                videoRef.current.volume = 1;
            }
        }
    };

    useEffect(() => {
        const handleFullscreenChange = () => {
            setIsFullscreen(!!document.fullscreenElement);
        };

        const container = containerRef.current;
        if (container) {
            container.addEventListener('fullscreenchange', handleFullscreenChange);
        }

        return () => {
            if (container) {
                container.removeEventListener('fullscreenchange', handleFullscreenChange);
            }
        };
    }, []);

    const toggleFullscreen = () => {
        if (!document.fullscreenElement) {
            containerRef.current.requestFullscreen().catch(err => {
                console.error(`Error attempting to enable full-screen mode: ${err.message} (${err.name})`);
            });
        } else {
            document.exitFullscreen();
        }
    };

    const formatTime = (time) => {
        const minutes = Math.floor(time / 60);
        const seconds = Math.floor(time % 60);
        return `${minutes}:${seconds < 10 ? '0' : ''}${seconds}`;
    };

    const handleError = (e) => {
        console.error("Video error:", e);
        setError("Unable to load video. Please try again later.");
        if (onError) onError(e);
    };

    return (
        <div
            ref={containerRef}
            className="relative w-full aspect-video bg-black rounded-xl overflow-hidden group shadow-2xl"
        >
            {error ? (
                <div className="absolute inset-0 flex items-center justify-center text-white bg-gray-900">
                    <div className="text-center">
                        <p className="text-red-400 mb-2 font-bold">Error</p>
                        <p>{error}</p>
                    </div>
                </div>
            ) : (
                <video
                    ref={videoRef}
                    src={src}
                    poster={poster}
                    className="w-full h-full object-contain"
                    onTimeUpdate={handleTimeUpdate}
                    onLoadedMetadata={handleLoadedMetadata}
                    onPlay={() => setIsPlaying(true)}
                    onPause={() => setIsPlaying(false)}
                    onEnded={onEnded}
                    onError={handleError}
                    onClick={togglePlay}
                />
            )}

            {/* Controls Overlay */}
            <div
                className={`absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent flex flex-col justify-end p-4 transition-opacity duration-300 ${showControls || !isPlaying ? 'opacity-100' : 'opacity-0'}`}
            >
                {/* Title (only visible when controls are shown) */}
                <div className="absolute top-4 left-4 text-white font-medium text-lg drop-shadow-md">
                    {title}
                </div>

                <div className="space-y-2">
                    {/* Progress Bar */}
                    <input
                        type="range"
                        min="0"
                        max={duration || 0}
                        value={currentTime}
                        onChange={handleSeek}
                        className="w-full h-1 bg-gray-600 rounded-lg appearance-none cursor-pointer accent-indigo-500 hover:h-2 transition-all"
                    />

                    <div className="flex items-center justify-between text-white">
                        <div className="flex items-center gap-4">
                            <button onClick={togglePlay} className="hover:text-indigo-400 transition-colors">
                                {isPlaying ? <Pause size={24} fill="currentColor" /> : <Play size={24} fill="currentColor" />}
                            </button>

                            <div className="flex items-center gap-2 group/vol">
                                <button onClick={toggleMute} className="hover:text-indigo-400 transition-colors">
                                    {isMuted || volume === 0 ? <VolumeX size={20} /> : <Volume2 size={20} />}
                                </button>
                                <input
                                    type="range"
                                    min="0"
                                    max="1"
                                    step="0.1"
                                    value={isMuted ? 0 : volume}
                                    onChange={handleVolumeChange}
                                    className="w-0 overflow-hidden group-hover/vol:w-20 transition-all duration-300 h-1 bg-gray-600 accent-indigo-500"
                                />
                            </div>

                            <span className="text-sm font-mono">
                                {formatTime(currentTime)} / {formatTime(duration)}
                            </span>
                        </div>

                        <div className="flex items-center gap-4">
                            <button onClick={toggleFullscreen} className="hover:text-indigo-400 transition-colors">
                                {isFullscreen ? <Minimize size={20} /> : <Maximize size={20} />}
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Center Play Button (Initial or Paused) */}
            {!isPlaying && !error && (
                <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                    <div
                        onClick={togglePlay}
                        className="bg-black/50 p-4 rounded-full backdrop-blur-sm border border-white/20 cursor-pointer hover:bg-black/70 transition-colors pointer-events-auto">
                        <Play size={48} fill="white" className="text-white translate-x-1" />
                    </div>
                </div>
            )}
        </div>
    );
};

export default VideoPlayer;
