document.addEventListener('DOMContentLoaded', () => {
    const utils = window.SteamWidget || {};
    const resolveSteamIdInput = utils.resolveSteamIdInput || (async (value) => value?.trim() || null);
    const persistSteamIdInQuery = utils.persistSteamIdInQuery || (() => {});
    const syncNavLinks = utils.syncNavLinks || (() => {});
    const bootstrapSteamId = utils.bootstrapSteamId || (() => null);

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

    const persistSteamIdInQueryLocal = (steamId) => {
        persistSteamIdInQuery(steamId);
        syncNavLinks(steamId);
    };

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const rawSteamId = steamIdInput.value.trim();
        if (!rawSteamId) {
            setState({
                badgeClass: 'badge-idle',
                badgeText: 'Missing Steam ID',
                message: 'Please provide a Steam ID to continue.',
                showMeta: false,
                hideLogin: false
            });
            return;
        }

        setState({
            badgeClass: 'badge-idle',
            badgeText: 'Resolving…',
            message: 'Resolving the provided identifier via Steam.',
            showMeta: false,
            hideLogin: true
        });

        let resolvedSteamId;
        try {
            resolvedSteamId = await resolveSteamIdInput(rawSteamId);
        } catch (error) {
            console.error(error);
            setState({
                badgeClass: 'badge-off',
                badgeText: 'Resolve failed',
                message: 'Unable to resolve this Steam identifier right now.',
                showMeta: false,
                hideLogin: false
            });
            return;
        }

        if (!resolvedSteamId) {
            setState({
                badgeClass: 'badge-off',
                badgeText: 'Unknown profile',
                message: 'We could not resolve that input to a Steam64 ID.',
                showMeta: false,
                hideLogin: false
            });
            return;
        }

        steamIdInput.value = resolvedSteamId;
        persistSteamIdInQueryLocal(resolvedSteamId);

        setState({
            badgeClass: 'badge-idle',
            badgeText: 'Checking…',
            message: 'Fetching profile details from the server.',
            showMeta: false,
            hideLogin: true
        });

        try {
            const profile = await fetchProfile(resolvedSteamId);
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
        const detected = bootstrapSteamId({
            input: steamIdInput,
            onDetected: (steamId) => {
                bootstrappedFromQuery = true;
                syncNavLinks(steamId);
                form.dispatchEvent(new Event('submit'));
            }
        });
        if (!detected) {
            syncNavLinks('');
        }
    };

    const syncNavLinksInput = () => {
        syncNavLinks(steamIdInput.value.trim());
    };

    steamIdInput.addEventListener('input', syncNavLinksInput);
    form.addEventListener('submit', syncNavLinksInput);
    runFromQuery();
    syncNavLinksInput();
});
