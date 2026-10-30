package gg.modl.backend.replaylite.data;

import gg.modl.backend.database.CollectionName;
import gg.modl.backend.database.mongo.codegen.GenerateMongoFields;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@Document(collection = CollectionName.REPLAY_LITE_DAILY_QUOTAS)
@GenerateMongoFields
public class ReplayLiteDailyQuotaDocument {
    @Id
    private String id;
    private UUID pluginServerUuid;
    private LocalDate day;
    private int count;
    private List<String> replayIds;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant expiresAt;
}
