package org.booklore.service.telemetry;

import lombok.RequiredArgsConstructor;
import org.booklore.model.dto.BookloreTelemetry;
import org.booklore.model.entity.LibraryEntity;
import org.booklore.repository.BookRepository;
import org.booklore.repository.LibraryRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LibraryTelemetryCollector implements TelemetryCollector<List<BookloreTelemetry.LibraryStatistics>> {

    private final LibraryRepository libraryRepository;
    private final BookRepository bookRepository;

    @Override
    public List<BookloreTelemetry.LibraryStatistics> collect() {
        return libraryRepository.findAll().stream()
                .map(this::mapLibraryStatistics)
                .toList();
    }

    private BookloreTelemetry.LibraryStatistics mapLibraryStatistics(LibraryEntity lib) {
        return BookloreTelemetry.LibraryStatistics.builder()
                .totalLibraryPaths(lib.getLibraryPaths() != null ? lib.getLibraryPaths().size() : 0)
                .bookCount(bookRepository.countByLibraryId(lib.getId()))
                .watchEnabled(lib.isWatch())
                .iconType(lib.getIconType() != null ? lib.getIconType().name() : null)
                .build();
    }
}

