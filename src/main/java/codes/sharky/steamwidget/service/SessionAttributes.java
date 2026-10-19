package codes.sharky.steamwidget.service;

/** Shared HttpSession attribute names used across the claim/settings flow. */
public final class SessionAttributes {

    /** The Steam ID the current session authenticated as, via Steam OpenID login. */
    public static final String AUTHENTICATED_STEAM_ID = "authenticatedSteamId";

    /** Pending claim target stashed before redirecting to Steam, read back on callback. */
    public static final String CLAIM_RETURN_TO = "claimReturnTo";

    private SessionAttributes() {
    }
}
