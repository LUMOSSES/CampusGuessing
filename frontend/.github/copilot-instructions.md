# GitHub Copilot Instructions for CampusGuessing Frontend

## Overview
This project is a frontend application for the SYSU Image Hunt game, built using React and Vite. It allows users to explore the campus, guess locations based on images, and compete with others.

## Architecture
- **Main Components**: The application is structured around several key pages: `Login`, `Dashboard`, `Game`, `SoloMode`, and `Home`. Each page is a React component that handles specific functionality.
- **Routing**: The application uses `react-router-dom` for navigation. The main routing is defined in `src/App.jsx`, which includes routes for all major pages.
- **API Integration**: API calls are managed through a centralized `apiClient` in `src/api/apiClient.js`, which handles authentication tokens and error responses uniformly.

## Developer Workflows
- **Development**: Use the command `npm run dev` to start the development server. This will allow you to view changes in real-time.
- **Building**: To create a production build, run `npm run build`. The output will be in the `dist` directory.
- **Linting**: Run `npm run lint` to check for code quality issues using ESLint.

## Project-Specific Conventions
- **Component Structure**: Components are organized by functionality within the `src/pages` and `src/layouts` directories. Each page component typically includes its own styles and logic.
- **State Management**: Local state is managed using React's `useState` and `useEffect` hooks. For global state, consider using context or a state management library if needed in the future.

## Integration Points
- **API Endpoints**: The application interacts with several backend endpoints, such as:
  - `POST /auth/login` for user authentication.
  - `GET /questions` for fetching questions.
  - `POST /api/comments` for managing comments.
- **External Libraries**: The project uses libraries like `axios` for HTTP requests, `framer-motion` for animations, and `leaflet` for map functionalities.

## Cross-Component Communication
- **Props and Callbacks**: Data is passed between components using props. For example, the `Game` component receives props for user data and game state.
- **Context API**: Consider using React's Context API for managing user authentication state across components.

## Examples
- **Login Component**: The `Login` component handles user authentication and redirects to the dashboard upon successful login.
- **Game Component**: The `Game` component manages the game state, including user guesses and results, and communicates with the backend to submit game records.

## Conclusion
This document serves as a guide for AI coding agents to understand the structure and workflows of the CampusGuessing frontend application. For further details, refer to the specific component files and the `PROJECT_REPORT.md` for project insights.