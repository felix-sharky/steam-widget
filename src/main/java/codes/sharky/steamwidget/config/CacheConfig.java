package codes.sharky.steamwidget.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine-backed caches for the Steam Web API calls behind the /u/{id} profile page, so repeat
 * views within the TTL window don't re-hit Steam's rate-limited API. Separate from the
 * unrelated, pre-existing ProfileCache/Profile.caching mechanism used by widget image serving.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PROFILE_PAGE_PLAYER = "profilePagePlayer";
    public static final String PROFILE_PAGE_TOP_OWNED_GAMES = "profilePageTopOwnedGames";
    public static final String PROFILE_PAGE_TOP_RECENT_GAMES = "profilePageTopRecentGames";
    public static final String PROFILE_PAGE_VANITY_RESOLVE = "profilePageVanityResolve";
    public static final String PROFILE_PAGE_TRACKED_STATS = "profilePageTrackedStats";
    public static final String SITEMAP = "sitemap";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                PROFILE_PAGE_PLAYER,
                PROFILE_PAGE_TOP_OWNED_GAMES,
                PROFILE_PAGE_TOP_RECENT_GAMES,
                PROFILE_PAGE_VANITY_RESOLVE,
                PROFILE_PAGE_TRACKED_STATS,
                SITEMAP);
        // maximumSize bounds every cache registered on this manager (attacker-controlled keys on a
        // public endpoint) and also keeps the owned-games cache from accumulating full catalogues
        // unbounded.
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000));
        return cacheManager;
    }
}
