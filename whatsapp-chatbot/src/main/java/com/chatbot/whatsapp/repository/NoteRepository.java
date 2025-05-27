package com.chatbot.whatsapp.repository;

import com.chatbot.whatsapp.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NoteRepository extends MongoRepository<Note, String> {
    // Spring Data MongoDB will auto-generate implementations for basic CRUD.
    // We can add custom query methods here if needed.
    List<Note> findByContentContainingIgnoreCase(String keyword);
}
