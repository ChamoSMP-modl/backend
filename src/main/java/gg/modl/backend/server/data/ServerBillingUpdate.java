package gg.modl.backend.server.data;

public interface ServerBillingUpdate {

    void setMaxStorageLimitBytes(Long maxStorageLimitBytes);

    void setMaxAiOverageRequests(Long maxAiOverageRequests);

    void setMigrationFileSizeLimit(Long migrationFileSizeLimit);
}
