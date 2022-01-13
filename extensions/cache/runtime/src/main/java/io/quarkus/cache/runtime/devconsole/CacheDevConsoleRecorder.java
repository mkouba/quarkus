package io.quarkus.cache.runtime.devconsole;

import java.util.Optional;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.cache.runtime.CaffeineCacheSupplier;
import io.quarkus.cache.runtime.caffeine.CaffeineCacheImpl;
import io.quarkus.devconsole.runtime.spi.DevConsolePostHandler;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.vertx.http.runtime.devmode.Json;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class CacheDevConsoleRecorder {
    public Handler<RoutingContext> clearCacheHandler() {
        return new DevConsolePostHandler() {

            @Override
            protected void handlePost(RoutingContext context, MultiMap form) {
                String cacheName = form.get("name");
                Optional<Cache> cache = CaffeineCacheSupplier.cacheManager().getCache(cacheName);
                HttpServerResponse response = context.response();
                if (cache.isPresent() && cache.get() instanceof CaffeineCache) {
                    CaffeineCacheImpl caffeineCache = (CaffeineCacheImpl) cache.get();

                    
                    String action = form.get("action");
                    if (action.equalsIgnoreCase("clearCache")) {
                        caffeineCache.invalidateAll().subscribe().with(ignored -> {
                            response.setStatusCode(HttpResponseStatus.OK.code());
                            response.end(createResponseMessage(caffeineCache));
                        });
                    } else if (action.equalsIgnoreCase("refresh")) {
                        response.setStatusCode(HttpResponseStatus.OK.code());
                        response.end(createResponseMessage(caffeineCache));
                    } else {
                        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                        response.end(createResponseError(cacheName, "Invalid action: " + action));
                    }
                } else {
                    String errorMessage = "Cache for " + cacheName + " not found";
                    response.setStatusCode(HttpResponseStatus.NOT_FOUND.code());
                    response.end(createResponseError(cacheName, errorMessage));
                }
            }

            @Override
            protected void actionSuccess(RoutingContext event) {
            }

            private String createResponseMessage(CaffeineCacheImpl cache) {
                Json.JsonObjectBuilder object = Json.object();
                object.put("name", cache.getName());
                object.put("size", cache.getSize());
                return object.build();
            }

            private String createResponseError(String name, String error) {
                Json.JsonObjectBuilder object = Json.object();
                object.put("name", name);
                object.put("error", error);
                return object.build();
            }
        };
    }
}
