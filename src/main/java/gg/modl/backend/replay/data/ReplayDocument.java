package gg.modl.backend.replay.data;

import gg.modl.backend.database.mongo.codegen.GenerateMongoFields;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Document
@GenerateMongoFields
public class ReplayDocument {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETE = "COMPLETE";
    public static final String STATUS_FAILED = "FAILED";
    @Id
    private String id;
    private String mcVersion;
    private String targetUuid;
    private String targetName;
    private long fileSize;
    private String storageKey;
    private String status;
    private Date createdAt;
    private List<ReplayLabel> labels;
}
