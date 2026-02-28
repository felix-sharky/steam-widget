package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.component.SteamOpenID;
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
 * Controller handling Steam OpenID login and callback flows.
 * Builds redirect URLs based on the incoming request and persists the Steam ID in a cookie on success.
 */
@Controller
public class SteamOpenIDController {

    private final SteamOpenID steamOpenID;

    /**
     * Creates the controller with the Steam OpenID helper.
     *
     * @param steamOpenID component handling OpenID URL generation and verification
     */
    public SteamOpenIDController(SteamOpenID steamOpenID) {
        this.steamOpenID = steamOpenID;
    }

    /**
     * Initiates Steam OpenID login by redirecting the user to Steam's login page.
     *
     * @param request              incoming HTTP request used to build the base URL
     * @param httpServletResponse  response used to issue the redirect
     */
    @GetMapping("/steam/login")
    public void loginRedirect(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        httpServletResponse.setHeader("Location", steamOpenID.login(baseUrl + "/steam/login/callback"));
        httpServletResponse.setStatus(302);
    }

    /**
     * Handles the Steam OpenID callback, verifies the response, sets a Steam ID cookie, and redirects back to the app.
     * If verification fails, redirects to the base URL without setting the cookie.
     *
     * @param allParams            all callback parameters received from Steam
     * @param request              incoming HTTP request used to build the base URL and verify the OpenID response
     * @param httpServletResponse  response used to set cookies and issue the redirect
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
        steamIdCookie.setMaxAge(60 * 60 * 24 * 30);
        steamIdCookie.setSecure(request.isSecure());
        httpServletResponse.addCookie(steamIdCookie);

        httpServletResponse.setHeader("Location", baseUrl + "/?steamId=" + steamId64);
        httpServletResponse.setStatus(302);
    }

}
