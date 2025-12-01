document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const steamIdParam = urlParams.get('steamId');
    if (steamIdParam) {
        const sanitizedSteamId = escapeHtml(decodeURIComponent(steamIdParam));
        document.getElementById('steamId').value = sanitizedSteamId;
        // Optionally, automatically generate the widget
        generateWidget();
    } else {
        const cookieSteamId = getCookie('steamId');
        if (cookieSteamId) {
            const sanitizedSteamId = escapeHtml(cookieSteamId);
            document.getElementById('steamId').value = sanitizedSteamId;
            generateWidget();
        }
    }

    const steamIdField = document.getElementById('steamId');
    const playingRightNowField = document.getElementById('playingRightNow');
    const gameListField = document.getElementById('gameList');
    const gameListSizeField = document.getElementById('gameListSize');
    const navLinks = Array.from(document.querySelectorAll('.nav-link[data-base]'));

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
        }
        anchor.href = url.toString();
    };

    navLinks.forEach((link) => {
        link.addEventListener('click', () => appendSteamIdToLink(link));
        link.addEventListener('mouseenter', () => appendSteamIdToLink(link));
    });
});

function generateWidget() {
    let steamId = document.getElementById('steamId').value;
    const widgetContainer = document.getElementById('widgetContainer');
    widgetContainer.innerHTML = ''; // Clear previous content

    if (!steamId) {
        // Display error message if steamId is empty
        const errorMessage = document.createElement('p');
        errorMessage.textContent = 'Please enter a valid Steam ID.';
        widgetContainer.appendChild(errorMessage);

        //Remove steamId query parameter from URL
        const urlParams = new URLSearchParams(window.location.search);
        const steamIdParam = urlParams.get('steamId');
        if (steamIdParam) {
            urlParams.delete('steamId');
            window.history.replaceState(null, null, "?" + urlParams.toString());
        }

        return;
    }

    // Sanitize the steamId to remove any HTML tags or JavaScript code
    steamId = escapeHtml(steamId);

    // Update the window's location to include steamId as a query parameter
    const queryParams = new URLSearchParams(window.location.search);
    queryParams.set('steamId', steamId);
    window.history.replaceState(null, null, "?" + queryParams.toString());

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
