package codes.sharky.steamwidget.component;

import com.lukaspradel.steamapi.webapi.client.SteamWebApiClient;
import codes.sharky.steamwidget.config.SteamAPIConfiguration;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Represents the component responsible for creating and managing the {@link SteamWebApiClient} instance.
 * This component is crucial for enabling communication with the Steam Web API by providing a configured client.
 * It leverages the {@link SteamAPIConfiguration} to obtain the necessary API key for client initialization.
 */
@Component
public class SteamWebAPI {

    private final SteamAPIConfiguration configuration;

    @Getter
    private SteamWebApiClient client;

    public SteamWebAPI(SteamAPIConfiguration configuration) {
        this.configuration = configuration;
        init();
    }

    /**
     * Initializes the {@link SteamWebApiClient} with the API key obtained from the {@link SteamAPIConfiguration}.
     * This method constructs a new {@link SteamWebApiClient} instance using the API key provided in the
     * application's configuration, facilitating the interaction with the Steam Web API.
     */
    private void init() {
        client = new SteamWebApiClient.SteamWebApiClientBuilder(configuration.getKey()).build();
    }

}
