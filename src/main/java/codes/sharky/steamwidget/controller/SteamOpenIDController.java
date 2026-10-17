package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.component.SteamOpenID;
import codes.sharky.steamwidget.service.ProfileService;
import codes.sharky.steamwidget.service.SessionAttributes;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

/**
 * Controller handling Steam OpenID login and callback flows.
 * Builds redirect URLs based on the incoming request, establishes a server-side session on
 * success, persists the Steam ID in a convenience cookie, and claims a profile page when the
 * login was initiated from that page's "Claim this page" link.
 */
@Controller
public class SteamOpenIDController {

    private final SteamOpenID steamOpenID;
    private final ProfileService profileService;

    public SteamOpenIDController(SteamOpenID steamOpenID, ProfileService profileService) {
        this.steamOpenID = steamOpenID;
        this.profileService = profileService;
    }

    /**
     * Initiates Steam OpenID login by redirecting the user to Steam's login page. If a
     * {@code returnTo} is provided (e.g. from a profile page's "Claim this page" link), it's
     * stashed in the session so the callback can redirect back there and claim the page.
     */
    @GetMapping("/steam/login")
    public void loginRedirect(@RequestParam(required = false) String returnTo,
                               HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        if (returnTo != null && returnTo.matches("/u/\\d{17}")) {
            request.getSession(true).setAttribute(SessionAttributes.CLAIM_RETURN_TO, returnTo);
        }

        httpServletResponse.setHeader("Location", steamOpenID.login(baseUrl + "/steam/login/callback"));
        httpServletResponse.setStatus(302);
    }

    /**
     * Handles the Steam OpenID callback. On success: establishes a real session identifying the
     * authenticated Steam ID (used to authorize claim/settings/un-claim actions — never the
     * convenience cookie below), sets the convenience cookie as before, and if the login was
     * initiated with a {@code returnTo} targeting the authenticated Steam ID's own profile page,
     * claims that page and redirects there. Otherwise redirects to the base URL as before.
     */
    @GetMapping("/steam/login/callback")
    public void loginCallback(@RequestParam Map<String, String> allParams, HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        String steamId64 = steamOpenID.verify(request.getRequestURL().toString(), request.getParameterMap());

        if (steamId64 == null) {
            httpServletResponse.setHeader("Location", baseUrl);
            httpServletResponse.setStatus(302);
            return;
        }

        Cookie steamIdCookie = new Cookie("steamId", steamId64);
        steamIdCookie.setPath("/");
        steamIdCookie.setMaxAge(60 * 60 * 24 * 7); // 7 days — convenience pre-fill, GDPR-minimal
        steamIdCookie.setSecure(request.isSecure());
        steamIdCookie.setAttribute("SameSite", "Lax");
        httpServletResponse.addCookie(steamIdCookie);

        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(SessionAttributes.AUTHENTICATED_STEAM_ID, steamId64);

        Object claimReturnTo = session.getAttribute(SessionAttributes.CLAIM_RETURN_TO);
        session.removeAttribute(SessionAttributes.CLAIM_RETURN_TO);
        String returnTo = claimReturnTo == null ? null : claimReturnTo.toString();

        if (returnTo != null && returnTo.equals("/u/" + steamId64)) {
            profileService.claimProfile(steamId64);
            httpServletResponse.setHeader("Location", baseUrl + returnTo);
            httpServletResponse.setStatus(302);
            return;
        }

        httpServletResponse.setHeader("Location", baseUrl + "/?steamId=" + steamId64);
        httpServletResponse.setStatus(302);
    }

    /**
     * Logs out: invalidates the claim/settings session (the convenience cookie is left alone —
     * it was never used for authorization) and redirects home.
     */
    @GetMapping("/steam/logout")
    public void logout(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        httpServletResponse.setHeader("Location", baseUrl + "/");
        httpServletResponse.setStatus(302);
    }

}
