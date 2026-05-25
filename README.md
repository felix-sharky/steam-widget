# Steam Widget

Generate embeddable Steam profile widgets (PNG badges) and query profile/tracking metrics for public Steam accounts.

## Live Service

- Generator page: `https://steam-widget.com`
- Widget image endpoint: `https://steam-widget.com/widget/img`

## What You Can Do

### Widget

Generate embeddable Steam profile badges as PNG images for websites, READMEs, forums, or dashboards.

- Supports `SteamID64`, vanity/custom URL segments, and community IDs.
- Offers display controls such as game list mode, list size, current-game visibility, and width scaling.
- Primary endpoint: `GET /widget/img`

### Play Tracking

Track and read gameplay history for a Steam profile with daily and monthly aggregates.

- Useful for building personal playtime timelines, summaries, or activity charts.
- Supports date-range filtering using `startDate` and `endDate`.
- Primary endpoints: `GET /api/tracking/profile-date`, `GET /api/tracking/profile-month`

### Metrics

Inspect widget usage and profile-level traffic with legacy counters and aggregated analytics views.

- Query profile hits by profile and by purpose tag.
- Access day/month/year/full rollups for both profile-specific and global metrics.
- Primary endpoints: `GET /metric`, `GET /metric/hits`, `GET /api/metrics/profile/*`, `GET /api/metrics/global/*`

## Widget API

Use this endpoint to render a PNG badge:

```text
https://steam-widget.com/widget/img?id=<SteamId>&gameList=<GameList>&gameListSize=<GameListSize>&playingRightNow=<playingRightNow>&purpose=<Purpose>&width=<Width>
```

### Query Parameters

| Parameter | Required | Default | Description |
|---|---|---|---|
| `id` | Yes | - | Steam account identifier (`SteamID64`, vanity/custom URL segment, or community ID). |
| `gameList` | No | `NONE` | Game list mode: `NONE`, `TOP_GAMES_TOTAL`, `TOP_GAMES_RECENT`, `RECENT_GAMES`. |
| `gameListSize` | No | `5` | Number of games shown. Values above `10` are capped to `10`. |
| `playingRightNow` | No | `true` | Include currently played game status. |
| `purpose` | No | `General` | Free-text tag used for analytics/hit segmentation. |
| `width` | No | `0` | Output width in pixels. `0` keeps original size. |

### Example

```text
https://steam-widget.com/widget/img?id=lizard_darksoul&purpose=github_repo&width=350
```

<img src="https://steam-widget.com/widget/img?id=lizard_darksoul&purpose=github_repo&width=350" alt="Steam widget example" />

### Embed Snippets

```html
<img src="https://steam-widget.com/widget/img?id=lizard_darksoul&purpose=github_repo&width=350" alt="Steam profile" />
```

```markdown
![Steam Profile](https://steam-widget.com/widget/img?id=lizard_darksoul&purpose=github_repo&width=350)
```

```bbcode
[img]https://steam-widget.com/widget/img?id=lizard_darksoul&purpose=github_repo&width=350[/img]
```

## Metrics APIs

### Legacy Profile Metrics

Endpoint: `GET /metric?id=<SteamId>`

- `Accept: application/json` -> returns profile metric payload.
- `Accept: */*` (or default) -> returns hit count as plain numeric output.

Endpoint: `GET /metric/hits?id=<SteamId>&purpose=<Purpose>`

- Returns hit count for the given profile and purpose.
- `purpose` defaults to `General` when omitted.

### Profile Metrics (Aggregated JSON)

Base path: `GET /api/metrics/profile/*`

| Endpoint | Description | Required Params | Optional Params |
|---|---|---|---|
| `/api/metrics/profile/day` | Daily profile hit aggregates | `steam64id` | `startDate`, `endDate` (`YYYY-MM-DD`) |
| `/api/metrics/profile/month` | Monthly profile hit aggregates | `steam64id` | `startDate`, `endDate` (`YYYY-MM-DD`) |
| `/api/metrics/profile/year` | Yearly profile hit aggregates | `steam64id` | - |
| `/api/metrics/profile/full` | Full profile hit history view | `steam64id` | - |

### Global Metrics (Aggregated JSON)

Base path: `GET /api/metrics/global/*`

- `/api/metrics/global/day`
- `/api/metrics/global/month`
- `/api/metrics/global/year`
- `/api/metrics/global/full`

### Playing Stats (Tracking Aggregates)

Use these endpoints to retrieve tracked gameplay time history for a profile.

| Endpoint | Description | Required Params | Optional Params |
|---|---|---|---|
| `/api/tracking/profile-month` | Monthly playtime tracking aggregates | `steamid` | `startDate`, `endDate` (`YYYY-MM-DD`) |
| `/api/tracking/profile-date` | Daily playtime tracking aggregates | `steamid` | `startDate`, `endDate` (`YYYY-MM-DD`) |

Examples:

```text
/api/tracking/profile-month?steamid=76561198000000000&startDate=2026-01-01&endDate=2026-12-31
/api/tracking/profile-date?steamid=76561198000000000&startDate=2026-05-01&endDate=2026-05-25
```

## Useful Notes

- Widget responses are PNG images with cache header `Cache-Control: max-age=60, must-revalidate`.
- Steam profiles must be public for reliable data output.
- ID resolution is handled server-side for endpoints that accept non-64-bit IDs.
- Some metric endpoints return `404` when no data exists for the requested profile/purpose.

## Credits

- [sharky.codes](https://sharky.codes)

## License

Copyright 2024-2026 sharky.codes

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

`http://www.apache.org/licenses/LICENSE-2.0`

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
