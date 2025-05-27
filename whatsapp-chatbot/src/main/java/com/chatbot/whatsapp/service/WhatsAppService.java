package com.chatbot.whatsapp.service;

// import com.chatbot.whatsapp.model.Reminder; // If direct model interaction is needed, otherwise through service
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.protobuf.Value;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
// import java.util.UUID; // For generating unique session IDs for Dialogflow - using senderId instead

@Service
public class WhatsAppService {

    public static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    public static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    public static final String TWILIO_PHONE_NUMBER = System.getenv("TWILIO_PHONE_NUMBER");
    private static final String DEFAULT_LANGUAGE_CODE = "en-US";


    private final TodoService todoService;
    private final NoteService noteService;
    private final CounterService counterService;
    private final ReminderService reminderService;
    private final DialogflowService dialogflowService; // Inject DialogflowService

    @Autowired
    public WhatsAppService(TodoService todoService, NoteService noteService, 
                           CounterService counterService, ReminderService reminderService,
                           DialogflowService dialogflowService) { // Add DialogflowService
        this.todoService = todoService;
        this.noteService = noteService;
        this.counterService = counterService;
        this.reminderService = reminderService;
        this.dialogflowService = dialogflowService; // Assign DialogflowService
        
        if (ACCOUNT_SID != null && AUTH_TOKEN != null && !ACCOUNT_SID.isEmpty() && !AUTH_TOKEN.isEmpty()) {
             Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        } else {
            System.err.println("WARNING: TWILIO_ACCOUNT_SID or TWILIO_AUTH_TOKEN not configured. WhatsApp sending will fail.");
        }
    }

