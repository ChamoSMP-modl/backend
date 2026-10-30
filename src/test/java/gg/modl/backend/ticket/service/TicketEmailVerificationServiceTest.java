package gg.modl.backend.ticket.service;

import gg.modl.backend.database.mongo.repository.TicketVerificationMongoRepository;
import gg.modl.backend.email.EmailService;
import gg.modl.backend.infrastructure.onetimecode.OneTimeCodeCodec;
import gg.modl.backend.server.data.Server;
import gg.modl.backend.server.data.ServerPlan;
import gg.modl.backend.ticket.config.TicketEmailVerificationConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TicketEmailVerificationServiceTest {
    @Test
    void failedVerificationIncrementsFailedAttempts() {
        TicketVerificationMongoRepository repository = mock(TicketVerificationMongoRepository.class);
        TicketEmailVerificationConfiguration config = new TicketEmailVerificationConfiguration();
        config.setCodeHashSecret("test-secret");
        TicketEmailVerificationService service = new TicketEmailVerificationService(
                repository,
                mock(EmailService.class),
                config,
                new OneTimeCodeCodec()
        );
        Server server = new Server("server", "domain", "db", "admin@example.com", true, ServerPlan.PREMIUM);
        when(repository.consumeMatchingCode(eq(server), eq("ticket-1"), any(), any(Date.class)))
                .thenReturn(Optional.empty());

        String token = service.verifyCode(server, "ticket-1", "123456");

        assertNull(token);
        verify(repository).incrementFailedAttempts(eq(server), eq("ticket-1"), any(Date.class));
    }
}
