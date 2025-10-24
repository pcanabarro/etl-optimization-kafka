package com.pcanabarro.controllers;

import com.pcanabarro.ProducerService;
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
        producerService.sendMessages();
        return "Messages sent!";
    }
}
