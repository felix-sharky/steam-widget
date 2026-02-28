package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.component.SteamOpenID;
import codes.sharky.steamwidget.service.ProfileService;
import codes.sharky.steamwidget.service.SteamTrackerService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

/**
 * Controller that drives Steam OpenID login flows for tracking status, enabling, and disabling.
 * Redirects users to Steam, processes callbacks, and persists the Steam ID in a cookie for client use.
 */
@Controller
public class SteamTrackingController {

    private final SteamOpenID steamOpenID;

    private final SteamTrackerService steamTrackerService;

    /**
     * Creates the tracking controller with OpenID helper and tracking service dependencies.
     *
     * @param steamOpenID         component handling Steam OpenID login and verification
     * @param steamTrackerService service used to enable or disable gameplay tracking
     */
    public SteamTrackingController(SteamOpenID steamOpenID, SteamTrackerService steamTrackerService) {
        this.steamOpenID = steamOpenID;
        this.steamTrackerService = steamTrackerService;
    }

    /**
     * Initiates a login flow to view tracking status by redirecting to Steam OpenID.
     * On return, the callback will redirect to the tracking page with the Steam ID.
     *
     * @param request             incoming request used to construct the base URL
     * @param httpServletResponse response used to issue the redirect
     */
    @GetMapping("/steam/tracking/status")
    public void trackingStatusRedirect(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        httpServletResponse.setHeader("Location", steamOpenID.login(baseUrl + "/steam/tracking/status/callback"));
        httpServletResponse.setStatus(302);
    }

    /**
     * Handles the tracking status callback: verifies the OpenID response, sets the Steam ID cookie, and redirects.
     *
     * @param allParams           all callback parameters returned by Steam
     * @param request             incoming request used to construct URLs and verify the response
     * @param httpServletResponse response used to set cookies and issue the redirect
     */
    @GetMapping("/steam/tracking/status/callback")
    public void trackingStatusCallback(@RequestParam Map<String, String> allParams, HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        String steamId64 = steamOpenID.verify(request.getRequestURL().toString(), request.getParameterMap());

        if (steamId64 == null) {
            httpServletResponse.setHeader("Location", baseUrl + "/tracking.html");
            httpServletResponse.setStatus(302);
            return;
        }

        addSteamIdCookie(httpServletResponse, request, steamId64);

        httpServletResponse.setHeader("Location", baseUrl + "/tracking.html?steamId=" + steamId64);
        httpServletResponse.setStatus(302);
    }

    /**
     * Initiates a login flow to enable tracking by redirecting to Steam OpenID.
     *
     * @param request             incoming request used to construct the base URL
     * @param httpServletResponse response used to issue the redirect
     */
    @GetMapping("/steam/tracking/enable")
    public void trackingEnableRedirect(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        httpServletResponse.setHeader("Location", steamOpenID.login(baseUrl + "/steam/tracking/enable/callback"));
        httpServletResponse.setStatus(302);
    }

    /**
     * Handles the enable tracking callback: verifies OpenID, enables tracking, sets cookie, and redirects.
     *
     * @param allParams           all callback parameters returned by Steam
     * @param request             incoming request used to construct URLs and verify the response
     * @param httpServletResponse response used to set cookies and issue the redirect
     */
    @GetMapping("/steam/tracking/enable/callback")
    public void trackingEnableCallback(@RequestParam Map<String, String> allParams, HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        String steamId64 = steamOpenID.verify(request.getRequestURL().toString(), request.getParameterMap());

        if (steamId64 == null) {
            httpServletResponse.setHeader("Location", baseUrl + "/tracking.html");
            httpServletResponse.setStatus(302);
            return;
        }

        steamTrackerService.enableTracking(steamId64);
        addSteamIdCookie(httpServletResponse, request, steamId64);

        httpServletResponse.setHeader("Location", baseUrl + "/tracking.html?steamId=" + steamId64);
        httpServletResponse.setStatus(302);
    }

    /**
     * Initiates a login flow to disable tracking by redirecting to Steam OpenID.
     *
     * @param request             incoming request used to construct the base URL
     * @param httpServletResponse response used to issue the redirect
     */
    @GetMapping("/steam/tracking/disable")
    public void trackingDisableRedirect(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        httpServletResponse.setHeader("Location", steamOpenID.login(baseUrl + "/steam/tracking/disable/callback"));
        httpServletResponse.setStatus(302);
    }

    /**
     * Handles the disable tracking callback: verifies OpenID, disables tracking, sets cookie, and redirects.
     *
     * @param allParams           all callback parameters returned by Steam
     * @param request             incoming request used to construct URLs and verify the response
     * @param httpServletResponse response used to set cookies and issue the redirect
     */
    @GetMapping("/steam/tracking/disable/callback")
    public void trackingDisableCallback(@RequestParam Map<String, String> allParams, HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        String steamId64 = steamOpenID.verify(request.getRequestURL().toString(), request.getParameterMap());

        if (steamId64 == null) {
            httpServletResponse.setHeader("Location", baseUrl + "/tracking.html");
            httpServletResponse.setStatus(302);
            return;
        }

        steamTrackerService.disableTracking(steamId64);
        addSteamIdCookie(httpServletResponse, request, steamId64);

        httpServletResponse.setHeader("Location", baseUrl + "/tracking.html?steamId=" + steamId64);
        httpServletResponse.setStatus(302);
    }

    /**
     * Adds or updates the Steam ID cookie shared across the application.
     *
     * @param response HTTP response used to write the cookie
     * @param request  current request used to inherit security attributes
     * @param steamId64 Steam64 ID value to persist
     */
    private void addSteamIdCookie(HttpServletResponse response, HttpServletRequest request, String steamId64) {
        Cookie steamIdCookie = new Cookie("steamId", steamId64);
        steamIdCookie.setPath("/");
        steamIdCookie.setMaxAge(60 * 60 * 24 * 30);
        steamIdCookie.setSecure(request.isSecure());
        response.addCookie(steamIdCookie);
    }

}
