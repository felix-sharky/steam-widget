package codes.sharky.steamwidget.model;

import java.time.LocalDate;

/** One day's total tracked playtime, for the profile page's activity heatmap. */
public record HeatmapDay(LocalDate date, long totalMinutes) {
}
