package io.quarkus.cache.runtime;

import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheManager;
import io.quarkus.cache.test.CacheTestState;

public class CacheTestStateImpl implements CacheTestState {

    private static final Logger LOG = Logger.getLogger(CacheTestStateImpl.class);

    @Inject
    CacheManager cacheManager;

    @Override
    public void reset() {
        LOG.info("Resetting cache state");
        for (String cacheName : cacheManager.getCacheNames()) {
            cacheManager.getCache(cacheName).ifPresent(Cache::invalidateAll);
        }
    }

}
