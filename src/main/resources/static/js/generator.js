document.addEventListener('DOMContentLoaded', async function() {
    const utils = window.SteamWidget || {};
    const syncNavLinks = utils.syncNavLinks || (() => {});
    const persistSteamIdInQuery = utils.persistSteamIdInQuery || (() => {});
    const bootstrapSteamId = utils.bootstrapSteamId || ((options = {}) => {
        const { input, onDetected, paramName = 'steamId' } = options;
        const params = new URLSearchParams(window.location.search);
        const trimmed = params.get(paramName)?.trim() || '';
        if (trimmed && input) {
            input.value = trimmed;
            if (typeof onDetected === 'function') {
                onDetected(trimmed, { source: 'query' });
            }
            return trimmed;
        }
        return null;
    });

    await loadWidgetStyles();

    const navLinks = Array.from(document.querySelectorAll('.nav-link[data-base]'));

    const steamIdField = document.getElementById('steamId');
    const playingRightNowField = document.getElementById('playingRightNow');
    const widgetContentField = document.getElementById('widgetContent');
    const gameListSizeField = document.getElementById('gameListSize');
    const gameListSizeSection = document.getElementById('gameListSizeSection');
    const widgetStyleField = document.getElementById('widgetStyle');
    const customCardsSection = document.getElementById('customCardsSection');
    const customCardFields = Array.from(document.querySelectorAll('.custom-card-field'));

    const syncContentControls = () => {
        if (!widgetContentField) {
            return;
        }
        const isCustomContent = widgetContentField.value === 'INSIGHT_CUSTOM';
        const isGameListContent = widgetContentField.value.startsWith('GAME_') && widgetContentField.value !== 'GAME_NONE';
        customCardsSection?.classList.toggle('hidden', !isCustomContent);
        gameListSizeSection?.classList.toggle('hidden', !isGameListContent);
    };

    bootstrapSteamId({
        input: steamIdField,
        onDetected: () => generateWidget()
    });

    const autoGenerateIfReady = () => {
        if (steamIdField && steamIdField.value.trim()) {
            generateWidget();
        }
    };

    [playingRightNowField, widgetContentField, gameListSizeField, widgetStyleField].forEach((control) => {
        if (!control) {
            return;
        }
        const eventName = control === gameListSizeField ? 'input' : 'change';
        control.addEventListener(eventName, () => {
            if (control === widgetContentField) {
                syncContentControls();
            }
            autoGenerateIfReady();
        });
    });

    customCardFields.forEach((control) => control.addEventListener('change', autoGenerateIfReady));
    syncContentControls();

    const appendSteamIdToLink = (anchor) => {
        if (!anchor || !steamIdField) {
            return;
        }
        const currentId = steamIdField.value.trim();
        const base = anchor.getAttribute('data-base') || anchor.getAttribute('href');
        const url = new URL(base, window.location.origin);
        if (currentId) {
            url.searchParams.set('steamId', currentId);
        } else {
            url.searchParams.delete('steamId');
        }
        anchor.href = url.toString();
    };

    navLinks.forEach((link) => {
        link.addEventListener('click', () => appendSteamIdToLink(link));
        link.addEventListener('mouseenter', () => appendSteamIdToLink(link));
    });

    steamIdField?.addEventListener('input', () => {
        currentWidgetShare = null;
        syncNavLinks(steamIdField.value.trim());
    });
    syncNavLinks(steamIdField?.value?.trim());
});

let currentWidgetShare = null;

// Populates the widget style picker from the backend instead of a hardcoded option list, so
// themes.json is the single source of truth for which styles exist.
async function loadWidgetStyles() {
    const widgetStyleField = document.getElementById('widgetStyle');
    if (!widgetStyleField) {
        return;
    }
    try {
        const response = await fetch('/api/widget/styles');
        if (!response.ok) {
            throw new Error(`Failed to load widget styles: ${response.status}`);
        }
        const styles = await response.json();
        if (!Array.isArray(styles) || styles.length === 0) {
            return;
        }
        const previousValue = widgetStyleField.value;
        widgetStyleField.innerHTML = '';
        styles.forEach(({ id, label }) => {
            const option = document.createElement('option');
            option.value = id;
            option.textContent = label || id;
            widgetStyleField.appendChild(option);
        });
        if (styles.some((style) => style.id === previousValue)) {
            widgetStyleField.value = previousValue;
        }
    } catch (error) {
        console.error(error);
        // Keep whatever options are already in the markup as a fallback.
    }
}

