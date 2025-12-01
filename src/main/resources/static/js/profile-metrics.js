document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('profileMetricsForm');
    const steamIdInput = document.getElementById('steamId');
    const tableBody = document.getElementById('tableBody');
    const tableStatus = document.getElementById('tableStatus');
    const chartStatus = document.getElementById('chartStatus');
    const chartEmpty = document.getElementById('chartEmpty');
    const chartCanvas = document.getElementById('profileMetricsChart');
    let chartInstance;

    const setTableStatus = (cls, text) => {
        tableStatus.className = `badge ${cls}`;
        tableStatus.textContent = text;
    };

    const setChartStatus = (cls, text) => {
        chartStatus.className = `badge ${cls}`;
        chartStatus.textContent = text;
    };

    const fetchMetrics = async (steamId) => {
        const response = await fetch(`/api/metrics/profile/month?steam64id=${encodeURIComponent(steamId)}`);
        if (!response.ok) {
            throw new Error(`Request failed: ${response.status}`);
        }
        return response.json();
    };

    const renderTable = (rows) => {
        if (!rows.length) {
            tableBody.innerHTML = '<tr><td colspan="4" class="muted">No metrics found for this profile.</td></tr>';
            return;
        }
        const sortedRows = [...rows].sort((a, b) => {
            const aYear = a.year ?? 0;
            const bYear = b.year ?? 0;
            if (aYear !== bYear) {
                return bYear - aYear;
            }
            const aMonth = a.month ?? 0;
            const bMonth = b.month ?? 0;
            if (aMonth !== bMonth) {
                return bMonth - aMonth;
            }
            const aPurpose = (a.purpose || '').toLowerCase();
            const bPurpose = (b.purpose || '').toLowerCase();
            return aPurpose.localeCompare(bPurpose);
        });
        tableBody.innerHTML = sortedRows.map((row) => {
            return `<tr>
                <td>${row.year ?? '—'}</td>
                <td>${row.month ?? '—'}</td>
                <td>${row.purpose ?? '—'}</td>
                <td>${row.count ?? 0}</td>
            </tr>`;
        }).join('');
    };

    const renderChart = (rows) => {
        if (!rows.length) {
            chartEmpty.hidden = false;
            if (chartInstance) {
                chartInstance.destroy();
                chartInstance = null;
            }
            setChartStatus('badge-idle', 'No data loaded');
            return;
        }
        chartEmpty.hidden = true;
        const sorted = [...rows].sort((a, b) => {
            const ay = (a.year ?? 0);
            const by = (b.year ?? 0);
            if (ay !== by) {
                return ay - by;
            }
            return (a.month ?? 0) - (b.month ?? 0);
        });
        const labels = sorted.map((row) => {
            const year = row.year ?? '—';
            const month = String(row.month ?? '—').padStart(2, '0');
            return `${year}-${month}`;
        });

        const uniqueLabels = [...new Set(labels)];
        const purposes = [...new Set(sorted.map((row) => row.purpose || 'Unknown'))];
        const buckets = new Map();
        sorted.forEach((row) => {
            const label = `${row.year ?? '—'}-${String(row.month ?? '—').padStart(2, '0')}`;
            const purpose = row.purpose || 'Unknown';
            const count = row.count ?? 0;
            if (!buckets.has(label)) {
                buckets.set(label, {});
            }
            buckets.get(label)[purpose] = (buckets.get(label)[purpose] || 0) + count;
        });

        const datasets = purposes.map((purpose, index) => {
            const hue = (index * 63) % 360;
            return {
                label: purpose,
                data: uniqueLabels.map((label) => buckets.get(label)?.[purpose] ?? 0),
                backgroundColor: `hsla(${hue}, 70%, 55%, 0.65)`,
                borderColor: `hsl(${hue}, 70%, 55%)`,
                borderWidth: 1,
            };
        });

        const config = {
            type: 'bar',
            data: {
                labels: uniqueLabels,
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

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const steamId = steamIdInput.value.trim();
        if (!steamId) {
            setTableStatus('badge-error', 'Steam ID required');
            setChartStatus('badge-error', 'Steam ID required');
            return;
        }
        setTableStatus('badge-loading', 'Loading…');
        setChartStatus('badge-loading', 'Loading…');
        tableBody.innerHTML = '<tr><td colspan="4" class="muted">Loading metrics…</td></tr>';
        try {
            const rows = await fetchMetrics(steamId);
            renderTable(rows);
            renderChart(rows);
            setTableStatus('badge-success', `Loaded ${rows.length} rows`);
        } catch (error) {
            console.error(error);
            tableBody.innerHTML = '<tr><td colspan="4" class="muted">Failed to load metrics.</td></tr>';
            setTableStatus('badge-error', 'Load failed');
            setChartStatus('badge-error', 'Load failed');
            if (chartInstance) {
                chartInstance.destroy();
                chartInstance = null;
            }
            chartEmpty.hidden = false;
        }
    });

    const bootstrap = () => {
        const params = new URLSearchParams(window.location.search);
        const steamId = params.get('steamId') || getCookie('steamId');
        if (steamId) {
            steamIdInput.value = steamId;
            form.dispatchEvent(new Event('submit'));
        }
    };

    bootstrap();
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
