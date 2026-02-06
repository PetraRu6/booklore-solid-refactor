package org.booklore.service;

import lombok.AllArgsConstructor;
import org.booklore.config.security.service.AuthenticationService;
import org.booklore.model.dto.MagicShelf;
import org.booklore.model.entity.MagicShelfEntity;
import org.booklore.repository.MagicShelfRepository;
import org.booklore.service.magicshelf.MagicShelfAuthorization;
import org.booklore.service.magicshelf.MagicShelfMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class MagicShelfService {

    private final MagicShelfRepository magicShelfRepository;
    private final AuthenticationService authenticationService;
    private final MagicShelfMapper mapper;
    private final MagicShelfAuthorization authorization;

    public List<MagicShelf> getUserShelves() {
        Long userId = authenticationService.getAuthenticatedUser().getId();
        return getShelvesForUser(userId);
    }

    public List<MagicShelf> getUserShelvesForOpds(Long userId) {
        return getShelvesForUser(userId);
    }

    private List<MagicShelf> getShelvesForUser(Long userId) {
        List<MagicShelf> userShelves = magicShelfRepository.findAllByUserId(userId).stream()
                .map(mapper::toDto)
                .toList();

        List<Long> userShelfIds = userShelves.stream().map(MagicShelf::getId).toList();

        List<MagicShelf> publicShelves = magicShelfRepository.findAllByIsPublicIsTrue().stream()
                .filter(shelf -> !userShelfIds.contains(shelf.getId()))
                .map(mapper::toDto)
                .toList();

        return concat(userShelves, publicShelves);
    }

    private static List<MagicShelf> concat(List<MagicShelf> a, List<MagicShelf> b) {
        if (b.isEmpty()) return a;
        var out = new java.util.ArrayList<>(a);
        out.addAll(b);
        return out;
    }

    @Transactional
    public MagicShelf createOrUpdateShelf(MagicShelf dto) {
        Long userId = authenticationService.getAuthenticatedUser().getId();

        if (dto.getId() == null) {
            if (magicShelfRepository.existsByUserIdAndName(userId, dto.getName())) {
                throw new IllegalArgumentException("A shelf with the same name already exists for this user.");
            }
            return mapper.toDto(magicShelfRepository.save(mapper.toEntity(dto, userId)));
        }

        MagicShelfEntity existing = magicShelfRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Shelf not found"));

        authorization.assertCanUpdate(authenticationService, existing);

        mapper.updateEntity(existing, dto);
        return mapper.toDto(magicShelfRepository.save(existing));
    }

    @Transactional
    public void deleteShelf(Long id) {
        MagicShelfEntity shelf = magicShelfRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shelf not found"));

        authorization.assertCanDelete(authenticationService, shelf);

        magicShelfRepository.deleteById(id);
    }

    public MagicShelf getShelf(Long id) {
        MagicShelfEntity shelf = magicShelfRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shelf not found"));
        return mapper.toDto(shelf);
    }
}