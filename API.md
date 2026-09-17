# Steam Widget — API Documentation

All endpoints are served from `https://steam-widget.com`.

---

## Widget

### `GET /widget/img`

Renders a Steam profile badge, game list, insight card set, or custom insight-card set as a PNG image.

| Parameter | Required | Default | Description |
|---|---|---|---|
| `id` | Yes | — | Steam account identifier (`SteamID64`, vanity/custom URL segment, or community ID). |
| `gameList` | No | `NONE` | Game list mode: `NONE`, `TOP_GAMES_TOTAL`, `TOP_GAMES_RECENT`, `RECENT_GAMES`. |
| `gameListSize` | No | `6` | Number of games shown. Values are clamped to `0..10`. |
| `insightCategory` | No | `NONE` | Insight mode: `NONE`, `ACTIVITY`, `PLAYTIME`, `GAMES`, `CUSTOM`. When set, insight cards are rendered instead of `gameList`. |
| `customCard` | No | — | Repeatable card key used when `insightCategory=CUSTOM`. Up to six selected cards are rendered in request order. |
| `style` | No | `STEAM` | Widget color style. See `GET /api/widget/styles` for the current list (currently `STEAM`, `MIDNIGHT`, `PASTEL`, `NEON`, `SUNSET`, `FOREST`, `CRIMSON`, `GOLD`, `MONOCHROME`, `PAPER`, `VIOLET`, `EMERALD`, `SYNTHWAVE`, `SLATE`). |
| `playingRightNow` | No | `true` | Include currently played game status. |
| `purpose` | No | `General` | Free-text tag used for analytics/hit segmentation. |
| `width` | No | `0` | Output width in pixels. `0` keeps original size. |

Response: PNG image · `Cache-Control: max-age=60, must-revalidate`

Example:

```text
/widget/img?id=lizard_darksoul&purpose=github_repo&width=900
/widget/img?id=lizard_darksoul&gameList=TOP_GAMES_TOTAL&gameListSize=6&style=MIDNIGHT&width=900
/widget/img?id=lizard_darksoul&insightCategory=PLAYTIME&style=FOREST&width=900
/widget/img?id=lizard_darksoul&insightCategory=CUSTOM&customCard=ACTIVITY_CURRENT_STREAK&customCard=PLAYTIME_ALLTIME&customCard=GAMES_MOST_PLAYED_YEAR&width=900
```

### `GET /api/widget/styles`

Lists the available `style` values for `GET /widget/img`, in display order.

Response:

```json
[
  { "id": "STEAM", "label": "Steam Blue" },
  { "id": "MIDNIGHT", "label": "Midnight" }
]
```

Custom insight card keys:

| Key | Card |
|---|---|
| `ACTIVITY_CURRENT_STREAK` | Current streak |
| `ACTIVITY_LONGEST_STREAK_YEAR` | Longest streak this year |
| `ACTIVITY_LONGEST_STREAK_ALLTIME` | Longest streak all-time |
| `ACTIVITY_MOST_ACTIVE_DAY` | Most active day |
| `ACTIVITY_MOST_ACTIVE_MONTH` | Most active month |
| `PLAYTIME_ALLTIME` | All-time playtime |
| `PLAYTIME_YEAR` | This year |
| `PLAYTIME_AVG_DAILY` | Average daily playtime this year |
| `PLAYTIME_BEST_DAY` | Best single day |
| `PLAYTIME_GAMES_YEAR` | Games this year |
| `PLAYTIME_GAMES_ALLTIME` | Games all-time |
| `GAMES_MOST_PLAYED_ALLTIME` | Most played game all-time |
| `GAMES_MOST_PLAYED_YEAR` | Most played game this year |
| `GAMES_LAST_PLAYED` | Last played game |
| `GAMES_STREAK_ALLTIME` | Longest game streak all-time |
| `GAMES_STREAK_YEAR` | Longest game streak this year |

---

## Play Tracking

### `GET /api/tracking/profile-month`

Monthly playtime aggregates for a profile.

| Parameter | Required | Description |
|---|---|---|
| `steamid` | Yes | Steam64 ID |
| `startDate` | No | Filter start date (`YYYY-MM-DD`) |
| `endDate` | No | Filter end date (`YYYY-MM-DD`) |

### `GET /api/tracking/profile-date`

Daily playtime aggregates for a profile.

| Parameter | Required | Description |
|---|---|---|
| `steamid` | Yes | Steam64 ID |
| `startDate` | No | Filter start date (`YYYY-MM-DD`) |
| `endDate` | No | Filter end date (`YYYY-MM-DD`) |

