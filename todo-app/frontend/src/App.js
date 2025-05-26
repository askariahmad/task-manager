import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [todos, setTodos] = useState([]);
  const [newTodoText, setNewTodoText] = useState('');

  // Fetch todos from backend
  useEffect(() => {
    fetch('/todos') // Assuming backend is proxied or on the same domain
      .then(res => {
        if (!res.ok) {
          throw new Error(`HTTP error! status: ${res.status}`);
        }
        return res.json();
      })
      .then(data => setTodos(data))
      .catch(error => console.error("Fetching todos failed:", error));
  }, []);

  const handleAddTodo = (e) => {
    e.preventDefault();
    if (!newTodoText.trim()) return;

    fetch('/todos', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ text: newTodoText }),
    })
    .then(res => {
      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }
      return res.json();
    })
    .then(newTodo => {
      setTodos([...todos, newTodo]);
      setNewTodoText('');
    })
    .catch(error => console.error("Adding todo failed:", error));
  };

  const toggleComplete = (id) => {
    const todo = todos.find(t => t._id === id);
    if (!todo) return;

    fetch(`/todos/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ completed: !todo.completed }),
    })
    .then(res => {
      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }
      return res.json();
    })
    .then(updatedTodo => {
      setTodos(todos.map(t => t._id === id ? updatedTodo : t));
    })
    .catch(error => console.error("Updating todo failed:", error));
  };

  const handleDeleteTodo = (id) => {
    fetch(`/todos/${id}`, {
      method: 'DELETE',
    })
    .then(res => {
      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }
      // Check if response has content before trying to parse as JSON
      if (res.headers.get("content-length") === "0" || res.status === 204) {
        return null; 
      }
      return res.json();
    })
    .then(() => {
      setTodos(todos.filter(todo => todo._id !== id));
    })
    .catch(error => console.error("Deleting todo failed:", error));
  };

  return (
    <div className="App">
      <h1>My To-Do List</h1>
      <form onSubmit={handleAddTodo}>
        <input
          type="text"
          value={newTodoText}
          onChange={(e) => setNewTodoText(e.target.value)}
          placeholder="Add a new to-do"
        />
        <button type="submit">Add</button>
      </form>
      <ul>
        {todos.map(todo => (
          <li key={todo._id} className={todo.completed ? 'completed' : ''}>
            <span onClick={() => toggleComplete(todo._id)}>
              {todo.text}
            </span>
            <button onClick={() => handleDeleteTodo(todo._id)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default App;
