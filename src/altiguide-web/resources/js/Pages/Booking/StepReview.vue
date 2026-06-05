<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const props = defineProps({
    selectedMountain: { type: Object, default: null },
    selectedRoute: { type: Object, default: null },
    groupName: { type: String, default: '' },
    startDate: { type: String, default: '' },
    endDate: { type: String, default: '' },
    memberCount: { type: Number, default: 2 },
    members: { type: Array, default: () => [] },
    user: { type: Object, default: null },
})

const emit = defineEmits(['checkout', 'prev'])

const priceData = ref(null)
const isLoadingPrice = ref(false)
const isSubmitting = ref(false)
const checkoutError = ref('')

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

const formatRupiah = (val) => {
    return 'Rp ' + Number(val).toLocaleString('id-ID')
}

const formatDate = (dateStr) => {
    if (!dateStr) return '-'
    const d = new Date(dateStr)
    return d.toLocaleDateString('id-ID', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

onMounted(async () => {
    isLoadingPrice.value = true
    try {
        const { data } = await axios.post('/booking/calculate', {
            route_id: props.selectedRoute?.id,
            member_count: props.memberCount,
        })
        priceData.value = data
    } catch (err) {
        console.error('Failed to calculate price:', err)
    } finally {
        isLoadingPrice.value = false
    }
})

const hikeTypeLabel = () => {
    return props.startDate === props.endDate ? 'Tektok (1 Hari)' : 'Camp (2 Hari 1 Malam)'
}

const submitCheckout = async () => {
    isSubmitting.value = true
    checkoutError.value = ''

    const hikeType = props.startDate === props.endDate ? 'tektok' : 'camp'

    const allMembers = [
        {
            user_id: props.user?.id,
            identity_number: props.user?.nik,
            full_name: props.user?.name,
            phone_number: props.user?.phone_number,
            emergency_contact: props.user?.emergency_contact,
        },
        ...props.members,
    ]

    try {
        const { data } = await axios.post('/booking/checkout', {
            route_id: props.selectedRoute?.id,
            start_date: props.startDate,
            hike_type: hikeType,
            group_name: props.groupName,
            members: allMembers,
        })

        emit('checkout', {
            orderId: data.data.order_id,
            snapToken: data.data.snap_token,
            paymentUrl: data.data.payment_url,
            grossAmount: data.data.gross_amount,
            expiryTime: data.data.expiry_time,
        })
    } catch (err) {
        checkoutError.value = err.response?.data?.message || 'Terjadi kesalahan saat memproses pembayaran.'
    } finally {
        isSubmitting.value = false
    }
}
</script>

<template>
    <div class="step4-container">
        <div class="curved-title-wrapper">
            <svg width="100%" height="100%" viewBox="0 0 750 110" preserveAspectRatio="xMidYMid meet" class="curved-svg">
                <path id="review-curve" d="M 10,95 Q 375,45 740,95" fill="transparent" />
                <text class="curved-title-text">
                    <textPath href="#review-curve" startOffset="50%" text-anchor="middle">REVIEW & PEMBAYARAN</textPath>
                </text>
            </svg>
        </div>

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

        <div class="form-body">
            <h3 class="section-title">RINGKASAN PERJALANAN</h3>

            <div class="summary-grid">
                <div class="summary-field">
                    <label class="field-label">Gunung & Jalur</label>
                    <div class="summary-value">{{ selectedMountain?.name }} - {{ selectedRoute?.name }}</div>
                </div>
                <div class="summary-field">
                    <label class="field-label">Nama Kelompok</label>
                    <div class="summary-value">{{ groupName }}</div>
                </div>
                <div class="summary-field">
                    <label class="field-label">Tanggal Pendakian</label>
                    <div class="summary-value">{{ formatDate(startDate) }} - {{ formatDate(endDate) }}</div>
                </div>
                <div class="summary-field">
                    <label class="field-label">Jumlah Anggota</label>
                    <div class="summary-value">{{ memberCount }} Orang Pendaki</div>
                </div>
            </div>

            <hr class="divider" />

            <h3 class="section-title">RINCIAN BIAYA</h3>

            <div v-if="isLoadingPrice" class="loading-price">
                <div class="spinner"></div>
                <span>Menghitung biaya...</span>
            </div>

            <div v-else-if="priceData" class="price-table-wrapper">
                <table class="price-table">
                    <thead>
                        <tr>
                            <th class="th-pill">ITEM</th>
                            <th class="th-pill">HARGA TIKET</th>
                            <th class="th-pill">JUMLAH</th>
                            <th class="th-pill">TOTAL BIAYA</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><span class="item-pill">TIKET MASUK</span></td>
                            <td class="td-center">@{{ formatRupiah(priceData.simaksi_price) }}</td>
                            <td class="td-center">{{ priceData.member_count }}</td>
                            <td class="td-right">{{ formatRupiah(priceData.subtotal_simaksi) }}</td>
                        </tr>
                        <tr>
                            <td><span class="item-pill">BIAYA LAYANAN</span></td>
                            <td class="td-center">@{{ formatRupiah(priceData.application_fee) }}</td>
                            <td class="td-center">{{ priceData.member_count }}</td>
                            <td class="td-right">{{ formatRupiah(priceData.subtotal_fee) }}</td>
                        </tr>
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="3" class="td-right total-label">+</td>
                            <td class="td-right">
                                <span class="total-pill">{{ formatRupiah(priceData.total) }}</span>
                            </td>
                        </tr>
                    </tfoot>
                </table>
            </div>

            <div v-if="checkoutError" class="error-msg">{{ checkoutError }}</div>

            <div class="action-bar">
                <button class="nav-link nav-prev" @click="emit('prev')">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><path d="m12 19-7-7 7-7"/></svg>
                    Kembali
                </button>
                <button
                    class="confirm-btn"
                    :disabled="isSubmitting || !priceData"
                    @click="submitCheckout"
                >
                    <span v-if="isSubmitting">Memproses...</span>
                    <span v-else>KONFIRMASI & BAYAR SEKARANG</span>
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Jost:wght@700&display=swap');

.step4-container {
    background: rgba(211, 200, 186, 0.40);
    border-radius: 24px;
    padding: 32px 28px 40px;
    max-width: 900px;
    margin: 0 auto;
}

.curved-title-wrapper { width: 100%; max-width: 650px; height: 120px; margin: 0 auto 20px; }
.curved-svg { overflow: visible; }
.curved-title-text {
    font-family: 'Jost', sans-serif; font-weight: 700; font-size: 62px;
    fill: #FFFEF0; stroke: #66533A; stroke-width: 7px; stroke-linejoin: round;
    paint-order: stroke fill; filter: drop-shadow(0px 6px 8px rgba(102, 83, 58, 0.5));
    text-transform: uppercase;
}

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

.form-body { width: 100%; margin: 0 auto; }
.section-title { font-family: 'Montserrat', sans-serif; font-weight: 700; font-size: 16px; color: #374426; margin-bottom: 16px; letter-spacing: 0.03em; }
.field-label { font-family: 'Montserrat', sans-serif; font-weight: 600; font-size: 13px; color: #374426; margin-bottom: 4px; }

.summary-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; }
.summary-field { display: flex; flex-direction: column; gap: 4px; }
.summary-value {
    font-family: 'Montserrat', sans-serif; font-size: 14px; color: #66533A;
    padding: 0 16px; min-height: 44px; display: flex; align-items: center;
    border: 2px solid #7E623F; border-radius: 10px; background: rgba(255,255,255,0.5);
}

.divider { border: none; border-top: 1px solid #D6CCAF; margin: 28px 0; }

.price-table-wrapper { overflow-x: auto; }
.price-table { width: 100%; border-collapse: separate; border-spacing: 8px 16px; font-family: 'Montserrat', sans-serif; font-size: 13px; margin-left: -8px; }
.th-pill {
    background: #66533A; color: #FFFEF0; font-weight: 600; font-size: 11px;
    padding: 8px 14px; text-align: center; letter-spacing: 0.04em;
    border-radius: 6px;
}
.th-pill:first-child { border-radius: 6px; }
.th-pill:last-child { border-radius: 6px; }

.price-table td { padding: 8px 12px; color: #374426; }
.td-center { text-align: center; }
.td-right { text-align: right; }

.item-pill {
    display: inline-block; padding: 4px 12px; background: #9F8C74;
    color: #FFFEF0; border-radius: 6px; font-weight: 600; font-size: 11px;
    letter-spacing: 0.03em;
}

.total-label { font-weight: 600; color: #9F8C74; }
.total-pill {
    display: inline-block; padding: 8px 20px; background: #66533A;
    color: #FFFEF0; border-radius: 8px; font-weight: 700; font-size: 15px;
}

.error-msg {
    font-family: 'Montserrat', sans-serif; font-size: 13px; color: #dc2626;
    margin-top: 16px; padding: 10px 14px; background: rgba(220,38,38,0.06); border-radius: 8px;
}

.action-bar { display: flex; align-items: center; justify-content: space-between; margin-top: 32px; }

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

.confirm-btn {
    padding: 14px 32px; 
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    color: #66533A;
    border: 1px solid rgba(255, 255, 255, 0.4); 
    border-radius: 20px;
    font-family: 'Montserrat', sans-serif; font-weight: 700; font-size: 14px;
    letter-spacing: 0.05em; cursor: pointer;
    transition: all 0.25s ease; text-transform: uppercase;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
.confirm-btn:hover:not(:disabled) { background: rgba(255, 255, 255, 0.35); transform: translateY(-2px); }
.confirm-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.loading-price { display: flex; align-items: center; gap: 10px; color: #9F8C74; font-family: 'Montserrat', sans-serif; font-size: 14px; padding: 16px 0; }
.spinner { width: 18px; height: 18px; border: 2.5px solid #D6CCAF; border-top-color: #66533A; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 600px) {
    .form-body { padding: 20px 20px; }
    .summary-grid { grid-template-columns: 1fr; }
    .curved-title-text { font-size: 40px; stroke-width: 3px; }
    .curved-title-wrapper { height: 90px; }
    .action-bar { flex-direction: column; gap: 16px; }
}
</style>
