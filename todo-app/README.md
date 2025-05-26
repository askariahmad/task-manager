# To-Do List Web Application

This project is a simple to-do list application built with the MERN stack (MongoDB, Express.js, React, Node.js).

## Project Structure

- `/backend`: Contains the Node.js/Express.js server-side code and API.
- `/frontend`: Contains the React client-side application.

## Prerequisites

- Node.js and npm (or yarn) installed.
- MongoDB installed and running (or a MongoDB Atlas account).

## Setup and Running the Application

### 1. Backend Setup

- **Navigate to the backend directory:**
  ```bash
  cd backend
  ```
- **Install dependencies:**
  ```bash
  npm install
  # or
  # yarn install
  ```
- **Configure MongoDB Connection:**
  The backend tries to connect to MongoDB using the URI `mongodb://localhost:27017/todoapp`.
  If your MongoDB instance is running elsewhere, or you're using a cloud service like MongoDB Atlas, set the `MONGODB_URI` environment variable to your connection string.
  For example, you can create a `.env` file in the `backend` directory (add `.env` to `backend/.gitignore`):
  ```
  MONGODB_URI=your_mongodb_connection_string
  ```
  And modify `backend/index.js` to load it using a package like `dotenv` (you'd need to install it: `npm install dotenv`). Or, you can set it directly in your terminal before running the server.
- **Start the backend server:**
  ```bash
  npm start
  # or
  # yarn start
  ```
  The backend server will typically run on `http://localhost:5000`.

### 2. Frontend Setup

- **Navigate to the frontend directory:**
  ```bash
  cd ../frontend 
  # (If you are in the backend directory)
  # or
  # cd frontend (If you are in the root todo-app directory)
  ```
- **Install dependencies:**
  ```bash
  npm install
  # or
  # yarn install
  ```
- **Start the frontend development server:**
  ```bash
  npm start
  # or
  # yarn start
  ```
  The React development server will typically run on `http://localhost:3000` and will open the application in your default web browser. API requests from the frontend to `/todos` (and other paths) will be proxied to the backend server running on `http://localhost:5000` (as configured in `frontend/package.json`).

## How to Use

1.  Once both servers are running, open your browser to `http://localhost:3000`.
2.  You should see the to-do list interface.
3.  Enter a task in the input field and click "Add" to add a new to-do.
4.  Click on a to-do item's text to toggle its completion status.
5.  Click the "Delete" button to remove a to-do item.
