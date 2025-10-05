package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.entity.Hit;
import codes.sharky.steamwidget.entity.Profile;
import codes.sharky.steamwidget.repository.HitRepository;
import codes.sharky.steamwidget.repository.ProfileRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ProfileService {

    private final ProfileRepository repository;
    private final HitRepository hitRepository;

    public ProfileService(ProfileRepository repository, HitRepository hitRepository) {
        this.repository = repository;
        this.hitRepository = hitRepository;
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
