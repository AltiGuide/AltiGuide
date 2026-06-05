<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
    mountains: { type: Array, default: () => [] },
    selectedMountain: { type: Object, default: null },
    selectedRoute: { type: Object, default: null },
})

const emit = defineEmits(['select', 'next'])

const activeMountainId = ref(props.selectedMountain?.id ?? null)
const activeRouteId = ref(props.selectedRoute?.id ?? null)

const getMountainImage = (mountain) => {
    if (mountain.image) {
        if (mountain.image.startsWith('http') || mountain.image.startsWith('/')) {
            return mountain.image
        }
        return `/storage/${mountain.image}`
    }
    const nameMap = {
        'andong': '/images/gunung_andong_1.png',
        'lawu': '/images/gunung_lawu_2.png',
        'slamet': '/images/gunung_slamet_3.png',
        'sumbing': '/images/gunung_sumbing_4.png',
        'ungaran': '/images/gunung_ungaran_5.png',
        'merbabu': '/images/gunung_merbabu_6.png',
        'prau': '/images/gunung_prau_7.png',
        'sindoro': '/images/gunung_sindoro_8.png',
    }
    const key = Object.keys(nameMap).find(k => mountain.name?.toLowerCase().includes(k))
    return key ? nameMap[key] : '/images/mountain_bg.png'
}

const selectRoute = (mountain, route) => {
    activeMountainId.value = mountain.id
    activeRouteId.value = route.id
    emit('select', mountain, route)
}

const canProceed = computed(() => activeMountainId.value && activeRouteId.value)
</script>

<template>
    <div class="dest-container">
        <!-- Curved Title -->
        <div class="curved-title-wrapper">
            <svg width="100%" height="100%" viewBox="0 0 700 120" preserveAspectRatio="xMidYMid meet" class="curved-svg">
                <path id="dest-curve" d="M 20,105 Q 350,55 680,105" fill="transparent" />
                <text class="curved-title-text">
                    <textPath href="#dest-curve" startOffset="50%" text-anchor="middle">DESTINASI & JALUR</textPath>
                </text>
            </svg>
        </div>

        <!-- Mountain Grid -->
        <div class="mountain-grid">
            <div
                v-for="mountain in mountains"
                :key="mountain.id"
                class="mountain-card"
                :class="{ 'card-selected': activeMountainId === mountain.id }"
            >
                <!-- Mountain Image -->
                <div class="card-image-wrapper">
                    <img
                        :src="getMountainImage(mountain)"
                        :alt="mountain.name"
                        class="card-image"
                        loading="lazy"
                    />
                    <div class="card-image-overlay">
                        <span class="card-mountain-name">{{ mountain.name }}</span>
                    </div>
                </div>

                <!-- Route Pills -->
                <div class="card-routes">
                    <div class="routes-header">Jalur Pendakian Resmi</div>
                    <button
                        v-for="route in mountain.routes"
                        :key="route.id"
                        class="route-pill"
                        :class="{
                            'pill-active': activeRouteId === route.id && activeMountainId === mountain.id,
                        }"
                        @click="selectRoute(mountain, route)"
                    >
                        {{ route.name }}
                    </button>
                    <p v-if="!mountain.routes || mountain.routes.length === 0" class="no-routes">
                        Belum ada jalur tersedia
                    </p>
                </div>
            </div>
        </div>

        <!-- Next Button -->
        <transition name="fade-up">
            <div v-if="canProceed" class="next-bar">
                <button class="next-btn" @click="emit('next')">
                    Lanjut ke Tahap Berikutnya
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
                </button>
            </div>
        </transition>
    </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Jost:wght@700&display=swap');

.dest-container {
    background: rgba(211, 200, 186, 0.40);
    border-radius: 24px;
    padding: 32px 28px 40px;
    max-width: 1100px;
    margin: 0 auto;
}

/* ── Curved Title ── */
.curved-title-wrapper {
    width: 100%;
    max-width: 600px;
    height: 130px;
    margin: 0 auto 24px;
}

.curved-svg {
    overflow: visible;
}

