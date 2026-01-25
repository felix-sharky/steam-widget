document.addEventListener('DOMContentLoaded', () => {
    const utils = window.SteamWidget || {};
    const resolveSteamIdInput = utils.resolveSteamIdInput || (async (value) => value?.trim() || null);
    const persistSteamIdInQuery = utils.persistSteamIdInQuery || (() => {});
    const syncNavLinks = utils.syncNavLinks || (() => {});
    const bootstrapSteamId = utils.bootstrapSteamId || (() => null);

    const form = document.getElementById('trackingMonthForm');
    const steamIdInput = document.getElementById('steamId');
    const tableBody = document.getElementById('tableBody');
    const tableStatus = document.getElementById('tableStatus');
    const backNav = document.getElementById('trackingBackNav');
    const chartStatus = document.getElementById('chartStatus');
    const chartCanvas = document.getElementById('trackingChart');
    const viewModeInputs = document.querySelectorAll('input[name="viewMode"]');
    const trendTitle = document.getElementById('trendTitle');
    const trendDescription = document.getElementById('trendDescription');
    const tableTitle = document.getElementById('tableTitle');
    const tableDescription = document.getElementById('tableDescription');

    const getViewMode = () => {
        const checked = document.querySelector('input[name="viewMode"]:checked');
        return checked?.value === 'date' ? 'date' : 'month';
    };
    let currentMode = getViewMode();
    let chartInstance;
    let currentRows = [];
    const selectedGames = new Set();

    const setStatus = (badge, text) => {
        tableStatus.className = `badge ${badge}`;
        tableStatus.textContent = text;
    };

    const setChartState = (badge, text) => {
        chartStatus.className = `badge ${badge}`;
        chartStatus.textContent = text;
    };

    const normalizeDateLabel = (value) => {
        if (value === undefined || value === null) {
            return '';
        }
        if (Array.isArray(value) && value.length >= 3) {
            const [year, month, day] = value;
            return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        }
        return String(value);
    };

    const normalizeRows = (rows, mode) => rows.map((row) => {
        const hours = Number(row.playtimeHours) || 0;
        const minutes = Number(row.playtimeMinutes) || 0;
        const game = row.gamename ?? row.name ?? 'Unknown';

        if (mode === 'date') {
            const rawDate = normalizeDateLabel((row.id || {}).date);
            const [year, month, day] = rawDate ? rawDate.split('-') : [];
            return {
                label: rawDate || '',
                year: Number(year) || 0,
                month: Number(month) || 0,
                day: Number(day) || 0,
                game,
                hours,
                minutes,
                totalHours: hours + minutes / 60
            };
        }

        const id = row.id || {};
        const year = Number(id.year) || 0;
        const month = Number(id.month) || 0;
        const label = year && month ? `${year}-${String(month).padStart(2, '0')}` : '';
        return {
            label,
            year,
            month,
            day: 0,
            game,
            hours,
            minutes,
            totalHours: hours + minutes / 60
        };
    });

    const describeVisibleGames = (visibleGames) => {
        if (!visibleGames.length) {
            return 'Showing totals only';
        }
        if (visibleGames.length >= 3) {
            return `Focused on ${visibleGames.length} games`;
        }
        return `Focused on ${visibleGames.join(', ')}`;
    };

    const updateChartBadgeFromChart = (chart) => {
        if (!chart) {
            return;
        }
        const visibility = chart.data.datasets.map((dataset, idx) => ({
            dataset,
            visible: chart.isDatasetVisible(idx)
        }));
        const anyVisible = visibility.some(({ visible }) => visible);
        if (!anyVisible) {
            setChartState('badge-idle', 'No data visible');
            return;
        }
        const visibleGames = visibility
            .filter(({ dataset, visible }) => visible && !dataset.meta?.isTotal)
            .map(({ dataset }) => dataset.label);
        setChartState('badge-success', describeVisibleGames(visibleGames));
    };

    const appendSteamIdToBackLink = () => {
        const id = steamIdInput.value.trim();
        syncNavLinks(id);
        if (!backNav) {
            return;
        }
        const base = backNav.getAttribute('data-base') || backNav.href;
        const url = new URL(base, window.location.origin);
        if (id) {
            url.searchParams.set('steamId', id);
        } else {
            url.searchParams.delete('steamId');
        }
        backNav.href = url.toString();
    };

    const renderRows = (rows, mode = currentMode) => {
        if (!rows.length) {
            tableBody.innerHTML = '<tr><td colspan="4" class="muted">No tracking data found for this profile.</td></tr>';
            return;
        }

        tableBody.innerHTML = rows.map((row, index) => {
            const isActive = selectedGames.has(row.game);
            const periodLabel = row.label || (mode === 'date' ? 'Unknown date' : 'Unknown period');
            return `<tr class="table-row-selectable ${isActive ? 'table-row-active' : ''}" data-index="${index}" data-game="${row.game ?? ''}">
                <td>${periodLabel}</td>
                <td>${row.game ?? 'Unknown'}</td>
                <td>${row.hours ?? 0}</td>
                <td>${row.minutes ?? 0}</td>
            </tr>`;
        }).join('');
    };

    const sortRowsByDate = (rows) => {
        const safeLabel = (label) => label || '';
        return [...rows].sort((a, b) => safeLabel(b.label).localeCompare(safeLabel(a.label)));
    };

    const fetchStats = async (steamId, mode) => {
        const endpoint = mode === 'date' ? '/api/tracking/profile-date' : '/api/tracking/profile-month';
        const response = await fetch(`${endpoint}?steamid=${encodeURIComponent(steamId)}`);
        if (!response.ok) {
            throw new Error(`Request failed: ${response.status}`);
        }
        return response.json();
    };

    const buildChartData = (rows) => {
        const buckets = rows.reduce((acc, row) => {
            const label = row.label;
            if (!label) {
                return acc;
            }
            if (!acc[label]) {
                acc[label] = { total: 0 };
            }
            const gameLabel = String(row.game ?? '').trim();
            if (gameLabel) {
                if (!acc[label][gameLabel]) {
                    acc[label][gameLabel] = 0;
                }
                acc[label][gameLabel] += row.totalHours;
            }
            acc[label].total += row.totalHours;
            return acc;
        }, {});

        const labels = Object.keys(buckets).sort((a, b) => a.localeCompare(b));
        const games = new Set();
        labels.forEach((label) => {
            Object.keys(buckets[label])
                .filter((key) => key !== 'total')
                .forEach((game) => games.add(game));
        });

        const datasets = Array.from(games)
            .map((game, index) => {
                const data = labels.map((label) => buckets[label][game] ?? 0);
                return {
                    label: game,
                    data,
                    borderColor: `hsl(${(index * 57) % 360} 80% 60%)`,
                    backgroundColor: `hsl(${(index * 57) % 360} 80% 60% / 0.2)`,
                    tension: 0.3,
                    fill: false,
                    pointRadius: 2,
                    meta: { isTotal: false }
                };
            })
            .filter((dataset) => dataset.label && dataset.data.some((value) => Number(value) > 0));

        datasets.push({
            label: 'All games total',
            data: labels.map((label) => buckets[label].total ?? 0),
            borderColor: '#ffffff',
            backgroundColor: 'rgba(255,255,255,0.15)',
            borderWidth: 2,
            tension: 0.3,
            fill: false,
            pointRadius: 2,
            meta: { isTotal: true }
        });

        return { labels, datasets };
    };

    const renderChart = (rows) => {
        if (!rows.length) {
             if (chartInstance) {
                 chartInstance.destroy();
                 chartInstance = null;
             }
             setChartState('badge-idle', selectedGames.size ? 'No data for selected games' : 'No data loaded');
             return;
         }

        const { labels, datasets } = buildChartData(rows);
        if (!labels.length) {
            setChartState('badge-idle', 'No data for chart');
            if (chartInstance) {
                chartInstance.destroy();
                chartInstance = null;
            }
            return;
        }

        datasets.forEach((dataset) => {
            if (dataset.meta?.isTotal) {
                dataset.hidden = false;
            } else if (selectedGames.size) {
                dataset.hidden = !selectedGames.has(dataset.label);
            } else {
                dataset.hidden = true;
            }
        });

        const defaultLegendClick = Chart?.defaults?.plugins?.legend?.onClick;
        const config = {
            type: 'line',
            data: { labels, datasets },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { precision: 0 }
                    }
                },
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { color: '#cbd5f5' },
                        onClick(evt, legendItem, legend) {
                            if (typeof defaultLegendClick === 'function') {
                                defaultLegendClick.call(this, evt, legendItem, legend);
                            }
                            updateChartBadgeFromChart(legend.chart);
                        }
                    }
                }
            }
        };

        if (chartInstance) {
            chartInstance.destroy();
        }
        chartInstance = new Chart(chartCanvas, config);
        updateChartBadgeFromChart(chartInstance);
    };

    const toggleRowSelection = (event) => {
        const row = event.target.closest('tr.table-row-selectable');
        if (!row) {
            return;
        }
        const game = row.dataset.game;
        if (!game) {
            return;
        }
        if (selectedGames.has(game)) {
            selectedGames.delete(game);
        } else {
            selectedGames.add(game);
        }
        renderRows(currentRows, currentMode);
        renderChart(currentRows);
    };

    tableBody.addEventListener('click', toggleRowSelection);

    const updateViewCopy = (mode) => {
        const isDaily = mode === 'date';
        const viewLabel = isDaily ? 'Daily' : 'Monthly';
        if (trendTitle) trendTitle.textContent = `${viewLabel} trend`;
        if (trendDescription) trendDescription.textContent = isDaily ? 'Aggregated playtime hours per day.' : 'Aggregated playtime hours per month.';
        if (tableTitle) tableTitle.textContent = isDaily ? 'Playtime by day' : 'Playtime by month';
        if (tableDescription) {
            const endpoint = isDaily ? '/api/tracking/profile-date' : '/api/tracking/profile-month';
            tableDescription.innerHTML = `Data is fetched from <code>${endpoint}</code>.`;
        }
    };

    updateViewCopy(currentMode);

    const handleViewModeChange = () => {
        currentMode = getViewMode();
        updateViewCopy(currentMode);
        selectedGames.clear();
        if (steamIdInput.value.trim()) {
            form.dispatchEvent(new Event('submit'));
        } else {
            currentRows = [];
            renderRows(currentRows, currentMode);
            renderChart(currentRows);
            setStatus('badge-idle', 'No data loaded');
            setChartState('badge-idle', 'No data loaded');
        }
    };

    viewModeInputs.forEach((input) => input.addEventListener('change', handleViewModeChange));

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const rawSteamId = steamIdInput.value.trim();
        const viewMode = getViewMode();
        currentMode = viewMode;
        updateViewCopy(viewMode);
        if (!rawSteamId) {
            setStatus('badge-error', 'Steam ID required');
            return;
        }

        setStatus('badge-loading', 'Resolving…');
        tableBody.innerHTML = '<tr><td colspan="4" class="muted">Resolving input…</td></tr>';

        let steamId;
        try {
            steamId = await resolveSteamIdInput(rawSteamId);
        } catch (error) {
            console.error(error);
            tableBody.innerHTML = '<tr><td colspan="4" class="muted">Failed to resolve identifier.</td></tr>';
            setStatus('badge-error', 'Resolve failed');
            setChartState('badge-error', 'Resolve failed');
            return;
        }

        if (!steamId) {
            tableBody.innerHTML = '<tr><td colspan="4" class="muted">Input did not resolve to a Steam64 ID.</td></tr>';
            setStatus('badge-error', 'Unknown identifier');
            setChartState('badge-error', 'Unknown identifier');
            return;
        }

        steamIdInput.value = steamId;
        persistSteamIdInQuery(steamId);
        syncNavLinks(steamId);
        appendSteamIdToBackLink();

        setStatus('badge-loading', 'Loading…');
        setChartState('badge-loading', 'Loading…');
        tableBody.innerHTML = '<tr><td colspan="4" class="muted">Loading data…</td></tr>';
        try {
            const rows = await fetchStats(steamId, viewMode);
            const normalized = normalizeRows(rows, viewMode);
            const sorted = sortRowsByDate(normalized);
            currentRows = sorted;
            selectedGames.clear();
            renderRows(sorted, viewMode);
            renderChart(sorted);
            setStatus('badge-success', `Loaded ${sorted.length} rows (${viewMode})`);
        } catch (error) {
            console.error(error);
            tableBody.innerHTML = '<tr><td colspan="4" class="muted">Failed to load stats.</td></tr>';
            setStatus('badge-error', 'Load failed');
            setChartState('badge-error', 'Load failed');
        }
    });

    const bootstrapFromQuery = () => {
        const detected = bootstrapSteamId({
            input: steamIdInput,
            onDetected: (steamId) => {
                syncNavLinks(steamId);
                appendSteamIdToBackLink();
                form.dispatchEvent(new Event('submit'));
            }
        });
        if (!detected) {
            syncNavLinks('');
            appendSteamIdToBackLink();
        }
    };

    bootstrapFromQuery();
});
