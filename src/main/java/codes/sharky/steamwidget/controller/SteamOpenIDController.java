package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.component.SteamOpenID;
import codes.sharky.steamwidget.service.SteamTrackerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
public class SteamOpenIDController {

    private final SteamOpenID steamOpenID;

    private final SteamTrackerService steamTrackerService;

    public SteamOpenIDController(SteamOpenID steamOpenID, SteamTrackerService steamTrackerService) {
        this.steamOpenID = steamOpenID;
        this.steamTrackerService = steamTrackerService;
    }

    @GetMapping("/steam/login")
    public void loginRedirect(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        httpServletResponse.setHeader("Location", steamOpenID.login(baseUrl + "/steam/login/callback"));
        httpServletResponse.setStatus(302);
    }

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

        httpServletResponse.setHeader("Location", baseUrl + "/?steamId=" + steamId64);
        httpServletResponse.setStatus(302);
    }

    @GetMapping("/steam/tracking/toggle")
    public void trackingRedirect(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        httpServletResponse.setHeader("Location", steamOpenID.login(baseUrl + "/steam/tracking/toggle/callback"));
        httpServletResponse.setStatus(302);
    }

    @GetMapping("/steam/tracking/toggle/callback")
    public void trackingCallback(@RequestParam Map<String, String> allParams, HttpServletRequest request, HttpServletResponse httpServletResponse) {
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

        steamTrackerService.toggleTracking(steamId64);

        httpServletResponse.setHeader("Location", baseUrl + "/?steamId=" + steamId64);
        httpServletResponse.setStatus(302);
    }

}
