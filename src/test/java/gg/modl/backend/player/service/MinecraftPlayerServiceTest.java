package gg.modl.backend.player.service;

import gg.modl.backend.database.mongo.repository.PlayerMongoRepository;
import gg.modl.backend.database.mongo.repository.TicketMongoRepository;
import gg.modl.backend.player.PlayerService;
import gg.modl.backend.player.data.Player;
import gg.modl.backend.player.dto.request.AcknowledgeNotificationsRequest;
import gg.modl.backend.player.dto.response.AcknowledgeResult;
import gg.modl.backend.player.dto.response.CreateNoteResult;
import gg.modl.backend.server.data.Server;
import gg.modl.backend.server.data.ServerPlan;
import gg.modl.backend.settings.service.PunishmentTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinecraftPlayerServiceTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private PlayerMongoRepository playerRepository;

    @Mock
    private TicketMongoRepository ticketRepository;

    @Mock
    private PlayerStatusCalculator statusCalculator;

    @Mock
    private PunishmentTypeService punishmentTypeService;

    @Mock
    private PunishmentLifecycleService punishmentLifecycleService;

    @Mock
    private AccountLinkingService accountLinkingService;

    @Mock
    private IssuerNameResolver issuerNameResolver;

    private MinecraftPlayerService minecraftPlayerService;

    @BeforeEach
    void setUp() {
        minecraftPlayerService = new MinecraftPlayerService(
                playerService,
                playerRepository,
                ticketRepository,
                statusCalculator,
                punishmentTypeService,
                punishmentLifecycleService,
                accountLinkingService,
                issuerNameResolver
        );
    }

    @Test
    void createNotePersistsThroughRepositorySaveChanges() {
        Server server = new Server("server", "domain", "db", "admin@example.com", true, ServerPlan.PREMIUM);
        Player player = Player.builder()
                .minecraftUuid(UUID.randomUUID())
                .build();

        when(playerRepository.findByMinecraftUuid(server, player.getMinecraftUuid().toString())).thenReturn(Optional.of(player));

        CreateNoteResult response = minecraftPlayerService.createNote(
                server,
                player.getMinecraftUuid().toString(),
                "Test note",
                "Moderator",
                null
        );

        assertEquals(new CreateNoteResult.Created("Note added"), response);
        verify(playerRepository).replaceNotes(server, player);
        assertEquals("Test note", player.getNotes().get(0).getText());
    }

    @Test
    void acknowledgeNotificationsRemovesOnlyRequestedNotificationIds() {
        Server server = new Server("server", "domain", "db", "admin@example.com", true, ServerPlan.PREMIUM);
        UUID playerUuid = UUID.randomUUID();
        Player player = Player.builder()
                .minecraftUuid(playerUuid)
                .data(new LinkedHashMap<>(Map.of(
                        "pendingNotifications", new ArrayList<>(List.of(
                                new LinkedHashMap<>(Map.of("id", "notif-1", "message", "one")),
                                new LinkedHashMap<>(Map.of("id", "notif-2", "message", "two"))
                        ))
                )))
                .build();

        when(playerRepository.findByMinecraftUuid(server, playerUuid.toString())).thenReturn(Optional.of(player));

        AcknowledgeResult response = minecraftPlayerService.acknowledgeNotifications(
                server,
                new AcknowledgeNotificationsRequest(playerUuid.toString(), List.of("notif-1"), null)
        );

        assertEquals(new AcknowledgeResult("Acknowledged 1 notification(s)"), response);
        verify(playerRepository).replacePendingNotifications(server, player, remainingNotifications(player));
        List<Map<String, Object>> remaining = player.data().pendingNotifications();
        assertEquals(1, remaining.size());
        assertEquals("notif-2", remaining.get(0).get("id"));
    }

    @Test
    void getPlayerReportsLowercasesUuidBeforeQueryingTicketRepository() {
        Server server = new Server("server", "domain", "db", "admin@example.com", true, ServerPlan.PREMIUM);
        when(ticketRepository.findReportedPlayerTickets(any(Server.class), any(), anyInt())).thenReturn(List.of());

        minecraftPlayerService.getPlayerReports(server, "AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE");

        verify(ticketRepository).findReportedPlayerTickets(server, "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", 50);
    }

    @Test
    void disconnectLowercasesUuidBeforeQueryingPlayerRepository() {
        Server server = new Server("server", "domain", "db", "admin@example.com", true, ServerPlan.PREMIUM);

        minecraftPlayerService.disconnect(server, "AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE", 5_000L);

        verify(playerRepository).markDisconnected(eq(server), eq("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), anyLong());
    }

    @Test
    void updateServerLowercasesUuidBeforeQueryingPlayerRepository() {
        Server server = new Server("server", "domain", "db", "admin@example.com", true, ServerPlan.PREMIUM);

        minecraftPlayerService.updateServer(server, "AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE", "lobby");

        verify(playerRepository).updateLastServer(server, "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "lobby");
    }

    @Test
    void submitIpInfoLowercasesUuidBeforeForwardingToPlayerService() {
        Server server = new Server("server", "domain", "db", "admin@example.com", true, ServerPlan.PREMIUM);

        minecraftPlayerService.submitIpInfo(server, "AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE",
                "1.2.3.4", "US", "CA", "ASN", false, false);

        verify(playerService).updateIpGeoData(eq(server), eq("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), eq("1.2.3.4"), any());
    }

    @Test
    void getPlayerPunishmentsLowercasesUuidBeforeQueryingPlayerRepository() {
        Server server = new Server("server", "domain", "db", "admin@example.com", true, ServerPlan.PREMIUM);
        when(playerRepository.findByMinecraftUuid(eq(server), eq("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")))
                .thenReturn(Optional.empty());

        minecraftPlayerService.getPlayerPunishments(server, "AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE", 1, 10);

        verify(playerRepository).findByMinecraftUuid(server, "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    }

    private List<Map<String, Object>> remainingNotifications(Player player) {
        return player.data().pendingNotifications();
    }
}


