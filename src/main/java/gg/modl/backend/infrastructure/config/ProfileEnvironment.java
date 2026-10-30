package gg.modl.backend.infrastructure.config;

import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Set;

public final class ProfileEnvironment {
    private ProfileEnvironment() {
    }

    public static boolean hasAnyActiveProfile(Environment environment, Set<String> profiles) {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profiles.contains(profile.toLowerCase()));
    }
}
