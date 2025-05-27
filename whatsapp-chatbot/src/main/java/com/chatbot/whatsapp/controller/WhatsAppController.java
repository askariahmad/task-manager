package com.chatbot.whatsapp.controller;

import com.chatbot.whatsapp.service.WhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    @Autowired
    public WhatsAppController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    @PostMapping(path = "/webhook", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void handleIncomingMessage(@RequestParam Map<String, String> body) {
        // Twilio sends messages as form data
        String message = body.get("Body");
        String from = body.get("From"); // Sender's WhatsApp ID (e.g., whatsapp:+14155238886)
        
        System.out.println("Received message: '" + message + "' from: " + from);
        whatsAppService.processIncomingMessage(from, message);
        // Twilio expects an empty response or TwiML. For now, a 200 OK is fine.
        // More advanced responses can be crafted if needed.
    }
}
