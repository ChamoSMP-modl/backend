package gg.modl.backend.admin.data;

import gg.modl.backend.database.CollectionName;
import gg.modl.backend.database.mongo.codegen.GenerateMongoFields;
import gg.modl.backend.email.EmailAddressUtil;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = CollectionName.ADMIN_USERS)
@GenerateMongoFields
public class AdminUser {
    @Id
    private String id;
    @Field("email")
    private String email;
    @Field("loggedInIps")
    private List<String> loggedInIps = new ArrayList<>();
    @Field("lastActivityAt")
    private Date lastActivityAt;
    @Field("createdAt")
    private Date createdAt;

    public AdminUser(String email) {
        this();
        this.email = EmailAddressUtil.normalize(email);
    }

    public AdminUser() {
        this.createdAt = new Date();
        this.lastActivityAt = new Date();
    }
}
