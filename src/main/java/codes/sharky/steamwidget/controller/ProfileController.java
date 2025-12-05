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

@RestController
public class ProfileController {

    private final ProfileService profileService;
    private final SteamWebAPIService steamWebAPIService;

    public ProfileController(ProfileService profileService, SteamWebAPIService steamWebAPIService) {
        this.profileService = profileService;
        this.steamWebAPIService = steamWebAPIService;
    }

    @GetMapping("/api/profile")
    public @ResponseBody Profile getProfile(@RequestParam(required = false) String steamId) {
        return profileService.getProfile(steamId);
    }

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
