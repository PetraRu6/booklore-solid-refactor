package org.booklore.service.telemetry;

import lombok.RequiredArgsConstructor;
import org.booklore.model.dto.BookloreTelemetry;
import org.booklore.model.enums.ProvisioningMethod;
import org.booklore.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserTelemetryCollector implements TelemetryCollector<BookloreTelemetry.UserStatistics> {

    private final UserRepository userRepository;

    @Override
    public BookloreTelemetry.UserStatistics collect() {
        long totalUsers = userRepository.count();
        long localUsers = userRepository.countByProvisioningMethod(ProvisioningMethod.LOCAL);
        long oidcUsers = userRepository.countByProvisioningMethod(ProvisioningMethod.OIDC);

        return BookloreTelemetry.UserStatistics.builder()
                .totalUsers((int) totalUsers)
                .totalLocalUsers((int) localUsers)
                .totalOidcUsers((int) oidcUsers)
                .oidcEnabled(oidcUsers > 0)
                .build();
    }
}

