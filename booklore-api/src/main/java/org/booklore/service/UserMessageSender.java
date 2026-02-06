package org.booklore.service;

public interface UserMessageSender {
    void sendToUser(String username, String destination, Object payload);
}
