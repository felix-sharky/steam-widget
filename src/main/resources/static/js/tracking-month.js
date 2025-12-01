document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('trackingMonthForm');
    const steamIdInput = document.getElementById('steamId');
    const tableBody = document.getElementById('tableBody');
    const tableStatus = document.getElementById('tableStatus');
    const backNav = document.getElementById('trackingBackNav');
    const chartStatus = document.getElementById('chartStatus');
    const chartEmpty = document.getElementById('chartEmpty');
    const chartCanvas = document.getElementById('trackingChart');
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
        if (!backNav) {
            return;
        }
        const base = backNav.getAttribute('data-base') || backNav.href;
        const url = new URL(base, window.location.origin);
        const id = steamIdInput.value.trim();
        if (id) {
            url.searchParams.set('steamId', id);
        }
        backNav.href = url.toString();
    };

    const renderRows = (rows) => {
        if (!rows.length) {
            tableBody.innerHTML = '<tr><td colspan="5" class="muted">No tracking data found for this profile.</td></tr>';
            return;
        }

        tableBody.innerHTML = rows.map((row, index) => {
            const id = row.id || {};
            const isActive = selectedGames.has(row.gamename || row.name);
            return `<tr class="table-row-selectable ${isActive ? 'table-row-active' : ''}" data-index="${index}" data-game="${row.gamename ?? row.name ?? ''}">
                <td>${id.year ?? '—'}</td>
                <td>${id.month ?? '—'}</td>
                <td>${row.gamename ?? row.name ?? 'Unknown'}</td>
                <td>${row.playtimeHours ?? 0}</td>
                <td>${row.playtimeMinutes ?? 0}</td>
            </tr>`;
        }).join('');
    };

    const sortRowsByDate = (rows) => {
        const safeNumber = (value) => Number(value) || 0;
        return [...rows].sort((a, b) => {
            const yearDelta = safeNumber((b.id || {}).year) - safeNumber((a.id || {}).year);
            if (yearDelta !== 0) {
                return yearDelta;
            }
            return safeNumber((b.id || {}).month) - safeNumber((a.id || {}).month);
        });
    };

    const fetchStats = async (steamId) => {
        const response = await fetch(`/api/tracking/profile-month?steamid=${encodeURIComponent(steamId)}`);
        if (!response.ok) {
            throw new Error(`Request failed: ${response.status}`);
        }
        return response.json();
    };

    const buildChartData = (rows) => {
        const buckets = rows.reduce((acc, row) => {
            const id = row.id || {};
            const year = Number(id.year) || 0;
            const month = Number(id.month) || 0;
            const game = row.gamename || row.name || 'Unknown';
            if (!year || !month) {
                return acc;
            }
            const label = `${year}-${String(month).padStart(2, '0')}`;
            if (!acc[label]) {
                acc[label] = { total: 0 };
            }
            if (!acc[label][game]) {
                acc[label][game] = 0;
            }
            const hours = Number(row.playtimeHours) || 0;
            const minutes = Number(row.playtimeMinutes) || 0;
            const totalHours = hours + minutes / 60;
            acc[label][game] += totalHours;
            acc[label].total += totalHours;
            return acc;
        }, {});

        const labels = Object.keys(buckets).sort((a, b) => a.localeCompare(b));
        const games = new Set();
        labels.forEach((label) => {
            Object.keys(buckets[label])
                .filter((key) => key !== 'total')
                .forEach((game) => games.add(game));
        });

        const datasets = Array.from(games).map((game, index) => ({
            label: game,
            data: labels.map((label) => buckets[label][game] ?? 0),
            borderColor: `hsl(${(index * 57) % 360} 80% 60%)`,
            backgroundColor: `hsl(${(index * 57) % 360} 80% 60% / 0.2)`,
            tension: 0.3,
            fill: false,
            pointRadius: 2,
            meta: { isTotal: false }
        }));

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
             chartEmpty.hidden = false;
             if (chartInstance) {
                 chartInstance.destroy();
                 chartInstance = null;
             }
             setChartState('badge-idle', selectedGames.size ? 'No data for selected games' : 'No data loaded');
             return;
         }

        const { labels, datasets } = buildChartData(rows);
        if (!labels.length) {
            chartEmpty.hidden = false;
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

        chartEmpty.hidden = true;

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
        renderRows(currentRows);
        renderChart(currentRows);
    };

    tableBody.addEventListener('click', toggleRowSelection);

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const steamId = steamIdInput.value.trim();
        if (!steamId) {
            setStatus('badge-error', 'Steam ID required');
            return;
        }

        appendSteamIdToBackLink();
        setStatus('badge-loading', 'Loading…');
        tableBody.innerHTML = '<tr><td colspan="5" class="muted">Loading data…</td></tr>';
        try {
            const rows = await fetchStats(steamId);
            const sorted = sortRowsByDate(rows);
            currentRows = sorted;
            selectedGames.clear();
            renderRows(sorted);
            renderChart(sorted);
            setStatus('badge-success', `Loaded ${sorted.length} rows`);
        } catch (error) {
            console.error(error);
            tableBody.innerHTML = '<tr><td colspan="5" class="muted">Failed to load stats.</td></tr>';
            setStatus('badge-error', 'Load failed');
            setChartState('badge-error', 'Load failed');
            chartEmpty.hidden = false;
        }
    });

    const bootstrapFromQuery = () => {
        const params = new URLSearchParams(window.location.search);
        const steamId = params.get('steamId');
        if (steamId) {
            steamIdInput.value = steamId;
            form.dispatchEvent(new Event('submit'));
            return;
        }
        const cookieSteamId = getCookie('steamId');
        if (cookieSteamId) {
            steamIdInput.value = cookieSteamId;
            form.dispatchEvent(new Event('submit'));
        }
    };

    bootstrapFromQuery();
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
