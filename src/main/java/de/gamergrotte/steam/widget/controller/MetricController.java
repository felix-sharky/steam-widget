package de.gamergrotte.steam.widget.controller;

import com.lukaspradel.steamapi.core.exception.SteamApiException;
import de.gamergrotte.steam.widget.entity.Profile;
import de.gamergrotte.steam.widget.service.SteamWidgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller responsible for handling metric-related requests for Steam widgets.
 * Provides endpoints for retrieving profile information and hit metrics for a given Steam ID.
 */
@Controller
public class MetricController {

    private final SteamWidgetService steamWidgetService;

    public MetricController(SteamWidgetService steamWidgetService) {
        this.steamWidgetService = steamWidgetService;
    }

    /**
     * Retrieves the profile information for a given Steam ID.
     * Resolves the Steam ID to its canonical form before fetching the profile.
     * Returns a ResponseEntity containing the profile data or a NOT_FOUND status if the profile does not exist.
     *
     * @param id The Steam ID or custom URL part of the user whose profile is being requested.
     * @return ResponseEntity containing the Profile object or a NOT_FOUND status.
     * @throws SteamApiException If there is an issue with accessing the Steam Web API.
     */
    @GetMapping(value = "/metric", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody ResponseEntity<Profile> getProfile(@RequestParam(name = "id") String id) throws SteamApiException {
        String resolvedId = steamWidgetService.resolveSteamId(id);
        Profile profile = steamWidgetService.getProfile(resolvedId);
        return new ResponseEntity<>(profile, profile.getSteam64id() == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    /**
     * Retrieves the total number of hits for a given profile identified by the Steam ID.
     * Resolves the Steam ID to its canonical form before fetching the hit count.
     * Returns a ResponseEntity containing the hit count or a NOT_FOUND status if the profile does not exist.
     *
     * @param id The Steam ID or custom URL part of the user whose hit count is being requested.
     * @return ResponseEntity containing the total hit count or a NOT_FOUND status.
     * @throws SteamApiException If there is an issue with accessing the Steam Web API.
     */
    @GetMapping(value = "/metric")
    public @ResponseBody ResponseEntity<Long> getProfileHit(@RequestParam(name = "id") String id) throws SteamApiException {
        String resolvedId = steamWidgetService.resolveSteamId(id);
        long hits = steamWidgetService.getProfileHitByProfile(resolvedId);
        return new ResponseEntity<>(hits, hits == 0 ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    /**
     * Retrieves the number of hits for a given profile, optionally filtered by purpose.
     * Resolves the Steam ID to its canonical form before fetching the hit count.
     * If a purpose is specified, the hit count is filtered accordingly; otherwise, the total hit count is returned.
     * Returns a ResponseEntity containing the filtered hit count or a NOT_FOUND status if no hits are found.
     *
     * @param id      The Steam ID or custom URL part of the user whose hit count is being requested.
     * @param purpose Optional parameter to filter hits by purpose. If not provided, all hits are counted.
     * @return ResponseEntity containing the filtered hit count or a NOT_FOUND status.
     * @throws SteamApiException If there is an issue with accessing the Steam Web API.
     */
    @GetMapping(value = "/metric/hits")
    public @ResponseBody ResponseEntity<Long> getHits(@RequestParam(name = "id") String id, @RequestParam(name = "purpose", required = false, defaultValue = "General") String purpose) throws SteamApiException {
        String resolvedId = steamWidgetService.resolveSteamId(id);
        long hits = steamWidgetService.getHitByProfileAndPurpose(resolvedId, purpose);
        return new ResponseEntity<>(hits, hits == 0 ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }
}