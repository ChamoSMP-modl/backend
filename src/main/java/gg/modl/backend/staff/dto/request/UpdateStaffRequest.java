package gg.modl.backend.staff.dto.request;

import gg.modl.backend.infrastructure.validation.RequestValidationLimits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record UpdateStaffRequest(
        @Nullable @Email @Size(max = RequestValidationLimits.EMAIL_MAX_LENGTH) String email
) {
}
