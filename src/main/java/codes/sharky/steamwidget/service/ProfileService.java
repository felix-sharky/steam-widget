package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.entity.Hit;
import codes.sharky.steamwidget.entity.PlayingTracker;
import codes.sharky.steamwidget.entity.Profile;
import codes.sharky.steamwidget.repository.HitRepository;
import codes.sharky.steamwidget.repository.PlayingTrackerRepository;
import codes.sharky.steamwidget.repository.ProfileRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {

    private final ProfileRepository repository;
    private final HitRepository hitRepository;
    private final PlayingTrackerRepository playingTrackerRepository;

    public ProfileService(ProfileRepository repository, HitRepository hitRepository, PlayingTrackerRepository playingTrackerRepository) {
        this.repository = repository;
        this.hitRepository = hitRepository;
        this.playingTrackerRepository = playingTrackerRepository;
    }

    /**
     * Retrieves the profile information for a given Steam ID. If the profile does not exist, it returns a new, empty Profile object.
     *
     * @param steamId The Steam ID of the profile to retrieve.
     * @return A Profile object containing the profile information. Returns an empty Profile object if the profile does not exist.
     */
    public Profile getProfile(String steamId) {
        Optional<Profile> profileOptional = repository.findById(steamId);
        return profileOptional.orElseGet(Profile::new);
    }

    /**
     * Retrieves the total number of hits for a given profile identified by the Steam ID.
     *
     * @param steamId The Steam ID of the profile for which the hit count is being queried.
     * @return The total number of hits for the profile. Returns 0 if the profile does not exist.
     */
    public long getProfileHitByProfile(String steamId) {
        Optional<Profile> profileOptional = repository.findById(steamId);
        if (profileOptional.isPresent()) {
            return profileOptional.get().getHits();
        } else {
            return 0;
        }
    }

    /**
     * Asynchronously adds a hit to a profile identified by the Steam ID. If the profile does not exist, it creates a new profile
     * with the given Steam ID and name, initializing the hit count to 1. Otherwise, it increments the hit count for the existing profile.
     * Additionally, it records the hit details including the Steam ID, timestamp, purpose, and IP address in the Hit entity.
     *
     * @param steamId       The Steam ID of the user for whom the hit is being recorded.
     * @param name          The name of the user associated with the Steam ID.
     * @param purpose       The reason for the hit, describing why the user's information was accessed.
     * @param ip            The IP address from which the request to access the user's information originated.
     * @param localDateTime The timestamp when the hit occurred.
     */
    @Async
    public void addHitToProfile(String steamId, String name, String purpose, String ip, LocalDateTime localDateTime) {
        if (!repository.existsById(steamId)) {
            Profile profile = new Profile(steamId, name, 1L);
            repository.save(profile);
        } else {
            repository.incrementHits(steamId, name);
        }

        Hit hit = new Hit(steamId, localDateTime, purpose, ip);
        hitRepository.save(hit);
    }

    /**
     * Inserts a new profile or updates an existing profile with the given Steam ID, name, and tracking status.
     * If the profile does not exist, it creates a new profile with the provided details and initializes the hit count to 0.
     * If the profile already exists, it updates the name and tracking status of the existing profile.
     *
     * @param steamId  The Steam ID of the profile to be inserted or updated.
     * @param name     The name of the user associated with the Steam ID.
     * @param tracking The tracking status indicating whether tracking is enabled for this profile.
     */
    public void upsertProfile(String steamId, String name, boolean tracking) {
        if (!repository.existsById(steamId)) {
            Profile profile = new Profile(steamId, name, 0L, tracking);
            repository.save(profile);
        } else {
            repository.updateNameAndTracking(steamId, name, tracking);
        }
    }

    public boolean profileTrackingActive(String steamId) {
        return repository.existsBySteam64idAndTrackingIsTrue(steamId);
    }

    public void resetPlayerTrackers(String steamId) {
        playingTrackerRepository.deleteAllBySteam64id(steamId);
    }

    /**
     * Retrieves a list of all profiles that have tracking enabled.
     *
     * @return A list of {@link Profile} objects with tracking enabled.
     */
    public List<Profile> getProfilesWithTracking() {
        return repository.findAllByTrackingIsTrue();
    }

    /**
     * Retrieves the most recent {@link PlayingTracker} entry for a given Steam ID and application ID (game).
     * If no entry is found, it returns a new, empty {@link PlayingTracker} object.
     *
     * @param steamId The Steam ID of the user.
     * @param appId   The application ID (game) for which the playing tracker is being queried.
     * @return A {@link PlayingTracker} object containing the most recent playing tracker information.
     *         Returns an empty {@link PlayingTracker} object if no entry is found.
     */
    public Optional<PlayingTracker> getLastPlayingTracker(String steamId, String appId) {
        return playingTrackerRepository.findFirstBySteam64idAndGameOrderByDatetimeDesc(steamId, appId);
    }

    /**
     * Saves a {@link PlayingTracker} entity to the database.
     *
     * @param playingTracker The {@link PlayingTracker} object to be saved.
     */
    public void savePlayingTracker(PlayingTracker playingTracker) {
        playingTrackerRepository.save(playingTracker);
    }

    /**
     * Retrieves the number of hits for a given profile identified by the Steam ID, optionally filtered by purpose.
     * If the purpose is not specified (empty string), it returns the total hit count for the profile.
     *
     * @param steamId The Steam ID of the profile for which the hit count is being queried.
     * @param purpose The purpose for filtering the hits. If empty, all hits for the profile are counted.
     * @return The number of hits for the profile, filtered by purpose if specified.
     */
    public long getHitByProfileAndPurpose(String steamId, @NotNull String purpose) {
        if (purpose.isEmpty()) {
            return hitRepository.countHitsBySteam64id(steamId);
        } else {
            return hitRepository.countHitsBySteam64idAndPurpose(steamId, purpose);
        }
    }

}
