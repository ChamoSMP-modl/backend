package gg.modl.backend.admin.service;

import gg.modl.backend.admin.dto.response.AdminAnalyticsUsage;
import gg.modl.backend.database.mongo.repository.*;
import gg.modl.backend.infrastructure.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAnalyticsServiceTest {

    @Mock
    private ServerMetricsRepository serverMetricsRepository;

    @Mock
    private ServerUsageRepository serverUsageRepository;

    @Mock
    private MetricSnapshotMongoRepository metricSnapshotRepository;

    @Mock
    private ServerInstanceSnapshotMongoRepository serverInstanceSnapshotRepository;

    @Mock
    private GlobalMongoAdminRepository globalMongoAdminRepository;

    @Mock
    private AdminServerService adminServerService;

    private AdminAnalyticsService adminAnalyticsService;

    @BeforeEach
    void setUp() {
        adminAnalyticsService = new AdminAnalyticsService(
                serverMetricsRepository,
                serverUsageRepository,
                metricSnapshotRepository,
                serverInstanceSnapshotRepository,
                globalMongoAdminRepository,
                adminServerService
        );
    }

    @Test
    void getHistoricalRejectsUnknownMetric() {
        assertThrows(ValidationException.class, () -> adminAnalyticsService.getHistorical("unknown", "30d"));
    }

    @Test
    void getUsageReadsStorageStatsFromMongo() {
        when(serverMetricsRepository.countActiveSince(any())).thenReturn(2L);
        when(globalMongoAdminRepository.getStorageSize()).thenReturn(2048L);

        AdminAnalyticsUsage usage = adminAnalyticsService.getUsage();

        assertEquals(2048L, usage.storage());
        assertEquals(0.0, usage.storagePercent());
    }
}
