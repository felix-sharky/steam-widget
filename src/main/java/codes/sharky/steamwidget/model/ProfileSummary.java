package codes.sharky.steamwidget.model;

/**
 * Minimal live Steam profile info (name and avatar) resolved directly from the Steam Web API,
 * for pages that need a profile picture without requiring the profile to be tracked/cached.
 */
public record ProfileSummary(String steamId, String name, String avatarUrl, String profileUrl) {
}
