package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.config.CacheConfig;
import codes.sharky.steamwidget.entity.Profile;
import codes.sharky.steamwidget.service.ProfileService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serves /sitemap.xml: the site's static pages plus one entry per claimed profile page. Replaces
 * the old static sitemap.xml file, which couldn't list profile pages since they don't exist
 * until claimed.
 */
@Controller
public class SitemapController {

    private static final String BASE_URL = "https://steam-widget.com";
    private static final DateTimeFormatter LASTMOD_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ProfileService profileService;

    public SitemapController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @Cacheable(cacheNames = CacheConfig.SITEMAP)
    public @ResponseBody String sitemap() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        appendStaticUrl(xml, "/", LocalDate.of(2026, 5, 30), "weekly", "1.0");
        appendStaticUrl(xml, "/playing-stats.html", LocalDate.of(2026, 5, 30), "weekly", "0.8");
        appendStaticUrl(xml, "/profile-metrics.html", LocalDate.of(2026, 5, 30), "weekly", "0.8");
        appendStaticUrl(xml, "/wrapped.html", LocalDate.of(2026, 10, 7), "weekly", "0.8");
        appendStaticUrl(xml, "/privacy.html", LocalDate.of(2026, 5, 30), "yearly", "0.3");

        List<Profile> claimedProfiles = profileService.getClaimedProfiles();
        for (Profile profile : claimedProfiles) {
            xml.append("  <url>\n");
            xml.append("    <loc>").append(BASE_URL).append("/u/").append(profile.getSteam64id()).append("</loc>\n");
            if (profile.getClaimedAt() != null) {
                xml.append("    <lastmod>").append(profile.getClaimedAt().format(LASTMOD_FORMAT)).append("</lastmod>\n");
            }
            xml.append("    <changefreq>daily</changefreq>\n");
            xml.append("    <priority>0.6</priority>\n");
            xml.append("  </url>\n");
        }

        xml.append("</urlset>\n");
        return xml.toString();
    }

    private void appendStaticUrl(StringBuilder xml, String path, LocalDate lastmod, String changefreq, String priority) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(BASE_URL).append(path).append("</loc>\n");
        xml.append("    <lastmod>").append(lastmod.format(LASTMOD_FORMAT)).append("</lastmod>\n");
        xml.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        xml.append("    <priority>").append(priority).append("</priority>\n");
        xml.append("  </url>\n");
    }
}
