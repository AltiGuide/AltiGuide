<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
    selectedMountain: { type: Object, default: null },
    selectedRoute: { type: Object, default: null },
    user: { type: Object, default: null },
    groupName: { type: String, default: '' },
    hikeType: { type: String, default: 'tektok' },
    startDate: { type: String, default: '' },
    endDate: { type: String, default: '' },
    memberCount: { type: Number, default: 2 },
})

const emit = defineEmits(['update', 'next', 'prev'])

const form = ref({
    groupName: props.groupName || '',
    startDate: props.startDate || '',
    endDate: props.endDate || '',
    memberCount: props.memberCount || 2,
})

// Auto-calculate endDate when startDate changes
watch(() => form.value.startDate, (newVal) => {
    if (newVal) {
        const start = new Date(newVal)
        const end = new Date(start)
        end.setDate(end.getDate() + 1)
        form.value.endDate = end.toISOString().split('T')[0]
    }
})

const today = computed(() => new Date().toISOString().split('T')[0])

const getMountainImage = (mountain) => {
    if (!mountain) return '/images/mountain_bg.png'
    if (mountain.image) {
        if (mountain.image.startsWith('http') || mountain.image.startsWith('/')) return mountain.image
        return `/storage/${mountain.image}`
    }
    const map = {
        'andong': '/images/gunung_andong_1.png', 'lawu': '/images/gunung_lawu_2.png',
        'slamet': '/images/gunung_slamet_3.png', 'sumbing': '/images/gunung_sumbing_4.png',
        'ungaran': '/images/gunung_ungaran_5.png', 'merbabu': '/images/gunung_merbabu_6.png',
        'prau': '/images/gunung_prau_7.png', 'sindoro': '/images/gunung_sindoro_8.png',
    }
    const key = Object.keys(map).find(k => mountain.name?.toLowerCase().includes(k))
    return key ? map[key] : '/images/mountain_bg.png'
}

const errors = ref({})

const validate = () => {
    errors.value = {}
    if (!form.value.groupName.trim()) errors.value.groupName = 'Nama kelompok wajib diisi'
    if (!form.value.startDate) errors.value.startDate = 'Tanggal berangkat wajib diisi'
    if (!form.value.endDate) errors.value.endDate = 'Tanggal pulang wajib diisi'
    if (form.value.startDate && form.value.endDate && form.value.endDate < form.value.startDate)
        errors.value.endDate = 'Tanggal pulang tidak boleh sebelum tanggal berangkat'
    if (form.value.memberCount < 2 || form.value.memberCount > 10)
        errors.value.memberCount = 'Jumlah anggota harus antara 2 sampai 10'
    if (!props.user?.name) errors.value.leaderName = 'Data ketua belum lengkap'
    if (!props.user?.nik) errors.value.leaderNik = 'NIK ketua belum diisi di profil'
    return Object.keys(errors.value).length === 0
}

const canProceed = computed(() => {
    return form.value.groupName.trim() &&
           form.value.startDate &&
           form.value.endDate &&
           form.value.memberCount >= 2 &&
           form.value.memberCount <= 10 &&
           props.user?.name &&
           props.user?.nik
})

const submitStep = () => {
    if (!validate()) return
    const hikeType = form.value.startDate === form.value.endDate ? 'tektok' : 'camp'
    emit('update', {
        groupName: form.value.groupName.trim(),
        startDate: form.value.startDate,
        endDate: form.value.endDate,
        hikeType,
        memberCount: parseInt(form.value.memberCount),
    })
    emit('next')
}
</script>

