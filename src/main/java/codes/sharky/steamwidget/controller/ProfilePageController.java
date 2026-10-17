package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.entity.Profile;
import codes.sharky.steamwidget.model.ProfilePageView;
import codes.sharky.steamwidget.model.ProfileVisibility;
import codes.sharky.steamwidget.service.ProfilePageService;
import codes.sharky.steamwidget.service.ProfileService;
import codes.sharky.steamwidget.service.SessionAttributes;
import com.lukaspradel.steamapi.data.json.playersummaries.Player;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Serves the public, server-rendered Steam profile page at /u/{id}. Unclaimed pages render
 * noindex; claimed pages are indexable and show owner-chosen section visibility.
 */
@Controller
public class ProfilePageController {

    private static final Pattern STEAM_ID_64 = Pattern.compile("\\d{17}");

    private final ProfilePageService profilePageService;
    private final ProfileService profileService;

    public ProfilePageController(ProfilePageService profilePageService, ProfileService profileService) {
        this.profilePageService = profilePageService;
        this.profileService = profileService;
    }

    @GetMapping("/u/{idOrVanity}")
    public ModelAndView profilePage(@PathVariable String idOrVanity, HttpSession session, HttpServletResponse response) {
        String steamId64;
        if (STEAM_ID_64.matcher(idOrVanity).matches()) {
            steamId64 = idOrVanity;
        } else {
            String resolved = profilePageService.resolveVanity(idOrVanity);
            if (resolved == null || !STEAM_ID_64.matcher(resolved).matches()) {
                return new ModelAndView("profile-not-found", HttpStatus.NOT_FOUND);
            }
            RedirectView redirectView = new RedirectView("/u/" + resolved);
            redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
            return new ModelAndView(redirectView);
        }

        Player player = profilePageService.getCachedPlayer(steamId64);
        if (!ProfilePageService.isPublicProfile(player)) {
            return new ModelAndView("profile-not-found", HttpStatus.NOT_FOUND);
        }

        Profile profile = profileService.getProfile(steamId64);
        boolean isOwner = session != null && steamId64.equals(session.getAttribute(SessionAttributes.AUTHENTICATED_STEAM_ID));
        ProfileVisibility visibility = new ProfileVisibility(
                Boolean.TRUE.equals(profile.getClaimed()),
                isOwner,
                Boolean.TRUE.equals(profile.getShowNowPlaying()),
                Boolean.TRUE.equals(profile.getShowHeadlineStats()),
                Boolean.TRUE.equals(profile.getShowTopGames()),
                Boolean.TRUE.equals(profile.getShowWidgetGallery())
        );

        List<com.lukaspradel.steamapi.data.json.ownedgames.Game> ownedGames = profilePageService.getCachedTopOwnedGames(steamId64);
        List<com.lukaspradel.steamapi.data.json.recentlyplayedgames.Game> recentGames = profilePageService.getCachedTopRecentlyPlayedGames(steamId64);
        ProfilePageView view = profilePageService.buildView(steamId64, player, ownedGames, recentGames, visibility.showNowPlaying());

        response.setHeader("Cache-Control", "max-age=300, must-revalidate");
        ModelAndView mav = new ModelAndView("profile", "view", view);
        mav.addObject("visibility", visibility);
        return mav;
    }
}
