package gg.modl.backend.replaylite.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.modl.backend.infrastructure.validation.RequestValidationLimits;
import jakarta.validation.constraints.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReplayLiteUploadInitRequest(
        @Positive
        @Max(RequestValidationLimits.REPLAY_LITE_MAX_REQUESTED_SIZE_BYTES)
        long requestedSize,
        @NotBlank
        @Size(max = RequestValidationLimits.REPLAY_LITE_MC_VERSION_MAX_LENGTH)
        @Pattern(regexp = "^[A-Za-z0-9_.+\\-]+$")
        String mcVersion
) {
}
