package codes.sharky.steamwidget.config;

import codes.sharky.steamwidget.component.SteamWebAPI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Steam API properties.
 * This class is responsible for mapping the Steam API properties defined in the application's configuration
 * files (e.g., application.properties or application.yml) to Java objects. It also scans for components
 * within the same package as the SteamWebAPI class.
 */
@Configuration
@ConfigurationProperties(prefix = "steam.api")
@ComponentScan(basePackageClasses = SteamWebAPI.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SteamAPIConfiguration {

    private String key;

}