    public void processIncomingMessage(String senderId, String messageContent) {
        System.out.println("Processing message from " + senderId + ": " + messageContent);
        String replyText = "Sorry, I couldn't process your request. Please try again."; // Default fallback reply

        // Using senderId as session ID is common for chatbots to maintain conversation context per user.
        String sessionId = senderId; 


        try {
            QueryResult dialogflowQueryResult = dialogflowService.detectIntent(messageContent, sessionId, DEFAULT_LANGUAGE_CODE);

            if (dialogflowQueryResult != null && dialogflowQueryResult.getIntent() != null && !dialogflowQueryResult.getIntent().getDisplayName().isEmpty()) {
                String intentName = dialogflowQueryResult.getIntent().getDisplayName();
                Map<String, Value> parameters = dialogflowQueryResult.getParameters().getFieldsMap();
                System.out.println("Detected Intent: " + intentName + ", Parameters: " + parameters);


                // Switch based on intent name
                switch (intentName) {
                    // Greetings & Farewells
                    case "greeting":
                        replyText = dialogflowQueryResult.getFulfillmentText(); 
                        if (replyText == null || replyText.isEmpty()) {
                            replyText = "Hello! How can I assist you today?";
                        }
                        break;
                    case "goodbye":
                        replyText = dialogflowQueryResult.getFulfillmentText();
                        if (replyText == null || replyText.isEmpty()) {
                            replyText = "Goodbye! Have a great day.";
                        }
                        break;

                    // To-Do Intents
                    case "add_todo":
                        String taskDescription = getStringParameter("task_description", parameters);
                        if (!taskDescription.isEmpty()) {
                            replyText = todoService.addTodo(taskDescription);
                        } else {
                            replyText = "Please tell me what task you want to add.";
                        }
                        break;
                    case "list_todos":
                        replyText = todoService.listTodos();
                        break;
                    case "complete_todo":
                        String todoToComplete = getStringParameter("task_identifier", parameters); 
                        if (!todoToComplete.isEmpty()) {
                            replyText = todoService.markTodoAsDone(todoToComplete);
                        } else {
                            replyText = "Please specify which to-do item to mark as complete.";
                        }
                        break;
                    case "remove_todo":
                        String todoToRemove = getStringParameter("task_identifier", parameters);
                        if (!todoToRemove.isEmpty()) {
                            replyText = todoService.removeTodo(todoToRemove);
                        } else {
                            replyText = "Please specify which to-do item to remove.";
                        }
                        break;

                    // Note Intents
                    case "add_note":
                        String noteContent = getStringParameter("note_content", parameters);
                        if (!noteContent.isEmpty()) {
                            replyText = noteService.addNote(noteContent);
                        } else {
                            replyText = "What content should the note have?";
                        }
                        break;
                    case "list_notes":
                        replyText = noteService.listNotes();
                        break;
                    case "find_note":
                        String noteKeyword = getStringParameter("search_keyword", parameters);
                        if (!noteKeyword.isEmpty()) {
                            replyText = noteService.findNote(noteKeyword);
                        } else {
                            replyText = "What keyword should I search for in your notes?";
                        }
                        break;
                    case "delete_note":
                        String noteIdentifier = getStringParameter("note_identifier", parameters);
                        if (!noteIdentifier.isEmpty()) {
                            replyText = noteService.deleteNote(noteIdentifier);
                        } else {
                            replyText = "Please specify which note to delete (e.g., by ID).";
                        }
                        break;

                    // Counter Intents
                    case "create_counter":
                        String counterNameToCreate = getStringParameter("counter_name", parameters);
                        if (!counterNameToCreate.isEmpty()) {
                            replyText = counterService.createCounter(counterNameToCreate);
                        } else {
                            replyText = "What name should the counter have?";
                        }
                        break;
                    case "increment_counter":
                        String counterToIncrement = getStringParameter("counter_name", parameters);
                        if (!counterToIncrement.isEmpty()) {
                            replyText = counterService.incrementCounter(counterToIncrement);
                        } else {
                            replyText = "Which counter should I increment?";
                        }
                        break;
                    case "decrement_counter":
                        String counterToDecrement = getStringParameter("counter_name", parameters);
                        if (!counterToDecrement.isEmpty()) {
                            replyText = counterService.decrementCounter(counterToDecrement);
                        } else {
                            replyText = "Which counter should I decrement?";
                        }
                        break;
                    case "show_counter":
                        String counterToShow = getStringParameter("counter_name", parameters);
                        if (!counterToShow.isEmpty()) {
                            replyText = counterService.getCounterValue(counterToShow);
                        } else {
                            replyText = "Which counter's value do you want to see?";
                        }
                        break;
                    case "delete_counter":
                        String counterToDelete = getStringParameter("counter_name", parameters);
                        if (!counterToDelete.isEmpty()) {
                            replyText = counterService.deleteCounter(counterToDelete);
                        } else {
                            replyText = "Which counter should I delete?";
                        }
                        break;
                     case "list_counters": 
                        replyText = counterService.listCounters();
                        break;

                    // Reminder Intents
                    case "set_reminder":
                        String reminderTask = getStringParameter("task_description", parameters);
                        // Dialogflow's @sys.date-time often comes as a struct for specific dates/times, or a string for relative ones.
                        String dateTimeStr = getDateTimeParameter("date-time", parameters); 
                        
                        if (!reminderTask.isEmpty() && !dateTimeStr.isEmpty()) {
                            try {
                                // Attempt to parse as ISO_OFFSET_DATE_TIME (e.g., "2023-12-25T10:00:00-07:00")
                                TemporalAccessor accessor = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(dateTimeStr);
                                LocalDateTime localDateTime = LocalDateTime.from(accessor);
                                String formattedDateTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                                replyText = reminderService.addReminder(senderId, reminderTask, formattedDateTime);
                            } catch (Exception e) {
                                // Fallback for simpler date/time strings or if parsing above fails (e.g. "tomorrow evening")
                                // This part is tricky as Dialogflow's @sys.date-time can be very flexible.
                                // For robust parsing, you might need a more sophisticated date/time parsing library
                                // or more specific entity extraction in Dialogflow if simple parsing fails.
                                // For now, we'll assume ReminderService can handle some flexibility or needs exact format.
                                // This indicates that the dateTimeStr received from Dialogflow was not in ISO_OFFSET_DATE_TIME.
                                // We might need to adjust ReminderService or add more parsing logic here.
                                System.err.println("Could not parse as ISO_OFFSET_DATE_TIME: " + dateTimeStr + ". Trying direct pass or specific formats.");
                                // Attempt direct pass if ReminderService can handle it or if it's already formatted.
                                // This part might need refinement based on testing with various Dialogflow date/time responses.
                                // The current ReminderService expects "yyyy-MM-dd HH:mm".
                                // If Dialogflow gives "tomorrow at 5pm", this will fail in ReminderService.
                                // This is a known challenge: mapping Dialogflow's flexible date/time to a specific format.
                                replyText = reminderService.addReminder(senderId, reminderTask, dateTimeStr);
                                if(replyText.startsWith("Invalid date/time format")){ // check if reminderService failed
                                    System.err.println("ReminderService failed to parse dateTimeStr: " + dateTimeStr);
                                    replyText = "I understood the reminder task '" + reminderTask + "', but had trouble with the date and time: '" + dateTimeStr +"'. Please try 'YYYY-MM-DD HH:MM' or simpler phrases like 'tomorrow at 5pm'.";
                                }
                            }
                        } else if (reminderTask.isEmpty()){
                            replyText = "What do you want to be reminded about?";
                        } else { // dateTimeStr is empty
                            replyText = "When should I remind you about '" + reminderTask + "'?";
                        }
                        break;
                    case "list_reminders":
                        replyText = reminderService.listReminders(senderId);
                        break;
                    case "delete_reminder":
                        String reminderIdentifier = getStringParameter("reminder_identifier", parameters);
                        if(!reminderIdentifier.isEmpty()){
                            replyText = reminderService.removeReminder(reminderIdentifier, senderId);
                        } else {
                            replyText = "Which reminder should I delete?";
                        }
                        break;
                        
                    case "Default Fallback Intent": 
                    default: 
                        replyText = dialogflowQueryResult.getFulfillmentText();
                        if (replyText == null || replyText.isEmpty()) {
                            replyText = "Sorry, I didn't quite understand that. Could you try rephrasing?";
                        }
                        break;
                }
            } else {
                System.err.println("Dialogflow query result, intent, or intent display name was null or empty.");
                replyText = "I'm having a bit of trouble understanding right now. Please try again later.";
            }

        } catch (IOException e) {
            System.err.println("Error communicating with Dialogflow: " + e.getMessage());
            e.printStackTrace(); 
            replyText = "I'm facing some technical difficulties connecting to my brain. Please try again in a moment.";
        }

        sendWhatsAppMessage(senderId, replyText);
    }

