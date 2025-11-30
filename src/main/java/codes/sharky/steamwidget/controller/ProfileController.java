package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.entity.Profile;
import codes.sharky.steamwidget.service.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/api/profile")
    public @ResponseBody Profile getProfile(@RequestParam(required = false) String steamId) {
        return profileService.getProfile(steamId);
    }

}
