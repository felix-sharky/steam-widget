package codes.sharky.steamwidget.model;

/**
 * The id and display label for a widget theme, as sent to the frontend so it can build the
 * style picker without hardcoding the list of themes.
 */
public record ThemeSummary(String id, String label) {
}
