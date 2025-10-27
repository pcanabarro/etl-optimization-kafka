package com.pcanabarro.controllers;

import com.pcanabarro.services.ProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProducerController {
    private final ProducerService producerService;

    public ProducerController(ProducerService producerService) {
        this.producerService = producerService;
    }

    @GetMapping("/send")
    public String send() {
        try {
            int messagesSent = producerService.sendMessages();

            return ResponseEntity.ok(messagesSent + " messages sent!").toString();
        } catch (Exception e) {
            return e.getMessage();
        }

    }
}
