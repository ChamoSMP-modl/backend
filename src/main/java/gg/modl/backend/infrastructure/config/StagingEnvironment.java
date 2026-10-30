package gg.modl.backend.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class StagingEnvironment {
    private static final String STAGING_PROFILE = "staging";

    private final Environment environment;

    public boolean isStaging() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(STAGING_PROFILE::equalsIgnoreCase);
    }
}
