package gg.modl.backend.analytics.data;

import gg.modl.backend.database.CollectionName;
import gg.modl.backend.database.mongo.codegen.GenerateMongoFields;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document(collection = CollectionName.METRIC_SNAPSHOTS)
@Data
@GenerateMongoFields
public class MetricSnapshot {
    @Id
    private String id;

    @Field
    private Date date;

    @Field
    private long activeServers;

    @Field
    private Date createdAt;
}
