package codes.sharky.steamwidget.scheduled;

import codes.sharky.steamwidget.repository.HitRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduled job that enforces a two-stage GDPR data-retention policy on raw hit records.
 *
 * <p><b>Stage 1 — IP nullification</b> (short window, default 90 days):<br>
 * IP addresses are personal data under the GDPR. Rather than deleting whole rows
 * (which would break hit-count analytics), this job nullifies only the {@code ip} column
 * for records older than the configured window, preserving all aggregated metrics while
 * erasing the personal identifier.</p>
 *
 * <p><b>Stage 2 — Raw record deletion</b> (long window, default 730 days / 2 years):<br>
 * Even after the IP is gone, a hit record still contains a pseudonymous Steam64 ID
 * (linkable to a person via Steam). After the long-term retention window, the entire
 * raw hit record is deleted. Aggregated metric views continue to serve historic counts
 * only for the retained period; older data is fully purged.</p>
 */
@Component
@EnableScheduling
@Slf4j
public class ScheduledRetention {

    private final HitRepository hitRepository;
    private final Environment env;

    /** Days after which the IP field in hit records is nullified. */
    @Value("${hit.ip.retention.days:90}")
    private int ipRetentionDays;

    /** Days after which the entire raw hit record is deleted. Must be &gt; ipRetentionDays. */
    @Value("${hit.raw.retention.days:730}")
    private int rawRetentionDays;

    public ScheduledRetention(HitRepository hitRepository, Environment env) {
        this.hitRepository = hitRepository;
        this.env = env;
    }

    /**
     * Runs daily at 03:00 (server time) on the primary profile.
     * Executes both retention stages in order: nullify IPs, then delete fully-aged records.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void enforceRetentionPolicy() {
        if (!env.acceptsProfiles(Profiles.of("primary"))) {
            return;
        }

        // Stage 1: nullify IP addresses older than ipRetentionDays
        LocalDateTime ipCutoff = LocalDateTime.now().minusDays(ipRetentionDays);
        int nullified = hitRepository.nullifyIpBefore(ipCutoff);
        if (nullified > 0) {
            log.info("Retention stage 1: nullified IP on {} hit record(s) older than {} days.", nullified, ipRetentionDays);
        } else {
            log.debug("Retention stage 1: no hit records required IP nullification.");
        }

        // Stage 2: delete raw records older than rawRetentionDays
        LocalDateTime rawCutoff = LocalDateTime.now().minusDays(rawRetentionDays);
        int deleted = hitRepository.deleteOlderThan(rawCutoff);
        if (deleted > 0) {
            log.info("Retention stage 2: deleted {} raw hit record(s) older than {} days.", deleted, rawRetentionDays);
        } else {
            log.debug("Retention stage 2: no raw hit records required deletion.");
        }
    }
}

