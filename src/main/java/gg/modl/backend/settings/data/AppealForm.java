package gg.modl.backend.settings.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppealForm {
    @Builder.Default
    private List<AppealFormField> fields = new ArrayList<>();
    @Builder.Default
    private List<AppealFormSection> sections = new ArrayList<>();
}
