package gg.modl.backend.storage.controller;

import gg.modl.backend.infrastructure.authorization.PanelAccessRule;
import gg.modl.backend.infrastructure.authorization.RequiresPanelPermission;
import gg.modl.backend.infrastructure.exception.ValidationException;
import gg.modl.backend.infrastructure.rest.RESTMappingV1;
import gg.modl.backend.infrastructure.rest.RequestUtil;
import gg.modl.backend.infrastructure.validation.RequestValidationLimits;
import gg.modl.backend.replay.service.ReplayDeletionService;
import gg.modl.backend.server.data.Server;
import gg.modl.backend.storage.dto.response.StorageFileResponse;
import gg.modl.backend.storage.service.*;
import gg.modl.proto.modl.v1.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(RESTMappingV1.PANEL_STORAGE)
@RequiresPanelPermission(view = "admin.settings.view.storage", modify = "admin.settings.modify.storage")
@RequiredArgsConstructor
public class PanelStorageController {
    private final S3StorageService s3StorageService;
    private final StorageQuotaService quotaService;
    private final StorageMetadataService storageMetadataService;
    private final StorageSyncService storageSyncService;
    private final MediaValidationService validationService;
    private final ReplayDeletionService replayDeletionService;

    @GetMapping("/quota")
    public ResponseEntity<StorageQuotaResponse> getQuota(HttpServletRequest request) {
        Server server = RequestUtil.getRequestServer(request);
        return ResponseEntity.ok(StorageProtoMapper.toStorageQuotaResponse(quotaService.getQuota(server)));
    }

    @GetMapping("/files")
    public ResponseEntity<StorageFilesResponse> getFiles(
            @RequestParam(required = false) String prefix,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        List<StorageFileResponse> files = storageMetadataService.listFiles(server, prefix);
        return ResponseEntity.ok(StorageProtoMapper.toStorageFilesResponse(files));
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<StorageBulkDeleteResponse> bulkDelete(
            @RequestBody BulkDeleteRequest body,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        List<String> keys = body.getKeysList();

        if (keys.size() > RequestValidationLimits.STORAGE_BULK_DELETE_MAX_KEYS) {
            throw new ValidationException("Too many keys in bulk delete request. Maximum is " + RequestValidationLimits.STORAGE_BULK_DELETE_MAX_KEYS);
        }
        for (String key : keys) {
            validationService.assertKeyOwnedByServer(server, key);
        }

        int deleted = s3StorageService.bulkDelete(keys);
        storageMetadataService.removeFiles(server, keys);
        replayDeletionService.reconcileDeletedStorageKeys(server, keys);
        return ResponseEntity.ok(StorageProtoMapper.toStorageBulkDeleteResponse(deleted));
    }

    @PostMapping("/sync")
    @RequiresPanelPermission(rule = PanelAccessRule.SUPER_ADMIN)
    public ResponseEntity<StorageSyncResponse> syncFiles(HttpServletRequest request) {
        Server server = RequestUtil.getRequestServer(request);
        int synced = storageSyncService.syncServerFiles(server, true);
        return ResponseEntity.ok(StorageProtoMapper.toStorageSyncResponse(synced));
    }

    @GetMapping("/download/{*key}")
    public ResponseEntity<?> getDownloadUrl(
            @PathVariable String key,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);

        String normalizedKey = StorageKeyUtils.stripLeadingSlash(key);
        validationService.assertKeyOwnedByServer(server, normalizedKey);

        String url = s3StorageService.getPresignedUrl(normalizedKey);
        if (url == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(StorageProtoMapper.toStorageDownloadUrlResponse(url));
    }
}
