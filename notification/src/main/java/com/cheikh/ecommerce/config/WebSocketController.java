package com.cheikh.ecommerce.config;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/send-message")
    @SendTo("/topic/notifications")
    public String sendMessage(String message) {
        System.out.println("message :"+message );
        return message;
    }
}

