package gg.modl.backend.admin.controller;

import gg.modl.backend.admin.data.SystemLog;
import gg.modl.backend.admin.dto.request.CreateSystemLogRequest;
import gg.modl.backend.admin.dto.request.ResolveLogRequest;
import gg.modl.backend.admin.dto.response.*;
import gg.modl.proto.modl.v1.*;

import java.util.Map;

import static gg.modl.backend.infrastructure.proto.ProtoMapperSupport.*;

final class AdminMonitoringProtoMapper {

    private AdminMonitoringProtoMapper() {
    }

    static CreateSystemLogRequest fromCreateLog(gg.modl.proto.modl.v1.CreateSystemLogRequest request) {
        Map<String, Object> metadata = request.hasMetadata()
                ? structToMap(request.getMetadata())
                : null;
        return new CreateSystemLogRequest(
                request.getLevel(),
                request.getMessage(),
                request.getSource(),
                request.getCategory(),
                request.getServerId(),
                metadata
        );
    }

    static ResolveLogRequest fromResolveLog(gg.modl.proto.modl.v1.ResolveLogRequest request) {
        return new ResolveLogRequest(request.hasResolvedBy() ? request.getResolvedBy() : null);
    }

    static AdminMonitoringDashboardResponse toDashboardResponse(AdminMonitoringDashboard response) {
        AdminMonitoringDashboard.ServerMetrics servers = response.servers();
        AdminMonitoringDashboard.LogMetrics logs = response.logs();
        AdminMonitoringDashboard.SystemHealth systemHealth = response.systemHealth();

        AdminMonitoringDashboardData.Builder builder = AdminMonitoringDashboardData.newBuilder()
                .setServers(AdminMonitoringServerMetrics.newBuilder()
                        .setTotal(servers.total())
                        .setActive(servers.active())
                        .setPending(servers.pending())
                        .setFailed(servers.failed())
                        .setRecentRegistrations(servers.recentRegistrations())
                        .setConcurrentServers(servers.concurrentServers())
                        .setConcurrentPlayers(servers.concurrentPlayers())
                        .build())
                .setLogs(AdminMonitoringLogMetrics.newBuilder()
                        .setLast24H(AdminMonitoringLogWindow.newBuilder()
                                .setTotal(logs.last24h().total())
                                .setCritical(logs.last24h().critical())
                                .setError(logs.last24h().error())
                                .setWarning(logs.last24h().warning())
                                .build())
                        .setUnresolved(AdminMonitoringUnresolvedLogs.newBuilder()
                                .setCritical(logs.unresolved().critical())
                                .setError(logs.unresolved().error())
                                .build())
                        .build())
                .setSystemHealth(AdminMonitoringSystemHealthSummary.newBuilder()
                        .setScore(systemHealth.score())
                        .setStatus(systemHealth.status())
                        .build());
        response.trends().forEach(trend -> builder.addTrends(toStruct(trend)));
        if (response.lastUpdated() != null) {
            builder.setLastUpdated(toTimestamp(response.lastUpdated()));
        }
        return AdminMonitoringDashboardResponse.newBuilder()
                .setSuccess(true)
                .setData(builder.build())
                .build();
    }

    static AdminMonitoringLogsResponse toLogsResponse(AdminMonitoringLogs response) {
        AdminMonitoringLogsData.Builder dataBuilder = AdminMonitoringLogsData.newBuilder()
                .setPagination(toPagination(response.pagination()))
                .setFilters(toLogFilters(response.filters()));
        response.logs().forEach(systemLog -> dataBuilder.addLogs(toSystemLogResponse(systemLog)));
        return AdminMonitoringLogsResponse.newBuilder()
                .setSuccess(true)
                .setData(dataBuilder.build())
                .build();
    }

    static AdminMonitoringSystemLogMutationResponse toSystemLogMutationResponse(SystemLog systemLog, String message) {
        AdminMonitoringSystemLogMutationResponse.Builder builder = AdminMonitoringSystemLogMutationResponse.newBuilder()
                .setSuccess(true)
                .setData(toSystemLogResponse(systemLog));
        if (message != null) {
            builder.setMessage(message);
        }
        return builder.build();
    }

    static AdminMonitoringSourcesResponse toSourcesResponse(AdminMonitoringSources response) {
        AdminMonitoringSourcesData dataBuilder = AdminMonitoringSourcesData.newBuilder()
                .addAllSources(response.sources())
                .addAllCategories(response.categories())
                .build();
        return AdminMonitoringSourcesResponse.newBuilder()
                .setSuccess(true)
                .setData(dataBuilder)
                .build();
    }

