document.addEventListener('DOMContentLoaded', () => {
    const utils = window.SteamWidget || {};
    const resolveSteamIdInput = utils.resolveSteamIdInput || (async (value) => value?.trim() || null);
    const persistSteamIdInQuery = utils.persistSteamIdInQuery || (() => {});
    const syncNavLinks = utils.syncNavLinks || (() => {});
    const bootstrapSteamId = utils.bootstrapSteamId || (() => null);
    const getSelectedViewMode = utils.getSelectedViewMode
        || (() => (document.querySelector('input[name="viewMode"]:checked')?.value === 'date' ? 'date' : 'month'));
    const normalizeDateLabel = utils.normalizeDateLabel || ((value) => {
        if (value === undefined || value === null) return '';
        if (Array.isArray(value) && value.length >= 3) {
            const [year, month, day] = value;
            return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        }
        return String(value);
    });
    const getDateFiltersFromInputs = utils.getDateFilters
        || ((startInput, endInput) => ({
            startDate: startInput?.value?.trim() || '',
            endDate: endInput?.value?.trim() || ''
        }));
    const toIsoDate = utils.toIsoDate || ((date) => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    });
    const getDefaultRange = utils.getDefaultDateRangeForMode || ((mode, now = new Date()) => {
        const year = now.getFullYear();
        if (mode === 'date') {
            const monthStart = new Date(year, now.getMonth(), 1);
            const monthEnd = new Date(year, now.getMonth() + 1, 0);
            return { startDate: toIsoDate(monthStart), endDate: toIsoDate(monthEnd) };
        }
        return {
            startDate: toIsoDate(new Date(year, 0, 1)),
            endDate: toIsoDate(new Date(year, 11, 31))
        };
    });
    const syncDateBoundsForInputs = utils.syncDateBounds
        || ((startInput, endInput) => {
            if (!startInput || !endInput) return;
            startInput.max = endInput.value || '';
            endInput.min = startInput.value || '';
        });
    const persistDateFilters = utils.persistDateFiltersInQuery
        || ((startDate, endDate) => {
            const url = new URL(window.location.href);
            if (startDate) url.searchParams.set('startDate', startDate);
            else url.searchParams.delete('startDate');
            if (endDate) url.searchParams.set('endDate', endDate);
            else url.searchParams.delete('endDate');
            window.history.replaceState({}, '', url.toString());
        });

    const form = document.getElementById('profileMetricsForm');
    const steamIdInput = document.getElementById('steamId');
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    const tableBody = document.getElementById('tableBody');
    const tableStatus = document.getElementById('tableStatus');
    const chartStatus = document.getElementById('chartStatus');
    const chartCanvas = document.getElementById('profileMetricsChart');
    const viewModeInputs = document.querySelectorAll('input[name="viewMode"]');
    const trendTitle = document.getElementById('trendTitle');
    const trendDescription = document.getElementById('trendDescription');
    const tableTitle = document.getElementById('tableTitle');
    const tableDescription = document.getElementById('tableDescription');
    const periodHeader = document.getElementById('periodHeader');

    let chartInstance;
    let currentMode = 'month';

    const getViewMode = () => {
        return getSelectedViewMode({ inputName: 'viewMode', dailyValue: 'date', defaultMode: 'month' });
    };

    const setTableStatus = (cls, text) => {
        tableStatus.className = `badge ${cls}`;
        tableStatus.textContent = text;
    };

    const setChartStatus = (cls, text) => {
        chartStatus.className = `badge ${cls}`;
        chartStatus.textContent = text;
    };

    const normalizeRows = (rows, mode) => rows.map((row) => {
        const count = Number(row.count) || 0;
        const purpose = row.purpose ?? 'Unknown';

        if (mode === 'date') {
            const id = row.id || row;
            const rawDate = id.date
                ? normalizeDateLabel(id.date)
                : (id.year && id.month && id.day
                    ? `${id.year}-${String(id.month).padStart(2, '0')}-${String(id.day).padStart(2, '0')}`
                    : '');
            const [year, month, day] = rawDate ? rawDate.split('-') : [];
            return {
                label: rawDate || '',
                year: Number(year) || 0,
                month: Number(month) || 0,
                day: Number(day) || 0,
                purpose,
                count
            };
        }

        const id = row.id || row;
        const year = Number(id.year) || 0;
        const month = Number(id.month) || 0;
        const label = year && month ? `${year}-${String(month).padStart(2, '0')}` : '';
        return {
            label,
            year,
            month,
            day: 0,
            purpose,
            count
        };
    });

    const getDateFilters = () => {
        return getDateFiltersFromInputs(startDateInput, endDateInput);
    };

    const getDefaultDateRangeForMode = (mode) => {
        return getDefaultRange(mode);
    };

    const applyDefaultDateRangeForMode = (mode, options = {}) => {
        const { force = false } = options;
        if (!startDateInput || !endDateInput) {
            return;
        }
        if (!force && startDateInput.value && endDateInput.value) {
            return;
        }
        const defaults = getDefaultDateRangeForMode(mode);
        startDateInput.value = defaults.startDate;
        endDateInput.value = defaults.endDate;
        syncDateBounds();
    };

    const syncDateBounds = () => {
        syncDateBoundsForInputs(startDateInput, endDateInput);
    };

    const persistDateFiltersInQuery = () => {
        const { startDate, endDate } = getDateFilters();
        persistDateFilters(startDate, endDate);
    };

    const fetchMetrics = async (steamId, mode, startDate, endDate) => {
        const endpoint = mode === 'date' ? '/api/metrics/profile/day' : '/api/metrics/profile/month';
        const params = new URLSearchParams({ steam64id: steamId });
        if (startDate) {
            params.set('startDate', startDate);
        }
        if (endDate) {
            params.set('endDate', endDate);
        }
        const response = await fetch(`${endpoint}?${params.toString()}`);
        if (!response.ok) {
            throw new Error(`Request failed: ${response.status}`);
        }
        return response.json();
    };

    const sortRowsByDate = (rows) => {
        const safeLabel = (label) => label || '';
        return [...rows].sort((a, b) => safeLabel(b.label).localeCompare(safeLabel(a.label)));
    };

    const renderTable = (rows, mode = currentMode) => {
        if (!rows.length) {
            const colspan = mode === 'date' ? 3 : 3;
            tableBody.innerHTML = `<tr><td colspan="${colspan}" class="muted">No metrics found for this profile.</td></tr>`;
            return;
        }
        tableBody.innerHTML = rows.map((row) => {
            const periodLabel = row.label || (mode === 'date' ? 'Unknown date' : 'Unknown period');
            return `<tr>
                <td>${periodLabel}</td>
                <td>${row.purpose ?? '—'}</td>
                <td>${row.count ?? 0}</td>
            </tr>`;
        }).join('');
    };

    const renderChart = (rows, mode = currentMode) => {
        if (!rows.length) {
            if (chartInstance) {
                chartInstance.destroy();
                chartInstance = null;
            }
            setChartStatus('badge-idle', 'No data loaded');
            return;
        }

        const buckets = rows.reduce((acc, row) => {
            const label = row.label;
            if (!label) {
                return acc;
            }
            if (!acc[label]) {
                acc[label] = { total: 0 };
            }
            const purpose = String(row.purpose ?? '').trim();
            if (purpose) {
                if (!acc[label][purpose]) {
                    acc[label][purpose] = 0;
                }
                acc[label][purpose] += row.count;
            }
            acc[label].total += row.count;
            return acc;
        }, {});

        const labels = Object.keys(buckets).sort((a, b) => a.localeCompare(b));
        const purposes = [...new Set(rows.map((row) => row.purpose || 'Unknown'))];

        const datasets = purposes.map((purpose, index) => {
            const hue = (index * 63) % 360;
            return {
                label: purpose,
                data: labels.map((label) => buckets[label][purpose] ?? 0),
                backgroundColor: `hsla(${hue}, 70%, 55%, 0.65)`,
                borderColor: `hsl(${hue}, 70%, 55%)`,
                borderWidth: 1,
            };
        });

        const config = {
            type: 'bar',
            data: {
                labels,
                datasets
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        stacked: true,
                        ticks: { color: '#cbd5f5' }
                    },
                    y: {
                        stacked: true,
                        beginAtZero: true,
                        ticks: {
                            color: '#cbd5f5',
                            precision: 0
                        }
                    }
                },
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { color: '#cbd5f5' }
                    }
                }
            }
        };
        if (chartInstance) {
            chartInstance.destroy();
        }
        chartInstance = new Chart(chartCanvas, config);
        setChartStatus('badge-success', `Stacked view of ${purposes.length} purposes`);
    };

    const updateViewCopy = (mode) => {
        const isDaily = mode === 'date';
        const viewLabel = isDaily ? 'Daily' : 'Monthly';
        if (trendTitle) trendTitle.textContent = `${viewLabel} hits trend`;
        if (trendDescription) trendDescription.textContent = isDaily ? 'Chart displays hits per day.' : 'Chart displays hits per month.';
        if (tableTitle) tableTitle.textContent = isDaily ? 'Daily hits table' : 'Monthly hits table';
        if (tableDescription) {
            const endpoint = isDaily ? '/api/metrics/profile/day' : '/api/metrics/profile/month';
            tableDescription.innerHTML = `Data from <code>${endpoint}</code>.`;
        }
        if (periodHeader) periodHeader.textContent = 'Period';
    };

    updateViewCopy(currentMode);

    const handleViewModeChange = () => {
        currentMode = getViewMode();
        updateViewCopy(currentMode);
        applyDefaultDateRangeForMode(currentMode, { force: true });
        persistDateFiltersInQuery();
        if (steamIdInput.value.trim()) {
            form.dispatchEvent(new Event('submit'));
        } else {
            tableBody.innerHTML = '<tr><td colspan="3" class="muted">Enter a Steam ID and load metrics.</td></tr>';
            renderChart([], currentMode);
            setTableStatus('badge-idle', 'No data loaded');
            setChartStatus('badge-idle', 'No data loaded');
        }
    };

    viewModeInputs.forEach((input) => input.addEventListener('change', handleViewModeChange));

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const rawSteamId = steamIdInput.value.trim();
        const viewMode = getViewMode();
        const { startDate, endDate } = getDateFilters();
        currentMode = viewMode;
        updateViewCopy(viewMode);

        if (!rawSteamId) {
            setTableStatus('badge-error', 'Steam ID required');
            setChartStatus('badge-error', 'Steam ID required');
            return;
        }

        if (startDate && endDate && startDate > endDate) {
            tableBody.innerHTML = '<tr><td colspan="3" class="muted">Start date must be before or equal to end date.</td></tr>';
            setTableStatus('badge-error', 'Invalid date range');
            setChartStatus('badge-error', 'Invalid date range');
            return;
        }

        setTableStatus('badge-loading', 'Resolving…');
        setChartStatus('badge-loading', 'Resolving…');
        tableBody.innerHTML = '<tr><td colspan="3" class="muted">Resolving input…</td></tr>';
        let steamId;
        try {
            steamId = await resolveSteamIdInput(rawSteamId);
        } catch (error) {
            console.error(error);
            tableBody.innerHTML = '<tr><td colspan="3" class="muted">Failed to resolve identifier.</td></tr>';
            setTableStatus('badge-error', 'Resolve failed');
            setChartStatus('badge-error', 'Resolve failed');
            return;
        }
        if (!steamId) {
            tableBody.innerHTML = '<tr><td colspan="3" class="muted">Input did not resolve to a Steam64 ID.</td></tr>';
            setTableStatus('badge-error', 'Unknown identifier');
            setChartStatus('badge-error', 'Unknown identifier');
            return;
        }
        steamIdInput.value = steamId;
        persistSteamIdInQuery(steamId);
        persistDateFiltersInQuery();
        syncNavLinks(steamId);
        setTableStatus('badge-loading', 'Loading…');
        setChartStatus('badge-loading', 'Loading…');
        tableBody.innerHTML = '<tr><td colspan="3" class="muted">Loading metrics…</td></tr>';
        try {
            const rows = await fetchMetrics(steamId, viewMode, startDate, endDate);
            const normalized = normalizeRows(rows, viewMode);
            const sorted = sortRowsByDate(normalized);
            renderTable(sorted, viewMode);
            renderChart(sorted, viewMode);
            setTableStatus('badge-success', `Loaded ${sorted.length} rows`);
        } catch (error) {
            console.error(error);
            tableBody.innerHTML = '<tr><td colspan="3" class="muted">Failed to load metrics.</td></tr>';
            setTableStatus('badge-error', 'Load failed');
            setChartStatus('badge-error', 'Load failed');
            if (chartInstance) {
                chartInstance.destroy();
                chartInstance = null;
            }
        }
    });

    const bootstrapFromQuery = () => {
        const query = new URLSearchParams(window.location.search);
        const hasQueryDateRange = Boolean(query.get('startDate') || query.get('endDate'));
        if (startDateInput) {
            startDateInput.value = query.get('startDate') || '';
        }
        if (endDateInput) {
            endDateInput.value = query.get('endDate') || '';
        }
        if (hasQueryDateRange) {
            syncDateBounds();
        } else {
            applyDefaultDateRangeForMode(currentMode, { force: true });
            persistDateFiltersInQuery();
        }

        const detected = bootstrapSteamId({
            input: steamIdInput,
            onDetected: (steamId) => {
                syncNavLinks(steamId);
                form.dispatchEvent(new Event('submit'));
            }
        });
        if (!detected) {
            syncNavLinks('');
        }
    };

    if (startDateInput) {
        startDateInput.addEventListener('input', () => {
            syncDateBounds();
            persistDateFiltersInQuery();
        });
    }

    if (endDateInput) {
        endDateInput.addEventListener('input', () => {
            syncDateBounds();
            persistDateFiltersInQuery();
        });
    }

    bootstrapFromQuery();
});
