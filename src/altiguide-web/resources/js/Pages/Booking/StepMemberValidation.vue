<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'

const props = defineProps({
    selectedMountain: { type: Object, default: null },
    selectedRoute: { type: Object, default: null },
    memberCount: { type: Number, default: 2 },
    startDate: { type: String, default: '' },
    hikeType: { type: String, default: 'tektok' },
    existingMembers: { type: Array, default: () => [] },
})

const emit = defineEmits(['update', 'next', 'prev'])

const nikInput = ref('')
const isChecking = ref(false)
const checkResult = ref(null)
const checkError = ref('')
const members = ref([...props.existingMembers])

const maxAdditionalMembers = computed(() => props.memberCount - 1) // minus ketua

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

const checkNik = async () => {
    if (nikInput.value.length !== 16) {
        checkError.value = 'NIK harus 16 digit.'
        return
    }

    // Check if NIK already added
    if (members.value.some(m => m.identity_number === nikInput.value)) {
        checkError.value = 'NIK ini sudah ditambahkan ke daftar anggota.'
        return
    }

    isChecking.value = true
    checkError.value = ''
    checkResult.value = null

    try {
        const { data } = await axios.post('/booking/validate-nik', {
            nik: nikInput.value,
            start_date: props.startDate,
            hike_type: props.hikeType,
        })
        checkResult.value = data.data
    } catch (err) {
        checkError.value = err.response?.data?.message || 'Gagal memvalidasi NIK.'
        checkResult.value = null
    } finally {
        isChecking.value = false
    }
}

const addMember = () => {
    if (!checkResult.value) return
    if (members.value.length >= maxAdditionalMembers.value) {
        checkError.value = `Maksimal ${maxAdditionalMembers.value} anggota tambahan.`
        return
    }
    members.value.push({
        user_id: checkResult.value.user_id,
        identity_number: checkResult.value.nik,
        full_name: checkResult.value.full_name,
        phone_number: checkResult.value.phone_number,
        emergency_contact: checkResult.value.emergency_contact,
    })
    // Reset form
    nikInput.value = ''
    checkResult.value = null
    checkError.value = ''
}

const removeMember = (idx) => {
    members.value.splice(idx, 1)
}

const canProceed = computed(() => members.value.length === maxAdditionalMembers.value)

const submitStep = () => {
    emit('update', { members: [...members.value] })
    emit('next')
}
</script>

