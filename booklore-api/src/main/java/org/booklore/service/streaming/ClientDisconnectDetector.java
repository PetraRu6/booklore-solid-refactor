package org.booklore.service.streaming;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketTimeoutException;

@Component
public class ClientDisconnectDetector {

    public boolean isClientDisconnect(IOException e) {
        if (e instanceof SocketTimeoutException) return true;

        String msg = e.getMessage();
        if (msg == null) return false;

        return msg.contains("Broken pipe")
                || msg.contains("Connection reset")
                || msg.contains("connection was aborted")
                || msg.contains("An established connection was aborted")
                || msg.contains("SocketTimeout")
                || msg.contains("timed out");
    }
}

