package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.model.YearEndResponse;
import codes.sharky.steamwidget.service.YearEndService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Exposes the year-end Wrapped window to the site-wide banner in {@code layout-effects.js}.
 */
@Controller
public class YearEndController {

    private final YearEndService yearEndService;

    public YearEndController(YearEndService yearEndService) {
        this.yearEndService = yearEndService;
    }

    /**
     * Returns the year being wrapped up while the year-end window is active.
     *
     * @return response whose {@code wrapYear} is {@code null} outside the window
     */
    @GetMapping(value = "/api/wrapped/year-end", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody YearEndResponse getYearEnd() {
        return new YearEndResponse(yearEndService.getCurrentWrapYear().orElse(null));
    }
}
