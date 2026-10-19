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


// ── Theme switcher ──────────────────────────────────────────────────────
// "default" is Conference ID Badge (this site's confirmed direction) and needs
// no data-theme attribute — themes.css falls back to it on :root. "dark" and
// "light" set the attribute explicitly. The actual pre-paint application (so
// a saved choice doesn't flash the default theme first) happens in the inline
// <script> at the top of <head> on every page; this just keeps the icon
// buttons in sync with that state and with each other (desktop + mobile
// instances are separate button groups on the same page).
const THEME_STORAGE_KEY = 'steamWidgetTheme';
const VALID_THEMES = ['default', 'dark', 'light'];

const applyTheme = (theme) => {
    if (theme === 'default') {
        document.documentElement.removeAttribute('data-theme');
    } else {
        document.documentElement.setAttribute('data-theme', theme);
    }
};

const initThemeSwitcher = () => {
    const buttons = document.querySelectorAll('.theme-toggle-btn');
    if (!buttons.length) return;

    // Chart.js bakes colors into the canvas at creation time and never reads CSS
    // again, so a chart already on screen would keep its old-theme colors after a
    // live switch. Reloading is the simple, robust fix — only needed on pages that
    // actually have a chart; everything else (Tailwind classes, badge-ui.css) is
    // already CSS-variable driven and updates instantly via the data-theme attribute.
    const hasChart = Boolean(document.querySelector('canvas'));

    const markActive = (theme) => {
        buttons.forEach((btn) => {
            btn.classList.toggle('active', btn.dataset.themeValue === theme);
            btn.setAttribute('aria-pressed', String(btn.dataset.themeValue === theme));
        });
    };

    // Keeps the widget style picker (Generator's "Badge finish", Wrapped's theme select) in
    // step with the site theme — card/dark/light map to the Card/Midnight/Light widget styles
    // (see SteamWidget.SITE_TO_WIDGET_STYLE). Only touches it if the matching <option> is
    // already loaded (both selects populate async from /api/widget/styles|themes), and fires
    // 'change' so whichever page it's on (live widget preview, Wrapped slide) re-renders.
    const syncWidgetStyle = (siteTheme) => {
        const widgetUtils = window.SteamWidget || {};
        const getMapped = widgetUtils.getWidgetStyleForSiteTheme || (() => 'CARD');
        const mapped = getMapped(siteTheme);
        ['widgetStyle', 'themeSelect'].forEach((id) => {
            const select = document.getElementById(id);
            if (!select) return;
            const hasOption = Array.from(select.options).some((o) => o.value === mapped);
            if (!hasOption || select.value === mapped) return;
            select.value = mapped;
            select.dispatchEvent(new Event('change'));
        });
    };

    // The badge's QR module color tracks the theme's text color too (see generator.js);
    // that only happens as a side effect of regenerating the widget, which needs a Steam
    // ID. This covers the default/no-ID state directly so the QR still re-colors.
    const refreshBadgeQr = () => {
        if (typeof window.updateBadgeIdentity !== 'function') return;
        const steamIdField = document.getElementById('steamId');
        window.updateBadgeIdentity(steamIdField?.value?.trim() || null);
    };

    markActive(document.documentElement.getAttribute('data-theme') || 'default');

    buttons.forEach((button) => {
        button.addEventListener('click', () => {
            const next = VALID_THEMES.includes(button.dataset.themeValue) ? button.dataset.themeValue : 'default';
            try {
                localStorage.setItem(THEME_STORAGE_KEY, next);
            } catch (e) { /* storage unavailable – theme still applies for this load */ }
            if (hasChart) {
                window.location.reload();
                return;
            }
            applyTheme(next);
            markActive(next);
            syncWidgetStyle(next);
            refreshBadgeQr();
        });
    });
};

document.addEventListener('DOMContentLoaded', initThemeSwitcher);


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
    const onTracking = path.startsWith('/playing-stats');

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
        base: '/playing-stats.html', steamId, variant, primary, wrapYear,
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


// ── Nav bar account menu ────────────────────────────────────────────────────
// Shown site-wide: always the same account-icon button. Logged out, its menu holds a
// "Login with Steam" action; logged in, it holds the profile (name + avatar, linking to
// /u/{id}) and "Log out" instead. Reads the claim-flow session (never the plain prefill
// cookie) via /api/session/me.