async function generateWidget() {
    const utils = window.SteamWidget || {};
    const resolveSteamIdInput = utils.resolveSteamIdInput || (async (value) => value?.trim() || null);
    const persistSteamIdInQuery = utils.persistSteamIdInQuery || (() => {});
    const syncNavLinks = utils.syncNavLinks || (() => {});

    const steamIdInput = document.getElementById('steamId');
    let steamId = steamIdInput.value?.trim();
    const widgetContainer = document.getElementById('widgetContainer');
    widgetContainer.innerHTML = '';
    // Switch from empty-state flex layout to content layout
    widgetContainer.classList.remove('flex', 'flex-col', 'items-center', 'justify-center', 'space-y-4');
    widgetContainer.classList.add('block', 'text-left');

    if (!steamId) {
        // Display error message if steamId is empty
        const errorMessage = document.createElement('p');
        errorMessage.textContent = 'Please enter a valid Steam ID.';
        widgetContainer.appendChild(errorMessage);

        persistSteamIdInQuery();
        syncNavLinks();

        currentWidgetShare = null;
        return null;
    }

    try {
        const resolved = await resolveSteamIdInput(steamId);
        if (!resolved) {
            const errorMessage = document.createElement('p');
            errorMessage.textContent = 'Unable to resolve that Steam ID or vanity URL.';
            widgetContainer.appendChild(errorMessage);
            currentWidgetShare = null;
            return null;
        }
        steamId = resolved;
        steamIdInput.value = resolved;
    } catch (error) {
        console.error(error);
        const errorMessage = document.createElement('p');
        errorMessage.textContent = 'Failed to resolve this Steam identifier. Please try again later.';
        widgetContainer.appendChild(errorMessage);
        currentWidgetShare = null;
        return null;
    }

    // Sanitize the steamId to remove any HTML tags or JavaScript code
    steamId = escapeHtml(steamId);

    // Update the window's location to include steamId as a query parameter
    persistSteamIdInQuery(steamId);
    syncNavLinks(steamId);

    const playingRightNow = document.getElementById('playingRightNow').checked;
    const widgetContent = document.getElementById('widgetContent').value;
    const { gameList, insightCategory } = resolveWidgetContent(widgetContent);
    const gameListSize = document.getElementById('gameListSize').value;
    const widgetStyle = document.getElementById('widgetStyle').value;
    const customCards = getCustomCards();
    const displayWidth = 900;

    const imageUrl = constructSafeUrl(steamId, playingRightNow, gameList, gameListSize, insightCategory, widgetStyle, displayWidth, customCards);

    // Preview
    const previewLabel = document.createElement('div');
    previewLabel.className = 'label';
    previewLabel.textContent = 'Preview:';
    widgetContainer.appendChild(previewLabel);

    const previewImageBox = document.createElement('div');
    previewImageBox.className = 'code-box';
    widgetContainer.appendChild(previewImageBox);

    const previewImage = document.createElement('img');
    previewImage.src = `${imageUrl}&purpose=generator`;
    previewImage.width = displayWidth;
    previewImageBox.appendChild(previewImage);

    // Link
    const linkLabel = document.createElement('div');
    linkLabel.className = 'label';
    linkLabel.textContent = 'Link:';
    widgetContainer.appendChild(linkLabel);

    const linkBox = document.createElement('div');
    linkBox.className = 'link-box';
    linkBox.textContent = imageUrl;
    widgetContainer.appendChild(linkBox);

    // HTML Code
    const htmlCodeLabel = document.createElement('div');
    htmlCodeLabel.className = 'label';
    htmlCodeLabel.textContent = 'HTML Code:';
    widgetContainer.appendChild(htmlCodeLabel);

    const htmlCodeBox = document.createElement('div');
    htmlCodeBox.className = 'code-box';
    htmlCodeBox.textContent = `<img src="${imageUrl}" width="${displayWidth}">`;
    widgetContainer.appendChild(htmlCodeBox);

    currentWidgetShare = { imageUrl, displayWidth };
    return currentWidgetShare;
}

