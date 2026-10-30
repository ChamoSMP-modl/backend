package gg.modl.backend.storage.dto.response;

import java.util.Map;

public record StorageQuotaResponse(
        long usedBytes,
        long maxBytes,
        double usedPercentage,
        String usedFormatted,
        String maxFormatted,
        Map<String, Long> byType,
        boolean isPremium,
        double storageOverageRate
) {
}
