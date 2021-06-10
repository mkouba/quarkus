package io.quarkus.grpc.runtime.config;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

/**
 * A gRPC service method configuration.
 */
@ConfigGroup
public class GrpcMethodConfig {

    /**
     * The retry policy used for this method.
     */
    @ConfigItem
    public Optional<GrpcRetryPolicy> retryPolicy;

}
