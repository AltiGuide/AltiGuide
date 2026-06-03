import './bootstrap';
import '../css/app.css';

import { createApp, h, Transition } from 'vue';
import { createInertiaApp, router } from '@inertiajs/vue3';

createInertiaApp({
    resolve: name => {
        const pages = import.meta.glob('./Pages/**/*.vue', { eager: true });
        return pages[`./Pages/${name}.vue`];
    },
    setup({ el, App, props, plugin }) {
        createApp({
            render: () =>
                h(Transition, { name: 'page', mode: 'out-in', appear: true }, {
                    default: () => h(App, { ...props, key: props.initialPage.url }),
                }),
        })
            .use(plugin)
            .mount(el);
    },
});