package de.gamergrotte.steam.widget.controller;

import com.lukaspradel.steamapi.core.exception.SteamApiException;
import com.lukaspradel.steamapi.data.json.playersummaries.Player;
import de.gamergrotte.steam.widget.model.ShowedGames;
import de.gamergrotte.steam.widget.service.SteamWidgetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Controller for handling requests related to Steam widgets.
 * Provides endpoints for generating and retrieving Steam user widgets in both HTML and image formats.
 */
@Controller
public class WidgetController {

    @Autowired
    private SteamWidgetService steamWidgetService;

    /**
     * Handles requests to generate an image widget for a Steam user.
     * Generates a BufferedImage for the user and returns it as a byte array in PNG format.
     *
     * @param id              The Steam ID of the user for whom the widget image is being generated.
     * @param gameList        The type of games to be shown on the widget (e.g., top recent games, top total games, recent games).
     * @param gameListSize    The number of games to be displayed on the widget.
     * @param playingRightNow A boolean indicating whether to show the game the user is currently playing.
     * @param purpose         Optional parameter indicating the purpose of the widget request.
     * @param width           The width to which the generated image should be scaled.
     * @param request         The HttpServletRequest object, used here to get the client's IP address.
     * @param response        The HttpServletResponse object, used here to set the cache control header.
     * @return A byte array containing the PNG data of the generated widget image.
     * @throws SteamApiException If there is an issue with accessing the Steam Web API.
     * @throws IOException       If there is an error during writing the image to the byte array output stream.
     */
    @GetMapping(value = "/widget/img", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getWidgetImage(
            @RequestParam(name = "id") String id,
            @RequestParam(name = "gameList", required = false, defaultValue = "NONE") ShowedGames gameList,
            @RequestParam(name = "gameListSize", required = false, defaultValue = "5") int gameListSize,
            @RequestParam(name = "playingRightNow", required = false, defaultValue = "true") boolean playingRightNow,
            @RequestParam(name = "purpose", required = false, defaultValue = "General") String purpose,
            @RequestParam(name = "width", required = false, defaultValue = "0") int width,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws SteamApiException, IOException {
        /* Always check that the gamesCount is not too high */
        gameListSize = gameListSize > 10 ? 10 : gameListSize;

        /* Generate Image */
        BufferedImage image = steamWidgetService.generateWidgetImage(id, gameList, gameListSize, playingRightNow, purpose, request);
        if (width > 0) {
            image = steamWidgetService.scaleImage(image, width);
        }

        ByteArrayOutputStream imageByteStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", imageByteStream);
        byte[] imageBytes = imageByteStream.toByteArray();

        /* Set Cache Control, so the image will be refreshed if it's behind a cache */
        response.addHeader("Cache-Control", "max-age=60, must-revalidate");

        return imageBytes;
    }

}