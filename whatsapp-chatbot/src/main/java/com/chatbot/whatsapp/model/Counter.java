package com.chatbot.whatsapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed; // Import Indexed
import java.time.LocalDateTime;

@Document(collection = "counters")
public class Counter {
    @Id
    private String id;

    @Indexed(unique = true) // Ensure counter names are unique in the DB
    private String name; 
    
    private int value;
    private LocalDateTime createdAt;

    public Counter() { 
        this.createdAt = LocalDateTime.now();
        this.value = 0;
    }
    
    public Counter(String name) { 
        this();
        this.name = name;
    }

    public Counter(String name, int initialValue) { 
        this();
        this.name = name;
        this.value = initialValue;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Convenience methods
    public void increment() { this.value++; }
    public void decrement() { this.value--; }
}
