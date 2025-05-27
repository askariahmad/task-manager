package com.chatbot.whatsapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "todo_items")
public class TodoItem {
    @Id
    private String id;
    private String description;
    private boolean isDone;
    private LocalDateTime createdAt;

    public TodoItem() { // Default constructor
        this.createdAt = LocalDateTime.now();
        this.isDone = false;
    }

    public TodoItem(String description) { // Constructor without ID
        this();
        this.description = description;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isDone() { return isDone; }
    public void setDone(boolean done) { isDone = done; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
