package gg.modl.backend.player.controller;

import gg.modl.backend.infrastructure.exception.ResourceNotFoundException;
import gg.modl.backend.infrastructure.exception.ValidationException;
import gg.modl.backend.infrastructure.proto.ProtoMapperSupport;
import gg.modl.backend.infrastructure.proto.ProtobufMediaTypes;
import gg.modl.backend.infrastructure.rest.RESTMappingV3;
import gg.modl.backend.infrastructure.rest.RequestUtil;
import gg.modl.backend.infrastructure.validation.RequestValidationLimits;
import gg.modl.backend.player.dto.response.*;
import gg.modl.backend.player.service.MinecraftPlayerService;
import gg.modl.backend.player.service.PlayerLookupService;
import gg.modl.backend.server.data.Server;
import gg.modl.proto.modl.v1.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(RESTMappingV3.PREFIX_MINECRAFT + "/players")
@RequiredArgsConstructor
public class MinecraftPlayerV3Controller {
    private final MinecraftPlayerService minecraftPlayerService;
    private final PlayerLookupService playerLookupService;

    private static boolean shouldQueryMojang(PlayerLookupRequest request) {
        return !request.hasQueryMojang() || request.getQueryMojang();
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }

    @PostMapping(
            value = "/login",
            consumes = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE,
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<PlayerLoginResponse> login(
            @RequestBody @Valid PlayerLoginRequest request,
            HttpServletRequest httpRequest
    ) {
        Server server = RequestUtil.getRequestServer(httpRequest);
        Map<String, Object> ipInfo = request.hasIpInfo()
                ? ProtoMapperSupport.structToMap(request.getIpInfo())
                : null;

        PlayerLoginResult result = minecraftPlayerService.login(
                server,
                UUID.fromString(request.getMinecraftUuid()),
                request.getUsername(),
                request.hasIpAddress() ? request.getIpAddress() : null,
                ipInfo,
                request.hasSkinHash() ? request.getSkinHash() : null,
                request.hasServerName() ? request.getServerName() : null
        );

        return ResponseEntity.status(result.status())
                .body(MinecraftPlayerProtoMapper.toPlayerLoginResponse(result));
    }

    @PostMapping(
            value = "/disconnect",
            consumes = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE,
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<SimpleResponse> disconnect(
            @RequestBody @Valid PlayerDisconnectRequest request,
            HttpServletRequest httpRequest
    ) {
        Server server = RequestUtil.getRequestServer(httpRequest);
        boolean success = minecraftPlayerService.disconnect(
                server,
                request.getMinecraftUuid(),
                request.getSessionDurationMs()
        ).success();

        return ResponseEntity.ok(MinecraftPlayerProtoMapper.toSimpleResponse(success));
    }

    @PostMapping(
            value = "/update-server",
            consumes = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE,
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<SimpleResponse> updateServer(
            @RequestBody @Valid UpdatePlayerServerRequest request,
            HttpServletRequest httpRequest
    ) {
        Server server = RequestUtil.getRequestServer(httpRequest);
        boolean success = minecraftPlayerService.updateServer(
                server,
                request.getMinecraftUuid(),
                request.getServerName()
        ).success();

        return ResponseEntity.ok(MinecraftPlayerProtoMapper.toSimpleResponse(success));
    }

    @PostMapping(
            value = "/submit-ip-info",
            consumes = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE,
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<SimpleResponse> submitIpInfo(
            @RequestBody @Valid SubmitPlayerIpInfoRequest request,
            HttpServletRequest httpRequest
    ) {
        Server server = RequestUtil.getRequestServer(httpRequest);
        boolean success = minecraftPlayerService.submitIpInfo(
                server,
                request.getMinecraftUuid(),
                request.getIp(),
                request.getCountry(),
                request.getRegion(),
                request.getAsn(),
                request.getProxy(),
                request.getHosting()
        ).success();

        return ResponseEntity.ok(MinecraftPlayerProtoMapper.toSimpleResponse(success));
    }

    @GetMapping(
            value = "/online",
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<OnlinePlayersResponse> getOnlinePlayers(HttpServletRequest httpRequest) {
        Server server = RequestUtil.getRequestServer(httpRequest);
        return ResponseEntity.ok(MinecraftPlayerProtoMapper.toOnlinePlayersResponse(minecraftPlayerService.getOnlinePlayers(server)));
    }

    @GetMapping(
            value = "/{uuid}",
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<PlayerProfileResponse> getPlayerByUuid(
            @PathVariable String uuid,
            @RequestParam(required = false) Integer punishmentLimit,
            @RequestParam(required = false) Integer noteLimit,
            HttpServletRequest httpRequest
    ) {
        Server server = RequestUtil.getRequestServer(httpRequest);
        return switch (playerLookupService.getPlayerByUuid(server, uuid, punishmentLimit, noteLimit)) {
            case PlayerProfileResult.Found found ->
                    ResponseEntity.ok(MinecraftPlayerProtoMapper.toPlayerProfileResponse(found.profile()));
            case PlayerProfileResult.NotFound notFound -> throw new ResourceNotFoundException(notFound.message());
        };
    }

    @GetMapping(
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<PlayerGetResponse> getPlayerByQuery(
            @RequestParam(required = false) String minecraftUuid,
            @RequestParam(defaultValue = "true") boolean queryMojang,
            HttpServletRequest httpRequest
    ) {
        requireNotBlank(minecraftUuid, "minecraftUuid parameter required");
        Server server = RequestUtil.getRequestServer(httpRequest);
        return switch (playerLookupService.getPlayerByMinecraftUuid(server, minecraftUuid, queryMojang)) {
            case PlayerFetchResult.Found found ->
                    ResponseEntity.ok(MinecraftPlayerProtoMapper.toPlayerGetResponse(found));
            case PlayerFetchResult.NotFound notFound -> throw new ResourceNotFoundException(notFound.message());
            case PlayerFetchResult.InvalidRequest invalid -> throw new ValidationException(invalid.message());
        };
    }

    @GetMapping(
            value = "/by-name",
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<PlayerNameResponse> getPlayerByUsername(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "true") boolean queryMojang,
            HttpServletRequest httpRequest
    ) {
        requireNotBlank(username, "username parameter required");
        Server server = RequestUtil.getRequestServer(httpRequest);
        return switch (playerLookupService.getPlayerByUsername(server, username, queryMojang)) {
            case PlayerFetchResult.Found found ->
                    ResponseEntity.ok(MinecraftPlayerProtoMapper.toPlayerNameResponse(found));
            case PlayerFetchResult.NotFound notFound -> throw new ResourceNotFoundException(notFound.message());
            case PlayerFetchResult.InvalidRequest invalid -> throw new ValidationException(invalid.message());
        };
    }

    @PostMapping(
            value = "/lookup",
            consumes = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE,
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<PlayerLookupResponse> lookupPlayer(
            @RequestBody @Valid PlayerLookupRequest request,
            HttpServletRequest httpRequest
    ) {
        requireNotBlank(request.getQuery(), "query is required");
        Server server = RequestUtil.getRequestServer(httpRequest);
        return switch (playerLookupService.lookupPlayer(server, request.getQuery(), shouldQueryMojang(request))) {
            case PlayerLookupResult.Found found ->
                    ResponseEntity.ok(MinecraftPlayerProtoMapper.toPlayerLookupResponse(found));
            case PlayerLookupResult.NotFound notFound -> throw new ResourceNotFoundException(notFound.message());
        };
    }

    @PostMapping(
            value = "/lookup-profile",
            consumes = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE,
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<PlayerProfileResponse> lookupProfile(
            @RequestBody @Valid PlayerLookupRequest request,
            @RequestParam(required = false) Integer punishmentLimit,
            @RequestParam(required = false) Integer noteLimit,
            HttpServletRequest httpRequest
    ) {
        requireNotBlank(request.getQuery(), "query is required");
        Server server = RequestUtil.getRequestServer(httpRequest);
        return switch (playerLookupService.lookupProfile(
                server,
                request.getQuery(),
                shouldQueryMojang(request),
                punishmentLimit,
                noteLimit
        )) {
            case PlayerProfileResult.Found found ->
                    ResponseEntity.ok(MinecraftPlayerProtoMapper.toPlayerProfileResponse(found.profile()));
            case PlayerProfileResult.NotFound notFound -> throw new ResourceNotFoundException(notFound.message());
        };
    }

    @PostMapping(
            value = "/{uuid}/notes",
            consumes = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE,
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<PlayerNoteCreateResponse> createPlayerNote(
            @PathVariable String uuid,
            @RequestBody @Valid CreatePlayerNoteRequest request,
            HttpServletRequest httpRequest
    ) {
        Server server = RequestUtil.getRequestServer(httpRequest);
        CreateNoteResult result = minecraftPlayerService.createNote(
                server,
                uuid,
                request.getText(),
                request.hasIssuerName() ? request.getIssuerName() : null,
                request.hasIssuerId() ? request.getIssuerId() : null
        );
        PlayerNoteCreateResponse response = MinecraftPlayerProtoMapper.toPlayerNoteCreateResponse(result);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping(
            value = "/{uuid}/linked-accounts",
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<LinkedAccountsResponse> getLinkedAccounts(
            @PathVariable String uuid,
            @RequestParam(required = false) @Min(RequestValidationLimits.PAGINATION_PAGE_MIN) Integer page,
            @RequestParam(required = false) @Min(RequestValidationLimits.PAGINATION_LIMIT_MIN) @Max(RequestValidationLimits.PAGINATION_LIMIT_MAX) Integer limit,
            HttpServletRequest httpRequest
    ) {
        Server server = RequestUtil.getRequestServer(httpRequest);
        return switch (playerLookupService.getLinkedAccounts(server, uuid, page, limit)) {
            case LinkedAccountsResult.Found found ->
                    ResponseEntity.ok(MinecraftPlayerProtoMapper.toLinkedAccountsResponse(found));
            case LinkedAccountsResult.NotFound notFound -> throw new ResourceNotFoundException(notFound.message());
        };
    }

    @GetMapping(
            value = "/{uuid}/punishments",
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<PaginatedPunishmentsResponse> getPlayerPunishments(
            @PathVariable String uuid,
            @RequestParam(defaultValue = "1") @Min(RequestValidationLimits.PAGINATION_PAGE_MIN) int page,
            @RequestParam(defaultValue = "7") @Min(RequestValidationLimits.PAGINATION_LIMIT_MIN) @Max(RequestValidationLimits.PAGINATION_LIMIT_MAX) int limit,
            HttpServletRequest httpRequest
    ) {
        Server server = RequestUtil.getRequestServer(httpRequest);
        return switch (minecraftPlayerService.getPlayerPunishments(server, uuid, page, limit)) {
            case PaginatedPunishmentsResult.Found found ->
                    ResponseEntity.ok(MinecraftPlayerProtoMapper.toPaginatedPunishmentsResponse(found));
            case PaginatedPunishmentsResult.NotFound notFound ->
                    throw new ResourceNotFoundException(notFound.message());
        };
    }

    @GetMapping(
            value = "/{uuid}/notes",
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<PaginatedNotesResponse> getPlayerNotes(
            @PathVariable String uuid,
            @RequestParam(defaultValue = "1") @Min(RequestValidationLimits.PAGINATION_PAGE_MIN) int page,
            @RequestParam(defaultValue = "7") @Min(RequestValidationLimits.PAGINATION_LIMIT_MIN) @Max(RequestValidationLimits.PAGINATION_LIMIT_MAX) int limit,
            HttpServletRequest httpRequest
    ) {
        Server server = RequestUtil.getRequestServer(httpRequest);
        return switch (minecraftPlayerService.getPlayerNotes(server, uuid, page, limit)) {
            case PaginatedNotesResult.Found found ->
                    ResponseEntity.ok(MinecraftPlayerProtoMapper.toPaginatedNotesResponse(found));
            case PaginatedNotesResult.NotFound notFound -> throw new ResourceNotFoundException(notFound.message());
        };
    }

    @GetMapping(
            value = "/{uuid}/reports",
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<ReportsResponse> getPlayerReports(
            @PathVariable String uuid,
            HttpServletRequest httpRequest
    ) {
        Server server = RequestUtil.getRequestServer(httpRequest);
        return ResponseEntity.ok(MinecraftPlayerProtoMapper.toReportsResponse(minecraftPlayerService.getPlayerReports(server, uuid)));
    }

    @PostMapping(
            value = "/pardon",
            consumes = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE,
            produces = ProtobufMediaTypes.APPLICATION_X_PROTOBUF_VALUE
    )
    public ResponseEntity<PardonResponse> pardonPlayer(
            @RequestBody @Valid PardonPlayerRequest request,
            HttpServletRequest httpRequest
    ) {
        Server server = RequestUtil.getRequestServer(httpRequest);
        PardonResponse response = MinecraftPlayerProtoMapper.toPardonResponse(minecraftPlayerService.pardonPlayer(
                server,
                request.getPlayerName(),
                request.hasPunishmentType() ? request.getPunishmentType() : null,
                request.hasIssuerName() ? request.getIssuerName() : null,
                request.hasIssuerId() ? request.getIssuerId() : null,
                request.hasReason() ? request.getReason() : null
        ));
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
