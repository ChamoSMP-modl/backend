package gg.modl.backend.server.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SubscriptionStatus {
    ACTIVE("ACTIVE");

    private final String value;

    SubscriptionStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static SubscriptionStatus fromValue(String value) {
        return ACTIVE;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
