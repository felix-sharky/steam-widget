# Steam Widget

> Embeddable Steam profile badges and playtime analytics for public Steam accounts.

[![Live](https://img.shields.io/website?url=https%3A%2F%2Fsteam-widget.com&label=steam-widget.com&style=flat-square&color=7c3aed)](https://steam-widget.com)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue?style=flat-square)](LICENSE)

---

## Table of Contents

- [Live Service](#live-service)
- [Live Preview](#live-preview)
- [Features](#features)
- [API Documentation](#api-documentation)
- [Credits](#credits)
- [License](#license)

---

## Live Service

| Tool | URL |
|---|---|
| Widget Generator | [steam-widget.com](https://steam-widget.com) |
| Play Tracking | [steam-widget.com/tracking.html](https://steam-widget.com/tracking.html) |
| Playing Stats | [steam-widget.com/playing-stats.html](https://steam-widget.com/playing-stats.html) |
| Profile Metrics | [steam-widget.com/profile-metrics.html](https://steam-widget.com/profile-metrics.html) |

---

## Live Preview

Embed your Steam profile in any README, forum, or website with a single image URL:

```markdown
![Steam Widget](https://steam-widget.com/widget/img?id=YOUR_STEAM_ID&width=350)
```

[![Steam Widget Example](https://steam-widget.com/widget/img?id=lizard_darksoul&width=350)](https://steam-widget.com)

---

## Features

### 🎮 Widget Generator

Create embeddable Steam profile badges as PNG images — perfect for websites, READMEs, forums, or dashboards.

- Supports `SteamID64`, vanity/custom URL segments, and community IDs
- Configurable game list mode, list size, current-game visibility, and width scaling

### 📅 Play Tracking

Enable tracking for a Steam profile via Steam OpenID login, then browse daily and monthly playtime history with interactive charts and filterable data tables.

### 📊 Playing Stats & Profile Insights

Explore long-term playtime trends with monthly and daily breakdowns, plus three insight panels loaded automatically for every profile:

- 🔥 **Streaks & Activity** — current streak, longest streaks (year & all-time), most active day of the week, most active month
- ⏱️ **Playtime Stats** — all-time & yearly totals, average daily playtime, best single day, unique game counts
- 🎮 **Game Insights** — most played game (year & all-time), last played game, longest game streak (year & all-time)

### 📈 Profile Metrics

Inspect widget usage and profile-level traffic with hit counters and aggregated daily/monthly analytics.

---

## API Documentation

Full API reference is available in [API.md](API.md).

---

## Credits

Built by [sharky.codes](https://sharky.codes).

---

## License

Copyright 2024-2026 sharky.codes

Licensed under the Apache License, Version 2.0. See [`LICENSE`](LICENSE) or [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0).
