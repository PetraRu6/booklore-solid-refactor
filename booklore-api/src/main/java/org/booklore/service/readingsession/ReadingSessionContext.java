package org.booklore.service.readingsession;

import lombok.RequiredArgsConstructor;
import org.booklore.config.security.service.AuthenticationService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadingSessionContext {

    private final AuthenticationService authenticationService;

    public Long userId() {
        return authenticationService.getAuthenticatedUser().getId();
    }
}
