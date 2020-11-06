package io.quarkus.cache.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import io.quarkus.arc.Arc;
import io.quarkus.cache.runtime.caffeine.CaffeineCache;

public class CaffeineCacheSupplier implements Supplier<Collection<CaffeineCache>> {

    @Override
    public List<CaffeineCache> get() {
        List<CaffeineCache> allCaches = new ArrayList<>(allCaches());
        allCaches.sort(Comparator.comparing(CaffeineCache::getName));
        return allCaches;
    }

    public static Collection<CaffeineCache> allCaches() {
        return Arc.container().instance(CacheRepository.class).get().getAllCaches();
    }
}
