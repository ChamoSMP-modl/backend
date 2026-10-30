package gg.modl.backend.settings.controller;

import gg.modl.backend.infrastructure.authorization.RequiresPanelPermission;
import gg.modl.backend.infrastructure.exception.ValidationException;
import gg.modl.backend.infrastructure.rest.RESTMappingV1;
import gg.modl.backend.infrastructure.rest.RequestUtil;
import gg.modl.backend.infrastructure.validation.BeanValidationRunner;
import gg.modl.backend.role.service.PermissionService;
import gg.modl.backend.server.data.Server;
import gg.modl.backend.settings.data.AIModerationSettings;
import gg.modl.backend.settings.data.GeneralSettings;
import gg.modl.backend.settings.data.OffenderThresholdSettings;
import gg.modl.backend.settings.data.QuickResponseSettings;
import gg.modl.backend.settings.data.ReplayRetentionSettings;
import gg.modl.backend.settings.data.TicketFormSettings;
import gg.modl.backend.settings.data.TicketLabelSettings;
import gg.modl.backend.settings.data.WebhookSettings;
import gg.modl.backend.settings.dto.request.PatchReplayRetentionSettingsRequest;
import gg.modl.backend.settings.dto.request.UpdateQuickResponsesRequest;
import gg.modl.backend.settings.service.*;
import gg.modl.proto.modl.v1.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(RESTMappingV1.PANEL_SETTINGS)
@RequiresPanelPermission(view = PermissionService.ADMIN_SETTINGS_VIEW, modify = "admin.settings.modify")
@RequiredArgsConstructor
public class PanelSettingsController {
    private final GeneralSettingsService generalSettingsService;
    private final TicketLabelSettingsService ticketLabelSettingsService;
    private final AIModerationSettingsService aiModerationSettingsService;
    private final WebhookSettingsService webhookSettingsService;
    private final TicketFormSettingsService ticketFormSettingsService;
    private final QuickResponseSettingsService quickResponseSettingsService;
    private final IconUploadService iconUploadService;
    private final OffenderThresholdSettingsService offenderThresholdSettingsService;
    private final ReplayRetentionSettingsService replayRetentionSettingsService;
    private final SettingsInvalidationPublisher settingsInvalidationPublisher;
    private final BeanValidationRunner validationRunner;

    @GetMapping("/general")
    public GeneralSettingsEnvelope getGeneralSettings(HttpServletRequest request) {
        Server server = RequestUtil.getRequestServer(request);
        return PanelSettingsProtoMapper.toGeneralSettingsEnvelope(generalSettingsService.getGeneralSettingsState(server));
    }

    @PatchMapping("/general")
    public GeneralSettingsEnvelope patchGeneralSettings(
            @RequestBody PatchGeneralSettingsRequest body,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        VersionedSettings<GeneralSettings> updated = generalSettingsService.patchGeneralSettings(
                server,
                body.getExpectedVersion(),
                PanelSettingsProtoMapper.fromPatchGeneralSettingsRequest(body)
        );
        settingsInvalidationPublisher.invalidateSettings(server);
        return PanelSettingsProtoMapper.toGeneralSettingsEnvelope(updated);
    }

    @GetMapping("/ticket-labels")
    public TicketLabelSettingsEnvelope getTicketLabelSettings(HttpServletRequest request) {
        Server server = RequestUtil.getRequestServer(request);
        return PanelSettingsProtoMapper.toTicketLabelSettingsEnvelope(
                ticketLabelSettingsService.getTicketLabelSettingsState(server));
    }

    @PatchMapping("/ticket-labels")
    public TicketLabelSettingsEnvelope patchTicketLabelSettings(
            @RequestBody PatchTicketLabelSettingsRequest body,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        VersionedSettings<TicketLabelSettings> updated = ticketLabelSettingsService.patchTicketLabelSettings(
                server,
                body.getExpectedVersion(),
                PanelSettingsProtoMapper.fromPatchTicketLabelSettingsRequest(body)
        );
        settingsInvalidationPublisher.invalidateSettings(server);
        return PanelSettingsProtoMapper.toTicketLabelSettingsEnvelope(updated);
    }

