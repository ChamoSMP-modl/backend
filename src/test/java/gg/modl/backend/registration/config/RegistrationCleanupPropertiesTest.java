package gg.modl.backend.registration.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrationCleanupPropertiesTest {
    @Test
    void rejectsZeroOrNegativeExpiry() {
        RegistrationCleanupProperties properties = new RegistrationCleanupProperties();

        assertThatThrownBy(() -> properties.setExpiry(Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiry");
        assertThatThrownBy(() -> properties.setExpiry(Duration.ofMillis(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiry");
    }

    @Test
    void rejectsZeroOrNegativeClaimTtl() {
        RegistrationCleanupProperties properties = new RegistrationCleanupProperties();

        assertThatThrownBy(() -> properties.setClaimTtl(Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("claimTtl");
        assertThatThrownBy(() -> properties.setClaimTtl(Duration.ofMillis(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("claimTtl");
    }
}
