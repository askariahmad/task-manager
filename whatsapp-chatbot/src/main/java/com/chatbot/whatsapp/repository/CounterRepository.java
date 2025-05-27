package com.chatbot.whatsapp.repository;

import com.chatbot.whatsapp.model.Counter;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface CounterRepository extends MongoRepository<Counter, String> {
    Optional<Counter> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    void deleteByNameIgnoreCase(String name); // Ensure this method is transactional if needed, or handle at service layer
}
