package com.codebridge.backend.exception;

public class GitHubApiException extends RuntimeException {

    public enum Reason {
        NOT_FOUND,
        RATE_LIMITED,
        UPSTREAM_ERROR
    }

    private final Reason reason;

    public GitHubApiException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
