package gg.modl.backend.ticket.controller;

import gg.modl.backend.infrastructure.authorization.RequiresPanelPermission;
import gg.modl.backend.infrastructure.exception.ResourceNotFoundException;
import gg.modl.backend.infrastructure.exception.UnauthorizedException;
import gg.modl.backend.infrastructure.rest.RESTMappingV1;
import gg.modl.backend.infrastructure.rest.RequestUtil;
import gg.modl.backend.infrastructure.validation.RequestValidationLimits;
import gg.modl.backend.realtime.publish.RealtimeEventPublisher;
import gg.modl.backend.server.data.Server;
import gg.modl.backend.ticket.dto.response.SubscriptionUpdateResponse;
import gg.modl.backend.ticket.dto.response.TicketSubscriptionResponse;
import gg.modl.backend.ticket.service.TicketSubscriptionService;
import gg.modl.proto.modl.v1.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(RESTMappingV1.PANEL_TICKET_SUBSCRIPTIONS)
@RequiresPanelPermission(view = "ticket.view.all", modify = "ticket.reply.all")
@RequiredArgsConstructor
@Validated
public class TicketSubscriptionController {
    private final TicketSubscriptionService subscriptionService;
    private final RealtimeEventPublisher realtimeEventPublisher;

    @GetMapping
    public ResponseEntity<TicketSubscriptionsResponse> getSubscriptions(HttpServletRequest request) {
        Server server = RequestUtil.getRequestServer(request);
        String staffEmail = sessionEmail(request);
        if (staffEmail == null) {
            return ResponseEntity.status(401).build();
        }

        List<TicketSubscriptionResponse> subscriptions = subscriptionService.getSubscriptions(server, staffEmail);
        return ResponseEntity.ok(PanelTicketProtoMapper.toTicketSubscriptionsResponse(subscriptions));
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<DeleteTicketSubscriptionResponse> unsubscribe(
            @PathVariable String ticketId,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        String staffEmail = sessionEmail(request);
        if (staffEmail == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        if (!subscriptionService.unsubscribe(server, staffEmail, ticketId)) {
            throw new ResourceNotFoundException("Subscription not found");
        }
        realtimeEventPublisher.invalidatePanel(server, PanelResource.PANEL_RESOURCE_NOTIFICATIONS);
        return ResponseEntity.ok(PanelTicketProtoMapper.toDeleteSubscriptionResponse("Successfully unsubscribed from ticket"));
    }

    @GetMapping("/updates")
    public ResponseEntity<SubscriptionUpdatesResponse> getUpdates(
            @RequestParam(defaultValue = "10") @Min(RequestValidationLimits.PAGINATION_LIMIT_MIN) @Max(RequestValidationLimits.PAGINATION_LIMIT_MAX) int limit,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        String staffEmail = sessionEmail(request);
        if (staffEmail == null) {
            return ResponseEntity.status(401).build();
        }

        List<SubscriptionUpdateResponse> updates = subscriptionService.getUpdates(server, staffEmail, limit);
        return ResponseEntity.ok(PanelTicketProtoMapper.toSubscriptionUpdatesResponse(updates));
    }

    @PostMapping("/updates/{updateId}/read")
    public ResponseEntity<MarkSubscriptionUpdateReadResponse> markAsRead(
            @PathVariable String updateId,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        String staffEmail = sessionEmail(request);
        if (staffEmail == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        boolean result = subscriptionService.markAsRead(server, staffEmail, updateId);
        realtimeEventPublisher.invalidatePanel(server, PanelResource.PANEL_RESOURCE_NOTIFICATIONS);
        return ResponseEntity.ok(PanelTicketProtoMapper.toMarkUpdateReadResponse("Update marked as read", result));
    }

    @PostMapping("/tickets/{ticketId}/read")
    public ResponseEntity<MarkTicketSubscriptionReadResponse> markTicketAsRead(
            @PathVariable String ticketId,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        String staffEmail = sessionEmail(request);
        if (staffEmail == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        subscriptionService.markTicketAsRead(server, ticketId, staffEmail);
        realtimeEventPublisher.invalidatePanel(server, PanelResource.PANEL_RESOURCE_NOTIFICATIONS, ticketId);
        return ResponseEntity.ok(PanelTicketProtoMapper.toMarkTicketReadResponse("All updates for ticket marked as read"));
    }

    @GetMapping("/assigned-updates")
    public ResponseEntity<SubscriptionUpdatesResponse> getAssignedUpdates(
            @RequestParam(defaultValue = "10") @Min(RequestValidationLimits.PAGINATION_LIMIT_MIN) @Max(RequestValidationLimits.PAGINATION_LIMIT_MAX) int limit,
            HttpServletRequest request
    ) {
        Server server = RequestUtil.getRequestServer(request);
        String staffEmail = sessionEmail(request);
        if (staffEmail == null) {
            return ResponseEntity.status(401).build();
        }

        List<SubscriptionUpdateResponse> updates = subscriptionService.getAssignedTicketUpdates(server, staffEmail, limit);
        return ResponseEntity.ok(PanelTicketProtoMapper.toSubscriptionUpdatesResponse(updates));
    }

    private String sessionEmail(HttpServletRequest request) {
        String staffEmail = RequestUtil.getSessionEmail(request);
        return staffEmail == null || staffEmail.isBlank() ? null : staffEmail;
    }
}
