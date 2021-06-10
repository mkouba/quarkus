package io.quarkus.grpc.runtime.config;

import java.util.List;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

/**
 * The retry policy configuration as defined in the
 * <a href="https://github.com/grpc/proposal/blob/master/A6-client-retries.md">gRPC Retry Design</a>.
 */
@ConfigGroup
public class GrpcRetryPolicy {

    /**
     * The maximum number of rpc attempts, including the original request. It must be an integer value greater than 1. Values
     * greater than 5 are treated as 5.
     */
    @ConfigItem
    public int maxAttempts;

    /**
     * Determines the randomized delay before retry attempts. The initial retry attempt will occur at
     * <code>random(0, initialBackoff)</code>. The n-th attempt will occur at
     * <code>random(0, min(initialBackoff*backoffMultiplier*(n-1), maxBackoff))</code>.
     * <p>
     * Must follow the JSON representaion of <a href="https://developers.google.com/protocol-buffers/docs/proto3#json">proto3
     * Duration type</a> and must have a duration value greater than 0.
     */
    @ConfigItem
    public String initialBackoff;

    /**
     * Determines the randomized delay before retry attempts. The n-th attempt will occur at
     * <code>random(0, min(initialBackoff*backoffMultiplier*(n-1), maxBackoff))</code>.
     * <p>
     * Must follow the JSON representaion of <a href="https://developers.google.com/protocol-buffers/docs/proto3#json">proto3
     * Duration type</a> and must have a duration value greater than 0.
     */
    @ConfigItem
    public String maxBackoff;

    /**
     * Determines the randomized delay before retry attempts. The n-th attempt will occur at
     * <code>random(0, min(initialBackoff*backoffMultiplier*(n-1), maxBackoff))</code>.
     * <p>
     * Must be a number greater than 0. Determines the randomized delay before retry attempts. The n-th attempt will occur at
     * <code>random(0, min(initialBackoff*backoffMultiplier*(n-1), maxBackoff))</code>.
     */
    @ConfigItem
    public double backoffMultiplier;

    /**
     * The list of valid gRPC status codes.
     */
    @ConfigItem
    public List<String> retryableStatusCodes;

}
