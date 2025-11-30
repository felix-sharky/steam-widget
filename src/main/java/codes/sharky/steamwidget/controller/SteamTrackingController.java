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

@Controller
public class SteamTrackingController {

    private final SteamOpenID steamOpenID;

    private final SteamTrackerService steamTrackerService;

    public SteamTrackingController(SteamOpenID steamOpenID, SteamTrackerService steamTrackerService) {
        this.steamOpenID = steamOpenID;
        this.steamTrackerService = steamTrackerService;
    }

    @GetMapping("/steam/tracking/status")
    public void trackingStatusRedirect(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        httpServletResponse.setHeader("Location", steamOpenID.login(baseUrl + "/steam/tracking/status/callback"));
        httpServletResponse.setStatus(302);
    }

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

    @GetMapping("/steam/tracking/enable")
    public void trackingEnableRedirect(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        httpServletResponse.setHeader("Location", steamOpenID.login(baseUrl + "/steam/tracking/enable/callback"));
        httpServletResponse.setStatus(302);
    }

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

    @GetMapping("/steam/tracking/disable")
    public void trackingDisableRedirect(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        httpServletResponse.setHeader("Location", steamOpenID.login(baseUrl + "/steam/tracking/disable/callback"));
        httpServletResponse.setStatus(302);
    }

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

    private void addSteamIdCookie(HttpServletResponse response, HttpServletRequest request, String steamId64) {
        Cookie steamIdCookie = new Cookie("steamId", steamId64);
        steamIdCookie.setPath("/");
        steamIdCookie.setMaxAge(60 * 60 * 24 * 30);
        steamIdCookie.setSecure(request.isSecure());
        response.addCookie(steamIdCookie);
    }

}