    @GetMapping("/status-thresholds")
    @RequiresPanelPermission(view = PermissionService.ADMIN_SETTINGS_VIEW_PUNISHMENTS, modify = PermissionService.ADMIN_SETTINGS_MODIFY_PUNISHMENTS)
    public OffenderThresholdSettingsEnvelope getStatusThresholds(HttpServletRequest request) {
        Server server = RequestUtil.getRequestServer(request);
        return PanelSettingsProtoMapper.toOffenderThresholdSettingsEnvelope(
                offenderThresholdSettingsService.getThresholdSettingsState(server));
    }

    @PatchMapping("/status-thresholds")
    @RequiresPanelPermission(view = PermissionService.ADMIN_SETTINGS_VIEW_PUNISHMENTS, modify = PermissionService.ADMIN_SETTINGS_MODIFY_PUNISHMENTS)
    public OffenderThresholdSettingsEnvelope patchStatusThresholds(
            @RequestBody PatchStatusThresholdSettingsRequest body,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        VersionedSettings<OffenderThresholdSettings> updated = offenderThresholdSettingsService.patchThresholdSettings(
                server,
                body.getExpectedVersion(),
                PanelSettingsProtoMapper.fromOffenderThresholdSettings(body.getSettings())
        );
        settingsInvalidationPublisher.invalidateSettings(server);
        return PanelSettingsProtoMapper.toOffenderThresholdSettingsEnvelope(updated);
    }

    @GetMapping("/replay-retention")
    public ReplayRetentionSettingsEnvelope getReplayRetentionSettings(HttpServletRequest request) {
        Server server = RequestUtil.getRequestServer(request);
        return PanelSettingsProtoMapper.toReplayRetentionSettingsEnvelope(
                replayRetentionSettingsService.getReplayRetentionSettingsState(server));
    }

    @PatchMapping("/replay-retention")
    public ReplayRetentionSettingsEnvelope patchReplayRetentionSettings(
            @RequestBody @Valid PatchReplayRetentionSettingsRequest body,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        VersionedSettings<ReplayRetentionSettings> updated = replayRetentionSettingsService.patchReplayRetentionSettings(
                server,
                body.expectedVersion(),
                body.enabled(),
                body.days()
        );
        settingsInvalidationPublisher.invalidateSettings(server);
        return PanelSettingsProtoMapper.toReplayRetentionSettingsEnvelope(updated);
    }

    @GetMapping("/ai-moderation")
    @RequiresPanelPermission(view = PermissionService.ADMIN_SETTINGS_VIEW_PUNISHMENTS, modify = PermissionService.ADMIN_SETTINGS_MODIFY_PUNISHMENTS)
    public gg.modl.proto.modl.v1.AIModerationSettings getAIModerationSettings(HttpServletRequest request) {
        Server server = RequestUtil.getRequestServer(request);
        AIModerationSettings settings = aiModerationSettingsService.getAIModerationSettings(server);
        return PanelSettingsProtoMapper.toAIModerationSettings(settings);
    }

    @PatchMapping("/ai-moderation")
    @RequiresPanelPermission(view = PermissionService.ADMIN_SETTINGS_VIEW_PUNISHMENTS, modify = PermissionService.ADMIN_SETTINGS_MODIFY_PUNISHMENTS)
    public gg.modl.proto.modl.v1.AIModerationSettings updateAIModerationSettings(
            @RequestBody UpdateAIModerationSettingsRequest requestBody,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        AIModerationSettings settings = PanelSettingsProtoMapper.fromUpdateAIModerationSettingsRequest(requestBody);
        validationRunner.validate(settings);
        AIModerationSettings updated = aiModerationSettingsService.updateAIModerationSettings(server, settings);
        settingsInvalidationPublisher.invalidateSettings(server);
        return PanelSettingsProtoMapper.toAIModerationSettings(updated);
    }

