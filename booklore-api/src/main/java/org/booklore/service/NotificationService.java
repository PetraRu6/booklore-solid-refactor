package org.booklore.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.booklore.config.security.service.AuthenticationService;
import org.booklore.model.enums.PermissionType;
import org.booklore.model.websocket.Topic;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class NotificationService {

    private final UserMessageSender sender;
    private final AuthenticationService authenticationService;
    private final PermissionUserSelector permissionUserSelector;

    public void sendMessage(Topic topic, Object message) {
        try {
            var user = authenticationService.getAuthenticatedUser();
            if (user == null) {
                log.warn("No authenticated user found. Message not sent: {}", topic);
                return;
            }
            sender.sendToUser(user.getUsername(), topic.getPath(), message);
        } catch (Exception e) {
            log.error("Error sending message to topic {}: {}", topic, e.getMessage(), e);
        }
    }

    public void sendMessageToPermissions(Topic topic, Object message, Set<PermissionType> permissionTypes) {
        List<String> usernames = permissionUserSelector.selectUsernames(permissionTypes);
        if (usernames.isEmpty()) return;

        for (String username : usernames) {
            try {
                sender.sendToUser(username, topic.getPath(), message);
            } catch (Exception e) {
                log.error("Error sending message to user {} for topic {}: {}", username, topic, e.getMessage(), e);
            }
        }
    }
}
