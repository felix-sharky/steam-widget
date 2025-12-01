document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('trackingForm');
    const steamIdInput = document.getElementById('steamId');
    const statusMessage = document.getElementById('statusMessage');
    const statusBadge = document.getElementById('trackingBadge');
    const profileMeta = document.getElementById('profileMeta');
    const profileName = document.getElementById('profileName');
    const profileSteamId = document.getElementById('profileSteamId');
    const profileHits = document.getElementById('profileHits');
    const loginCard = document.getElementById('loginCard');
    const navLinks = Array.from(document.querySelectorAll('.nav-link[data-base]'));

    let bootstrappedFromQuery = false;

    const setState = (state) => {
        const { badgeClass, badgeText, message, showMeta, meta } = state;
        statusBadge.className = `badge ${badgeClass}`;
        statusBadge.textContent = badgeText;
        statusMessage.textContent = message;
        profileMeta.hidden = !showMeta;
        if (meta) {
            profileName.textContent = meta.name || 'Unknown';
            profileSteamId.textContent = meta.steamId || '—';
            profileHits.textContent = typeof meta.hits === 'number' ? meta.hits : '—';
        }
    };

    const fetchProfile = async (steamId) => {
        const response = await fetch(`/api/profile?steamId=${encodeURIComponent(steamId)}`);
        if (!response.ok) {
            throw new Error(`Request failed: ${response.status}`);
        }
        return response.json();
    };

    const persistSteamIdInQuery = (steamId) => {
        const next = new URL(window.location.href);
        if (steamId) {
            next.searchParams.set('steamId', steamId);
        } else {
            next.searchParams.delete('steamId');
        }
        window.history.replaceState({}, '', next);
    };

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const steamId = steamIdInput.value.trim();
        if (!steamId) {
            setState({
                badgeClass: 'badge-idle',
                badgeText: 'Missing Steam ID',
                message: 'Please provide a Steam ID to continue.',
                showMeta: false,
                hideLogin: false
            });
            return;
        }

        persistSteamIdInQuery(steamId);

        setState({
            badgeClass: 'badge-idle',
            badgeText: 'Checking…',
            message: 'Fetching profile details from the server.',
            showMeta: false,
            hideLogin: true
        });

        try {
            const profile = await fetchProfile(steamId);
            const isTracked = Boolean(profile.tracking);
            setState({
                badgeClass: isTracked ? 'badge-on' : 'badge-off',
                badgeText: isTracked ? 'Tracking enabled' : 'Tracking disabled',
                message: isTracked
                    ? 'Great! This profile is currently tracked.'
                    : 'Tracking is disabled. Use the Steam login below to enable it.',
                showMeta: Boolean(profile.steam64id),
                hideLogin: isTracked,
                meta: {
                    name: profile.name,
                    steamId: profile.steam64id,
                    hits: profile.hits
                }
            });
        } catch (error) {
            console.error(error);
            setState({
                badgeClass: 'badge-off',
                badgeText: 'Lookup failed',
                message: 'Unable to fetch profile information. Please try again later.',
                showMeta: false,
                hideLogin: false
            });
        }
    });

    const runFromQuery = () => {
        const params = new URLSearchParams(window.location.search);
        const steamId = params.get('steamId');
        if (steamId) {
            steamIdInput.value = steamId;
            bootstrappedFromQuery = true;
            form.dispatchEvent(new Event('submit'));
            return;
        }
        const cookieSteamId = getCookie('steamId');
        if (cookieSteamId) {
            steamIdInput.value = cookieSteamId;
            form.dispatchEvent(new Event('submit'));
        }
    };

    const syncNavLinks = () => {
        const steamId = steamIdInput.value.trim();
        navLinks.forEach((link) => {
            const base = link.getAttribute('data-base') || link.href;
            const target = new URL(base, window.location.origin);
            if (steamId) {
                target.searchParams.set('steamId', steamId);
            }
            link.href = target.toString();
        });
    };

    steamIdInput.addEventListener('input', syncNavLinks);
    form.addEventListener('submit', syncNavLinks);
    runFromQuery();
    syncNavLinks();
});

function getCookie(name) {
    const cookies = document.cookie ? document.cookie.split('; ') : [];
    for (const cookie of cookies) {
        const [key, ...rest] = cookie.split('=');
        if (key === name) {
            return decodeURIComponent(rest.join('='));
        }
    }
    return null;
}