<template>
    <div class="step2-container">
        <!-- Curved Title -->
        <div class="curved-title-wrapper">
            <svg width="100%" height="100%" viewBox="0 0 900 110" preserveAspectRatio="xMidYMid meet" class="curved-svg">
                <path id="group-curve" d="M 10,95 Q 450,45 890,95" fill="transparent" />
                <text class="curved-title-text">
                    <textPath href="#group-curve" startOffset="50%" text-anchor="middle">INISIASI GRUP PENDAKIAN</textPath>
                </text>
            </svg>
        </div>

        <!-- Mountain Preview Card -->
        <div v-if="selectedMountain" class="preview-card-wrapper">
            <div class="preview-card">
                <div class="preview-image-wrapper">
                    <img :src="getMountainImage(selectedMountain)" :alt="selectedMountain.name" class="preview-image" />
                    <div class="preview-overlay">
                        <span class="preview-mountain-name">{{ selectedMountain.name }}</span>
                    </div>
                </div>
                <div v-if="selectedRoute" class="preview-route">
                    {{ selectedRoute.name }}
                </div>
            </div>
        </div>

        <!-- Form -->
        <div class="form-body">
            <!-- IDENTITAS KELOMPOK -->
            <h3 class="section-title">IDENTITAS KELOMPOK</h3>

            <div class="field">
                <label class="field-label">Nama Kelompok</label>
                <input
                    type="text"
                    v-model="form.groupName"
                    placeholder="Masukkan nama kelompok"
                    class="field-input"
                />
                <span v-if="errors.groupName" class="field-error">{{ errors.groupName }}</span>
            </div>

            <div class="field-row">
                <div class="field">
                    <label class="field-label">Tanggal Berangkat Pendakian</label>
                    <input
                        type="date"
                        v-model="form.startDate"
                        :min="today"
                        class="field-input"
                    />
                    <span v-if="errors.startDate" class="field-error">{{ errors.startDate }}</span>
                </div>
                <div class="field">
                    <label class="field-label">Tanggal Pulang Pendakian</label>
                    <input
                        type="date"
                        v-model="form.endDate"
                        :min="form.startDate || today"
                        class="field-input"
                    />
                    <span v-if="errors.endDate" class="field-error">{{ errors.endDate }}</span>
                </div>
            </div>

            <div class="field">
                <label class="field-label">Jumlah Anggota</label>
                <select v-model.number="form.memberCount" class="field-input">
                    <option v-for="n in 9" :key="n+1" :value="n+1">{{ n + 1 }} Orang (termasuk ketua)</option>
                </select>
                <span v-if="errors.memberCount" class="field-error">{{ errors.memberCount }}</span>
            </div>

            <hr class="divider" />

            <!-- IDENTITAS KETUA -->
            <h3 class="section-title">IDENTITAS KETUA</h3>

            <div class="field">
                <label class="field-label">Nama Lengkap</label>
                <input
                    type="text"
                    :value="user?.name || ''"
                    readonly
                    placeholder="Masukkan nama lengkap"
                    class="field-input input-readonly"
                />
                <span v-if="errors.leaderName" class="field-error">{{ errors.leaderName }}</span>
            </div>

            <div class="field">
                <label class="field-label">NIK</label>
                <input
                    type="text"
                    :value="user?.nik || ''"
                    readonly
                    placeholder="33xxxxxxxxxxxxxx"
                    class="field-input input-readonly"
                />
                <span v-if="errors.leaderNik" class="field-error">{{ errors.leaderNik }}</span>
            </div>

            <div class="field">
                <label class="field-label">Nomor Telepon</label>
                <input
                    type="text"
                    :value="user?.phone_number || ''"
                    readonly
                    placeholder="+62"
                    class="field-input input-readonly"
                />
            </div>

            <div class="field">
                <label class="field-label">Nomor Darurat</label>
                <input
                    type="text"
                    :value="user?.emergency_contact || ''"
                    readonly
                    placeholder="+62"
                    class="field-input input-readonly"
                />
            </div>

            <!-- Navigation -->
            <div class="nav-actions">
                <button class="nav-link nav-prev" @click="emit('prev')">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><path d="m12 19-7-7 7-7"/></svg>
                    Kembali
                </button>
                <button
                    class="nav-link nav-next"
                    :class="{ disabled: !canProceed }"
                    :disabled="!canProceed"
                    @click="submitStep"
                >
                    Lanjut Data Anggota
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Jost:wght@700&display=swap');

.step2-container {
    background: rgba(211, 200, 186, 0.40);
    border-radius: 24px;
    padding: 32px 28px 40px;
    max-width: 900px;
    margin: 0 auto;
}