    @GetMapping("/webhooks")
    public gg.modl.proto.modl.v1.WebhookSettings getWebhookSettings(HttpServletRequest request) {
        Server server = RequestUtil.getRequestServer(request);
        WebhookSettings settings = webhookSettingsService.getWebhookSettings(server);
        return PanelSettingsProtoMapper.toWebhookSettings(settings);
    }

    @PatchMapping("/webhooks")
    public gg.modl.proto.modl.v1.WebhookSettings updateWebhookSettings(
            @RequestBody UpdateWebhookSettingsRequest requestBody,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        WebhookSettings settings = PanelSettingsProtoMapper.fromUpdateWebhookSettingsRequest(requestBody);
        WebhookSettings updated = webhookSettingsService.updateWebhookSettings(server, settings);
        settingsInvalidationPublisher.invalidateSettings(server);
        return PanelSettingsProtoMapper.toWebhookSettings(updated);
    }

    @PostMapping("/webhooks/test")
    public WebhookTestResponse testWebhook(HttpServletRequest request) {
        Server server = RequestUtil.getRequestServer(request);
        boolean success = webhookSettingsService.testWebhook(server);

        if (!success) {
            throw new ValidationException("Failed to send webhook test");
        }
        return PanelSettingsProtoMapper.toWebhookTestResponse("Webhook test sent successfully");
    }

    @GetMapping("/ticket-forms")
    public TicketFormSettingsEnvelope getTicketFormSettings(HttpServletRequest request) {
        Server server = RequestUtil.getRequestServer(request);
        return PanelSettingsProtoMapper.toTicketFormSettingsEnvelope(
                ticketFormSettingsService.getTicketFormSettingsState(server));
    }

    @PatchMapping("/ticket-forms")
    public TicketFormSettingsEnvelope patchTicketFormSettings(
            @RequestBody PatchTicketFormSettingsRequest body,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        VersionedSettings<TicketFormSettings> updated = ticketFormSettingsService.patchTicketFormSettings(
                server,
                body.getExpectedVersion(),
                PanelSettingsProtoMapper.fromTicketFormSettings(body.getSettings())
        );
        settingsInvalidationPublisher.invalidateSettings(server);
        return PanelSettingsProtoMapper.toTicketFormSettingsEnvelope(updated);
    }

    @GetMapping("/ticket-forms/{type}")
    public ResponseEntity<TicketForm> getTicketForm(
            @PathVariable String type,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        TicketFormSettings.TicketForm form = ticketFormSettingsService.getFormByType(server, type);

        if (form == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(PanelSettingsProtoMapper.toTicketForm(form));
    }

    @GetMapping("/quick-responses")
    public QuickResponseSettingsEnvelope getQuickResponses(HttpServletRequest request) {
        Server server = RequestUtil.getRequestServer(request);
        return PanelSettingsProtoMapper.toQuickResponseSettingsEnvelope(
                quickResponseSettingsService.getQuickResponseSettingsState(server));
    }

    @PatchMapping("/quick-responses")
    public QuickResponseSettingsEnvelope patchQuickResponses(
            @RequestBody PatchQuickResponsesRequest body,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        UpdateQuickResponsesRequest quickResponses = new UpdateQuickResponsesRequest(
                PanelSettingsProtoMapper.fromPatchQuickResponsesRequest(body));
        validationRunner.validate(quickResponses);
        VersionedSettings<QuickResponseSettings> updated = quickResponseSettingsService.patchQuickResponseSettings(
                server,
                body.getExpectedVersion(),
                quickResponses
        );
        settingsInvalidationPublisher.invalidateSettings(server);
        return PanelSettingsProtoMapper.toQuickResponseSettingsEnvelope(updated);
    }

    @PostMapping("/upload-icon")
    public ResponseEntity<?> uploadIcon(
            @RequestParam("icon") MultipartFile file,
            @RequestParam("iconType") String iconType,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        ResponseEntity<?> response = iconUploadService.uploadIcon(server, file, iconType);
        if (response.getStatusCode().is2xxSuccessful()) {
            settingsInvalidationPublisher.invalidateSettings(server);
        }
        return response;
    }
}
