import { render } from '@testing-library/react';
import { describe, it } from 'vitest';
import App from './App';

describe('App', () => {
    it('renders without crashing', () => {
        render(<App />);
        // Check for a known element, e.g., the health check button or title
        // Adjust this expectation based on your actual App.jsx content.
        // For now, ensuring render doesn't throw is a good start.
    });
});
