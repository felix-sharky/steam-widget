package codes.sharky.steamwidget.utils;

import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import java.util.regex.Pattern;

/**
 * Utility methods for extracting client network metadata (IP, country, city)
 * from servlet requests, honoring common proxy/CDN headers.
 */
public class IPUtils {

    private IPUtils() {}

    /**
     * Simple pattern to validate IPv4 and IPv6 addresses.
     * IPv4: strict dotted-quad; IPv6: hex and colons only, with at least one colon.
     */
    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile(
            "^(?:" +
                    "(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)" +
                    "(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}" +
                    "|" +
                    "[0-9a-fA-F]{0,4}(?::[0-9a-fA-F]{0,4}){2,7}" +
                    ")$"
    );

    /**
     * Validates whether the provided string is a syntactically valid IPv4 or IPv6 address.
     *
     * @param ip candidate IP string to validate
     * @return true if the string matches the accepted IP address pattern
     */
    private static boolean isValidIpAddress(@NotNull String ip) {
        return IP_ADDRESS_PATTERN.matcher(ip).matches();
    }

    /**
     * Resolves the client IP address, preferring proxy/CDN headers and falling back to the remote address.
     * Checks `cf-connecting-ip`, then `X-Forwarded-For`, and finally {@link HttpServletRequest#getRemoteAddr()}.
     *
     * @param request current HTTP servlet request
     * @return best-effort client IP address string
     */
    public static String getIPAddress(@NotNull HttpServletRequest request) {
        String cfConnectingIpHeader = request.getHeader("cf-connecting-ip");
        if (!Strings.isNullOrEmpty(cfConnectingIpHeader)) {
            String cfIp = cfConnectingIpHeader.split(",")[0].trim();
            if (isValidIpAddress(cfIp)) {
                return cfIp;
            }
        }

        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (!Strings.isNullOrEmpty(xForwardedForHeader)) {
            String xffIp = xForwardedForHeader.split(",")[0].trim();
            if (isValidIpAddress(xffIp)) {
                return xffIp;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Retrieves the Cloudflare-provided country code (e.g., ISO alpha-2) from the request headers.
     *
     * @param request current HTTP servlet request
     * @return uppercased country code if present; otherwise null
     */
    public static @Nullable String getCountry(@NotNull HttpServletRequest request) {
        String country = request.getHeader("cf-ipcountry");
        return Strings.isNullOrEmpty(country) ? null : country.trim();
    }

    /**
     * Retrieves the Cloudflare-provided city name from the request headers.
     *
     * @param request current HTTP servlet request
     * @return city name if present; otherwise null
     */
    public static @Nullable String getCity(@NotNull HttpServletRequest request) {
        String city = request.getHeader("cf-ipcity");
        return Strings.isNullOrEmpty(city) ? null : city.trim();
    }

}
