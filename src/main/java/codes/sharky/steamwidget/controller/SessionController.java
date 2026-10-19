package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.model.SessionInfo;
import codes.sharky.steamwidget.service.SessionAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the current visitor's claim/settings session state to client-side scripts, so static
 * pages (which can't read {@code HttpSession} server-side) can show a site-wide login/identity
 * indicator in the nav bar.
 */
@RestController
public class SessionController {

    @GetMapping("/api/session/me")
    public SessionInfo getCurrentSession(HttpServletRequest request) {
        // getSession(false): never create a session just for an anonymous visitor checking
        // whether they're logged in — every page load calls this endpoint.
        HttpSession session = request.getSession(false);
        String steamId = session == null ? null : (String) session.getAttribute(SessionAttributes.AUTHENTICATED_STEAM_ID);
        return new SessionInfo(steamId);
    }
}
