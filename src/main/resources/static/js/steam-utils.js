(function (window) {
    'use strict';

    const namespace = window.SteamWidget = window.SteamWidget || {};

    namespace.resolveSteamIdInput = async function resolveSteamIdInput(steamLike) {
        const trimmed = (steamLike || '').trim();
        if (!trimmed) {
            return null;
        }
        const response = await fetch(`/api/profile/resolve?vanityUrl=${encodeURIComponent(trimmed)}`);
        if (!response.ok) {
            throw new Error(`Resolve failed: ${response.status}`);
        }
        const payload = await response.json();
        return payload?.steamId?.trim() || null;
    };

    namespace.persistSteamIdInQuery = function persistSteamIdInQuery(steamId, paramName = 'steamId') {
        const next = new URL(window.location.href);
        if (steamId) {
            next.searchParams.set(paramName, steamId);
        } else {
            next.searchParams.delete(paramName);
        }
        window.history.replaceState({}, '', next);
    };

    namespace.syncNavLinks = function syncNavLinks(steamId, selector = '.nav-link[data-base]') {
        document.querySelectorAll(selector).forEach((link) => {
            const base = link.getAttribute('data-base') || link.getAttribute('href') || link.href;
            const target = new URL(base, window.location.origin);
            if (steamId) {
                target.searchParams.set('steamId', steamId);
            } else {
                target.searchParams.delete('steamId');
            }
            link.href = target.toString();
        });
    };

    namespace.getCookie = function getCookie(name) {
        if (!name) {
            return null;
        }
        const cookies = document.cookie ? document.cookie.split('; ') : [];
        for (const cookie of cookies) {
            const [key, ...rest] = cookie.split('=');
            if (key === name) {
                return decodeURIComponent(rest.join('='));
            }
        }
        return null;
    };

    namespace.bootstrapSteamId = function bootstrapSteamId(options = {}) {
        const {
            paramName = 'steamId',
            cookieName = 'steamId',
            input,
            onDetected,
            syncNav = true
        } = options;
        const params = new URLSearchParams(window.location.search);
        const rawQuery = params.get(paramName);
        const queryValue = typeof rawQuery === 'string' ? rawQuery.trim() : '';
        const rawCookie = namespace.getCookie?.(cookieName);
        const cookieValue = typeof rawCookie === 'string' ? rawCookie.trim() : '';
        const value = queryValue || cookieValue || '';
        if (!value) {
            if (syncNav) {
                namespace.syncNavLinks?.('');
            }
            return null;
        }
        if (input) {
            input.value = value;
        }
        if (syncNav) {
            namespace.syncNavLinks?.(value);
        }
        if (typeof onDetected === 'function') {
            onDetected(value, { source: queryValue ? 'query' : 'cookie' });
        }
        return value;
    };
})(window);
