package io.quarkus.cache.runtime.devconsole;

import io.quarkus.cache.runtime.CaffeineCacheSupplier;
import io.quarkus.cache.runtime.caffeine.CaffeineCache;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class CacheDevConsoleRecorder {

    public Handler<RoutingContext> clearCacheHandler() {
        return new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext event) {
                event.request().setExpectMultipart(true);
                event.request().bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buf) {
                        try {
                            String cacheName = event.request().formAttributes().get("name");
                            for (CaffeineCache cache : CaffeineCacheSupplier.allCaches()) {
                                if (cache.getName().equals(cacheName)) {
                                    cache.invalidateAll();
                                    // redirect to the same page so we can make sure we see the updated results
                                    event.response().putHeader("location", event.normalisedPath()).setStatusCode(302).end();
                                    return;
                                }
                            }
                            event.response().setStatusCode(404).end();
                        } catch (Throwable t) {
                            t.printStackTrace();
                            event.response().setStatusCode(500).end();
                        }
                    }
                });
            }
        };
    }
}
