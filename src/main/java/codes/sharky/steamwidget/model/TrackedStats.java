package codes.sharky.steamwidget.model;

import java.util.List;

/** Headline stats and heatmap shown only for profiles with the "tracking" feature enabled. */
public record TrackedStats(long totalHours, long gamesPlayedThisYear, long currentStreakDays, List<HeatmapDay> heatmap) {
}
