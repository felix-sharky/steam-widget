package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.entity.ProfileCache;
import codes.sharky.steamwidget.model.BooleanResultResponse;
import codes.sharky.steamwidget.service.ProfileCachingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing profile caching endpoints for retrieval, registration, and unregistration.
 */
@Controller
public class ProfileCachingController {

    private final ProfileCachingService profileCachingService;

    /**
     * Creates the controller with the required caching service dependency.
     *
     * @param profileCachingService service handling profile cache operations
     */
    public ProfileCachingController(ProfileCachingService profileCachingService) {
        this.profileCachingService = profileCachingService;
    }

    /**
     * Returns the cached profile for the provided Steam ID, if available.
     *
     * @param steamId optional Steam ID to look up; if absent, service determines default handling
     * @return cached profile details
     */
    @GetMapping("/api/profile/cache")
    public @ResponseBody ProfileCache getProfile(@RequestParam(required = false) String steamId) {
            return profileCachingService.getProfileCache(steamId);
    }

    /**
     * Returns cached profiles for a list of Steam IDs.
     *
     * @param steamIds list of Steam IDs to resolve
     * @return cached profiles matching the requested IDs
     */
    @GetMapping("/api/profile/caches")
    public @ResponseBody List<ProfileCache> getProfiles(@RequestParam List<String> steamIds) {
        return profileCachingService.getProfileCachesBySteamIds(steamIds);
    }

    /**
     * Disables caching for the specified Steam ID.
     *
     * @param steamId Steam ID to unregister from caching
     * @return operation success flag
     */
    @PutMapping("/api/profile/cache/unregister")
    public @ResponseBody BooleanResultResponse unregisterProfile(@RequestParam String steamId) {
        return profileCachingService.disableCaching(steamId);
    }

    /**
     * Enables caching for the specified Steam ID.
     *
     * @param steamId Steam ID to register for caching
     * @return operation success flag
     */
    @PutMapping("/api/profile/cache/register")
    public @ResponseBody BooleanResultResponse registerProfile(@RequestParam String steamId) {
        return profileCachingService.enableCaching(steamId);
    }

}