    static AdminMonitoringHealthResponse toHealthResponse(AdminMonitoringHealth response) {
        AdminMonitoringHealthData.Builder dataBuilder = AdminMonitoringHealthData.newBuilder()
                .setStatus(stringValue(response.status()));
        response.checks().forEach(check -> dataBuilder.addChecks(toHealthCheck(check)));
        if (response.timestamp() != null) {
            dataBuilder.setTimestamp(toTimestamp(response.timestamp()));
        }
        return AdminMonitoringHealthResponse.newBuilder()
                .setSuccess(true)
                .setData(dataBuilder.build())
                .build();
    }

    static AdminMonitoringDeleteLogsResponse toDeleteLogsResponse(long deletedCount, String message) {
        AdminMonitoringDeleteLogsResponse.Builder builder = AdminMonitoringDeleteLogsResponse.newBuilder()
                .setSuccess(true)
                .setData(AdminMonitoringDeletedCount.newBuilder().setDeletedCount(deletedCount).build());
        if (message != null) {
            builder.setMessage(message);
        }
        return builder.build();
    }

    static AdminMonitoringPm2StatusResponse toPm2StatusResponse() {
        return AdminMonitoringPm2StatusResponse.newBuilder()
                .setSuccess(true)
                .setData(AdminMonitoringPm2Status.newBuilder()
                        .setIsEnabled(false)
                        .setIsStreaming(false)
                        .setReconnectAttempts(0)
                        .setRecentLogsCount(0)
                        .build())
                .build();
    }

    static AdminMonitoringPm2RestartResponse toPm2RestartResponse(String message) {
        AdminMonitoringPm2RestartResponse.Builder builder = AdminMonitoringPm2RestartResponse.newBuilder()
                .setSuccess(true);
        if (message != null) {
            builder.setMessage(message);
        }
        return builder.build();
    }

    static AdminMonitoringPm2ToggleResponse toPm2ToggleResponse(boolean enabled, String message) {
        AdminMonitoringPm2ToggleResponse.Builder builder = AdminMonitoringPm2ToggleResponse.newBuilder()
                .setSuccess(true)
                .setData(AdminMonitoringPm2ToggleData.newBuilder()
                        .setIsEnabled(enabled)
                        .setIsStreaming(enabled)
                        .build());
        if (message != null) {
            builder.setMessage(message);
        }
        return builder.build();
    }

    private static AdminMonitoringPagination toPagination(AdminPagination pagination) {
        return AdminMonitoringPagination.newBuilder()
                .setPage(pagination.page())
                .setLimit(pagination.limit())
                .setTotal(pagination.total())
                .setPages(pagination.pages())
                .build();
    }

    private static AdminMonitoringLogFilters toLogFilters(AdminMonitoringLogs.Filters filters) {
        AdminMonitoringLogFilters.Builder builder = AdminMonitoringLogFilters.newBuilder();
        setOptionalString(builder::setLevel, filters.level());
        setOptionalString(builder::setSource, filters.source());
        setOptionalString(builder::setServerId, filters.serverId());
        setOptionalString(builder::setCategory, filters.category());
        setOptionalString(builder::setResolved, filters.resolved());
        setOptionalString(builder::setSearch, filters.search());
        return builder.build();
    }

    private static AdminMonitoringHealthCheck toHealthCheck(AdminMonitoringHealth.HealthCheck check) {
        AdminMonitoringHealthCheck.Builder builder = AdminMonitoringHealthCheck.newBuilder()
                .setName(stringValue(check.name()))
                .setStatus(stringValue(check.status()));
        if (check.message() != null) {
            builder.setMessage(check.message());
        }
        if (check.responseTime() != null) {
            builder.setResponseTime(check.responseTime());
        }
        if (check.error() != null) {
            builder.setError(check.error());
        }
        if (check.count() != null) {
            builder.setCount(check.count());
        }
        return builder.build();
    }

    private static SystemLogResponse toSystemLogResponse(SystemLog systemLog) {
        SystemLogResponse.Builder builder = SystemLogResponse.newBuilder()
                .setId(stringValue(systemLog.getId()))
                .setMessage(stringValue(systemLog.getMessage()))
                .setLevel(stringValue(systemLog.getLevel()))
                .setSource(stringValue(systemLog.getSource()))
                .setResolved(systemLog.isResolved());
        if (systemLog.getTimestamp() != null) {
            builder.setTimestamp(toTimestamp(systemLog.getTimestamp()));
        }
        if (systemLog.getCategory() != null) {
            builder.setCategory(systemLog.getCategory());
        }
        if (systemLog.getServerId() != null) {
            builder.setServerId(systemLog.getServerId());
        }
        if (systemLog.getMetadata() != null) {
            builder.setMetadata(toStruct(systemLog.getMetadata()));
        }
        if (systemLog.getResolvedBy() != null) {
            builder.setResolvedBy(systemLog.getResolvedBy());
        }
        if (systemLog.getResolvedAt() != null) {
            builder.setResolvedAt(toTimestamp(systemLog.getResolvedAt()));
        }
        return builder.build();
    }
}
