package com.chatbot.whatsapp.service;

import com.chatbot.whatsapp.model.Reminder;
import com.chatbot.whatsapp.repository.ReminderRepository;
import com.twilio.Twilio; 
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private ScheduledExecutorService scheduler;
    
    public static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    public static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    public static final String TWILIO_PHONE_NUMBER = System.getenv("TWILIO_PHONE_NUMBER");
    private static final DateTimeFormatter USER_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    public ReminderService(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    @PostConstruct
    public void init() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::checkAndSendReminders, 0, 1, TimeUnit.MINUTES);
    }

    public String addReminder(String recipientId, String task, String remindAtStr) {
        LocalDateTime remindAt;
        try {
            remindAt = LocalDateTime.parse(remindAtStr, USER_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return "Invalid date/time format. Please use 'YYYY-MM-DD HH:MM'.";
        }
        if (remindAt.isBefore(LocalDateTime.now())) {
            return "Cannot set a reminder for a past date/time.";
        }
        Reminder newReminder = new Reminder(task, remindAt, recipientId);
        reminderRepository.save(newReminder);
        return "Reminder set for '" + task + "' on " + remindAt.format(USER_DATE_TIME_FORMATTER) + " (ID: " + newReminder.getId() + ")";
    }

    public String listReminders(String recipientId) { // List reminders for a specific user
        List<Reminder> userReminders = reminderRepository.findByRecipientIdAndSentFalseOrderByRemindAtAsc(recipientId);
        if (userReminders.isEmpty()) {
            return "You have no pending reminders.";
        }
        return "Your Reminders:\n" +
                userReminders.stream()
                        .map(r -> "(ID: " + r.getId() + ") '" + r.getTask() + "' at " + r.getRemindAt().format(USER_DATE_TIME_FORMATTER))
                        .collect(Collectors.joining("\n"));
    }
    
    public String removeReminder(String identifier, String recipientId) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return "Please provide an ID or task keyword for the reminder to remove.";
        }
        Optional<Reminder> reminderOpt = reminderRepository.findById(identifier);
        if (reminderOpt.isPresent() && reminderOpt.get().getRecipientId().equals(recipientId) && !reminderOpt.get().isSent()) {
            reminderRepository.deleteById(identifier);
            return "Reminder (ID: " + identifier + ") removed.";
        }
        
        List<Reminder> userReminders = reminderRepository.findByRecipientIdAndSentFalseOrderByRemindAtAsc(recipientId);
        Optional<Reminder> foundByTask = userReminders.stream()
                            .filter(r -> r.getTask().toLowerCase().contains(identifier.toLowerCase()))
                            .findFirst();
        if(foundByTask.isPresent()){
            reminderRepository.deleteById(foundByTask.get().getId());
            return "Reminder '" + foundByTask.get().getTask() + "' (ID: " + foundByTask.get().getId() + ") removed.";
        }
        return "Pending reminder not found with ID or task keyword: '" + identifier + "'.";
    }

    private void checkAndSendReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> dueReminders = reminderRepository.findBySentFalseAndRemindAtBefore(now);

        dueReminders.forEach(reminder -> {
            sendWhatsAppReminder(reminder.getRecipientId(), "REMINDER: " + reminder.getTask());
            reminder.setSent(true);
            reminderRepository.save(reminder); 
        });
    }
    
    private void sendWhatsAppReminder(String recipientId, String messageText) {
        if (ACCOUNT_SID == null || ACCOUNT_SID.isEmpty() || 
            AUTH_TOKEN == null || AUTH_TOKEN.isEmpty() || 
            TWILIO_PHONE_NUMBER == null || TWILIO_PHONE_NUMBER.isEmpty() || 
            recipientId == null || recipientId.isEmpty()) {
            System.err.println("Cannot send WhatsApp reminder. Twilio credentials, recipient, or phone number not configured/missing.");
            return;
        }
        try {
            // Ensure Twilio is initialized (centralized in WhatsAppService, but good to have a check or use a shared client)
            if (Twilio.getRestClient() == null || !Twilio.getRestClient().getAccountSid().equals(ACCOUNT_SID)) {
                 // This might indicate an issue if another part of the app uses a different SID or if not initialized.
                 // For this task, we assume WhatsAppService initializes it. A robust solution would use a singleton Twilio client.
                System.err.println("Twilio client not initialized or SID mismatch. Attempting to initialize for ReminderService.");
                Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            }
            Message.creator(
                new PhoneNumber(recipientId),
                new PhoneNumber(TWILIO_PHONE_NUMBER),
                messageText).create();
            System.out.println("Reminder sent successfully to " + recipientId);
        } catch (Exception e) {
            System.err.println("Error sending WhatsApp reminder to " + recipientId + ": " + e.getMessage());
            // e.printStackTrace(); // Consider logging framework for production
        }
    }

    @PreDestroy
    public void cleanup() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
