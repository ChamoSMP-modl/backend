package gg.modl.backend.server.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ServerPlan {
    PREMIUM("PREMIUM");

    private final String value;

    ServerPlan(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ServerPlan fromValue(String value) {
        return PREMIUM;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
