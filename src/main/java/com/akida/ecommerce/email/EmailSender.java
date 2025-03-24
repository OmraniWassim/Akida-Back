package com.akida.ecommerce.email;

public interface EmailSender {
    void send(String to,String from, String email);
}