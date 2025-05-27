package com.chatbot.whatsapp.service;

import com.chatbot.whatsapp.model.TodoItem;
import com.chatbot.whatsapp.repository.TodoItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TodoService {

    private final TodoItemRepository todoItemRepository;

    @Autowired
    public TodoService(TodoItemRepository todoItemRepository) {
        this.todoItemRepository = todoItemRepository;
    }

    public String addTodo(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "Todo description cannot be empty.";
        }
        // Check for duplicates by description (optional, based on requirements)
        if (todoItemRepository.findByDescriptionIgnoreCase(description.trim()).isPresent()) {
             return "Todo '" + description.trim() + "' already exists.";
        }
        TodoItem newItem = new TodoItem(description.trim());
        todoItemRepository.save(newItem);
        return "Todo added: '" + newItem.getDescription() + "' (ID: " + newItem.getId() + ")";
    }

    public String listTodos() {
        List<TodoItem> todoItems = todoItemRepository.findAllByOrderByCreatedAtDesc();
        if (todoItems.isEmpty()) {
            return "Your to-do list is empty.";
        }
        return "Your To-Dos:\n" +
                todoItems.stream()
                        .map(item -> (item.isDone() ? "[X]" : "[ ]") + " (ID: " + item.getId() + ") " + item.getDescription())
                        .collect(Collectors.joining("\n"));
    }

    public String markTodoAsDone(String identifier) {
        Optional<TodoItem> itemOpt = todoItemRepository.findById(identifier);
        if (!itemOpt.isPresent()) { // If not found by ID, try by description (case-insensitive)
            itemOpt = todoItemRepository.findByDescriptionIgnoreCase(identifier);
        }

        if (itemOpt.isPresent()) {
            TodoItem item = itemOpt.get();
            if (item.isDone()) {
                return "Todo '" + item.getDescription() + "' (ID: " + item.getId() + ") was already marked as done.";
            }
            item.setDone(true);
            todoItemRepository.save(item);
            return "Todo '" + item.getDescription() + "' (ID: " + item.getId() + ") marked as done.";
        }
        return "Todo not found with identifier: '" + identifier + "'.";
    }

    public String removeTodo(String identifier) {
        Optional<TodoItem> itemOpt = todoItemRepository.findById(identifier);
        if (!itemOpt.isPresent()) {
            itemOpt = todoItemRepository.findByDescriptionIgnoreCase(identifier);
        }

        if (itemOpt.isPresent()) {
            TodoItem item = itemOpt.get();
            todoItemRepository.deleteById(item.getId());
            return "Todo '" + item.getDescription() + "' (ID: " + item.getId() + ") removed.";
        }
        return "Todo not found with identifier: '" + identifier + "' to remove.";
    }
}
