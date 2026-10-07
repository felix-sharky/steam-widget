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

    const STAGE_WIDTH = 1080;
    const STAGE_HEIGHT = 1920;
    const SLIDE_DURATION_MS = 6000;

    let themeColorsById = {};
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

    const bigNumberBlock = (value, unit) => `
        <div class="wrapped-big-number">${escapeHtml(value)}</div>
        <div class="wrapped-eyebrow" style="margin-top:-18px;">${escapeHtml(unit)}</div>`;

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

    const year = new Date().getFullYear();

    const buildCoverSlide = (profile) => {
        const name = profile?.name ? escapeHtml(profile.name) : 'Your';
        const avatar = profile?.avatarUrl
            ? `<img class="wrapped-avatar" src="${escapeHtml(profile.avatarUrl)}" crossorigin="anonymous" alt="">`
            : `<div class="wrapped-icon-badge"><span class="material-symbols-outlined">person</span></div>`;
        return `
            ${blob('width:620px;height:620px;top:-220px;left:-180px;')}
            ${blob('width:520px;height:520px;bottom:-180px;right:-160px;')}
            <div class="wrapped-slide-inner">
                ${avatar}
                <span class="wrapped-eyebrow">Steam Wrapped ${year}</span>
                <h2 class="wrapped-headline">${name}'s year<br>in gaming</h2>
                <p class="wrapped-caption">Let's relive the hours, the streaks, and the game that owned your year.</p>
            </div>
            ${footerBrand()}`;
    };

    const buildTotalPlaytimeSlide = (playtime) => {
        const hours = Number(playtime.yearHours) || 0;
        const minutes = Number(playtime.yearMinutes) || 0;
        const alltimeMinutesTotal = (Number(playtime.alltimeHours) || 0) * 60 + (Number(playtime.alltimeMinutes) || 0);
        const yearMinutesTotal = hours * 60 + minutes;
        let subcaption = '';
        if (alltimeMinutesTotal > 0) {
            const pct = Math.min(100, Math.round((yearMinutesTotal / alltimeMinutesTotal) * 100));
            subcaption = `That's ${pct}% of your all-time ${fmtDuration(playtime.alltimeHours, playtime.alltimeMinutes)} on record.`;
        }
        return `
            ${blob('width:560px;height:560px;top:-200px;right:-180px;')}
            <div class="wrapped-slide-inner">
                <span class="wrapped-icon-badge"><span class="material-symbols-outlined">timer</span></span>
                <span class="wrapped-eyebrow">Total playtime in ${year}</span>
                ${bigNumberBlock(hours, 'HOURS')}
                <p class="wrapped-caption">That's ${fmtDuration(hours, minutes)} across your library this year.</p>
                ${subcaption ? `<p class="wrapped-subcaption">${subcaption}</p>` : ''}
            </div>
            ${footerBrand()}`;
    };

    const buildTopGameSlide = (games) => {
        const duration = fmtDuration(games.mostPlayedYearHours, games.mostPlayedYearMinutes);
        return `
            ${blob('width:600px;height:600px;bottom:-220px;left:-180px;')}
            <div class="wrapped-slide-inner">
                <span class="wrapped-icon-badge"><span class="material-symbols-outlined">emoji_events</span></span>
                <span class="wrapped-eyebrow">Most played game</span>
                <h2 class="wrapped-headline">${escapeHtml(games.mostPlayedYearGame)}</h2>
                <p class="wrapped-caption">${duration} this year — your undisputed #1.</p>
            </div>
            ${footerBrand()}`;
    };

    const buildVarietySlide = (playtime) => {
        const thisYear = Number(playtime.uniqueGamesThisYear) || 0;
        const alltime = Number(playtime.uniqueGamesAlltime) || 0;
        const subcaption = alltime > 0 ? `Out of ${alltime} games you've played all-time.` : '';
        return `
            ${blob('width:560px;height:560px;top:-200px;left:-180px;')}
            <div class="wrapped-slide-inner">
                <span class="wrapped-icon-badge"><span class="material-symbols-outlined">sports_esports</span></span>
                <span class="wrapped-eyebrow">Game rotation</span>
                ${bigNumberBlock(thisYear, thisYear === 1 ? 'GAME' : 'GAMES')}
                <p class="wrapped-caption">different games kept you busy in ${year}.</p>
                ${subcaption ? `<p class="wrapped-subcaption">${subcaption}</p>` : ''}
            </div>
            ${footerBrand()}`;
    };

    const buildStreakSlide = (activity) => {
        const days = Number(activity.longestStreakYearDays) || 0;
        const range = fmtDateRange(activity.longestStreakYearStart, activity.longestStreakYearEnd);
        const current = Number(activity.currentStreakDays) || 0;
        const subcaption = current > 0 ? `You're currently on a ${current} day streak — keep it going.` : '';
        return `
            ${blob('width:560px;height:560px;bottom:-200px;right:-180px;')}
            <div class="wrapped-slide-inner">
                <span class="wrapped-icon-badge"><span class="material-symbols-outlined">local_fire_department</span></span>
                <span class="wrapped-eyebrow">Dedication</span>
                ${bigNumberBlock(days, 'DAY STREAK')}
                <p class="wrapped-caption">Your longest run in ${year}${range ? ` · ${range}` : ''}.</p>
                ${subcaption ? `<p class="wrapped-subcaption">${subcaption}</p>` : ''}
            </div>
            ${footerBrand()}`;
    };

    const buildPeakDaySlide = (playtime) => {
        const hours = Number(playtime.bestDayHours) || 0;
        const minutes = Number(playtime.bestDayMinutes) || 0;
        const date = fmtDate(playtime.bestDayDate);
        const value = hours > 0 ? hours : minutes;
        const unit = hours > 0 ? 'HOURS' : 'MINUTES';
        return `
            ${blob('width:560px;height:560px;top:-200px;right:-180px;')}
            <div class="wrapped-slide-inner">
                <span class="wrapped-icon-badge"><span class="material-symbols-outlined">star</span></span>
                <span class="wrapped-eyebrow">Peak day</span>
                ${bigNumberBlock(value, unit)}
                <p class="wrapped-caption">played in a single day${date ? ` on ${date}` : ''} — your most intense session of ${year}.</p>
            </div>
            ${footerBrand()}`;
    };

    const buildRecapSlide = ({ activity, playtime, games }) => {
        const items = [];
        if (games?.mostPlayedYearGame) {
            items.push({ icon: 'emoji_events', label: 'Top game', value: games.mostPlayedYearGame });
        }
        if (playtime && (playtime.yearHours || playtime.yearMinutes)) {
            items.push({ icon: 'timer', label: 'Total playtime', value: fmtDuration(playtime.yearHours, playtime.yearMinutes) });
        }
        if (playtime?.uniqueGamesThisYear) {
            items.push({ icon: 'sports_esports', label: 'Games played', value: `${playtime.uniqueGamesThisYear}` });
        }
        if (activity?.longestStreakYearDays) {
            items.push({ icon: 'local_fire_department', label: 'Longest streak', value: `${activity.longestStreakYearDays} days` });
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
                <span class="wrapped-eyebrow">${year} Recap</span>
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

    const buildSlides = ({ activity, playtime, games, profile }) => {
        const built = [{ render: () => buildCoverSlide(profile) }];

        if (playtime && (playtime.yearHours || playtime.yearMinutes)) {
            built.push({ render: () => buildTotalPlaytimeSlide(playtime) });
        }
        if (games?.mostPlayedYearGame) {
            built.push({ render: () => buildTopGameSlide(games) });
        }
        if (playtime?.uniqueGamesThisYear) {
            built.push({ render: () => buildVarietySlide(playtime) });
        }
        if (activity?.longestStreakYearDays) {
            built.push({ render: () => buildStreakSlide(activity) });
        }
        if (playtime && (playtime.bestDayHours || playtime.bestDayMinutes)) {
            built.push({ render: () => buildPeakDaySlide(playtime) });
        }
        built.push({ render: () => buildRecapSlide({ activity, playtime, games }) });
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
            a.download = `steam-wrapped-${year}-slide-${currentIndex + 1}.png`;
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
            const file = new File([blob2], `steam-wrapped-${year}.png`, { type: 'image/png' });
            if (navigator.canShare && navigator.canShare({ files: [file] })) {
                await navigator.share({ files: [file], title: 'My Steam Wrapped' });
            } else {
                const url = URL.createObjectURL(blob2);
                const a = document.createElement('a');
                a.href = url;
                a.download = `steam-wrapped-${year}-slide-${currentIndex + 1}.png`;
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
        setMessage('Loading your year in gaming…');
        wrappedMessage.classList.remove('hidden');
        wrappedSection.classList.add('hidden');
        storyActive = false;
        cancelTimer();

        const params = new URLSearchParams({ steamid: steamId });
        const [activity, playtime, games, profile] = await Promise.all([
            fetchJsonOrNull(`/api/tracking/insights/activity?${params}`),
            fetchJsonOrNull(`/api/tracking/insights/playtime?${params}`),
            fetchJsonOrNull(`/api/tracking/insights/games?${params}`),
            fetchJsonOrNull(`/api/profile/live?steamId=${encodeURIComponent(steamId)}`)
        ]);

        if (!activity && !playtime && !games) {
            setMessage('No tracking insights yet for this profile. Enable Play Tracking and check back after a few days of play.', true);
            return;
        }

        if (profile?.avatarUrl) {
            // Warm the browser cache so the cover slide's <img> paints immediately instead of
            // popping in, and so an early download/share doesn't race the network fetch.
            const preload = new Image();
            preload.crossOrigin = 'anonymous';
            preload.src = profile.avatarUrl;
        }

        slides = buildSlides({ activity, playtime, games, profile });
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

    const detected = bootstrapSteamId({
        input: steamIdInput,
        onDetected: () => form.dispatchEvent(new Event('submit'))
    });
    if (!detected) {
        syncNavLinks('');
    }
});
