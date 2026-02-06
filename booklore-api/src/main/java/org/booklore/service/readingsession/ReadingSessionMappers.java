package org.booklore.service.readingsession;

import org.booklore.model.dto.request.ReadingSessionRequest;
import org.booklore.model.dto.response.*;
import org.booklore.model.entity.BookEntity;
import org.booklore.model.entity.BookLoreUserEntity;
import org.booklore.model.entity.ReadingSessionEntity;
import org.booklore.model.enums.ReadStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@Component
public class ReadingSessionMappers {

    public interface HeatmapPoint {
        LocalDate getDate();
        long getCount();
    }

    public interface TimelinePoint {
        Long getBookId();
        Object getBookFileType();     
        String getBookTitle();
        Instant getStartDate();
        Instant getEndDate();
        long getTotalSessions();
        long getTotalDurationSeconds();
    }

    public interface ReadingSpeedPoint {
        LocalDate getDate();
        double getAvgProgressPerMinute();
        long getTotalSessions();
    }

    public interface PeakHoursPoint {
        int getHourOfDay();
        long getSessionCount();
        long getTotalDurationSeconds();
    }

    public interface FavoriteDaysPoint {
        int getDayOfWeek(); // 1..7
        long getSessionCount();
        long getTotalDurationSeconds();
    }

    public interface GenreStatsPoint {
        String getGenre();
        long getBookCount();
        long getTotalSessions();
        long getTotalDurationSeconds();
    }

    public interface BookCompletionHeatmapPoint {
        int getYear();
        int getMonth();
        long getCount();
    }

    public ReadingSessionEntity toEntity(ReadingSessionRequest request, BookLoreUserEntity user, BookEntity book) {
        return ReadingSessionEntity.builder()
                .user(user)
                .book(book)
                .bookType(request.getBookType())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationSeconds(request.getDurationSeconds())
                .durationFormatted(request.getDurationFormatted())
                .startProgress(request.getStartProgress())
                .endProgress(request.getEndProgress())
                .progressDelta(request.getProgressDelta())
                .startLocation(request.getStartLocation())
                .endLocation(request.getEndLocation())
                .build();
    }

    public ReadingSessionHeatmapResponse toHeatmap(HeatmapPoint dto) {
        return ReadingSessionHeatmapResponse.builder()
                .date(dto.getDate())
                .count(dto.getCount())
                .build();
    }

    public ReadingSessionTimelineResponse toTimeline(TimelinePoint dto) {
        return ReadingSessionTimelineResponse.builder()
                .bookId(dto.getBookId())
                .bookType(dto.getBookFileType())
                .bookTitle(dto.getBookTitle())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .totalSessions(dto.getTotalSessions())
                .totalDurationSeconds(dto.getTotalDurationSeconds())
                .build();
    }

    public ReadingSpeedResponse toReadingSpeed(ReadingSpeedPoint dto) {
        return ReadingSpeedResponse.builder()
                .date(dto.getDate())
                .avgProgressPerMinute(dto.getAvgProgressPerMinute())
                .totalSessions(dto.getTotalSessions())
                .build();
    }

    public PeakReadingHoursResponse toPeakHours(PeakHoursPoint dto) {
        return PeakReadingHoursResponse.builder()
                .hourOfDay(dto.getHourOfDay())
                .sessionCount(dto.getSessionCount())
                .totalDurationSeconds(dto.getTotalDurationSeconds())
                .build();
    }

    public FavoriteReadingDaysResponse toFavoriteDays(FavoriteDaysPoint dto, String dayName) {
        return FavoriteReadingDaysResponse.builder()
                .dayOfWeek(dto.getDayOfWeek())
                .dayName(dayName)
                .sessionCount(dto.getSessionCount())
                .totalDurationSeconds(dto.getTotalDurationSeconds())
                .build();
    }

    public GenreStatisticsResponse toGenreStatistics(GenreStatsPoint dto) {
        double avgSessionsPerBook = dto.getBookCount() > 0
                ? (double) dto.getTotalSessions() / dto.getBookCount()
                : 0.0;

        return GenreStatisticsResponse.builder()
                .genre(dto.getGenre())
                .bookCount(dto.getBookCount())
                .totalSessions(dto.getTotalSessions())
                .totalDurationSeconds(dto.getTotalDurationSeconds())
                .averageSessionsPerBook(round2(avgSessionsPerBook))
                .build();
    }

    public CompletionTimelineResponse toCompletionTimeline(String yearMonthKey, Map<ReadStatus, Long> statusBreakdown) {
        YearMonthKey key = parseYearMonth(yearMonthKey);

        long totalBooks = statusBreakdown.values().stream().mapToLong(Long::longValue).sum();
        long finishedBooks = statusBreakdown.getOrDefault(ReadStatus.READ, 0L);
        double completionRate = totalBooks > 0 ? (finishedBooks * 100.0 / totalBooks) : 0.0;

        return CompletionTimelineResponse.builder()
                .year(key.year())
                .month(key.month())
                .totalBooks(totalBooks)
                .statusBreakdown(statusBreakdown)
                .finishedBooks(finishedBooks)
                .completionRate(round2(completionRate))
                .build();
    }

    public ReadingSessionResponse toReadingSession(ReadingSessionEntity session) {
        return ReadingSessionResponse.builder()
                .id(session.getId())
                .bookId(session.getBook().getId())
                .bookTitle(session.getBook().getMetadata().getTitle())
                .bookType(session.getBookType())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .durationSeconds(session.getDurationSeconds())
                .startProgress(session.getStartProgress())
                .endProgress(session.getEndProgress())
                .progressDelta(session.getProgressDelta())
                .startLocation(session.getStartLocation())
                .endLocation(session.getEndLocation())
                .createdAt(session.getCreatedAt())
                .build();
    }

    public BookCompletionHeatmapResponse toBookCompletionHeatmap(BookCompletionHeatmapPoint dto) {
        return BookCompletionHeatmapResponse.builder()
                .year(dto.getYear())
                .month(dto.getMonth())
                .count(dto.getCount())
                .build();
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static YearMonthKey parseYearMonth(String key) {
        String[] parts = key.split("-");
        int y = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        return new YearMonthKey(y, m);
    }

    private record YearMonthKey(int year, int month) {}
}
