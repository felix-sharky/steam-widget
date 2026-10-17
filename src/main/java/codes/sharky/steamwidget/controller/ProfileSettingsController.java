package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.entity.Profile;
import codes.sharky.steamwidget.service.ProfileService;
import codes.sharky.steamwidget.service.SessionAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Owner-only settings for a claimed /u/{steamId} profile page: toggling section visibility and
 * un-claiming. Every route redirects non-owners (no session, wrong session, or unclaimed
 * profile) straight back to /u/{steamId} without changing anything — the page exists either way,
 * they just can't manage it.
 */
@Controller
public class ProfileSettingsController {

    private final ProfileService profileService;

    public ProfileSettingsController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/u/{steamId}/settings")
    public ModelAndView settingsPage(@PathVariable String steamId, HttpSession session) {
        if (!isAuthorized(session, steamId)) {
            return redirectToProfile(steamId);
        }
        Profile profile = profileService.getProfile(steamId);
        ModelAndView mav = new ModelAndView("profile-settings");
        mav.addObject("steamId", steamId);
        mav.addObject("profile", profile);
        return mav;
    }

    @PostMapping("/u/{steamId}/settings")
    public ModelAndView updateSettings(@PathVariable String steamId, HttpSession session,
                                        @RequestParam(defaultValue = "false") boolean showNowPlaying,
                                        @RequestParam(defaultValue = "false") boolean showHeadlineStats,
                                        @RequestParam(defaultValue = "false") boolean showTopGames,
                                        @RequestParam(defaultValue = "false") boolean showWidgetGallery) {
        if (!isAuthorized(session, steamId)) {
            return redirectToProfile(steamId);
        }
        profileService.updateSectionVisibility(steamId, showNowPlaying, showHeadlineStats, showTopGames, showWidgetGallery);
        return new ModelAndView(new RedirectView("/u/" + steamId + "/settings"));
    }

    @PostMapping("/u/{steamId}/unclaim")
    public ModelAndView unclaim(@PathVariable String steamId, HttpSession session) {
        if (!isAuthorized(session, steamId)) {
            return redirectToProfile(steamId);
        }
        profileService.unclaimProfile(steamId);
        return redirectToProfile(steamId);
    }

    private ModelAndView redirectToProfile(String steamId) {
        return new ModelAndView(new RedirectView("/u/" + steamId));
    }

    private boolean isAuthorized(HttpSession session, String steamId) {
        return isOwner(session, steamId) && profileService.isProfileClaimed(steamId);
    }

    private boolean isOwner(HttpSession session, String steamId) {
        if (session == null) {
            return false;
        }
        return steamId.equals(session.getAttribute(SessionAttributes.AUTHENTICATED_STEAM_ID));
    }
}
