package codes.sharky.steamwidget.model;

/**
 * The current visitor's claim/settings session state, for client-side scripts that can't read
 * {@code HttpSession} directly (static pages, not server-rendered).
 */
public record SessionInfo(String steamId) {
}
