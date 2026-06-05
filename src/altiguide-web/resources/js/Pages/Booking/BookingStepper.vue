<script setup>
const props = defineProps({
    currentStep: { type: Number, default: 1 },
})

const emit = defineEmits(['goTo'])

const steps = [
    { number: 1, label: 'Pilih Destinasi\ndan Jalur' },
    { number: 2, label: 'Inisiasi Grup' },
    { number: 3, label: 'Validasi Anggota' },
    { number: 4, label: 'Review dan\nKalkulasi Biaya' },
    { number: 5, label: 'Pembayaran QRIS' },
]

const isActiveOrDone = (stepNum) => stepNum <= props.currentStep
</script>

<template>
    <div class="stepper-container">
        <div v-for="(step, idx) in steps" :key="step.number" class="stepper-item">
            <div
                v-if="idx > 0"
                class="stepper-line"
                :class="{ 'line-active': isActiveOrDone(step.number) }"
            ></div>
            <button
                class="stepper-circle"
                :class="{ 'circle-active': isActiveOrDone(step.number) }"
                @click="step.number < currentStep && emit('goTo', step.number)"
                :style="{ cursor: step.number < currentStep ? 'pointer' : 'default' }"
            >
                {{ step.number }}
            </button>
            <span class="stepper-label" :class="{ 'label-active': isActiveOrDone(step.number) }">
                <template v-for="(line, lIdx) in step.label.split('\n')" :key="lIdx">
                    {{ line }}<br v-if="lIdx < step.label.split('\n').length - 1" />
                </template>
            </span>
        </div>
    </div>
</template>

<style scoped>
.stepper-container {
    display: flex;
    align-items: flex-start;
    justify-content: center;
    gap: 0;
    padding: 0 24px;
    max-width: 820px;
    margin: 0 auto;
}

.stepper-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
    flex: 1;
}

.stepper-line {
    position: absolute;
    top: 24px;
    right: 50%;
    width: 100%;
    height: 2.5px;
    background: #C4B99A;
    z-index: 0;
    transform: translateX(-0%);
}

.stepper-line.line-active {
    background: #8B7B62;
}

.stepper-circle {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: #9F8C74;
    color: #E1D1BD;
    font-family: 'Montserrat', sans-serif;
    font-weight: 700;
    font-size: 22px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    position: relative;
    z-index: 1;
    transition: background-color 0.35s ease, color 0.35s ease, transform 0.2s ease;
}

.stepper-circle.circle-active {
    background: #66533A;
    color: #FFFEF0;
    box-shadow: 0 3px 12px rgba(102, 83, 58, 0.35);
}

.stepper-circle:hover {
    transform: scale(1.06);
}

.stepper-label {
    margin-top: 10px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 14px;
    color: #9F8C74;
    text-align: center;
    line-height: 1.3;
    transition: color 0.35s ease;
    white-space: nowrap;
}

.stepper-label.label-active {
    color: #66533A;
}

@media (max-width: 768px) {
    .stepper-container {
        padding: 0 8px;
    }
    .stepper-circle {
        width: 40px;
        height: 40px;
        font-size: 18px;
    }
    .stepper-line {
        top: 20px;
    }
    .stepper-label {
        font-size: 11px;
    }
}
</style>
