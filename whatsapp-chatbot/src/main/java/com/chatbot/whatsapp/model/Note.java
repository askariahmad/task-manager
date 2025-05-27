package com.chatbot.whatsapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "notes") // Specify MongoDB collection name
public class Note {
    @Id // Mark this field as the primary key for MongoDB
    private String id; // MongoDB can auto-generate String IDs
    private String content;
    private LocalDateTime createdAt;

    // Constructors
    public Note() { // Default constructor needed by Spring Data
        this.createdAt = LocalDateTime.now();
    }
    
    public Note(String content) { // Constructor without ID
        this();
        this.content = content;
    }
    
    // Keep existing constructor for compatibility if needed, but ID will be auto-generated
    // public Note(String id, String content) { ... } // Or remove if ID is always auto-generated

    // Getters and Setters (ensure all fields have them)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
