package com.taller.ia.exception;

public class RateLimitExceededException extends RuntimeException {

    private final long retryAfter;

    public RateLimitExceededException(long retryAfter) {
        super("Rate limit exceeded. Retry after " + retryAfter + " seconds");
        this.retryAfter = retryAfter;
    }

    /** Seconds the client must wait before sending a new request. */
    public long getRetryAfter() {
        return retryAfter;
    }
}
