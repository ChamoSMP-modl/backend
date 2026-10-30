package gg.modl.backend.database.mongo.codegen;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(MongoFieldAliases.class)
public @interface MongoFieldAlias {
    String name();

    String path();
}
