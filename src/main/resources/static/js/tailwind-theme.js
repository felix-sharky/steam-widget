window.tailwind = window.tailwind || {};
window.tailwind.config = {
    darkMode: 'class',
    theme: {
        extend: {
            colors: {
                primary: '#d0bcff',
                'surface-container-lowest': '#060e20',
                'surface-bright': '#31394d',
                'surface-variant': '#2d3449',
                'surface-dim': '#0b1326',
                outline: '#958ea0',
                'surface-container': '#171f33',
                'primary-container': '#8b5cf6',
                'primary-fixed': '#e9ddff',
                'outline-variant': '#494454',
                'on-primary-container': '#ffffff',
                tertiary: '#ffb2b7',
                'secondary-fixed-dim': '#4cd7f6',
                'surface-container-high': '#222a3d',
                secondary: '#4cd7f6',
                'on-surface-variant': '#cbc3d7',
                'on-secondary': '#003640',
                'inverse-surface': '#dae2fd',
                'on-surface': '#dae2fd',
                'tertiary-container': '#ff516a',
                surface: '#0b1326',
                'secondary-container': '#03b5d3',
                'surface-container-low': '#131b2e',
                'inverse-on-surface': '#283044',
                'surface-tint': '#d0bcff',
                error: '#ffb4ab',
                'inverse-primary': '#6d3bd7',
                'surface-container-highest': '#2d3449',
                'on-primary': '#3c0091',
                'on-background': '#dae2fd',
                background: '#0b1326',
                'primary-fixed-dim': '#d0bcff'
            },
            borderRadius: {
                DEFAULT: '0.25rem',
                lg: '0.5rem',
                xl: '0.75rem',
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
                'body-sm': ['Sora'],
                'body-md': ['Sora'],
                'label-md': ['JetBrains Mono'],
                'display-lg-mobile': ['Sora'],
                'headline-md': ['Sora'],
                'label-sm': ['JetBrains Mono'],
                'display-lg': ['Sora'],
                'body-lg': ['Sora'],
                'headline-sm': ['Sora'],
                'headline-lg': ['Sora'],
                'headline-xl': ['Sora']
            },
            fontSize: {
                'body-sm': ['14px', { lineHeight: '20px', fontWeight: '400' }],
                'body-md': ['16px', { lineHeight: '24px', fontWeight: '400' }],
                'label-md': ['14px', { lineHeight: '20px', letterSpacing: '0.05em', fontWeight: '500' }],
                'display-lg-mobile': ['32px', { lineHeight: '40px', letterSpacing: '-0.01em', fontWeight: '800' }],
                'headline-md': ['24px', { lineHeight: '32px', fontWeight: '700' }],
                'label-sm': ['12px', { lineHeight: '16px', letterSpacing: '0.05em', fontWeight: '500' }],
                'display-lg': ['48px', { lineHeight: '56px', letterSpacing: '-0.02em', fontWeight: '800' }],
                'body-lg': ['18px', { lineHeight: '28px', fontWeight: '400' }],
                'headline-sm': ['20px', { lineHeight: '28px', fontWeight: '600' }],
                'headline-lg': ['32px', { lineHeight: '40px', letterSpacing: '-0.01em', fontWeight: '700' }],
                'headline-xl': ['48px', { lineHeight: '56px', letterSpacing: '-0.02em', fontWeight: '800' }]
            }
        }
    }
};


