package codes.sharky.steamwidget.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Optional;

/**
 * Single source of truth for the year-end Wrapped window, exposed to the site-wide banner via
 * {@code /api/wrapped/year-end}.
 */
@Service
public class YearEndService {

    /** First day (inclusive) of the year-end window in December. */
    private static final MonthDay WINDOW_START = MonthDay.of(12, 25);

    /** Last day (inclusive) of the year-end window in January. */
    private static final MonthDay WINDOW_END = MonthDay.of(1, 7);

    /**
     * Returns the year being wrapped up if today falls in the year-end window.
     *
     * @return the wrap year, or empty outside the window
     */
    public Optional<Integer> getCurrentWrapYear() {
        return getWrapYear(LocalDate.now());
    }

    /**
     * Returns the year being wrapped up if {@code date} falls in the year-end window.
     *
     * @param date the date to check
     * @return the ending year in late December, the previous year in early January, otherwise empty
     */
    public Optional<Integer> getWrapYear(LocalDate date) {
        MonthDay day = MonthDay.from(date);
        if (!day.isBefore(WINDOW_START)) {
            return Optional.of(date.getYear());
        }
        if (!day.isAfter(WINDOW_END)) {
            return Optional.of(date.getYear() - 1);
        }
        return Optional.empty();
    }
}
