package com.akida.ecommerce.serviceimpl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketServiceImpl {
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketServiceImpl(final SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendMessage(final String topicSuffix) {
        messagingTemplate.convertAndSend("/topic/" + topicSuffix);
    }

    public void sendMessage(final String topicSuffix, final String payload) {
        messagingTemplate.convertAndSend("/topic/" + topicSuffix, payload);
    }
}