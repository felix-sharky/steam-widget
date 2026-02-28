package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.entity.Profile;
import codes.sharky.steamwidget.model.ResolvedProfile;
import codes.sharky.steamwidget.service.ProfileService;
import codes.sharky.steamwidget.service.SteamWebAPIService;
import com.google.common.base.Strings;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing profile retrieval and vanity URL resolution endpoints.
 */
@RestController
public class ProfileController {

    private final ProfileService profileService;
    private final SteamWebAPIService steamWebAPIService;

    /**
     * Creates the controller with services for profile persistence and Steam Web API resolution.
     *
     * @param profileService    service providing profile lookups
     * @param steamWebAPIService service for resolving Steam IDs from vanity URLs
     */
    public ProfileController(ProfileService profileService, SteamWebAPIService steamWebAPIService) {
        this.profileService = profileService;
        this.steamWebAPIService = steamWebAPIService;
    }

    /**
     * Returns profile details for the provided Steam ID. If no ID is provided, delegates to service handling default behavior.
     *
     * @param steamId optional Steam ID to look up
     * @return stored profile or an empty profile if none exists
     */
    @GetMapping("/api/profile")
    public @ResponseBody Profile getProfile(@RequestParam(required = false) String steamId) {
        return profileService.getProfile(steamId);
    }

    /**
     * Resolves a vanity URL to a Steam64 ID using the Steam Web API.
     * Returns an empty response object when the vanity URL is blank or resolution fails.
     *
     * @param vanityUrl vanity URL segment to resolve
     * @return resolved profile containing the Steam ID when successful
     */
    @GetMapping("/api/profile/resolve")
    public @ResponseBody ResolvedProfile resolveVanityUrl(@RequestParam String vanityUrl) {
        ResolvedProfile profile = new ResolvedProfile();
        if (Strings.isNullOrEmpty(vanityUrl)) {
            return profile;
        }
        try {
            String steamid = steamWebAPIService.resolveSteamId(vanityUrl);
            profile.setSteamId(steamid);
        } catch (Exception ex) {
            // Ignore
        }
        return profile;
    }

}
