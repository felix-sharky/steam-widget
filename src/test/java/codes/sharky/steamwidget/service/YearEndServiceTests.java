package codes.sharky.steamwidget.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YearEndServiceTests {

    private final YearEndService yearEndService = new YearEndService();

    @Test
    void wrapYearIsSetOnlyAroundNewYear() {
        assertEquals(Optional.empty(), yearEndService.getWrapYear(LocalDate.of(2026, 12, 24)));
        assertEquals(Optional.of(2026), yearEndService.getWrapYear(LocalDate.of(2026, 12, 25)));
        assertEquals(Optional.of(2026), yearEndService.getWrapYear(LocalDate.of(2026, 12, 31)));
        assertEquals(Optional.of(2026), yearEndService.getWrapYear(LocalDate.of(2027, 1, 1)));
        assertEquals(Optional.of(2026), yearEndService.getWrapYear(LocalDate.of(2027, 1, 7)));
        assertEquals(Optional.empty(), yearEndService.getWrapYear(LocalDate.of(2027, 1, 8)));
        assertEquals(Optional.empty(), yearEndService.getWrapYear(LocalDate.of(2026, 10, 8)));
    }
}
