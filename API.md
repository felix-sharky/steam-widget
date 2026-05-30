# Steam Widget — API Documentation

All endpoints are served from `https://steam-widget.com`.

---

## Widget

### `GET /widget/img`

Renders a Steam profile badge as a PNG image.

| Parameter | Required | Default | Description |
|---|---|---|---|
| `id` | Yes | — | Steam account identifier (`SteamID64`, vanity/custom URL segment, or community ID). |
| `gameList` | No | `NONE` | Game list mode: `NONE`, `TOP_GAMES_TOTAL`, `TOP_GAMES_RECENT`, `RECENT_GAMES`. |
| `gameListSize` | No | `5` | Number of games shown. Values above `10` are capped to `10`. |
| `playingRightNow` | No | `true` | Include currently played game status. |
| `purpose` | No | `General` | Free-text tag used for analytics/hit segmentation. |
| `width` | No | `0` | Output width in pixels. `0` keeps original size. |

Response: PNG image · `Cache-Control: max-age=60, must-revalidate`

Example:

```text
/widget/img?id=lizard_darksoul&purpose=github_repo&width=350
```

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

