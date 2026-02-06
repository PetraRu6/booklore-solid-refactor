package org.booklore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.booklore.exception.ApiError;
import org.booklore.model.dto.request.ReadingSessionRequest;
import org.booklore.model.dto.response.*;
import org.booklore.model.entity.BookEntity;
import org.booklore.model.entity.BookLoreUserEntity;
import org.booklore.model.entity.ReadingSessionEntity;
import org.booklore.model.enums.ReadStatus;
import org.booklore.repository.BookRepository;
import org.booklore.repository.ReadingSessionRepository;
import org.booklore.repository.UserBookProgressRepository;
import org.booklore.repository.UserRepository;
import org.booklore.service.readingsession.ReadingSessionContext;
import org.booklore.service.readingsession.ReadingSessionMappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadingSessionService {

    private final ReadingSessionContext context;
    private final ReadingSessionMappers mappers;

    private final ReadingSessionRepository readingSessionRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final UserBookProgressRepository userBookProgressRepository;

    
    @Transactional
    public void recordSession(ReadingSessionRequest request) {
        Long userId = context.userId();

        BookLoreUserEntity userEntity = loadUserEntity(userId);
        BookEntity book = loadBookEntity(request.getBookId());

        ReadingSessionEntity session = mappers.toEntity(request, userEntity, book);
        readingSessionRepository.save(session);

        log.info(
                "Reading session persisted successfully: sessionId={}, userId={}, bookId={}, duration={}s",
                session.getId(), userId, request.getBookId(), request.getDurationSeconds()
        );
    }

    @Transactional(readOnly = true)
    public List<ReadingSessionHeatmapResponse> getSessionHeatmapForYear(int year) {
        Long userId = context.userId();

        return readingSessionRepository.findSessionCountsByUserAndYear(userId, year)
                .stream()
                .map(mappers::toHeatmap)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReadingSessionHeatmapResponse> getSessionHeatmapForMonth(int year, int month) {
        Long userId = context.userId();

        return readingSessionRepository.findSessionCountsByUserAndYearAndMonth(userId, year, month)
                .stream()
                .map(mappers::toHeatmap)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReadingSessionTimelineResponse> getSessionTimelineForWeek(int year, int week) {
        Long userId = context.userId();

        WeekWindow window = weekWindow(year, week);

        return readingSessionRepository.findSessionTimelineByUserAndWeek(
                        userId,
                        window.start().atZone(ZoneId.systemDefault()).toInstant(),
                        window.endExclusive().atZone(ZoneId.systemDefault()).toInstant()
                )
                .stream()
                .map(mappers::toTimeline)
                .collect(Collectors.toList());
    }

  
    @Transactional(readOnly = true)
    public List<ReadingSpeedResponse> getReadingSpeedForYear(int year) {
        Long userId = context.userId();

        return readingSessionRepository.findReadingSpeedByUserAndYear(userId, year)
                .stream()
                .map(mappers::toReadingSpeed)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PeakReadingHoursResponse> getPeakReadingHours(Integer year, Integer month) {
        Long userId = context.userId();

        return readingSessionRepository.findPeakReadingHoursByUser(userId, year, month)
                .stream()
                .map(mappers::toPeakHours)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FavoriteReadingDaysResponse> getFavoriteReadingDays(Integer year, Integer month) {
        Long userId = context.userId();

        return readingSessionRepository.findFavoriteReadingDaysByUser(userId, year, month)
                .stream()
                .map(dto -> mappers.toFavoriteDays(dto, dayName(dto.getDayOfWeek())))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<GenreStatisticsResponse> getGenreStatistics() {
        Long userId = context.userId();

        return readingSessionRepository.findGenreStatisticsByUser(userId)
                .stream()
                .map(mappers::toGenreStatistics)
                .collect(Collectors.toList());
    }

    
    @Transactional(readOnly = true)
    public List<CompletionTimelineResponse> getCompletionTimeline(int year) {
        Long userId = context.userId();

        Map<String, EnumMap<ReadStatus, Long>> timelineMap = new HashMap<>();

        userBookProgressRepository.findCompletionTimelineByUser(userId, year).forEach(dto -> {
            String key = dto.getYear() + "-" + dto.getMonth();
            timelineMap.computeIfAbsent(key, k -> new EnumMap<>(ReadStatus.class))
                    .put(dto.getReadStatus(), dto.getBookCount());
        });

        return timelineMap.entrySet().stream()
                .map(entry -> mappers.toCompletionTimeline(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> {
                    int cmp = b.getYear().compareTo(a.getYear());
                    return cmp != 0 ? cmp : b.getMonth().compareTo(a.getMonth());
                })
                .collect(Collectors.toList());
    }

    
    @Transactional(readOnly = true)
    public Page<ReadingSessionResponse> getReadingSessionsForBook(Long bookId, int page) {
        Long userId = context.userId();

        loadBookEntity(bookId);

        Pageable pageable = PageRequest.of(page, 5);
        Page<ReadingSessionEntity> sessions = readingSessionRepository.findByUserIdAndBookId(userId, bookId, pageable);

        return sessions.map(mappers::toReadingSession);
    }


    @Transactional(readOnly = true)
    public List<BookCompletionHeatmapResponse> getBookCompletionHeatmap() {
        Long userId = context.userId();

        int currentYear = LocalDate.now().getYear();
        int startYear = currentYear - 9;

        return userBookProgressRepository.findBookCompletionHeatmap(userId, startYear, currentYear)
                .stream()
                .map(mappers::toBookCompletionHeatmap)
                .collect(Collectors.toList());
    }

    private BookLoreUserEntity loadUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
    }

    private BookEntity loadBookEntity(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> ApiError.BOOK_NOT_FOUND.createException(bookId));
    }

    private static WeekWindow weekWindow(int year, int week) {
        LocalDate date = LocalDate.of(year, 1, 1)
                .with(WeekFields.of(DayOfWeek.MONDAY, 1).weekOfYear(), week);

        LocalDateTime startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime endExclusive = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1).atStartOfDay();
        return new WeekWindow(startOfWeek, endExclusive);
    }

    private static String dayName(int dayOfWeek1to7) {
        return switch (dayOfWeek1to7) {
            case 1 -> "Sunday";
            case 2 -> "Monday";
            case 3 -> "Tuesday";
            case 4 -> "Wednesday";
            case 5 -> "Thursday";
            case 6 -> "Friday";
            case 7 -> "Saturday";
            default -> "Unknown";
        };
    }

    private record WeekWindow(LocalDateTime start, LocalDateTime endExclusive) {}
}