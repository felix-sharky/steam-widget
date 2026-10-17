// Theme tokens for "Conference ID Badge" — this site's confirmed design
// direction (see DESIGN.md / .impeccable/surfaces/index-html.md): a dark
// expo-hall page with dark-cardstock panels and one orange accent. Values
// below read from CSS variables (see css/themes.css) instead of literal
// hex so the whole token set — including every existing opacity-modified
// class like bg-primary-container/30 — responds to the [data-theme]
// attribute theme-switcher.js sets on <html>. css/themes.css defines the
// "default" (this theme), "dark", and "light" variable sets.
window.tailwind = window.tailwind || {};
window.tailwind.config = {
    darkMode: 'class',
    theme: {
        extend: {
            colors: {
                background: 'rgb(var(--c-background) / <alpha-value>)',
                'on-background': 'rgb(var(--c-on-background) / <alpha-value>)',
                surface: 'rgb(var(--c-surface) / <alpha-value>)',
                'surface-dim': 'rgb(var(--c-surface-dim) / <alpha-value>)',
                'surface-bright': 'rgb(var(--c-surface-bright) / <alpha-value>)',
                'surface-variant': 'rgb(var(--c-surface-variant) / <alpha-value>)',
                'surface-container-lowest': 'rgb(var(--c-surface-container-lowest) / <alpha-value>)',
                'surface-container-low': 'rgb(var(--c-surface-container-low) / <alpha-value>)',
                'surface-container': 'rgb(var(--c-surface-container) / <alpha-value>)',
                'surface-container-high': 'rgb(var(--c-surface-container-high) / <alpha-value>)',
                'surface-container-highest': 'rgb(var(--c-surface-container-highest) / <alpha-value>)',
                'on-surface': 'rgb(var(--c-on-surface) / <alpha-value>)',
                'on-surface-variant': 'rgb(var(--c-on-surface-variant) / <alpha-value>)',
                outline: 'rgb(var(--c-outline) / <alpha-value>)',
                'outline-variant': 'rgb(var(--c-outline-variant) / <alpha-value>)',

                primary: 'rgb(var(--c-primary) / <alpha-value>)',
                'primary-container': 'rgb(var(--c-primary-container) / <alpha-value>)',
                'on-primary': 'rgb(var(--c-on-primary) / <alpha-value>)',
                'on-primary-container': 'rgb(var(--c-on-primary-container) / <alpha-value>)',
                'primary-fixed': 'rgb(var(--c-primary-fixed) / <alpha-value>)',
                'primary-fixed-dim': 'rgb(var(--c-primary-fixed-dim) / <alpha-value>)',

                secondary: 'rgb(var(--c-secondary) / <alpha-value>)',
                'secondary-container': 'rgb(var(--c-secondary-container) / <alpha-value>)',
                'on-secondary': 'rgb(var(--c-on-secondary) / <alpha-value>)',
                'on-secondary-container': 'rgb(var(--c-on-secondary-container) / <alpha-value>)',
                'secondary-fixed-dim': 'rgb(var(--c-secondary-fixed-dim) / <alpha-value>)',

                tertiary: 'rgb(var(--c-tertiary) / <alpha-value>)',
                'tertiary-container': 'rgb(var(--c-tertiary-container) / <alpha-value>)',

                'inverse-surface': 'rgb(var(--c-inverse-surface) / <alpha-value>)',
                'inverse-on-surface': 'rgb(var(--c-inverse-on-surface) / <alpha-value>)',
                'inverse-primary': 'rgb(var(--c-inverse-primary) / <alpha-value>)',
                'surface-tint': 'rgb(var(--c-surface-tint) / <alpha-value>)',

                error: 'rgb(var(--c-error) / <alpha-value>)'
            },
            borderRadius: {
                DEFAULT: '8px',
                lg: '14px',
                xl: '16px',
                full: '9999px'
            },
            spacing: {
                'margin-mobile': '16px',
                xs: '4px',
                unit: '4px',
                gutter: '20px',
                md: '16px',
                lg: '24px',
                'margin-desktop': '32px',
                sm: '8px',
                xl: '40px',
                'stack-md': '1rem',
                'stack-sm': '0.5rem',
                'stack-lg': '2rem',
                'container-max': '1200px'
            },
            fontFamily: {
                'body-sm': ['Public Sans'],
                'body-md': ['Public Sans'],
                'body-lg': ['Public Sans'],
                'label-md': ['Public Sans'],
                'label-sm': ['Public Sans'],
                'display-lg-mobile': ['"Barlow Condensed"'],
                'display-lg': ['"Barlow Condensed"'],
                'headline-sm': ['"Barlow Condensed"'],
                'headline-md': ['"Barlow Condensed"'],
                'headline-lg': ['"Barlow Condensed"'],
                'headline-xl': ['"Barlow Condensed"']
            },
            fontSize: {
                'body-sm': ['14px', { lineHeight: '20px', fontWeight: '400' }],
                'body-md': ['16px', { lineHeight: '25px', fontWeight: '400' }],
                'body-lg': ['18px', { lineHeight: '28px', fontWeight: '400' }],
                'label-md': ['13px', { lineHeight: '18px', letterSpacing: '0.04em', fontWeight: '600' }],
                'label-sm': ['11px', { lineHeight: '16px', letterSpacing: '0.06em', fontWeight: '600' }],
                'display-lg-mobile': ['40px', { lineHeight: '42px', letterSpacing: '0', fontWeight: '700' }],
                'display-lg': ['58px', { lineHeight: '56px', letterSpacing: '0', fontWeight: '700' }],
                'headline-sm': ['24px', { lineHeight: '28px', fontWeight: '700' }],
                'headline-md': ['30px', { lineHeight: '34px', fontWeight: '700' }],
                'headline-lg': ['40px', { lineHeight: '42px', fontWeight: '700' }],
                'headline-xl': ['60px', { lineHeight: '58px', letterSpacing: '0', fontWeight: '700' }]
            }
        }
    }
};