async function ensureCurrentWidgetShare() {
    if (currentWidgetShare?.imageUrl) {
        return currentWidgetShare;
    }
    return generateWidget();
}

async function openWidgetShareModal() {
    const share = await ensureCurrentWidgetShare();
    if (!share?.imageUrl) {
        showShareToast('Generate a widget first.');
        return;
    }
    const modal = document.getElementById('widgetShareModal');
    const preview = document.getElementById('widgetSharePreview');
    if (!modal || !preview) {
        return;
    }
    preview.src = `${share.imageUrl}&purpose=share-preview`;
    preview.width = share.displayWidth;
    modal.classList.remove('hidden');
    modal.classList.add('flex');
}

function closeWidgetShareModal() {
    const modal = document.getElementById('widgetShareModal');
    if (!modal) {
        return;
    }
    modal.classList.add('hidden');
    modal.classList.remove('flex');
}

async function shareCurrentWidgetImage() {
    const share = await ensureCurrentWidgetShare();
    if (!share?.imageUrl) {
        showShareToast('Generate a widget first.');
        return;
    }

    try {
        const response = await fetch(`${share.imageUrl}&purpose=share-image`);
        if (!response.ok) {
            throw new Error(`Image request failed: ${response.status}`);
        }
        const blob = await response.blob();
        const file = new File([blob], 'steam-widget.png', { type: blob.type || 'image/png' });
        if (navigator.canShare?.({ files: [file] })) {
            await navigator.share({
                title: 'My Steam widget',
                text: 'Generated with steam-widget.com',
                files: [file]
            });
            return;
        }
    } catch (error) {
        console.error(error);
    }

    await copyToClipboard(share.imageUrl, 'Image link copied!');
}

async function shareCurrentSite() {
    const data = {
        title: document.title,
        text: 'Generate a Steam widget with steam-widget.com',
        url: location.href
    };
    if (navigator.share) {
        try {
            await navigator.share(data);
            return;
        } catch (error) {
            if (error?.name === 'AbortError') {
                return;
            }
        }
    }
    await copyToClipboard(location.href, 'Site link copied!');
}

async function copyToClipboard(value, message) {
    try {
        await navigator.clipboard.writeText(value);
        showShareToast(message);
    } catch (error) {
        console.error(error);
        showShareToast('Copy failed.');
    }
}

function showShareToast(message) {
    const toast = document.getElementById('shareToast');
    if (!toast) {
        return;
    }
    toast.textContent = message;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 2200);
}

function resolveWidgetContent(widgetContent) {
    if (widgetContent.startsWith('INSIGHT_')) {
        return {
            gameList: 'NONE',
            insightCategory: widgetContent.replace('INSIGHT_', '')
        };
    }
    return {
        gameList: widgetContent.replace('GAME_', ''),
        insightCategory: 'NONE'
    };
}

function getCustomCards() {
    return Array.from(document.querySelectorAll('.custom-card-field'))
        .map((field) => field.value.trim())
        .filter((value) => value)
        .slice(0, 6);
}

// Function to escape special HTML characters to prevent XSS
function escapeHtml(input) {
    return input.replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function constructSafeUrl(steamId, playingRightNow, gameList, gameListSize, insightCategory, widgetStyle, displayWidth, customCards = []) {
    const baseUrl = window.location.origin;
    const params = new URLSearchParams();

    params.append('id', steamId);

    if (playingRightNow !== true) {
        params.append('playingRightNow', playingRightNow);
    }

    if (insightCategory && insightCategory !== 'NONE') {
        params.append('insightCategory', insightCategory);
    } else if (gameList !== 'NONE') {
        params.append('gameList', gameList);
    }

    if ((!insightCategory || insightCategory === 'NONE') && gameListSize !== '6') {
        params.append('gameListSize', gameListSize);
    }

    if (displayWidth !== 350) {
        params.append('width', displayWidth);
    }

    if (widgetStyle && widgetStyle !== 'STEAM') {
        params.append('style', widgetStyle);
    }

    if (insightCategory === 'CUSTOM') {
        customCards.slice(0, 6).forEach((cardKey) => {
            params.append('customCard', cardKey);
        });
    }

    return `${baseUrl}/widget/img?${params.toString()}`;
}
