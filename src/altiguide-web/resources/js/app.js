import './bootstrap';
import '../css/app.css';

import { createApp, h } from 'vue';
import { createInertiaApp } from '@inertiajs/vue3';
import GlobalLayout from './Layouts/GlobalLayout.vue';

createInertiaApp({
    resolve: name => {
        const pages = import.meta.glob('./Pages/**/*.vue', { eager: true });
        let page = pages[`./Pages/${name}.vue`];
        
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
});