<template>
    <div class="step3-container">
        <!-- Curved Title -->
        <div class="curved-title-wrapper">
            <svg width="100%" height="100%" viewBox="0 0 700 110" preserveAspectRatio="xMidYMid meet" class="curved-svg">
                <path id="member-curve" d="M 10,95 Q 350,45 690,95" fill="transparent" />
                <text class="curved-title-text">
                    <textPath href="#member-curve" startOffset="50%" text-anchor="middle">VALIDASI ANGGOTA</textPath>
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
                <div v-if="selectedRoute" class="preview-route">{{ selectedRoute.name }}</div>
            </div>
        </div>

        <!-- Form Body -->
        <div class="form-body">
            <!-- INPUT ANGGOTA -->
            <h3 class="section-title">INPUT ANGGOTA</h3>

            <div class="nik-row">
                <div class="field" style="flex:1">
                    <label class="field-label">NIK Anggota</label>
                    <input
                        type="text"
                        v-model="nikInput"
                        maxlength="16"
                        placeholder="Masukkan NIK Anggota"
                        class="field-input"
                        @keyup.enter="checkNik"
                    />
                </div>
                <button
                    class="check-btn"
                    :disabled="isChecking || nikInput.length !== 16"
                    @click="checkNik"
                >
                    <svg v-if="!isChecking" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
                    <span v-if="isChecking" class="spinner"></span>
                    {{ isChecking ? 'Checking...' : 'Check' }}
                </button>
            </div>

            <div v-if="checkError" class="error-msg">{{ checkError }}</div>

            <!-- HASIL VALIDASI -->
            <transition name="fade">
                <div v-if="checkResult" class="validation-result">
                    <h3 class="section-title">HASIL VALIDASI ANGGOTA</h3>
                    <div class="result-row">
                        <div class="field">
                            <label class="field-label">Nama</label>
                            <input type="text" :value="checkResult.full_name" readonly class="field-input input-readonly" />
                        </div>
                        <div class="field">
                            <label class="field-label">Nomor Telepon</label>
                            <input type="text" :value="checkResult.phone_number" readonly class="field-input input-readonly" />
                        </div>
                    </div>
                    <div class="add-member-bar">
                        <button class="add-btn" @click="addMember" :disabled="members.length >= maxAdditionalMembers">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" x2="19" y1="8" y2="14"/><line x1="22" x2="16" y1="11" y2="11"/></svg>
                            Tambahkan Anggota
                        </button>
                    </div>
                </div>
            </transition>

            <hr class="divider" />

            <!-- LIST ANGGOTA TERDAFTAR -->
            <h3 class="section-title">LIST ANGGOTA TERDAFTAR ({{ members.length }}/{{ maxAdditionalMembers }})</h3>

            <div class="member-list">
                <div v-for="(member, idx) in members" :key="idx" class="member-item">
                    <span class="member-number">{{ idx + 1 }}.</span>
                    <span class="member-name">{{ member.full_name }}</span>
                    <span class="member-nik">{{ member.identity_number }}</span>
                    <button class="remove-btn" @click="removeMember(idx)" title="Hapus anggota">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
                    </button>
                </div>
                <div v-for="n in Math.max(0, maxAdditionalMembers - members.length)" :key="'empty-' + n" class="member-item member-empty">
                    <span class="member-number">{{ members.length + n }}.</span>
                    <span class="member-placeholder">Belum diisi</span>
                </div>
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
                    Lanjut Review
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Jost:wght@700&display=swap');

.step3-container {
    background: rgba(211, 200, 186, 0.40);
    border-radius: 24px;
    padding: 32px 28px 40px;
    max-width: 900px;
    margin: 0 auto;
}

.curved-title-wrapper { width: 100%; max-width: 600px; height: 120px; margin: 0 auto 20px; }
.curved-svg { overflow: visible; }
.curved-title-text {
    font-family: 'Jost', sans-serif; font-weight: 700; font-size: 68px;
    fill: #FFFEF0; stroke: #66533A; stroke-width: 7px; stroke-linejoin: round;
    paint-order: stroke fill; filter: drop-shadow(0px 6px 8px rgba(102, 83, 58, 0.5));
    text-transform: uppercase;
}