    private String getStringParameter(String paramName, Map<String, Value> parameters) {
        if (parameters.containsKey(paramName) && parameters.get(paramName).hasStringValue()) {
            return parameters.get(paramName).getStringValue();
        }
        return "";
    }

    private String getDateTimeParameter(String paramName, Map<String, Value> parameters) {
        if (parameters.containsKey(paramName)) {
            Value paramValue = parameters.get(paramName);
            if (paramValue.hasStringValue()) { 
                return paramValue.getStringValue();
            } else if (paramValue.hasStructValue()) { 
                Map<String, Value> structMap = paramValue.getStructValue().getFieldsMap();
                // Common fields for @sys.date-time struct. Prioritize more complete date-time representations.
                if (structMap.containsKey("date_time") && structMap.get("date_time").hasStringValue()) { // Often full ISO
                    return structMap.get("date_time").getStringValue();
                } else if (structMap.containsKey("iso_date_time") && structMap.get("iso_date_time").hasStringValue()) {
                     return structMap.get("iso_date_time").getStringValue();
                }
                // Fallback for partial date/time or other struct representations.
                // This might need to be assembled or further processed.
                // For now, just returning the struct as a string is a placeholder and likely won't parse directly.
                // A more robust solution would inspect the struct for specific date, time, year fields.
                System.err.println("Complex date-time struct received for " + paramName + ": " + paramValue.getStructValue().toString());
                return paramValue.getStructValue().toString(); // This will likely fail parsing in ReminderService.
            }
        }
        return "";
    }

    public void sendWhatsAppMessage(String recipientId, String messageText) {
         if (ACCOUNT_SID == null || ACCOUNT_SID.isEmpty() || 
            AUTH_TOKEN == null || AUTH_TOKEN.isEmpty() || 
            TWILIO_PHONE_NUMBER == null || TWILIO_PHONE_NUMBER.isEmpty() ||
            "YOUR_TWILIO_WHATSAPP_NUMBER".equals(TWILIO_PHONE_NUMBER) || 
            "whatsapp:+14155238886".equals(TWILIO_PHONE_NUMBER) ) { // Example number check
            System.err.println("Cannot send WhatsApp message. Twilio credentials not configured or using placeholder/example values.");
            System.err.println("Recipient: " + recipientId + ", Message: " + messageText);
            return;
        }
        try {
            Message.creator(
                            new PhoneNumber(recipientId),
                            new PhoneNumber(TWILIO_PHONE_NUMBER),
                            messageText)
                    .create();
            System.out.println("Message sent successfully. SID: (omitted for brevity)"); 
        } catch (Exception e) {
            System.err.println("Error sending WhatsApp message: " + e.getMessage());
        }
    }
}
