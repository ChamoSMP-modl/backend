package gg.modl.backend.realtime.auth;

import gg.modl.proto.modl.v1.ErrorCode;
import lombok.Getter;

@Getter
public class RealtimeAuthenticationException extends Exception {
    private final ErrorCode errorCode;

    public RealtimeAuthenticationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
