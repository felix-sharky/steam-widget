package codes.sharky.steamwidget.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheConfigTests {

    private final CacheConfig cacheConfig = new CacheConfig();

    @Test
    void cacheManagerExposesAllProfilePageCaches() {
        CacheManager cacheManager = cacheConfig.cacheManager();

        assertNotNull(cacheManager.getCache(CacheConfig.PROFILE_PAGE_PLAYER));
        assertNotNull(cacheManager.getCache(CacheConfig.PROFILE_PAGE_TOP_OWNED_GAMES));
        assertNotNull(cacheManager.getCache(CacheConfig.PROFILE_PAGE_TOP_RECENT_GAMES));
        assertNotNull(cacheManager.getCache(CacheConfig.PROFILE_PAGE_VANITY_RESOLVE));
        assertNotNull(cacheManager.getCache(CacheConfig.PROFILE_PAGE_TRACKED_STATS));
    }

    @Test
    void cachesAreBoundedBySize() {
        CacheManager cacheManager = cacheConfig.cacheManager();

        org.springframework.cache.caffeine.CaffeineCache cache =
                (org.springframework.cache.caffeine.CaffeineCache) cacheManager.getCache(CacheConfig.PROFILE_PAGE_PLAYER);
        assertNotNull(cache);
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = cache.getNativeCache();
        assertTrue(nativeCache.policy().eviction().isPresent());
        assertEquals(1000L, nativeCache.policy().eviction().get().getMaximum());
    }
}
