package gg.modl.backend.storage.data;

import gg.modl.backend.database.mongo.codegen.GenerateMongoFields;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@Document
@GenerateMongoFields
public class StorageFileDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String key;

    private String fileName;
    private long size;
    private String contentType;
    private String category;
    private Date createdAt;

    public StorageFileDocument(String key, String fileName, long size, String contentType, String category) {
        this.key = key;
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
        this.category = category;
        this.createdAt = new Date();
    }
}
