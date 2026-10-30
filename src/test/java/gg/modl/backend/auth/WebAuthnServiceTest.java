package gg.modl.backend.auth;

import gg.modl.backend.auth.data.WebAuthnChallenge;
import gg.modl.backend.database.mongo.repository.WebAuthnChallengeMongoRepository;
import gg.modl.backend.database.mongo.repository.WebAuthnCredentialMongoRepository;
import gg.modl.backend.server.data.Server;
import gg.modl.backend.server.data.ServerPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebAuthnServiceTest {
    private WebAuthnChallengeMongoRepository challengeRepository;
    private WebAuthnCredentialMongoRepository credentialRepository;
    private WebAuthnService webAuthnService;

    private static Server server() {
        return new Server("server", "tenant", "db", "admin@example.com", true, ServerPlan.PREMIUM);
    }

    @BeforeEach
    void setUp() {
        challengeRepository = mock(WebAuthnChallengeMongoRepository.class);
        credentialRepository = mock(WebAuthnCredentialMongoRepository.class);
        AuthConfiguration authConfiguration = new AuthConfiguration();
        webAuthnService = new WebAuthnService(challengeRepository, credentialRepository, authConfiguration);
        when(challengeRepository.saveEntity(any(Server.class), any(WebAuthnChallenge.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));
    }

    @Test
    void registrationOptionsRequireUserVerification() {
        WebAuthnService.StartRegistrationResult result =
                webAuthnService.startRegistration(server(), "admin@example.com");

        assertTrue(result.optionsJson().contains("\"userVerification\":\"required\""));
    }

    @Test
    void discoverableAuthenticationOptionsRequireUserVerification() {
        WebAuthnService.StartAuthenticationResult result =
                webAuthnService.startDiscoverableAuthentication(server());

        assertTrue(result.optionsJson().contains("\"userVerification\":\"required\""));
    }
}
