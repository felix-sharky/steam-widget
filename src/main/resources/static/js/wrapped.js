document.addEventListener('DOMContentLoaded', () => {
    const utils = window.SteamWidget || {};
    const resolveSteamIdInput = utils.resolveSteamIdInput || (async (value) => value?.trim() || null);
    const persistSteamIdInQuery = utils.persistSteamIdInQuery || (() => {});
    const syncNavLinks = utils.syncNavLinks || (() => {});
    const bootstrapSteamId = utils.bootstrapSteamId || (() => null);

    const form = document.getElementById('wrappedForm');
    const steamIdInput = document.getElementById('steamId');
    const themeSelect = document.getElementById('themeSelect');
    const wrappedMessage = document.getElementById('wrappedMessage');
    const wrappedSection = document.getElementById('wrappedSection');
    const scaler = document.getElementById('wrappedScaler');
    const stage = document.getElementById('wrappedStage');
    const slideContent = document.getElementById('wrappedSlideContent');
    const progressRow = document.getElementById('wrappedProgressRow');
    const tapPrev = document.getElementById('wrappedTapPrev');
    const tapNext = document.getElementById('wrappedTapNext');
    const restartBtn = document.getElementById('wrappedRestartBtn');
    const downloadBtn = document.getElementById('wrappedDownloadBtn');
    const shareBtn = document.getElementById('wrappedShareBtn');

    const periodModeInputs = document.querySelectorAll('input[name="periodMode"]');
    const periodYearInput = document.getElementById('periodYear');
    const periodMonthInput = document.getElementById('periodMonth');
    const periodWeekInput = document.getElementById('periodWeek');
    const periodFields = {
        year: document.querySelector('[data-period-field="year"]'),
        month: document.querySelector('[data-period-field="month"]'),
        week: document.querySelector('[data-period-field="week"]')
    };

    const STAGE_WIDTH = 1080;
    const STAGE_HEIGHT = 1920;
    const SLIDE_DURATION_MS = 6000;

    let themeColorsById = {};
    let currentPeriod = null;
    let slides = [];
    let currentIndex = 0;
    let rafId = null;
    let slideStart = 0;
    let elapsedBeforePause = 0;
    let storyActive = false;
    let pressState = null;

    // ── Helpers ──────────────────────────────────────────────────────────────

    const escapeHtml = (value) => String(value ?? '').replace(/[&<>"']/g, (ch) => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[ch]);

    const fmtDate = (val) => {
        if (!val) return null;
        if (Array.isArray(val) && val.length >= 3) {
            const [y, mo, d] = val;
            return `${y}-${String(mo).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
        }
        return String(val).substring(0, 10);
    };

    const fmtDateRange = (start, end) => {
        const s = fmtDate(start);
        const e = fmtDate(end);
        if (!s && !e) return '';
        if (s === e || !e) return s || '';
        return `${s} → ${e}`;
    };

    const fmtDuration = (hours, minutes) => {
        const h = Number(hours) || 0;
        const m = Number(minutes) || 0;
        if (h === 0 && m === 0) return '0m';
        return h > 0 ? `${h}h ${m}m` : `${m}m`;
    };

    const minutesToHM = (totalMinutes) => ({
        hours: Math.floor(totalMinutes / 60),
        minutes: Math.round(totalMinutes % 60)
    });

    const fmtMinutes = (totalMinutes) => {
        const { hours, minutes } = minutesToHM(totalMinutes);
        return fmtDuration(hours, minutes);
    };

    // ── Period (year / month / week) helpers ────────────────────────────────

    const pad2 = (n) => String(n).padStart(2, '0');
    const toIsoDate = (date) => `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`;
    const parseIsoDate = (iso) => {
        const [y, m, d] = iso.split('-').map(Number);
        return new Date(y, m - 1, d);
    };
    const fmtShort = (date) => date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    const isLeapYear = (y) => (y % 4 === 0 && y % 100 !== 0) || y % 400 === 0;

    const mostRecentMonday = (date) => {
        const d = new Date(date);
        const day = d.getDay(); // 0 = Sunday .. 6 = Saturday
        const diff = day === 0 ? -6 : 1 - day;
        d.setDate(d.getDate() + diff);
        return d;
    };

    const getPeriodMode = () => document.querySelector('input[name="periodMode"]:checked')?.value || 'year';

    const updatePeriodFieldVisibility = () => {
        const mode = getPeriodMode();
        Object.entries(periodFields).forEach(([key, el]) => {
            if (el) el.classList.toggle('hidden', key !== mode);
        });
    };

    // Builds the {startDate, endDate, label, noun, periodDays, includesToday} descriptor for
    // whichever period is currently selected in the form. Returns null if the value is incomplete.
    const getPeriodRange = (mode) => {
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (mode === 'month') {
            const raw = periodMonthInput.value;
            if (!raw) return null;
            const [y, m] = raw.split('-').map(Number);
            if (!y || !m) return null;
            const start = new Date(y, m - 1, 1);
            const end = new Date(y, m, 0);
            return {
                startDate: toIsoDate(start),
                endDate: toIsoDate(end),
                label: start.toLocaleDateString('en-US', { month: 'long', year: 'numeric' }),
                noun: 'month',
                periodDays: end.getDate(),
                includesToday: today >= start && today <= end
            };
        }

        if (mode === 'week') {
            const raw = periodWeekInput.value;
            if (!raw) return null;
            const anchor = mostRecentMonday(parseIsoDate(raw));
            const end = new Date(anchor);
            end.setDate(end.getDate() + 6);
            return {
                startDate: toIsoDate(anchor),
                endDate: toIsoDate(end),
                label: `${fmtShort(anchor)} – ${fmtShort(end)}, ${end.getFullYear()}`,
                noun: 'week',
                periodDays: 7,
                includesToday: today >= anchor && today <= end
            };
        }

        // year
        const y = Number(periodYearInput.value) || today.getFullYear();
        const start = new Date(y, 0, 1);
        const end = new Date(y, 11, 31);
        return {
            startDate: toIsoDate(start),
            endDate: toIsoDate(end),
            label: String(y),
            noun: 'year',
            periodDays: isLeapYear(y) ? 366 : 365,
            includesToday: today >= start && today <= end
        };
    };

    // The immediately-preceding period of the same length/kind (previous year, previous
    // calendar month, or the 7 days before this week), used to power "vs last {noun}" trend
    // chips. Derived purely from the current period's startDate so it never depends on form state.
    const getPreviousPeriodRange = (period, mode) => {
        const start = parseIsoDate(period.startDate);

        if (mode === 'month') {
            const prevEnd = new Date(start);
            prevEnd.setDate(0); // "day 0" rolls back to the last day of the previous month
            const prevStart = new Date(prevEnd.getFullYear(), prevEnd.getMonth(), 1);
            return {
                startDate: toIsoDate(prevStart),
                endDate: toIsoDate(prevEnd),
                label: prevStart.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
            };
        }

        if (mode === 'week') {
            const prevStart = new Date(start);
            prevStart.setDate(prevStart.getDate() - 7);
            const prevEnd = new Date(prevStart);
            prevEnd.setDate(prevEnd.getDate() + 6);
            return {
                startDate: toIsoDate(prevStart),
                endDate: toIsoDate(prevEnd),
                label: `${fmtShort(prevStart)} – ${fmtShort(prevEnd)}, ${prevEnd.getFullYear()}`
            };
        }

        // year
        const prevYear = start.getFullYear() - 1;
        const prevStart = new Date(prevYear, 0, 1);
        const prevEnd = new Date(prevYear, 11, 31);
        return { startDate: toIsoDate(prevStart), endDate: toIsoDate(prevEnd), label: String(prevYear) };
    };

    const initPeriodDefaults = () => {
        const today = new Date();
        periodYearInput.value = String(today.getFullYear());
        periodMonthInput.value = `${today.getFullYear()}-${pad2(today.getMonth() + 1)}`;
        periodWeekInput.value = toIsoDate(mostRecentMonday(today));

        // ?year=YYYY (used by the year-end banner) preselects a full-year Wrapped.
        const requestedYear = Number(new URLSearchParams(window.location.search).get('year'));
        if (Number.isInteger(requestedYear) && requestedYear >= 2003 && requestedYear <= today.getFullYear()) {
            periodYearInput.value = String(requestedYear);
            const yearRadio = document.querySelector('input[name="periodMode"][value="year"]');
            if (yearRadio) yearRadio.checked = true;
        }
        updatePeriodFieldVisibility();
    };

    periodModeInputs.forEach((input) => input.addEventListener('change', updatePeriodFieldVisibility));

    // Aggregates raw /api/tracking/profile-date rows (one row per game per day) into the handful
    // of stats the slides need, scoped to whatever date range was requested.
    const aggregatePeriod = (rows) => {
        const minutesByDate = new Map();
        const minutesByGame = new Map();
        let totalMinutes = 0;

        rows.forEach((row) => {
            const dateStr = fmtDate(row.id?.date);
            const minutes = (Number(row.playtimeHours) || 0) * 60 + (Number(row.playtimeMinutes) || 0);
            if (!dateStr || minutes <= 0) return;
            minutesByDate.set(dateStr, (minutesByDate.get(dateStr) || 0) + minutes);
            const game = (row.gamename ?? row.name ?? '').trim();
            if (game) {
                minutesByGame.set(game, (minutesByGame.get(game) || 0) + minutes);
            }
            totalMinutes += minutes;
        });

        if (totalMinutes <= 0) return null;

        let topGameName = null;
        let topGameMinutes = 0;
        minutesByGame.forEach((minutes, name) => {
            if (minutes > topGameMinutes) { topGameMinutes = minutes; topGameName = name; }
        });

        let peakDate = null;
        let peakMinutes = 0;
        minutesByDate.forEach((minutes, date) => {
            if (minutes > peakMinutes) { peakMinutes = minutes; peakDate = date; }
        });

        const activeDates = Array.from(minutesByDate.keys()).sort();
        let longestDays = 0;
        let longestStart = null;
        let longestEnd = null;
        let runDays = 0;
        let runStart = null;
        let prevDate = null;
        activeDates.forEach((dateStr) => {
            const current = parseIsoDate(dateStr);
            if (prevDate && (current - prevDate) === 86400000) {
                runDays += 1;
            } else {
                runDays = 1;
                runStart = dateStr;
            }
            if (runDays > longestDays) {
                longestDays = runDays;
                longestStart = runStart;
                longestEnd = dateStr;
            }
            prevDate = current;
        });

        return {
            totalMinutes,
            topGame: topGameName ? { name: topGameName, minutes: topGameMinutes } : null,
            uniqueGames: minutesByGame.size,
            peakDay: peakDate ? { date: peakDate, minutes: peakMinutes } : null,
            longestStreak: longestDays ? { days: longestDays, start: longestStart, end: longestEnd } : null,
            activeDays: minutesByDate.size
        };
    };

    const bigNumberBlock = (value, unit) => `
        <div class="wrapped-big-number">${escapeHtml(value)}</div>
        <div class="wrapped-eyebrow" style="margin-top:-18px;">${escapeHtml(unit)}</div>`;

    // A small "vs last {noun}" pill comparing a numeric stat (minutes, a game count, a day count,
    // ...) against the same stat from the immediately-preceding period. `previous` is null when
    // there's no data for that prior period at all (e.g. the very first tracked month).
    const trendChip = (current, previous, noun) => {
        if (previous === null || previous === undefined) {
            return `<div class="wrapped-trend-chip"><span class="material-symbols-outlined">new_releases</span><span>First time tracking this ${escapeHtml(noun)}</span></div>`;
        }
        if (previous === 0) {
            if (current === 0) return '';
            return `<div class="wrapped-trend-chip wrapped-trend-up"><span class="material-symbols-outlined">trending_up</span><span>Up from zero last ${escapeHtml(noun)}</span></div>`;
        }
        const pct = Math.round(((current - previous) / previous) * 100);
        if (pct === 0) {
            return `<div class="wrapped-trend-chip"><span class="material-symbols-outlined">trending_flat</span><span>Same as last ${escapeHtml(noun)}</span></div>`;
        }
        const up = pct > 0;
        const icon = up ? 'trending_up' : 'trending_down';
        const cls = up ? 'wrapped-trend-up' : 'wrapped-trend-down';
        return `<div class="wrapped-trend-chip ${cls}"><span class="material-symbols-outlined">${icon}</span><span>${up ? '+' : ''}${pct}% vs last ${escapeHtml(noun)}</span></div>`;
    };

    const footerBrand = () => `
        <div class="wrapped-footer-brand">
            <span class="brand-name">steam-widget</span>
            <span class="brand-url">steam-widget.com</span>
        </div>`;

    const blob = (style) => `<div class="wrapped-blob" style="${style}"></div>`;

    const applyTheme = (themeId) => {
        const theme = themeColorsById[themeId] || themeColorsById.STEAM || {
            backgroundStart: '#140a1f', backgroundEnd: '#3b0764', accent: '#c084fc', text: '#faf5ff', muted: '#e9d5ff'
        };
        slideContent.style.setProperty('--w-bg-start', theme.backgroundStart);
        slideContent.style.setProperty('--w-bg-end', theme.backgroundEnd);
        slideContent.style.setProperty('--w-accent', theme.accent);
        slideContent.style.setProperty('--w-text', theme.text);
        slideContent.style.setProperty('--w-muted', theme.muted);
    };

    // ── Theme picker ─────────────────────────────────────────────────────────

    const loadThemes = async () => {
        try {
            const res = await fetch('/api/widget/themes');
            if (!res.ok) throw new Error(String(res.status));
            const themes = await res.json();
            if (!Array.isArray(themes) || !themes.length) return;
            themeColorsById = Object.fromEntries(themes.map((t) => [t.id, t]));
            themeSelect.innerHTML = themes.map((t) => `<option value="${escapeHtml(t.id)}">${escapeHtml(t.label)}</option>`).join('');
        } catch (e) {
            console.warn('Failed to load themes, using default.', e);
        }
    };

    // ── Slide builders ───────────────────────────────────────────────────────
    // All builders take the period descriptor from getPeriodRange() (start/end/label/noun) plus
    // the aggregated periodStats for that window, so the same slides work for a year, a month or
    // a week alike. `alltime` (from /api/tracking/insights/playtime, optional) only powers the
    // "% of all-time" context line.

    const buildCoverSlide = (profile, period) => {
        const name = profile?.name ? escapeHtml(profile.name) : 'Your';
        const avatar = profile?.avatarUrl
            ? `<img class="wrapped-avatar" src="${escapeHtml(profile.avatarUrl)}" crossorigin="anonymous" alt="">`
            : `<div class="wrapped-icon-badge"><span class="material-symbols-outlined">person</span></div>`;
        return `
            ${blob('width:620px;height:620px;top:-220px;left:-180px;')}
            ${blob('width:520px;height:520px;bottom:-180px;right:-160px;')}
            <div class="wrapped-slide-inner">
                ${avatar}
                <span class="wrapped-eyebrow">Steam Wrapped</span>
                <h2 class="wrapped-headline">${name}'s ${escapeHtml(period.noun)}<br>in gaming</h2>
                <p class="wrapped-caption">${escapeHtml(period.label)} — let's relive the hours, the streaks, and the game that owned it.</p>
            </div>
            ${footerBrand()}`;
    };

    const buildTotalPlaytimeSlide = (periodStats, period, alltime, previousStats) => {
        const { hours, minutes } = minutesToHM(periodStats.totalMinutes);
        let subcaption = '';
        const alltimeMinutesTotal = alltime ? (Number(alltime.alltimeHours) || 0) * 60 + (Number(alltime.alltimeMinutes) || 0) : 0;
        if (alltimeMinutesTotal > periodStats.totalMinutes) {
            const pct = Math.max(1, Math.min(100, Math.round((periodStats.totalMinutes / alltimeMinutesTotal) * 100)));
            subcaption = `That's ${pct}% of your all-time ${fmtDuration(alltime.alltimeHours, alltime.alltimeMinutes)} on record.`;
        }
        return `
            ${blob('width:560px;height:560px;top:-200px;right:-180px;')}
            <div class="wrapped-slide-inner">
                <span class="wrapped-icon-badge"><span class="material-symbols-outlined">timer</span></span>
                <span class="wrapped-eyebrow">Total playtime · ${escapeHtml(period.label)}</span>
                ${bigNumberBlock(hours, 'HOURS')}
                <p class="wrapped-caption">That's ${fmtDuration(hours, minutes)} across your library this ${escapeHtml(period.noun)}.</p>
                ${subcaption ? `<p class="wrapped-subcaption">${subcaption}</p>` : ''}
                ${trendChip(periodStats.totalMinutes, previousStats ? previousStats.totalMinutes : null, period.noun)}
            </div>
            ${footerBrand()}`;
    };

    const buildTopGameSlide = (periodStats, period) => {
        const duration = fmtMinutes(periodStats.topGame.minutes);
        return `
            ${blob('width:600px;height:600px;bottom:-220px;left:-180px;')}
            <div class="wrapped-slide-inner">
                <span class="wrapped-icon-badge"><span class="material-symbols-outlined">emoji_events</span></span>
                <span class="wrapped-eyebrow">Most played game</span>
                <h2 class="wrapped-headline">${escapeHtml(periodStats.topGame.name)}</h2>
                <p class="wrapped-caption">${duration} this ${escapeHtml(period.noun)} — your undisputed #1.</p>
            </div>
            ${footerBrand()}`;
    };

    const buildVarietySlide = (periodStats, period, previousStats) => {
        const count = periodStats.uniqueGames;
        return `
            ${blob('width:560px;height:560px;top:-200px;left:-180px;')}
            <div class="wrapped-slide-inner">
                <span class="wrapped-icon-badge"><span class="material-symbols-outlined">sports_esports</span></span>
                <span class="wrapped-eyebrow">Game rotation</span>
                ${bigNumberBlock(count, count === 1 ? 'GAME' : 'GAMES')}
                <p class="wrapped-caption">different games kept you busy ${period.noun === 'week' ? 'this week' : `in ${escapeHtml(period.label)}`}.</p>
                ${trendChip(count, previousStats ? previousStats.uniqueGames : null, period.noun)}
            </div>
            ${footerBrand()}`;
    };

    const buildActiveDaysSlide = (periodStats, period, previousStats) => {
        const days = periodStats.activeDays;
        return `
            ${blob('width:560px;height:560px;bottom:-200px;left:-180px;')}
            <div class="wrapped-slide-inner">
                <span class="wrapped-icon-badge"><span class="material-symbols-outlined">event_available</span></span>
                <span class="wrapped-eyebrow">Showing up</span>
                ${bigNumberBlock(days, days === 1 ? 'DAY ACTIVE' : 'DAYS ACTIVE')}
                <p class="wrapped-caption">out of ${period.periodDays} days in ${escapeHtml(period.label)} — that's ${Math.round((days / period.periodDays) * 100)}% of the ${escapeHtml(period.noun)}.</p>
                ${trendChip(days, previousStats ? previousStats.activeDays : null, period.noun)}
            </div>
            ${footerBrand()}`;
    };

    const buildStreakSlide = (periodStats, period, currentStreakDays) => {
        const { days, start, end } = periodStats.longestStreak;
        const range = fmtDateRange(start, end);
        const subcaption = period.includesToday && currentStreakDays > 0
            ? `You're currently on a ${currentStreakDays} day streak — keep it going.`
            : '';
        return `
            ${blob('width:560px;height:560px;bottom:-200px;right:-180px;')}
            <div class="wrapped-slide-inner">
                <span class="wrapped-icon-badge"><span class="material-symbols-outlined">local_fire_department</span></span>
                <span class="wrapped-eyebrow">Dedication</span>
                ${bigNumberBlock(days, 'DAY STREAK')}
                <p class="wrapped-caption">Your longest run in ${escapeHtml(period.label)}${range ? ` · ${range}` : ''}.</p>
                ${subcaption ? `<p class="wrapped-subcaption">${subcaption}</p>` : ''}
            </div>
            ${footerBrand()}`;
    };

    const buildPeakDaySlide = (periodStats, period) => {
        const { hours, minutes } = minutesToHM(periodStats.peakDay.minutes);
        const value = hours > 0 ? hours : minutes;
        const unit = hours > 0 ? 'HOURS' : 'MINUTES';
        return `
            ${blob('width:560px;height:560px;top:-200px;right:-180px;')}
            <div class="wrapped-slide-inner">
                <span class="wrapped-icon-badge"><span class="material-symbols-outlined">star</span></span>
                <span class="wrapped-eyebrow">Peak day</span>
                ${bigNumberBlock(value, unit)}
                <p class="wrapped-caption">played in a single day on ${periodStats.peakDay.date} — your most intense session of ${escapeHtml(period.label)}.</p>
            </div>
            ${footerBrand()}`;
    };

    const buildRecapSlide = (periodStats, period) => {
        const items = [];
        if (periodStats.topGame) {
            items.push({ icon: 'emoji_events', label: 'Top game', value: periodStats.topGame.name });
        }
        items.push({ icon: 'timer', label: 'Total playtime', value: fmtMinutes(periodStats.totalMinutes) });
        items.push({ icon: 'sports_esports', label: 'Games played', value: `${periodStats.uniqueGames}` });
        if (periodStats.longestStreak) {
            items.push({ icon: 'local_fire_department', label: 'Longest streak', value: `${periodStats.longestStreak.days} days` });
        }
        const grid = items.map((item) => `
            <div class="wrapped-recap-item">
                <span class="material-symbols-outlined">${item.icon}</span>
                <span class="recap-label">${escapeHtml(item.label)}</span>
                <span class="recap-value">${escapeHtml(item.value)}</span>
            </div>`).join('');
        return `
            ${blob('width:600px;height:600px;top:-220px;left:-180px;')}
            ${blob('width:500px;height:500px;bottom:-180px;right:-160px;')}
            <div class="wrapped-slide-inner" style="gap:28px;">
                <span class="wrapped-eyebrow">${escapeHtml(period.label)} Recap</span>
                <h2 class="wrapped-headline" style="font-size:72px;">That's a wrap!</h2>
                <div class="wrapped-recap-grid">${grid}</div>
            </div>
            ${footerBrand()}`;
    };

    // ── Data fetching ────────────────────────────────────────────────────────

    const fetchJsonOrNull = async (url) => {
        try {
            const res = await fetch(url);
            if (!res.ok) return null;
            return await res.json();
        } catch (e) {
            return null;
        }
    };

    const buildSlides = ({ periodStats, period, profile, alltime, currentStreakDays, previousStats }) => {
        const built = [{ render: () => buildCoverSlide(profile, period) }];

        built.push({ render: () => buildTotalPlaytimeSlide(periodStats, period, alltime, previousStats) });
        if (periodStats.topGame) {
            built.push({ render: () => buildTopGameSlide(periodStats, period) });
        }
        if (periodStats.uniqueGames > 0) {
            built.push({ render: () => buildVarietySlide(periodStats, period, previousStats) });
        }
        built.push({ render: () => buildActiveDaysSlide(periodStats, period, previousStats) });
        if (periodStats.longestStreak && periodStats.longestStreak.days > 1) {
            built.push({ render: () => buildStreakSlide(periodStats, period, currentStreakDays) });
        }
        if (periodStats.peakDay) {
            built.push({ render: () => buildPeakDaySlide(periodStats, period) });
        }
        built.push({ render: () => buildRecapSlide(periodStats, period) });
        return built;
    };

    // ── Story playback engine ────────────────────────────────────────────────

    const cancelTimer = () => {
        if (rafId) {
            cancelAnimationFrame(rafId);
            rafId = null;
        }
    };

    const setFillForIndex = (index, pct) => {
        const seg = progressRow.children[index];
        if (!seg) return;
        const fill = seg.querySelector('.wrapped-progress-fill');
        if (fill) fill.style.width = `${pct}%`;
    };

    const renderProgressBars = () => {
        progressRow.innerHTML = slides.map(() =>
            `<div class="wrapped-progress-seg"><div class="wrapped-progress-fill"></div></div>`
        ).join('');
    };

    const tick = () => {
        rafId = requestAnimationFrame(() => {
            const elapsed = elapsedBeforePause + (performance.now() - slideStart);
            const progress = Math.min(1, elapsed / SLIDE_DURATION_MS);
            setFillForIndex(currentIndex, progress * 100);
            if (progress >= 1) {
                goNext({ auto: true });
            } else {
                tick();
            }
        });
    };

    const startTimer = () => {
        cancelTimer();
        elapsedBeforePause = 0;
        slideStart = performance.now();
        tick();
    };

    const pauseTimer = () => {
        if (!rafId) return;
        cancelTimer();
        elapsedBeforePause += performance.now() - slideStart;
    };

    const resumeTimer = () => {
        if (!storyActive || rafId) return;
        slideStart = performance.now();
        tick();
    };

    const showSlide = (index) => {
        currentIndex = Math.max(0, Math.min(slides.length - 1, index));
        slideContent.innerHTML = slides[currentIndex].render();
        slides.forEach((_, i) => setFillForIndex(i, i < currentIndex ? 100 : 0));
        startTimer();
    };

    const goNext = ({ auto = false } = {}) => {
        if (!storyActive) return;
        if (currentIndex >= slides.length - 1) {
            cancelTimer();
            setFillForIndex(currentIndex, 100);
            return;
        }
        showSlide(currentIndex + 1);
    };

    const goPrev = () => {
        if (!storyActive || currentIndex === 0) {
            if (storyActive) showSlide(0);
            return;
        }
        showSlide(currentIndex - 1);
    };

    const onZonePointerDown = (action) => (e) => {
        pressState = { action, time: performance.now(), x: e.clientX, y: e.clientY };
        pauseTimer();
    };

    const onWindowPointerUp = (e) => {
        if (!pressState) return;
        const held = performance.now() - pressState.time;
        const dx = (e.clientX ?? pressState.x) - pressState.x;
        const dy = (e.clientY ?? pressState.y) - pressState.y;
        const moved = Math.hypot(dx, dy) > 12;
        const action = pressState.action;
        pressState = null;
        if (held < 400 && !moved) {
            if (action === 'next') goNext(); else goPrev();
        } else {
            resumeTimer();
        }
    };

    const onWindowPointerCancel = () => {
        if (pressState) {
            pressState = null;
            resumeTimer();
        }
    };

    tapPrev.addEventListener('pointerdown', onZonePointerDown('prev'));
    tapNext.addEventListener('pointerdown', onZonePointerDown('next'));
    window.addEventListener('pointerup', onWindowPointerUp);
    window.addEventListener('pointercancel', onWindowPointerCancel);

    document.addEventListener('keydown', (e) => {
        if (!storyActive) return;
        if (e.key === 'ArrowRight' || e.key === ' ') { e.preventDefault(); goNext(); }
        else if (e.key === 'ArrowLeft') { e.preventDefault(); goPrev(); }
    });

    restartBtn.addEventListener('click', () => {
        if (!storyActive) return;
        showSlide(0);
    });

    // ── Responsive stage scaling ─────────────────────────────────────────────

    const updateStageScale = () => {
        const outer = scaler.parentElement;
        const availableWidth = Math.min(outer.clientWidth - 16, 420);
        const availableHeight = Math.min(window.innerHeight * 0.72, 760);
        const scale = Math.max(0.1, Math.min(availableWidth / STAGE_WIDTH, availableHeight / STAGE_HEIGHT));
        stage.style.transform = `scale(${scale})`;
        scaler.style.width = `${STAGE_WIDTH * scale}px`;
        scaler.style.height = `${STAGE_HEIGHT * scale}px`;
    };

    window.addEventListener('resize', updateStageScale);

    // ── Export (download / share) ────────────────────────────────────────────

    const setMessage = (text, isError = false) => {
        wrappedMessage.textContent = text;
        wrappedMessage.className = `mt-4 text-sm ${isError ? 'text-[#ff516a]' : 'text-[#4cd7f6]'}`;
    };

    // Avatar/other <img> tags inside a freshly-rendered slide may still be mid-fetch; html2canvas
    // snapshots whatever is painted right now, so an export taken too early would show a blank
    // box instead of the image. Wait for every image in the slide to settle (load or fail) first.
    const waitForImages = async (container, timeoutMs = 4000) => {
        const imgs = Array.from(container.querySelectorAll('img'));
        await Promise.all(imgs.map((img) => {
            if (img.complete) return Promise.resolve();
            return new Promise((resolve) => {
                img.addEventListener('load', resolve, { once: true });
                img.addEventListener('error', resolve, { once: true });
                setTimeout(resolve, timeoutMs);
            });
        }));
    };

    const captureCurrentSlide = async () => {
        if (typeof html2canvas !== 'function') {
            throw new Error('html2canvas not loaded yet');
        }
        const prevTransform = stage.style.transform;
        stage.style.transform = 'none';
        await waitForImages(slideContent);
        // A plain timeout (not requestAnimationFrame) so this can't stall if the tab is
        // backgrounded/throttled right as the user taps download/share.
        await new Promise((resolve) => setTimeout(resolve, 50));
        try {
            return await html2canvas(slideContent, {
                backgroundColor: null,
                useCORS: true,
                width: STAGE_WIDTH,
                height: STAGE_HEIGHT,
                scale: 1
            });
        } finally {
            stage.style.transform = prevTransform;
        }
    };

    const periodSlug = () => (currentPeriod?.label || 'wrapped').toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '');

    downloadBtn.addEventListener('click', async () => {
        if (!storyActive) return;
        pauseTimer();
        try {
            const canvas = await captureCurrentSlide();
            const blob2 = await new Promise((resolve) => canvas.toBlob(resolve, 'image/png'));
            if (!blob2) throw new Error('Could not render image');
            const url = URL.createObjectURL(blob2);
            const a = document.createElement('a');
            a.href = url;
            a.download = `steam-wrapped-${periodSlug()}-slide-${currentIndex + 1}.png`;
            document.body.appendChild(a);
            a.click();
            a.remove();
            setTimeout(() => URL.revokeObjectURL(url), 2000);
        } catch (e) {
            console.error('Download failed:', e);
            setMessage('Could not generate the image. Try again — if it keeps failing, the profile avatar image may be blocking export.', true);
        } finally {
            resumeTimer();
        }
    });

    shareBtn.addEventListener('click', async () => {
        if (!storyActive) return;
        pauseTimer();
        try {
            const canvas = await captureCurrentSlide();
            const blob2 = await new Promise((resolve) => canvas.toBlob(resolve, 'image/png'));
            if (!blob2) throw new Error('Could not render image');
            const file = new File([blob2], `steam-wrapped-${periodSlug()}.png`, { type: 'image/png' });
            if (navigator.canShare && navigator.canShare({ files: [file] })) {
                await navigator.share({ files: [file], title: 'My Steam Wrapped' });
            } else {
                const url = URL.createObjectURL(blob2);
                const a = document.createElement('a');
                a.href = url;
                a.download = `steam-wrapped-${periodSlug()}-slide-${currentIndex + 1}.png`;
                document.body.appendChild(a);
                a.click();
                a.remove();
                setTimeout(() => URL.revokeObjectURL(url), 2000);
                setMessage('Your browser can\'t share files directly — the image was downloaded instead. Post it to your Instagram Story from your gallery.');
            }
        } catch (e) {
            if (e?.name !== 'AbortError') {
                console.error('Share failed:', e);
                setMessage('Could not share the image. Try downloading it instead.', true);
            }
        } finally {
            resumeTimer();
        }
    });

    // ── Main flow ────────────────────────────────────────────────────────────

    const generateWrapped = async (steamId) => {
        const mode = getPeriodMode();
        const period = getPeriodRange(mode);
        if (!period) {
            setMessage('Pick a valid period first.', true);
            wrappedMessage.classList.remove('hidden');
            return;
        }

        setMessage(`Loading your ${period.noun} in gaming…`);
        wrappedMessage.classList.remove('hidden');
        wrappedSection.classList.add('hidden');
        storyActive = false;
        cancelTimer();
        currentPeriod = period;

        const previousRange = getPreviousPeriodRange(period, mode);
        const dateParams = new URLSearchParams({ steamid: steamId, startDate: period.startDate, endDate: period.endDate });
        const previousDateParams = new URLSearchParams({ steamid: steamId, startDate: previousRange.startDate, endDate: previousRange.endDate });
        const insightParams = new URLSearchParams({ steamid: steamId });
        const [rows, prevRows, playtimeInsights, activityInsights, profile] = await Promise.all([
            fetchJsonOrNull(`/api/tracking/profile-date?${dateParams}`),
            fetchJsonOrNull(`/api/tracking/profile-date?${previousDateParams}`),
            fetchJsonOrNull(`/api/tracking/insights/playtime?${insightParams}`),
            fetchJsonOrNull(`/api/tracking/insights/activity?${insightParams}`),
            fetchJsonOrNull(`/api/profile/live?steamId=${encodeURIComponent(steamId)}`)
        ]);

        const periodStats = Array.isArray(rows) ? aggregatePeriod(rows) : null;

        if (!periodStats) {
            const hasAnyTrackingAtAll = playtimeInsights || activityInsights || (Array.isArray(rows) && rows.length > 0);
            if (!hasAnyTrackingAtAll) {
                setMessage('No tracking data yet for this profile. Enable Play Tracking and check back after a few days of play.', true);
            } else {
                setMessage(`No activity recorded for ${period.label}. Try a different period.`, true);
            }
            return;
        }

        // null = the fetch failed and the previous period is genuinely unknown (trend chips say
        // so). An array that aggregates to no activity is a known zero, not an unknown.
        const previousStats = Array.isArray(prevRows)
            ? (aggregatePeriod(prevRows) || { totalMinutes: 0, uniqueGames: 0, activeDays: 0, topGame: null, peakDay: null, longestStreak: null })
            : null;

        if (profile?.avatarUrl) {
            // Warm the browser cache so the cover slide's <img> paints immediately instead of
            // popping in, and so an early download/share doesn't race the network fetch.
            const preload = new Image();
            preload.crossOrigin = 'anonymous';
            preload.src = profile.avatarUrl;
        }

        slides = buildSlides({
            periodStats,
            period,
            profile,
            alltime: playtimeInsights,
            currentStreakDays: Number(activityInsights?.currentStreakDays) || 0,
            previousStats
        });
        renderProgressBars();
        applyTheme(themeSelect.value);
        wrappedMessage.classList.add('hidden');
        wrappedSection.classList.remove('hidden');
        updateStageScale();
        storyActive = true;
        showSlide(0);
    };

    themeSelect.addEventListener('change', () => {
        applyTheme(themeSelect.value);
    });

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const rawSteamId = steamIdInput.value.trim();
        if (!rawSteamId) {
            setMessage('Steam ID required', true);
            wrappedMessage.classList.remove('hidden');
            return;
        }

        setMessage('Resolving profile…');
        wrappedMessage.classList.remove('hidden');

        let steamId;
        try {
            steamId = await resolveSteamIdInput(rawSteamId);
        } catch (error) {
            console.error(error);
            setMessage('Failed to resolve identifier.', true);
            return;
        }

        if (!steamId) {
            setMessage('Input did not resolve to a Steam64 ID.', true);
            return;
        }

        steamIdInput.value = steamId;
        persistSteamIdInQuery(steamId);
        syncNavLinks(steamId);

        try {
            await generateWrapped(steamId);
        } catch (error) {
            console.error(error);
            setMessage('Failed to generate Wrapped.', true);
        }
    });

    loadThemes();
    updateStageScale();
    initPeriodDefaults();

    const detected = bootstrapSteamId({
        input: steamIdInput,
        onDetected: () => form.dispatchEvent(new Event('submit'))
    });
    if (!detected) {
        syncNavLinks('');
    }
});
