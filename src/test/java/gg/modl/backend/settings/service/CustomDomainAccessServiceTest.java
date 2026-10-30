package gg.modl.backend.settings.service;

import gg.modl.backend.limits.DefaultServerLimitPolicy;
import gg.modl.backend.server.data.Server;
import gg.modl.backend.server.data.ServerPlan;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomDomainAccessServiceTest {

    private final CustomDomainAccessService service = new CustomDomainAccessService(new DefaultServerLimitPolicy());

    @Test
    void premiumServerCanManageCustomDomainWithoutGrandfatherFlag() {
        Server server = new Server("test-server", "test", "server_test", "admin@example.com", false, ServerPlan.PREMIUM);

        assertTrue(service.canManageCustomDomain(server));
    }
}
