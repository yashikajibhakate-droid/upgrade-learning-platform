# Full Stack Monorepo

This is a monorepo containing a ReactJS frontend and a Spring Boot backend.

## Project Structure

- **frontend/**: ReactJS application using Vite.
- **backend/**: Spring Boot application using Gradle.

## Getting Started

### Prerequisites

- Java 17
- Node.js (v18+ recommended)

### Running the Backend

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Run the application:
   ```bash
   ./gradlew bootRun
   ```
   The backend will start on [http://localhost:8080](http://localhost:8080).

### Running the Frontend

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
   The frontend will start on [http://localhost:3000](http://localhost:3000).

## API Endpoints

- Health Check: `GET /api/health`
