package codes.sharky.steamwidget.utils;

/**
 * Shared mapping from a Steam {@code personastate} value to its display label. Used by both
 * {@link codes.sharky.steamwidget.service.SteamWidgetService} (widget image rendering) and
 * {@link codes.sharky.steamwidget.service.ProfilePageService} (the /u/{id} profile page) so the
 * "online / busy / away / ..." mapping lives in exactly one place.
 */
public class PersonaStateFormatter {

    private PersonaStateFormatter() {}

    /**
     * Maps a Steam {@code personastate} value to its display label.
     *
     * @param personaState the player's personastate (0-6), or null if unknown
     * @return the mapped label, or "Unknown" for null/out-of-range values
     */
    public static String formatPersonaState(Long personaState) {
        int state = personaState == null ? -1 : personaState.intValue();
        return switch (state) {
            case 0 -> "Offline";
            case 1 -> "Online";
            case 2 -> "Busy";
            case 3 -> "Away";
            case 4 -> "Snooze";
            case 5 -> "Looking to trade";
            case 6 -> "Looking to play";
            default -> "Unknown";
        };
    }
}