/* Preview Card (same as Step 2) */
.preview-card-wrapper { display: flex; justify-content: center; margin-bottom: 32px; }
.preview-card {
    width: 250px;
    background: #9F8C74;
    border-radius: 20px;
    padding: 22px;
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.15);
    border: none;
}
.preview-image-wrapper { position: relative; width: 100%; aspect-ratio: 3/4; overflow: hidden; border-radius: 12px; }
.preview-image { width: 100%; height: 100%; object-fit: cover; }
.preview-overlay { position: absolute; top: 0; left: 0; right: 0; padding: 10px 14px; background: linear-gradient(180deg, rgba(55,43,24,0.55), transparent); }
.preview-mountain-name { font-family: 'Montserrat', sans-serif; font-weight: 700; font-size: 15px; color: #FFFEF0; text-shadow: 0 1px 3px rgba(0,0,0,0.5); text-align: center; display: block; }
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

/* Form */
.form-body { width: 100%; margin: 0 auto; }
.section-title { font-family: 'Montserrat', sans-serif; font-weight: 700; font-size: 16px; color: #374426; margin-bottom: 16px; letter-spacing: 0.03em; }
.field { margin-bottom: 16px; display: flex; flex-direction: column; gap: 4px; }
.field-label { font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 13px; color: #374426; }
.field-input {
    width: 100%; height: 44px; padding: 0 16px; border: 2px solid #7E623F;
    background: #FFFFFF; font-family: 'Montserrat', sans-serif; font-size: 14px; color: #333;
    outline: none; transition: all 0.25s ease; border-radius: 10px;
}
.field-input:focus { border-color: #66533A; box-shadow: 0 0 0 3px rgba(102, 83, 58, 0.15); }
.field-input::placeholder { color: #B5A993; }
.input-readonly { color: #66533A; background: rgba(255,255,255,0.5); cursor: default; }

/* NIK Row */
.nik-row { display: flex; align-items: flex-end; gap: 12px; }
.check-btn {
    display: inline-flex; align-items: center; gap: 6px; padding: 10px 20px;
    background: #E5DCC5; border: 1px solid rgba(102, 83, 58, 0.1); border-radius: 20px;
    font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 13px;
    color: #66533A; cursor: pointer; transition: all 0.2s ease; white-space: nowrap;
    margin-bottom: 16px;
    height: 44px; /* matches input height */
}
.check-btn:hover:not(:disabled) { background: #D8CDB2; color: #374426; transform: translateY(-2px); }
.check-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.error-msg { font-family: 'Montserrat', sans-serif; font-size: 13px; color: #dc2626; margin-bottom: 12px; padding: 8px 12px; background: rgba(220,38,38,0.06); border-radius: 8px; }

/* Validation result */
.validation-result { margin-bottom: 8px; }
.result-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.add-member-bar { display: flex; justify-content: flex-end; margin-top: 4px; }
.add-btn {
    display: inline-flex; align-items: center; gap: 6px;
    font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 13px;
    color: #66533A; background: #E5DCC5; border: 1px solid rgba(102, 83, 58, 0.1);
    border-radius: 20px; padding: 10px 20px; cursor: pointer;
    transition: all 0.2s ease;
}
.add-btn:hover:not(:disabled) { background: #D8CDB2; color: #374426; transform: translateY(-2px); }
.add-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.divider { border: none; border-top: 1px solid #D6CCAF; margin: 24px 0; }

/* Member list */
.member-list { display: flex; flex-direction: column; gap: 12px; margin-bottom: 8px; }
.member-item {
    display: flex; align-items: center; gap: 10px; padding: 0 16px; height: 44px;
    background: #FFFFFF; border: 2px solid #7E623F; border-radius: 10px;
    font-family: 'Montserrat', sans-serif; font-size: 14px;
}
.member-empty { opacity: 0.5; background: rgba(255,255,255,0.5); }
.member-number { font-weight: 700; color: #374426; min-width: 24px; }
.member-name { font-weight: 600; color: #374426; flex: 1; }
.member-nik { font-weight: 400; color: #66533A; font-size: 12px; }
.member-placeholder { color: #B5A993; font-style: italic; }
.remove-btn {
    background: none; border: none; color: #dc2626; cursor: pointer; padding: 4px;
    opacity: 0.6; transition: opacity 0.2s;
}
.remove-btn:hover { opacity: 1; }

/* Navigation */
.nav-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 28px; }
.nav-link {
    display: inline-flex; align-items: center; gap: 6px;
    font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 14px;
    color: #66533A; background: #E5DCC5; border: 1px solid rgba(102, 83, 58, 0.1);
    border-radius: 20px; padding: 10px 20px; cursor: pointer;
    transition: all 0.2s ease;
}
.nav-link:hover { background: #D8CDB2; color: #374426; transform: translateY(-2px); }
.nav-prev { background: transparent; border: 1px solid transparent; }
.nav-prev:hover { background: rgba(102, 83, 58, 0.1); transform: translateY(-2px); }
.nav-link.disabled { color: #B5A993; cursor: not-allowed; background: #E5DCC5; transform: none; opacity: 0.6; }

/* Spinner */
.spinner { width: 14px; height: 14px; border: 2px solid #C4B99A; border-top-color: #66533A; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Transitions */
.fade-enter-active { animation: fadeIn 0.35s ease both; }
.fade-leave-active { animation: fadeIn 0.25s ease reverse both; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 600px) {
    .form-body { padding: 20px 20px; }
    .result-row { grid-template-columns: 1fr; }
    .curved-title-text { font-size: 44px; stroke-width: 3px; }
    .curved-title-wrapper { height: 90px; }
}
</style>
