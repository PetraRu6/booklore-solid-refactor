package org.booklore.service;

import lombok.AllArgsConstructor;
import org.booklore.model.entity.BookLoreUserEntity;
import org.booklore.model.enums.PermissionType;
import org.booklore.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.booklore.util.UserPermissionUtils.hasPermission;

@Service
@AllArgsConstructor
public class PermissionUserSelector {

    private final UserRepository userRepository;

    public List<String> selectUsernames(Set<PermissionType> permissionTypes) {
        if (permissionTypes == null || permissionTypes.isEmpty()) return List.of();

        Set<PermissionType> permissionSet = EnumSet.copyOf(permissionTypes);

        return userRepository.findAll().stream()
                .filter(u -> u.getPermissions() != null)
                .filter(u -> permissionSet.stream().anyMatch(p -> hasPermission(u.getPermissions(), p)))
                .map(BookLoreUserEntity::getUsername)
                .distinct()
                .toList();
    }
}
