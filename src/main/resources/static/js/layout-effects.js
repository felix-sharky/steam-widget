document.addEventListener('DOMContentLoaded', () => {
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    const mobileMenu = document.getElementById('mobileMenu');

    if (mobileMenuBtn && mobileMenu) {
        mobileMenuBtn.addEventListener('click', () => {
            mobileMenu.classList.toggle('hidden');
        });
    }

    document.querySelectorAll('.glass-card').forEach((card) => {
        card.addEventListener('mousemove', (event) => {
            const rect = card.getBoundingClientRect();
            card.style.setProperty('--mouse-x', `${event.clientX - rect.left}px`);
            card.style.setProperty('--mouse-y', `${event.clientY - rect.top}px`);
        });
    });
});


// ── Year-end Wrapped banner ────────────────────────────────────────────────
// Shown site-wide around New Year. The window is defined server-side (YearEndService) and
// exposed via /api/wrapped/year-end.

// Resolves to the year being wrapped up, or null outside the window / on error.
const fetchWrapYear = async () => {
    try {
        const response = await fetch('/api/wrapped/year-end');
        if (!response.ok) return null;
        const payload = await response.json();
        return Number.isInteger(payload?.wrapYear) ? payload.wrapYear : null;
    } catch (e) {
        return null;
    }
};

// Dismissing the banner hides it for this long, so it reappears after the holidays.
const YEAR_END_BANNER_SNOOZE_MS = 3 * 24 * 60 * 60 * 1000;

// Steam ID the visitor has used before (query param or the cookie set by Steam login).
const getKnownSteamId = () => {
    const fromQuery = new URLSearchParams(window.location.search).get('steamId')?.trim();
    return fromQuery || window.SteamWidget?.getCookie?.('steamId')?.trim() || null;
};

// Resolves to true/false for a known profile's tracking flag, or null when unknown.
const fetchTrackingStatus = async (steamId) => {
    if (!steamId) return null;
    try {
        const response = await fetch(`/api/profile?steamId=${encodeURIComponent(steamId)}`);
        if (!response.ok) return null;
        const profile = await response.json();
        return typeof profile?.tracking === 'boolean' ? profile.tracking : null;
    } catch (e) {
        return null;
    }
};

const buildBannerLink = ({ base, steamId, variant, primary, icon, label, event, wrapYear }) => {
    const target = new URL(base, window.location.origin);
    if (steamId) target.searchParams.set('steamId', steamId);
    return `<a class="year-end-banner__btn year-end-banner__btn--${primary ? 'primary' : 'secondary'}"
               href="${target.pathname}${target.search}" data-base="${base}"
               data-umami-event="${event}" data-umami-event-variant="${variant}" data-umami-event-year="${wrapYear}">
                <span class="material-symbols-outlined" aria-hidden="true">${icon}</span>${label}</a>`;
};

// On the Wrapped page, puts the wrap year into the title and meta tags (e.g. "Steam Wrapped 2026").
// Only crawlers that execute JS (like Google) see this; link-preview bots read the static HTML.
const applyWrapYearToWrappedPage = (wrapYear) => {
    document.title = document.title.replace('steam-widget | Steam Wrapped', `Steam Wrapped ${wrapYear} | steam-widget`);
    document.querySelectorAll('meta[name="description"], meta[property^="og:"], meta[name^="twitter:"]').forEach((meta) => {
        const content = meta.getAttribute('content');
        if (!content) return;
        meta.setAttribute('content', content
            .replace('steam-widget | Steam Wrapped', `Steam Wrapped ${wrapYear} | steam-widget`)
            .replace('Generate your Steam Wrapped —', `Generate your Steam Wrapped ${wrapYear} —`));
    });
};

const showYearEndBanner = async () => {
    const wrapYear = await fetchWrapYear();
    if (wrapYear === null) return;

    const path = window.location.pathname;
    const onWrapped = path.startsWith('/wrapped');
    const onTracking = path.startsWith('/tracking');

    if (onWrapped) applyWrapYearToWrappedPage(wrapYear);

    const storageKey = `yearEndBannerDismissed:${wrapYear}`;
    try {
        const dismissedAt = Number(localStorage.getItem(storageKey));
        if (dismissedAt && Date.now() - dismissedAt < YEAR_END_BANNER_SNOOZE_MS) return;
    } catch (e) { /* storage unavailable – always show */ }

    // Only look up the profile once we know the banner will actually be shown.
    const steamId = getKnownSteamId();
    const tracking = await fetchTrackingStatus(steamId);

    const variant = tracking === true ? 'tracked' : tracking === false ? 'untracked' : 'unknown';

    const wrappedLink = (primary) => onWrapped ? '' : buildBannerLink({
        base: `/wrapped.html?year=${wrapYear}`, steamId, variant, primary, wrapYear,
        icon: 'auto_awesome', label: 'Get my Wrapped', event: 'year-end-banner-wrapped'
    });
    const trackingLink = (primary) => onTracking ? '' : buildBannerLink({
        base: '/tracking.html', steamId, variant, primary, wrapYear,
        icon: 'monitoring', label: 'Start tracking', event: 'year-end-banner-tracking'
    });

    // Untracked profiles would get an empty recap, so pitch tracking to them instead.
    let title, body, actions;
    if (variant === 'tracked') {
        title = `Your Steam Wrapped ${wrapYear} is ready!`;
        body = 'Relive your year in gaming &mdash; top games, total playtime and longest streaks in a shareable recap.';
        actions = wrappedLink(true);
    } else if (variant === 'untracked') {
        title = `Start tracking today &mdash; your ${wrapYear + 1} Wrapped will thank you!`;
        body = `Your profile isn't tracked yet, so there's no ${wrapYear} recap to show.
                Turn on free play tracking now and get a complete Wrapped with your top games, playtime and streaks next year.`;
        actions = trackingLink(true);
    } else {
        title = `Your Steam Wrapped ${wrapYear} is ready!`;
        body = `Relive your year in gaming &mdash; top games, total playtime and longest streaks in a shareable recap.
                Not tracking yet? Turn on free play tracking now so your ${wrapYear + 1} Wrapped is complete.`;
        actions = wrappedLink(true) + trackingLink(false);
    }

    const banner = document.createElement('aside');
    banner.className = 'year-end-banner';
    banner.setAttribute('role', 'region');
    banner.setAttribute('aria-label', `Steam Wrapped ${wrapYear} announcement`);
    banner.innerHTML = `
        <div class="year-end-banner__inner">
            <span class="material-symbols-outlined year-end-banner__icon" aria-hidden="true">celebration</span>
            <div class="year-end-banner__text">
                <p class="year-end-banner__title">${title}</p>
                <p class="year-end-banner__body">${body}</p>
            </div>
            ${actions ? `<div class="year-end-banner__actions">${actions}</div>` : ''}
            <button type="button" class="year-end-banner__close" aria-label="Dismiss announcement"
                    data-umami-event="year-end-banner-dismiss" data-umami-event-variant="${variant}" data-umami-event-year="${wrapYear}">
                <span class="material-symbols-outlined" aria-hidden="true">close</span>
            </button>
        </div>`;

    banner.querySelector('.year-end-banner__close').addEventListener('click', () => {
        banner.remove();
        try {
            localStorage.setItem(storageKey, String(Date.now()));
        } catch (e) { /* ignore */ }
    });

    const header = document.querySelector('body > header');
    if (header) {
        header.after(banner);
    } else {
        document.body.prepend(banner);
    }
};

document.addEventListener('DOMContentLoaded', showYearEndBanner);