/* ── Curved Title ── */
.curved-title-wrapper {
    width: 100%;
    max-width: 650px;
    height: 120px;
    margin: 0 auto 20px;
}

.curved-svg { overflow: visible; }

.curved-title-text {
    font-family: 'Jost', sans-serif;
    font-weight: 700;
    font-size: 62px;
    fill: #FFFEF0;
    stroke: #66533A;
    stroke-width: 7px;
    stroke-linejoin: round;
    paint-order: stroke fill;
    filter: drop-shadow(0px 6px 8px rgba(102, 83, 58, 0.5));
    text-transform: uppercase;
}

/* ── Preview Card ── */
.preview-card-wrapper {
    display: flex;
    justify-content: center;
    margin-bottom: 32px;
}

.preview-card {
    width: 250px;
    background: #9F8C74;
    border-radius: 20px;
    padding: 22px;
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.15);
    border: none;
}

.preview-image-wrapper {
    position: relative;
    width: 100%;
    aspect-ratio: 3 / 4;
    overflow: hidden;
    border-radius: 12px;
}

.preview-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.preview-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    padding: 10px 14px;
    background: linear-gradient(180deg, rgba(55, 43, 24, 0.55), transparent);
}

.preview-mountain-name {
    font-family: 'Montserrat', sans-serif;
    font-weight: 700;
    font-size: 15px;
    color: #FFFEF0;
    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
    text-align: center;
    display: block;
}

.preview-route {
    margin-top: 14px;
    padding: 10px 14px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 500;
    font-size: 13px;
    color: #FFFEF0;
    background: #584D40;
    border-radius: 10px;
    text-align: left;
    box-shadow: inset 0 0 0 1px rgba(255,255,255,0.1);
}

/* ── Form ── */
.form-body {
    width: 100%;
}

.section-title {
    font-family: 'Montserrat', sans-serif;
    font-weight: 700;
    font-size: 18px;
    color: #374426;
    margin-bottom: 16px;
    letter-spacing: 0.03em;
}

.field {
    margin-bottom: 16px;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.field-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
}

.field-label {
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 13px;
    color: #374426;
}

.field-input {
    width: 100%;
    height: 44px;
    padding: 0 16px;
    border: 2px solid #7E623F;
    border-radius: 15px;
    background: #FFFFFF;
    font-family: 'Montserrat', sans-serif;
    font-size: 14px;
    color: #333;
    outline: none;
    transition: all 0.25s ease;
    appearance: none;
    -webkit-appearance: none;
}

.field-input:focus {
    border-color: #66533A;
    box-shadow: 0 0 0 3px rgba(102, 83, 58, 0.15);
}

.field-input::placeholder {
    color: #B5A993;
}

.input-readonly {
    color: #66533A;
    background: rgba(255, 255, 255, 0.5);
    cursor: default;
}

.field-error {
    font-family: 'Montserrat', sans-serif;
    font-size: 12px;
    color: #dc2626;
}

select.field-input {
    cursor: pointer;
    padding-right: 30px;
}

.divider {
    border: none;
    border-top: 1px solid #D6CCAF;
    margin: 24px 0;
}

/* ── Navigation ── */
.nav-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 28px;
}

.nav-link {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 14px;
    color: #66533A;
    background: #E5DCC5;
    border: 1px solid rgba(102, 83, 58, 0.1);
    border-radius: 20px;
    padding: 10px 20px;
    cursor: pointer;
    transition: all 0.2s ease;
}

.nav-link:hover {
    background: #D8CDB2;
    color: #374426;
    transform: translateY(-2px);
}

.nav-prev {
    background: transparent;
    border: 1px solid transparent;
}
.nav-prev:hover {
    background: rgba(102, 83, 58, 0.1);
    transform: translateY(-2px);
}

.nav-link.disabled {
    color: #B5A993;
    cursor: not-allowed;
}

@media (max-width: 600px) {
    .form-body { padding: 20px 20px; }
    .field-row { grid-template-columns: 1fr; }
    .curved-title-text { font-size: 40px; stroke-width: 3px; }
    .curved-title-wrapper { height: 90px; }
}
</style>
