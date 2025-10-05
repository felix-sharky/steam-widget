package codes.sharky.steamwidget.component;

import lombok.extern.slf4j.Slf4j;
import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Steam OpenID Login Helper
 * <p>Represents the functionality for integrating Steam OpenID authentication within an application.
 * <p>This class provides methods to initiate the login process, verify the Steam OpenID login, and manage discovery information.
 * <p>
 * <p>Usage:</p>
 * <p>
 * Generate a new openid object
 * <br />
 * {@code SteamOpenID openid = new SteamOpenID() }
 * <p/>
 * <p>
 * Redirect the user to the steam login page
 * <br />
 * {@code openid.login("http://www.mysite.com/postLogin") }
 * <p>
 * This will return null or a string containing the long variant of the steam id.
 * <br />
 * {@code String steamId64 = openid.verify(request.getRequestURL().toString(), request.getParameterMap());}
 * <p/>
 */
@Component
@Slf4j
public class SteamOpenID {
    private static final String STEAM_OPENID = "http://steamcommunity.com/openid";
    private final ConsumerManager manager;
    private final Pattern STEAM_REGEX = Pattern.compile("(\\d+)");
    private DiscoveryInformation discovered;

    /**
     * Creates the {@link ConsumerManager} and sets up
     * the {@link DiscoveryInformation}
     */
    public SteamOpenID() {
        manager = new ConsumerManager();
        manager.setMaxAssocAttempts(0);
        try {
            discovered = manager.associate(manager.discover(STEAM_OPENID));
        } catch (DiscoveryException e) {
            log.error("Error while discovering OpenID:\n", e);
            discovered = null;
        }
    }

    /**
     * Initiates the Steam OpenID login process by generating an authentication request.
     * <p>This method prepares an OpenID authentication request using the discovered information
     * from the Steam OpenID provider.
     * <p>If the discovery process has not been successfully completed
     * prior to this call, the method will return {@code null}, indicating that the login process cannot proceed.
     * <p>Upon successful creation of the authentication request, this method returns the URL to which the user
     * should be redirected to complete the login process with Steam.
     *
     * @param callbackUrl The absolute URL to which Steam should redirect users after they have authenticated.
     *                    This URL is where the application will receive and process the OpenID login response.
     * @return A string representing the URL of the Steam OpenID login page, to which the user should be redirected,
     * or {@code null} if the authentication request cannot be generated.
     */
    public String login(String callbackUrl) {
        if (this.discovered == null) {
            return null;
        }
        try {
            AuthRequest authReq = manager.authenticate(this.discovered, callbackUrl);
            return authReq.getDestinationUrl(true);
        } catch (MessageException | ConsumerException e) {
            log.error("Error while generating login url:\n", e);
        }
        return null;
    }

    /**
     * Verifies the Steam OpenID login by processing the response received from Steam.
     * <p>This method checks if the discovery information is set (indicating a prior successful call to {@link #login(String)}),
     * then attempts to verify the login response against the discovered information.
     * <p>If successful, it extracts and returns the Steam Community ID from the response.
     *
     * @param receivingUrl The URL that received the login response. This should be the same as the callback URL used in the {@link #login(String)} method.
     * @param responseMap  A map containing the response parameters from Steam's login attempt. These parameters include information necessary for verification.
     * @return The Steam Community ID as a string if verification is successful; {@code null} if the discovery information is not set or if verification fails.
     */
    public String verify(String receivingUrl, Map responseMap) {
        if (this.discovered == null) {
            return null;
        }
        ParameterList responseList = new ParameterList(responseMap);
        try {
            VerificationResult verification = manager.verify(receivingUrl, responseList, this.discovered);
            Identifier verifiedId = verification.getVerifiedId();
            if (verifiedId != null) {
                String id = verifiedId.getIdentifier();
                Matcher matcher = STEAM_REGEX.matcher(id);
                if (matcher.find()) {
                    System.out.println();
                    return matcher.group(1);
                }
            }
        } catch (MessageException | DiscoveryException | AssociationException e) {
            log.error("Error while verifying login callback:\n", e);
        }
        return null;
    }
}
