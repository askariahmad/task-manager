package com.chatbot.whatsapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "reminders")
public class Reminder {
    @Id
    private String id;
    private String task;
    private LocalDateTime remindAt;
    private LocalDateTime createdAt;
    private String recipientId;
    private boolean sent;

    public Reminder() { // Default constructor
        this.createdAt = LocalDateTime.now();
        this.sent = false;
    }

    public Reminder(String task, LocalDateTime remindAt, String recipientId) { // Constructor without ID
        this();
        this.task = task;
        this.remindAt = remindAt;
        this.recipientId = recipientId;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }
    public LocalDateTime getRemindAt() { return remindAt; }
    public void setRemindAt(LocalDateTime remindAt) { this.remindAt = remindAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public boolean isSent() { return sent; }
    public void setSent(boolean sent) { this.sent = sent; }
}