.curved-title-text {
    font-family: 'Jost', sans-serif;
    font-weight: 700;
    font-size: 68px;
    fill: #FFFEF0;
    stroke: #66533A;
    stroke-width: 7px;
    stroke-linejoin: round;
    paint-order: stroke fill;
    filter: drop-shadow(0px 6px 8px rgba(102, 83, 58, 0.5));
    text-transform: uppercase;
}

/* ── Mountain Grid ── */
.mountain-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
}

.mountain-card {
    background: #9F8C74;
    border-radius: 16px;
    padding: 22px;
    border: 2px solid transparent;
    transition: border-color 0.3s ease, box-shadow 0.3s ease, transform 0.2s ease;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.mountain-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.mountain-card.card-selected {
    border-color: #66533A;
    box-shadow: 0 4px 18px rgba(102, 83, 58, 0.25);
}

.card-image-wrapper {
    position: relative;
    width: 100%;
    aspect-ratio: 4 / 3;
    overflow: hidden;
    border-radius: 12px;
}

.card-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: transform 0.4s ease;
}

.mountain-card:hover .card-image {
    transform: scale(1.05);
}

.card-image-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 10px 14px;
    background: linear-gradient(transparent, rgba(55, 43, 24, 0.7));
}

.card-mountain-name {
    font-family: 'Montserrat', sans-serif;
    font-weight: 700;
    font-size: 15px;
    color: #FFFEF0;
    text-shadow: 0 1px 4px rgba(0, 0, 0, 0.5);
}

/* ── Route Pills ── */
.card-routes {
    margin-top: 14px;
    padding: 14px;
    display: flex;
    flex-direction: column;
    gap: 7px;
    background: rgba(255, 255, 255, 0.20);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border-radius: 20px;
    border: 1px solid rgba(255, 255, 255, 0.15);
}

.routes-header {
    background: rgba(255, 255, 255, 0.25);
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 10px;
    padding: 8px 14px;
    text-align: center;
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 13px;
    color: #FFFEF0;
    margin-bottom: 2px;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.route-pill {
    display: inline-block;
    width: 100%;
    text-align: left;
    padding: 9px 16px;
    border-radius: 10px;
    border: none;
    background: #D6CCAF;
    color: #4A3D2C;
    font-family: 'Montserrat', sans-serif;
    font-weight: 500;
    font-size: 12px;
    cursor: pointer;
    transition: all 0.25s ease;
}

.route-pill:hover {
    background: #C9BDA2;
    color: #374426;
}

.route-pill.pill-active {
    background: #66533A;
    color: #FFFEF0;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(102, 83, 58, 0.3);
}

.no-routes {
    font-family: 'Montserrat', sans-serif;
    font-size: 12px;
    color: #9F8C74;
    padding: 8px 0;
    text-align: center;
}

/* ── Next Button ── */
.next-bar {
    display: flex;
    justify-content: center;
    margin-top: 32px;
}

.next-btn {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    padding: 14px 36px;
    background: #66533A;
    color: #FFFEF0;
    border: none;
    border-radius: 40px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 16px;
    cursor: pointer;
    transition: background-color 0.25s ease, transform 0.15s ease, box-shadow 0.25s ease;
    box-shadow: 0 4px 16px rgba(102, 83, 58, 0.3);
}

.next-btn:hover {
    background: #7D664A;
    transform: translateY(-2px);
    box-shadow: 0 6px 24px rgba(102, 83, 58, 0.35);
}

.next-btn:active {
    transform: translateY(0);
}

/* ── Animations ── */
.fade-up-enter-active {
    animation: fadeUp 0.4s ease both;
}
.fade-up-leave-active {
    animation: fadeUp 0.3s ease reverse both;
}

@keyframes fadeUp {
    from {
        opacity: 0;
        transform: translateY(16px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* ── Responsive ── */
@media (max-width: 900px) {
    .mountain-grid {
        grid-template-columns: repeat(2, 1fr);
    }
    .curved-title-text {
        font-size: 52px;
        stroke-width: 3px;
    }
}

@media (max-width: 600px) {
    .mountain-grid {
        grid-template-columns: 1fr;
    }
    .dest-container {
        padding: 20px 16px 32px;
    }
    .curved-title-text {
        font-size: 40px;
        stroke-width: 2.5px;
    }
    .curved-title-wrapper {
        height: 100px;
    }
}
</style>
