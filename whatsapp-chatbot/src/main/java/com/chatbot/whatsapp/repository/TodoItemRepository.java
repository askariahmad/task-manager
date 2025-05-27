package com.chatbot.whatsapp.repository;

import com.chatbot.whatsapp.model.TodoItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface TodoItemRepository extends MongoRepository<TodoItem, String> {
    Optional<TodoItem> findByIdAndIsDone(String id, boolean isDone);
    List<TodoItem> findAllByOrderByCreatedAtDesc(); // Example: list all, newest first
    Optional<TodoItem> findByDescriptionIgnoreCase(String description);
}
