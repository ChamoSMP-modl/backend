package gg.modl.backend.beta;

import gg.modl.backend.infrastructure.filter.AdminAuthFilter;
import gg.modl.backend.infrastructure.rest.RESTMappingV1;
import gg.modl.proto.modl.v1.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(RESTMappingV1.ADMIN_BETA_TESTERS)
@RequiredArgsConstructor
@Profile("staging")
public class AdminBetaTesterController {
    private static final int DEFAULT_AUDIT_LIMIT = 50;

    private final AdminBetaTesterService betaTesterService;

    @GetMapping
    public ResponseEntity<BetaTesterListResponse> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(BetaProtoMapper.toListResponse(betaTesterService.list(page, limit, search)));
    }

    @PostMapping
    public ResponseEntity<BetaTesterRecord> create(
            @RequestBody BetaTesterCreateRequest request,
            HttpServletRequest httpRequest) {
        BetaTesterDetails created = betaTesterService.create(
                BetaProtoMapper.fromCreateRequest(request), actingAdminEmail(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(BetaProtoMapper.toRecord(created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BetaTesterRecord> revoke(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(BetaProtoMapper.toRecord(betaTesterService.revoke(id, actingAdminEmail(httpRequest))));
    }

    @PostMapping("/{id}/reset")
    public ResponseEntity<BetaTesterResetResponse> reset(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(BetaProtoMapper.toResetResponse(betaTesterService.reset(id, actingAdminEmail(httpRequest))));
    }

    @PostMapping("/reset-all")
    public CompletableFuture<ResponseEntity<BetaTesterResetAllResponse>> resetAll(HttpServletRequest httpRequest) {
        return betaTesterService.resetAll(actingAdminEmail(httpRequest))
                .thenApply(results -> ResponseEntity.ok(BetaProtoMapper.toResetAllResponse(results)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BetaTesterRecord> get(@PathVariable String id) {
        return ResponseEntity.ok(BetaProtoMapper.toRecord(betaTesterService.get(id)));
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<BetaAuditResponse> audit(
            @PathVariable String id,
            @RequestParam(defaultValue = "" + DEFAULT_AUDIT_LIMIT) int limit) {
        return ResponseEntity.ok(BetaProtoMapper.toAuditResponse(betaTesterService.audit(id, limit)));
    }

    private String actingAdminEmail(HttpServletRequest request) {
        return AdminAuthFilter.actingEmail(request)
                .orElseThrow(() -> new BetaRequestException("Admin authentication required.", HttpStatus.UNAUTHORIZED));
    }
}
