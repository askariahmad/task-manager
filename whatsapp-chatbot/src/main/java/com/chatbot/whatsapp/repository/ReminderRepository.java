package com.chatbot.whatsapp.repository;

import com.chatbot.whatsapp.model.Reminder;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends MongoRepository<Reminder, String> {
    List<Reminder> findBySentFalseAndRemindAtBefore(LocalDateTime dateTime);
    List<Reminder> findByRecipientIdAndSentFalseOrderByRemindAtAsc(String recipientId); // For listing user's pending reminders
}
