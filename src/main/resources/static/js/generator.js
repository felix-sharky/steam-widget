document.addEventListener('DOMContentLoaded', function() {
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

    const navLinks = Array.from(document.querySelectorAll('.nav-link[data-base]'));

    const steamIdField = document.getElementById('steamId');
    const playingRightNowField = document.getElementById('playingRightNow');
    const gameListField = document.getElementById('gameList');
    const gameListSizeField = document.getElementById('gameListSize');

    bootstrapSteamId({
        input: steamIdField,
        onDetected: () => generateWidget()
    });

    const autoGenerateIfReady = () => {
        if (steamIdField && steamIdField.value.trim()) {
            generateWidget();
        }
    };

    [playingRightNowField, gameListField, gameListSizeField].forEach((control) => {
        if (!control) {
            return;
        }
        const eventName = control === gameListSizeField ? 'input' : 'change';
        control.addEventListener(eventName, autoGenerateIfReady);
    });

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

    steamIdField?.addEventListener('input', () => syncNavLinks(steamIdField.value.trim()));
    syncNavLinks(steamIdField?.value?.trim());
});

async function generateWidget() {
    const utils = window.SteamWidget || {};
    const resolveSteamIdInput = utils.resolveSteamIdInput || (async (value) => value?.trim() || null);
    const persistSteamIdInQuery = utils.persistSteamIdInQuery || (() => {});
    const syncNavLinks = utils.syncNavLinks || (() => {});

    const steamIdInput = document.getElementById('steamId');
    let steamId = steamIdInput.value?.trim();
    const widgetContainer = document.getElementById('widgetContainer');
    widgetContainer.innerHTML = '';

    if (!steamId) {
        // Display error message if steamId is empty
        const errorMessage = document.createElement('p');
        errorMessage.textContent = 'Please enter a valid Steam ID.';
        widgetContainer.appendChild(errorMessage);

        persistSteamIdInQuery();
        syncNavLinks();

        return;
    }

    try {
        const resolved = await resolveSteamIdInput(steamId);
        if (!resolved) {
            const errorMessage = document.createElement('p');
            errorMessage.textContent = 'Unable to resolve that Steam ID or vanity URL.';
            widgetContainer.appendChild(errorMessage);
            return;
        }
        steamId = resolved;
        steamIdInput.value = resolved;
    } catch (error) {
        console.error(error);
        const errorMessage = document.createElement('p');
        errorMessage.textContent = 'Failed to resolve this Steam identifier. Please try again later.';
        widgetContainer.appendChild(errorMessage);
        return;
    }

    // Sanitize the steamId to remove any HTML tags or JavaScript code
    steamId = escapeHtml(steamId);

    // Update the window's location to include steamId as a query parameter
    persistSteamIdInQuery(steamId);
    syncNavLinks(steamId);

    const playingRightNow = document.getElementById('playingRightNow').checked;
    const gameList = document.getElementById('gameList').value;
    const gameListSize = document.getElementById('gameListSize').value;

    const imageUrl = constructSafeUrl(steamId, playingRightNow, gameList, gameListSize);

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
    previewImage.width = 350;
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
    htmlCodeBox.textContent = `<img src="${imageUrl}" width="350">`;
    widgetContainer.appendChild(htmlCodeBox);
}

// Function to escape special HTML characters to prevent XSS
function escapeHtml(input) {
    return input.replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// Use encodeURIComponent for URL parameters
function constructSafeUrl(steamId, playingRightNow, gameList, gameListSize) {
    const baseUrl = window.location.origin;
    const params = new URLSearchParams();

    params.append('id', encodeURIComponent(steamId));

    if (playingRightNow !== true) {
        params.append('playingRightNow', encodeURIComponent(playingRightNow));
    }

    if (gameList !== 'NONE') {
        params.append('gameList', encodeURIComponent(gameList));
    }

    if (gameListSize !== '5') {
        params.append('gameListSize', encodeURIComponent(gameListSize));
    }

    return `${baseUrl}/widget/img?${params.toString()}`;
}
