package org.booklore.service.telemetry;

import lombok.RequiredArgsConstructor;
import org.booklore.model.dto.BookloreTelemetry;
import org.booklore.model.enums.BookFileType;
import org.booklore.repository.BookRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BookTelemetryCollector implements TelemetryCollector<BookloreTelemetry.BookStatistics> {

    private final BookRepository bookRepository;

    @Override
    public BookloreTelemetry.BookStatistics collect() {
        return BookloreTelemetry.BookStatistics.builder()
                .totalBooks(bookRepository.count())
                .bookCountByType(getBookFileTypeCounts())
                .build();
    }

    private Map<String, Long> getBookFileTypeCounts() {
        Map<String, Long> countByType = new HashMap<>();
        for (BookFileType type : BookFileType.values()) {
            countByType.put(type.name(), bookRepository.countByBookType(type));
        }
        return countByType;
    }
}

