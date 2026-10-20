document.addEventListener('DOMContentLoaded', () => {
    const source = document.getElementById('heatmapSource');
    const target = document.getElementById('heatmap');
    if (!source || !target) return;

    const days = [...source.querySelectorAll('[data-date]')].map((el) => ({
        date: el.dataset.date,
        minutes: Number(el.dataset.minutes || 0)
    }));
    if (!days.length) return;

    const CELL = 11;
    const GAP = 3;
    const STEP = CELL + GAP;
    const MONTH_NAMES = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const MONTH_NAMES_FULL = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
    const DAY_LABELS = ['', 'Mon', '', 'Wed', '', 'Fri', ''];

    const toDate = (iso) => {
        const [y, m, d] = iso.split('-').map(Number);
        return new Date(y, m - 1, d);
    };

    const ordinal = (n) => {
        if (n % 10 === 1 && n % 100 !== 11) return `${n}st`;
        if (n % 10 === 2 && n % 100 !== 12) return `${n}nd`;
        if (n % 10 === 3 && n % 100 !== 13) return `${n}rd`;
        return `${n}th`;
    };

    const formatTooltip = (iso, minutes) => {
        const d = toDate(iso);
        const dateLabel = `${MONTH_NAMES_FULL[d.getMonth()]} ${ordinal(d.getDate())}`;
        if (minutes <= 0) return `No playtime on ${dateLabel}`;
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        const duration = hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;
        return `${duration} played on ${dateLabel}`;
    };

    const levelFor = (minutes) => {
        if (minutes <= 0) return 0;
        if (minutes < 60) return 1;
        if (minutes < 120) return 2;
        if (minutes < 240) return 3;
        return 4;
    };

    const leadingPad = toDate(days[0].date).getDay();
    const cells = new Array(leadingPad).fill(null)
        .concat(days.map((d) => ({ ...d, level: levelFor(d.minutes) })));
    while (cells.length % 7 !== 0) cells.push(null);

    const weeks = [];
    for (let i = 0; i < cells.length; i += 7) weeks.push(cells.slice(i, i + 7));

    const monthsRow = document.createElement('div');
    monthsRow.className = 'heatmap-months';
    let lastMonth = -1;
    weeks.forEach((week, weekIndex) => {
        const dayCell = week.find((c) => c);
        if (!dayCell) return;
        const month = toDate(dayCell.date).getMonth();
        if (month === lastMonth) return;
        lastMonth = month;
        const label = document.createElement('span');
        label.className = 'heatmap-month-label';
        label.textContent = MONTH_NAMES[month];
        label.style.left = `${weekIndex * STEP}px`;
        monthsRow.appendChild(label);
    });

    const body = document.createElement('div');
    body.className = 'heatmap-body';

    const dayLabels = document.createElement('div');
    dayLabels.className = 'heatmap-daylabels';
    DAY_LABELS.forEach((name) => {
        const span = document.createElement('span');
        span.textContent = name;
        dayLabels.appendChild(span);
    });

    const weeksGrid = document.createElement('div');
    weeksGrid.className = 'heatmap-weeks';
    weeks.forEach((week) => {
        const col = document.createElement('div');
        col.className = 'heatmap-week';
        week.forEach((cell) => {
            const div = document.createElement('div');
            if (!cell) {
                div.className = 'heatmap-cell heatmap-pad';
            } else {
                div.className = `heatmap-cell heatmap-${cell.level}`;
                div.tabIndex = 0;
                div.dataset.tooltip = formatTooltip(cell.date, cell.minutes);
                div.setAttribute('aria-label', div.dataset.tooltip);
            }
            col.appendChild(div);
        });
        weeksGrid.appendChild(col);
    });

    body.append(dayLabels, weeksGrid);

    const legend = document.createElement('div');
    legend.className = 'heatmap-legend';
    const less = document.createElement('span');
    less.textContent = 'Less';
    const more = document.createElement('span');
    more.textContent = 'More';
    legend.appendChild(less);
    [0, 1, 2, 3, 4].forEach((level) => {
        const swatch = document.createElement('span');
        swatch.className = `heatmap-cell heatmap-${level}`;
        legend.appendChild(swatch);
    });
    legend.appendChild(more);

    const graph = document.createElement('div');
    graph.className = 'heatmap-graph';
    graph.append(monthsRow, body);

    target.replaceChildren(graph, legend);

    const tooltip = document.createElement('div');
    tooltip.className = 'heatmap-tooltip';
    tooltip.setAttribute('role', 'tooltip');
    document.body.appendChild(tooltip);

    const positionTooltip = (cell) => {
        const rect = cell.getBoundingClientRect();
        const tipRect = tooltip.getBoundingClientRect();
        let left = rect.left + rect.width / 2 - tipRect.width / 2;
        left = Math.max(4, Math.min(left, window.innerWidth - tipRect.width - 4));
        tooltip.style.left = `${left}px`;
        tooltip.style.top = `${rect.top - tipRect.height - 8}px`;
        tooltip.style.setProperty('--arrow-left', `${rect.left + rect.width / 2 - left}px`);
    };

    const showTooltip = (cell) => {
        const text = cell.dataset.tooltip;
        if (!text) return;
        tooltip.textContent = text;
        positionTooltip(cell);
        tooltip.classList.add('visible');
    };

    const hideTooltip = () => tooltip.classList.remove('visible');

    weeksGrid.addEventListener('mouseover', (event) => {
        const cell = event.target.closest('.heatmap-cell[data-tooltip]');
        if (cell) showTooltip(cell);
    });
    weeksGrid.addEventListener('mouseout', (event) => {
        const cell = event.target.closest('.heatmap-cell[data-tooltip]');
        if (cell) hideTooltip();
    });
    weeksGrid.addEventListener('focusin', (event) => {
        const cell = event.target.closest('.heatmap-cell[data-tooltip]');
        if (cell) showTooltip(cell);
    });
    weeksGrid.addEventListener('focusout', (event) => {
        const cell = event.target.closest('.heatmap-cell[data-tooltip]');
        if (cell) hideTooltip();
    });
    target.addEventListener('scroll', hideTooltip);
    window.addEventListener('resize', hideTooltip);

    // Touch has no hover: tap a cell to toggle its tooltip, tap elsewhere to
    // dismiss it. stopPropagation keeps the same tap from immediately
    // re-triggering the document-level dismiss listener below.
    weeksGrid.addEventListener('click', (event) => {
        const cell = event.target.closest('.heatmap-cell[data-tooltip]');
        if (!cell) return;
        event.stopPropagation();
        const isShowingThisCell = tooltip.classList.contains('visible') && tooltip.dataset.forCell === cell.dataset.tooltip;
        if (isShowingThisCell) {
            hideTooltip();
        } else {
            tooltip.dataset.forCell = cell.dataset.tooltip;
            showTooltip(cell);
        }
    });
    document.addEventListener('click', hideTooltip);
});
