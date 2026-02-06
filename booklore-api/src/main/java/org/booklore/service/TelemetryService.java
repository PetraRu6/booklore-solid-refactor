@Service
@AllArgsConstructor
public class TelemetryService {

    private final VersionService versionService;
    private final InstallationService installationService;

    private final LibraryRepository libraryRepository; // само ако ти трябва totalLibraries като count
    private final BookAdditionalFileRepository bookAdditionalFileRepository;
    private final AuthorRepository authorRepository;
    private final BookMarkRepository bookMarkRepository;
    private final BookNoteRepository bookNoteRepository;
    private final ShelfRepository shelfRepository;
    private final MagicShelfRepository magicShelfRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final MoodRepository moodRepository;
    private final KoreaderUserRepository koreaderUserRepository;
    private final OpdsUserV2Repository opdsUserV2Repository;
    private final EmailProviderV2Repository emailProviderV2Repository;
    private final EmailRecipientV2Repository emailRecipientV2Repository;
    private final KoboUserSettingsRepository koboUserSettingsRepository;
    private final UserSettingRepository userSettingRepository;

    // collectors
    private final UserTelemetryCollector userTelemetryCollector;
    private final BookTelemetryCollector bookTelemetryCollector;
    private final LibraryTelemetryCollector libraryTelemetryCollector;
    // (след това: MetadataTelemetryCollector, KoboTelemetryCollector, EmailTelemetryCollector...)

    public BookloreTelemetry collectTelemetry() {
        var installation = installationService.getOrCreateInstallation();

        var userStats = userTelemetryCollector.collect();
        var bookStats = bookTelemetryCollector.collect();
        var libraryStats = libraryTelemetryCollector.collect();

        return BookloreTelemetry.builder()
                .telemetryVersion(2)
                .installationId(installation.getId())
                .installationDate(installation.getDate() != null ? installation.getDate().toString() : null)
                .appVersion(versionService.appVersion)

                .totalLibraries((int) libraryRepository.count())
                .totalAdditionalBookFiles(bookAdditionalFileRepository.count())
                .totalAuthors(authorRepository.count())
                .totalBookmarks(bookMarkRepository.count())
                .totalBookNotes(bookNoteRepository.count())
                .totalShelves((int) shelfRepository.count())
                .totalMagicShelves((int) magicShelfRepository.count())
                .totalCategories((int) categoryRepository.count())
                .totalTags((int) tagRepository.count())
                .totalMoods((int) moodRepository.count())
                .totalKoreaderUsers((int) koreaderUserRepository.count())

                .userStatistics(userStats)
                .bookStatistics(bookStats)
                .libraryStatisticsList(libraryStats)

                .opdsStatistics(BookloreTelemetry.OpdsStatistics.builder()
                        .totalOpdsUsers((int) opdsUserV2Repository.count())
                        .build())
                .emailStatistics(BookloreTelemetry.EmailStatistics.builder()
                        .totalEmailProviders((int) emailProviderV2Repository.count())
                        .totalEmailRecipients((int) emailRecipientV2Repository.count())
                        .build())
                .koboStatistics(BookloreTelemetry.KoboStatistics.builder()
                        .totalKoboUsers((int) koboUserSettingsRepository.count())
                        .totalHardcoverSyncEnabled((int) userSettingRepository.countBySettingKeyAndSettingValue(
                                UserSettingKey.HARDCOVER_SYNC_ENABLED.getDbKey(), "true"))
                        .totalAutoAddToShelf((int) koboUserSettingsRepository.countByAutoAddToShelfTrue())
                        .build())
                .build();
    }
}