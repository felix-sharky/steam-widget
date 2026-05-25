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

    namespace.getSelectedViewMode = function getSelectedViewMode(options = {}) {
        const {
            inputName = 'viewMode',
            dailyValue = 'date',
            defaultMode = 'month'
        } = options;
        const checked = document.querySelector(`input[name="${inputName}"]:checked`);
        return checked?.value === dailyValue ? dailyValue : defaultMode;
    };

    namespace.normalizeDateLabel = function normalizeDateLabel(value) {
        if (value === undefined || value === null) {
            return '';
        }
        if (Array.isArray(value) && value.length >= 3) {
            const [year, month, day] = value;
            return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        }
        return String(value);
    };

    namespace.toIsoDate = function toIsoDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    };

    namespace.getDefaultDateRangeForMode = function getDefaultDateRangeForMode(mode, now = new Date()) {
        const year = now.getFullYear();
        if (mode === 'date') {
            const monthStart = new Date(year, now.getMonth(), 1);
            const monthEnd = new Date(year, now.getMonth() + 1, 0);
            return {
                startDate: namespace.toIsoDate(monthStart),
                endDate: namespace.toIsoDate(monthEnd)
            };
        }
        const yearStart = new Date(year, 0, 1);
        const yearEnd = new Date(year, 11, 31);
        return {
            startDate: namespace.toIsoDate(yearStart),
            endDate: namespace.toIsoDate(yearEnd)
        };
    };

    namespace.getDateFilters = function getDateFilters(startInput, endInput) {
        const startDate = startInput?.value?.trim() || '';
        const endDate = endInput?.value?.trim() || '';
        return { startDate, endDate };
    };

    namespace.syncDateBounds = function syncDateBounds(startInput, endInput) {
        if (!startInput || !endInput) {
            return;
        }
        startInput.max = endInput.value || '';
        endInput.min = startInput.value || '';
    };

    namespace.persistDateFiltersInQuery = function persistDateFiltersInQuery(startDate, endDate) {
        const next = new URL(window.location.href);
        if (startDate) {
            next.searchParams.set('startDate', startDate);
        } else {
            next.searchParams.delete('startDate');
        }
        if (endDate) {
            next.searchParams.set('endDate', endDate);
        } else {
            next.searchParams.delete('endDate');
        }
        window.history.replaceState({}, '', next.toString());
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
