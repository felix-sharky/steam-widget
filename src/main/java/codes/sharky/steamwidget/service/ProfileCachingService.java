package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.entity.Profile;
import codes.sharky.steamwidget.entity.ProfileCache;
import codes.sharky.steamwidget.model.BooleanResultResponse;
import codes.sharky.steamwidget.repository.ProfileCacheRepository;
import com.lukaspradel.steamapi.data.json.playersummaries.Player;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service that manages profile caching lifecycle: enabling/disabling caching, refreshing caches,
 * and resolving cached profile data via the Steam Web API.
 */
@Service
@Slf4j
public class ProfileCachingService {

    private final SteamWebAPIService steamWebAPIService;
    private final ProfileCacheRepository profileCacheRepository;
    private final ProfileService profileService;

    /**
     * Creates a caching service with dependencies for Steam API access, cache persistence, and profile metadata.
     *
     * @param steamWebAPIService      client for Steam Web API user lookups
     * @param profileCacheRepository  repository for profile cache entities
     * @param profileService          profile service used to toggle caching flags
     */
    public ProfileCachingService(SteamWebAPIService steamWebAPIService, ProfileCacheRepository profileCacheRepository, ProfileService profileService) {
        this.steamWebAPIService = steamWebAPIService;
        this.profileCacheRepository = profileCacheRepository;
        this.profileService = profileService;
    }

    /**
     * Registers a user for caching, enabling the caching flag if not already active.
     *
     * @param player Steam player data used to set the caching flag
     * @return true if registration succeeds or was already active; false on error
     */
    public boolean registerUser(@NotNull Player player) {
        try {
            if (profileService.profileCachingActive(player.getSteamid())) {
                return true;
            }
            profileService.upsertProfileCaching(player.getSteamid(), player.getPersonaname(), true);
            return true;
        } catch (Exception e) {
            log.error("Failed to register user for caching: " + player.getSteamid(), e);
            return false;
        }
    }

    /**
     * Unregisters a user from caching, disabling the caching flag if active.
     *
     * @param player Steam player data used to clear the caching flag
     * @return true if unregistration succeeds or was already inactive; false on error
     */
    public boolean unregisterUser(@NotNull Player player) {
        try {
            if (!profileService.profileCachingActive(player.getSteamid())) {
                return true;
            }
            profileService.upsertProfileCaching(player.getSteamid(), player.getPersonaname(), false);
            return true;
        } catch (Exception e) {
            log.error("Failed to unregister user from caching: " + player.getSteamid(), e);
            return false;
        }
    }

    /**
     * Enables caching for a Steam ID resolved via the Steam Web API.
     *
     * @param steamId Steam ID to enable caching for
     * @return result indicating the Steam ID and whether the operation succeeded
     */
    public BooleanResultResponse enableCaching(String steamId) {
        Player player = steamWebAPIService.getUserBySteamId(steamId);
        boolean enabled = this.registerUser(player);
        return new BooleanResultResponse(player.getSteamid(), enabled);
    }

    /**
     * Disables caching for a Steam ID resolved via the Steam Web API.
     *
     * @param steamId Steam ID to disable caching for
     * @return result indicating the Steam ID and whether the operation succeeded
     */
    public BooleanResultResponse disableCaching(String steamId) {
        Player player = steamWebAPIService.getUserBySteamId(steamId);
        boolean enabled = this.unregisterUser(player);
        return new BooleanResultResponse(player.getSteamid(), enabled);
    }

    /**
     * Deactivates caches that have not been refreshed within the last 12 hours.
     * Resets caching flags on stale profiles.
     */
    public void deactivateOldCaches() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(12);
        profileService.getProfilesWithCachingBefore(cutoff).forEach(profile -> {
            profileService.upsertProfileCaching(profile.getSteam64id(), profile.getName(), false);
        });
    }

    /**
     * Retrieves cached profiles for a list of Steam IDs, resolving vanity IDs when needed.
     *
     * @param steamIds Steam IDs or vanity IDs to resolve and fetch caches for
     * @return list of matching profile caches; entries with unresolved IDs are skipped
     */
    public List<ProfileCache> getProfileCachesBySteamIds(@NonNull List<String> steamIds) {
        List<String> resolvedSteamIds = steamIds.stream()
            .map(id -> {
                try {
                    return steamWebAPIService.resolveSteamId(id);
                } catch (Exception e) {
                    log.error("Failed to resolve Steam ID: " + id, e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return profileCacheRepository.findBySteam64idIn(resolvedSteamIds);
    }

    /**
     * Retrieves a cached profile by Steam ID or vanity ID.
     *
     * @param steamId Steam ID or vanity ID to resolve
     * @return cached profile if found; otherwise null
     */
    public ProfileCache getProfileCache(String steamId) {
        try {
            String resolvedSteamId = steamWebAPIService.resolveSteamId(steamId);
            return profileCacheRepository.findBySteam64id(resolvedSteamId).orElse(null);
        } catch (Exception e) {
            log.error("Failed to resolve Steam ID: " + steamId, e);
            return null;
        }
    }

    /**
     * Refreshes cached data for all profiles with caching enabled by fetching current player data.
     */
    public void updateActiveProfileCaches() {
        List<Profile> activeProfiles = profileService.getProfilesWithCaching();
        if (activeProfiles.isEmpty()) return;

        List<Player> players = steamWebAPIService.getUsersBySteamIds(
            activeProfiles.stream().map(Profile::getSteam64id).collect(Collectors.toList())
        );

        List<ProfileCache> caches = new ArrayList<>();
        activeProfiles.forEach(profile -> players.stream()
            .filter(player -> player.getSteamid().equals(profile.getSteam64id()))
            .findFirst()
            .ifPresent(player -> {
                ProfileCache cache = profile.getProfileCache();
                if (cache == null)  {
                    cache = new ProfileCache();
                    cache.setSteam64id(profile.getSteam64id());
                    cache.setLastrequest(LocalDateTime.now());
                }
                cache.setLastupdate(LocalDateTime.now());
                cache.setLastgame(player.getAdditionalProperties().getOrDefault("gameextrainfo", "").toString());
                cache.setLastpersonastate(Math.toIntExact(player.getPersonastate()));
                caches.add(cache);
            }));

        profileCacheRepository.saveAll(caches);
    }
}