Examples:

```text
/api/tracking/profile-month?steamid=76561198000000000&startDate=2026-01-01&endDate=2026-12-31
/api/tracking/profile-date?steamid=76561198000000000&startDate=2026-05-01&endDate=2026-05-25
```

---

## Profile Insights

All insight endpoints require `steamid` (Steam64 ID) and return `404` if no data exists for the profile.

### `GET /api/tracking/insights/activity` — Streaks & Activity

| Field | Description |
|---|---|
| `currentStreakDays` | Number of consecutive days currently played |
| `currentStreakStart` / `currentStreakEnd` | Date range of the current streak |
| `longestStreakYearDays` | Longest streak in the current calendar year |
| `longestStreakYearStart` / `longestStreakYearEnd` | Date range of that streak |
| `longestStreakAlltimeDays` | All-time longest streak |
| `longestStreakAlltimeStart` / `longestStreakAlltimeEnd` | Date range of that streak |
| `mostActiveDow` | Day of week with the most play sessions this year |
| `mostActiveDowCount` | Number of sessions on that day |
| `mostActiveMonth` | Month with the most active days this year |
| `mostActiveMonthDays` | Number of active days in that month |

### `GET /api/tracking/insights/playtime` — Playtime Stats

| Field | Description |
|---|---|
| `alltimeHours` / `alltimeMinutes` | Total all-time playtime (from most recent cumulative counter) |
| `yearHours` / `yearMinutes` | Total playtime for the current calendar year |
| `avgDailyHours` / `avgDailyMinutes` | Average playtime per active day this year |
| `bestDayDate` | Date of the single highest playtime day |
| `bestDayHours` / `bestDayMinutes` | Playtime on that day |
| `uniqueGamesThisYear` | Number of distinct games played this year |
| `uniqueGamesAlltime` | Number of distinct games played all-time |

### `GET /api/tracking/insights/games` — Game Insights

| Field | Description |
|---|---|
| `mostPlayedAlltimeGame` | Game with the highest all-time playtime |
| `mostPlayedAlltimeHours` / `mostPlayedAlltimeMinutes` | Playtime for that game |
| `mostPlayedYearGame` | Game with the most playtime this year |
| `mostPlayedYearHours` / `mostPlayedYearMinutes` | Playtime for that game |
| `lastPlayedGame` | Most recently played game |
| `lastPlayedDate` | Date it was last played |
| `longestStreakAlltimeGame` | Game played on the most consecutive days all-time |
| `longestStreakAlltimeDays` | Length of that streak |
| `longestStreakAlltimeStart` / `longestStreakAlltimeEnd` | Date range of that streak |
| `longestStreakYearGame` | Game with the longest streak this year |
| `longestStreakYearDays` | Length of that streak |
| `longestStreakYearStart` / `longestStreakYearEnd` | Date range of that streak |

Examples:

```text
/api/tracking/insights/activity?steamid=76561198000000000
/api/tracking/insights/playtime?steamid=76561198000000000
/api/tracking/insights/games?steamid=76561198000000000
```

---

## Metrics

### Legacy — `GET /metric`

| Parameter | Required | Description |
|---|---|---|
| `id` | Yes | Steam account identifier |

- `Accept: application/json` → profile metric payload
- `Accept: */*` → hit count as plain number

### Legacy — `GET /metric/hits`

| Parameter | Required | Default | Description |
|---|---|---|---|
| `id` | Yes | — | Steam account identifier |
| `purpose` | No | `General` | Analytics tag |

### Profile Metrics — `GET /api/metrics/profile/*`

| Endpoint | Description | Required Params | Optional Params |
|---|---|---|---|
| `/api/metrics/profile/day` | Daily profile hit aggregates | `steam64id` | `startDate`, `endDate` (`YYYY-MM-DD`) |
| `/api/metrics/profile/month` | Monthly profile hit aggregates | `steam64id` | `startDate`, `endDate` (`YYYY-MM-DD`) |
| `/api/metrics/profile/year` | Yearly profile hit aggregates | `steam64id` | — |
| `/api/metrics/profile/full` | Full profile hit history | `steam64id` | — |

### Global Metrics — `GET /api/metrics/global/*`

- `/api/metrics/global/day`
- `/api/metrics/global/month`
- `/api/metrics/global/year`
- `/api/metrics/global/full`

---

## Notes

- Steam profiles must be public for reliable data.
- ID resolution is handled server-side for endpoints that accept non-64-bit IDs.
- Endpoints return `404` when no data exists for the requested profile/purpose.
