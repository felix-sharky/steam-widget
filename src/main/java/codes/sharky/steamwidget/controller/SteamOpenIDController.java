package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.component.SteamOpenID;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

@Controller
public class SteamOpenIDController {

    private final SteamOpenID steamOpenID;

    public SteamOpenIDController(SteamOpenID steamOpenID) {
        this.steamOpenID = steamOpenID;
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
    public void loginRedirect(@RequestParam Map<String, String> allParams, HttpServletRequest request, HttpServletResponse httpServletResponse) {
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

}
