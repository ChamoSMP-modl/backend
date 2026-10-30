package gg.modl.backend.storage.service;

import gg.modl.backend.infrastructure.util.ByteFormatUtil;
import gg.modl.backend.limits.ServerLimitPolicy;
import gg.modl.backend.server.data.Server;
import gg.modl.backend.storage.dto.response.StorageQuotaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.OptionalLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageQuotaService {
    private static final long BYTES_PER_GB = 1024L * 1024 * 1024;
    public static final long PREMIUM_BASE_BYTES = 200L * BYTES_PER_GB;
    private static final double STORAGE_OVERAGE_RATE = 0.08;
    private final StorageMetadataService storageMetadataService;
    private final StorageUsageAccountant storageUsageAccountant;
    private final StorageSyncService storageSyncService;
    private final ServerLimitPolicy serverLimitPolicy;

    public boolean canUpload(Server server, long fileSize) {
        if (fileSize < 0) {
            return false;
        }
        long used = currentTrackedUsage(server);
        long max = getMaxBytesForServer(server);
        return used + fileSize <= max;
    }

    private long currentTrackedUsage(Server server) {
        OptionalLong tracked = storageUsageAccountant.trackedBytes(server);
        if (tracked.isPresent()) {
            return tracked.getAsLong();
        }
        storageSyncService.triggerAsyncSync(server);
        return 0L;
    }

    public ConfirmResult confirmAndRecordFile(Server server, String key, long size, String contentType) {
        if (size < 0) {
            return ConfirmResult.RECORD_FAILED;
        }
        if (storageMetadataService.hasFile(server, key)) {
            return ConfirmResult.SUCCESS;
        }

        long maxBytes = getMaxBytesForServer(server);
        if (!storageUsageAccountant.tryReserveWithinLimit(server, size, maxBytes)) {
            return ConfirmResult.QUOTA_EXCEEDED;
        }

        StorageMetadataService.RecordFileResult recordResult =
                storageMetadataService.recordReservedFile(server, key, size, contentType);
        if (recordResult == StorageMetadataService.RecordFileResult.INSERTED) {
            return ConfirmResult.SUCCESS;
        }

        storageUsageAccountant.recordRemoval(server, size);
        return recordResult == StorageMetadataService.RecordFileResult.ALREADY_EXISTS
                ? ConfirmResult.SUCCESS
                : ConfirmResult.RECORD_FAILED;
    }

    public StorageQuotaResponse getQuota(Server server) {
        Map<String, Long> byType = storageMetadataService.calculateStorageByType(server);
        long usedBytes = byType.values()
                .stream().mapToLong(Long::longValue).sum();
        long maxBytes = getMaxBytesForServer(server);

        double usedPercentage = maxBytes > 0 ? (double) usedBytes / maxBytes * 100 : 0;

        boolean isPremium = true;

        return new StorageQuotaResponse(
                usedBytes,
                maxBytes,
                Math.round(usedPercentage * 100) / 100.0,
                ByteFormatUtil.format(usedBytes),
                ByteFormatUtil.format(maxBytes),
                byType,
                isPremium,
                STORAGE_OVERAGE_RATE
        );
    }

    private long getMaxBytesForServer(Server server) {
        return serverLimitPolicy.resolve(server).getMaxStorageBytes();
    }

    public enum ConfirmResult {
        SUCCESS,
        QUOTA_EXCEEDED,
        RECORD_FAILED
    }

}