const fetchSessionSteamId = async () => {
    try {
        const response = await fetch('/api/session/me');
        if (!response.ok) return null;
        const payload = await response.json();
        return payload?.steamId || null;
    } catch (e) {
        return null;
    }
};

const fetchProfileSummary = async (steamId) => {
    try {
        const response = await fetch(`/api/profile/live?steamId=${encodeURIComponent(steamId)}`);
        if (!response.ok) return null;
        return await response.json();
    } catch (e) {
        return null;
    }
};

const loggedOutAccountMenuContent = () => `
    <a href="/steam/login" class="flex items-center gap-2 px-4 py-2 text-on-surface font-semibold hover:bg-surface-container-highest transition-colors">
        <span class="material-symbols-outlined text-base">login</span>
        Login with Steam
    </a>`;

const loggedInAccountMenuContent = (steamId, profile) => {
    const name = profile?.name || 'My profile';
    const avatarUrl = profile?.avatarUrl;
    const avatarMarkup = avatarUrl
        ? `<img src="${avatarUrl}" alt="" class="w-8 h-8 rounded-full shrink-0">`
        : '<span class="material-symbols-outlined text-2xl shrink-0">account_circle</span>';

    return `
        <a href="/u/${steamId}" class="flex items-center gap-3 px-4 py-2 hover:bg-surface-container-highest transition-colors">
            ${avatarMarkup}
            <span class="font-body-md text-on-surface font-semibold truncate">${name}</span>
        </a>
        <div class="border-t border-outline-variant/20 my-1"></div>
        <a href="/steam/logout" class="flex items-center gap-2 px-4 py-2 text-on-surface-variant hover:text-tertiary-container hover:bg-surface-container-highest transition-colors">
            <span class="material-symbols-outlined text-base">logout</span>
            Log out
        </a>`;
};

const buildNavAccountMenu = (steamId, profile) => {
    const wrapper = document.createElement('div');
    wrapper.className = 'relative';

    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'flex items-center p-2 rounded-lg text-on-surface-variant hover:text-primary-container transition-colors';
    button.setAttribute('aria-haspopup', 'true');
    button.setAttribute('aria-expanded', 'false');
    button.setAttribute('aria-label', 'Account menu');
    button.innerHTML = '<span class="material-symbols-outlined">account_circle</span>';

    const dropdown = document.createElement('div');
    dropdown.className = 'hidden absolute right-0 mt-2 w-56 rounded-lg bg-surface-container-high border border-outline-variant/20 shadow-lg py-2 z-50';
    dropdown.innerHTML = steamId ? loggedInAccountMenuContent(steamId, profile) : loggedOutAccountMenuContent();

    const closeDropdown = () => {
        dropdown.classList.add('hidden');
        button.setAttribute('aria-expanded', 'false');
    };

    button.addEventListener('click', (event) => {
        event.stopPropagation();
        const isOpen = !dropdown.classList.contains('hidden');
        dropdown.classList.toggle('hidden');
        button.setAttribute('aria-expanded', String(!isOpen));
    });

    document.addEventListener('click', (event) => {
        if (!wrapper.contains(event.target)) closeDropdown();
    });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') closeDropdown();
    });

    wrapper.append(button, dropdown);
    return wrapper;
};

const showNavLoginIndicator = async () => {
    const nav = document.querySelector('header nav[aria-label="Primary navigation"]');
    if (!nav) return;

    const steamId = await fetchSessionSteamId();
    const profile = steamId ? await fetchProfileSummary(steamId) : null;

    // Join the desktop link group when the page has one (index.html-style nav), or a page's
    // explicitly marked link group (simpler single-row navs, e.g. profile-settings.html);
    // only fall back to the nav element itself as a last resort, since appending there adds
    // a 3rd justify-between child and drags the existing links toward the center.
    const linkGroup = nav.querySelector('.hidden.md\\:flex') || nav.querySelector('#navLinkGroup') || nav;
    linkGroup.appendChild(buildNavAccountMenu(steamId, profile));
};

document.addEventListener('DOMContentLoaded', showNavLoginIndicator);
