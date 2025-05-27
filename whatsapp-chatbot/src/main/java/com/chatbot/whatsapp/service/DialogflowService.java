package com.chatbot.whatsapp.service;

import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.TextInput;

import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class DialogflowService {

    private String projectId = System.getenv("DIALOGFLOW_PROJECT_ID");
    // private String languageCode = "en-US"; // Default language code

    public QueryResult detectIntent(String text, String sessionId, String languageCode) throws IOException {
        if (projectId == null || projectId.isEmpty()) {
            System.err.println("DIALOGFLOW_PROJECT_ID environment variable not set. Dialogflow integration will fail.");
            // Optionally throw an exception or return a specific error state
            return null; 
        }

        // Instantiates a client
        try (SessionsClient sessionsClient = SessionsClient.create()) {
            // Set the session name components
            SessionName session = SessionName.of(projectId, sessionId);

            // Set the text input to be sent to Dialogflow
            TextInput.Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);

            // Build the query with the TextInput
            QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

            // Performs the detect intent request
            DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);

            // Display sentiments
            QueryResult queryResult = response.getQueryResult();
            System.out.println("Detected Intent: " + queryResult.getIntent().getDisplayName());
            System.out.println("Confidence: " + queryResult.getIntentDetectionConfidence());
            System.out.println("Fulfillment Text: " + queryResult.getFulfillmentText());
            
            return queryResult;
        } catch (Exception e) {
            System.err.println("Error detecting intent with Dialogflow: " + e.getMessage());
            e.printStackTrace();
            // Consider how to handle this error upstream, maybe re-throw a custom exception
            return null; // Or throw custom exception
        }
    }
    // Add more methods as needed, e.g., for event intents, context management etc.
}
