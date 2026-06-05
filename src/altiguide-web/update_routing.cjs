const fs = require('fs');
const path = require('path');

// Update app.js
const appJsPath = path.join(__dirname, 'resources/js/app.js');
let appJs = fs.readFileSync(appJsPath, 'utf8');

// Strip any comments
appJs = appJs.replace(/\/\*[\s\S]*?\*\/|\/\/.*/g, '');

const newAppJs = `import './bootstrap';
import '../css/app.css';

import { createApp, h } from 'vue';
import { createInertiaApp } from '@inertiajs/vue3';
import GlobalLayout from './Layouts/GlobalLayout.vue';

createInertiaApp({
    resolve: name => {
        const pages = import.meta.glob('./Pages/**/*.vue', { eager: true });
        let page = pages[\`./Pages/\${name}.vue\`];
        
        if (page.default && !page.default.layout) {
            page.default.layout = GlobalLayout;
        }
        
        return page;
    },
    setup({ el, App, props, plugin }) {
        createApp({
            render: () => h(App, props),
        })
            .use(plugin)
            .mount(el);
    },
});`;

fs.writeFileSync(appJsPath, newAppJs, 'utf8');

// Update app.css
const appCssPath = path.join(__dirname, 'resources/css/app.css');
let appCss = fs.readFileSync(appCssPath, 'utf8');

// Remove existing animations
appCss = appCss.replace(/\/\*[\s\S]*?\*\/|\/\/.*/g, ''); // strip comments
appCss = appCss.replace(/\.page-enter-active[\s\S]*?@keyframes page-out[\s\S]*?}/g, '');

const newTransitions = `
.page-enter-active,
.page-leave-active {
    transition: opacity 350ms cubic-bezier(0.4, 0, 0.2, 1), transform 350ms cubic-bezier(0.4, 0, 0.2, 1);
}

.page-enter-from {
    opacity: 0;
    transform: translateY(15px);
}

.page-leave-to {
    opacity: 0;
}
`;

// Append new transitions and clean empty lines
appCss = appCss.trim() + '\n' + newTransitions.trim() + '\n';
// Remove multiple empty lines
appCss = appCss.replace(/\n{3,}/g, '\n\n');

fs.writeFileSync(appCssPath, appCss, 'utf8');
console.log('Routing and CSS updated.');
