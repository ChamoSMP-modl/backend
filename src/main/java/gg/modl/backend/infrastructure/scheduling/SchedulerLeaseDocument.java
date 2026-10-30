package gg.modl.backend.infrastructure.scheduling;

import gg.modl.backend.database.CollectionName;
import gg.modl.backend.database.mongo.codegen.GenerateMongoFields;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = CollectionName.SCHEDULER_LEASES)
@GenerateMongoFields
public class SchedulerLeaseDocument {
    @Id
    private String id;
    private Instant heldUntil;
    private String owner;
}
