<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import axios from 'axios'
import { Link } from '@inertiajs/vue3'

const props = defineProps({
    orderId: { type: String, default: '' },
    snapToken: { type: String, default: '' },
    paymentUrl: { type: String, default: '' },
    grossAmount: { type: [Number, String], default: 0 },
    expiryTime: { type: String, default: '' },
    selectedMountain: { type: Object, default: null },
    selectedRoute: { type: Object, default: null },
    groupName: { type: String, default: '' },
    startDate: { type: String, default: '' },
    endDate: { type: String, default: '' },
    memberCount: { type: Number, default: 2 },
    members: { type: Array, default: () => [] },
    user: { type: Object, default: null },
})

const emit = defineEmits(['complete'])

const paymentStatus = ref('pending') // pending | settlement | expire
const countdown = ref('59:59')
const isExpired = ref(false)
let pollInterval = null
let countdownInterval = null

const formatRupiah = (val) => 'Rp ' + Number(val).toLocaleString('id-ID')
const formatDate = (dateStr) => {
    if (!dateStr) return '-'
    return new Date(dateStr).toLocaleDateString('id-ID', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

const startCountdown = () => {
    if (!props.expiryTime) return
    const updateCountdown = () => {
        const now = new Date().getTime()
        const expiry = new Date(props.expiryTime).getTime()
        const diff = expiry - now
        if (diff <= 0) {
            countdown.value = '00:00'
            isExpired.value = true
            clearInterval(countdownInterval)
            return
        }
        const mins = Math.floor(diff / 60000)
        const secs = Math.floor((diff % 60000) / 1000)
        countdown.value = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
    }
    updateCountdown()
    countdownInterval = setInterval(updateCountdown, 1000)
}

const startPolling = () => {
    pollInterval = setInterval(async () => {
        try {
            const { data } = await axios.get(`/booking/status/${props.orderId}`)
            if (data.status === 'settlement') {
                paymentStatus.value = 'settlement'
                clearInterval(pollInterval)
                clearInterval(countdownInterval)
            } else if (data.status === 'expire' || data.status === 'cancel') {
                paymentStatus.value = 'expire'
                clearInterval(pollInterval)
                clearInterval(countdownInterval)
            }
        } catch (err) {
            console.error('Polling error:', err)
        }
    }, 5000)
}

const qrCanvas = ref(null)
const qrDataUrl = ref('')

const generateQR = async () => {
    const qrUrl = props.paymentUrl || `https://altiguide.id/ticket/${props.orderId}`
    
    qrDataUrl.value = qrUrl
}

const payWithSnap = () => {
    if (!props.snapToken) return
    
    if (window.snap) {
        window.snap.embed(props.snapToken, {
            embedId: 'snap-container',
            onSuccess: function (result) {
                paymentStatus.value = 'settlement'
            },
            onPending: function (result) {
                paymentStatus.value = 'pending'
            },
            onError: function (result) {
                console.error('Snap error:', result)
            },
            onClose: function () {
                console.log('Snap embedded closed')
            }
        })
    } else {
        console.error('Midtrans Snap is not loaded')
    }
}

onMounted(() => {
    startCountdown()
    startPolling()
    generateQR()
    payWithSnap()
})

onUnmounted(() => {
    if (pollInterval) clearInterval(pollInterval)
    if (countdownInterval) clearInterval(countdownInterval)
})

const isPaymentSuccess = computed(() => paymentStatus.value === 'settlement')

const downloadETicket = () => {
    const formatFullDate = (dateStr) => {
        if (!dateStr) return '-'
        const options = { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }
        return new Date(dateStr + 'T00:00:00').toLocaleDateString('id-ID', options)
    }

    const mountainImages = {
        'Gunung Sumbing': '/images/gunung_sumbing_4.png',
        'Gunung Sindoro': '/images/gunung_sindoro_8.png',
        'Gunung Prau':    '/images/gunung_prau_7.png',
        'Gunung Merbabu': '/images/gunung_merbabu_6.png',
        'Gunung Lawu':    '/images/gunung_lawu_2.png',
        'Gunung Andong':  '/images/gunung_andong_1.png',
        'Gunung Ungaran': '/images/gunung_ungaran_5.png',
        'Gunung Slamet':  '/images/gunung_slamet_3.png',
    };
    const coverImage = mountainImages[props.selectedMountain?.name] || '/images/logo_2.png';

    const membersHtml = props.members && props.members.length > 0
        ? props.members.map((m, i) => `
            <tr>
                <td style="text-align: center;">${i + 1}</td>
                <td style="text-align: left; padding-left: 20px;">${m.full_name}</td>
                <td style="text-align: center;">${m.identity_number}</td>
                <td style="text-align: center;">${m.phone_number || '-'}</td>
            </tr>
        `).join('')
        : `<tr><td colspan="4" style="text-align: center; color: #8B9A7B;">Tidak ada anggota tambahan</td></tr>`;

    const ticketHtml = `
    <!DOCTYPE html>
    <html>
    <head>
        <title>E-Ticket AltiGuide - ${props.orderId}</title>
        <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700;800&display=swap" rel="stylesheet">
        <style>
            body {
                font-family: 'Montserrat', sans-serif;
                background-color: #F4F1E6;
                margin: 0;
                padding: 40px 20px;
                display: flex;
                justify-content: center;
                align-items: center;
            }
            .ticket-card {
                background-color: #F0EADE;
                border-radius: 30px;
                padding: 40px;
                width: 100%;
                max-width: 600px;
                box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
                border: 1px solid #D6CCAF;
                box-sizing: border-box;
            }
            .ticket-title {
                text-align: center;
                font-size: 32px;
                font-weight: 800;
                color: #F0EADE;
                -webkit-text-stroke: 1.5px #374426;
                margin: 0;
                letter-spacing: 1.5px;
            }
            .ticket-subtitle {
                text-align: center;
                font-size: 11px;
                color: #6B553D;
                margin: 5px 0 25px 0;
                font-weight: 600;
                letter-spacing: 0.5px;
            }
            .cover-image-container {
                position: relative;
                width: 100%;
                height: 180px;
                border-radius: 20px;
                overflow: hidden;
                margin-bottom: 25px;
            }
            .cover-image {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
            .logo-overlay {
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                display: flex;
                align-items: center;
                gap: 8px;
                background: rgba(240, 238, 230, 0.85);
                padding: 6px 16px;
                border-radius: 25px;
                backdrop-filter: blur(4px);
            }
            .logo-overlay img {
                width: 22px;
                height: 22px;
            }
            .logo-overlay span {
                font-size: 16px;
                font-weight: 700;
                color: #374426;
                letter-spacing: 0.5px;
            }
            .info-row-3 {
                display: grid;
                grid-template-cols: 1.2fr 1fr 1fr;
                gap: 15px;
                margin-bottom: 20px;
            }
            .info-row-2 {
                display: grid;
                grid-template-cols: 1.2fr 1fr;
                gap: 20px;
                margin-bottom: 25px;
            }
            .info-label {
                font-size: 10px;
                color: #9F8C74;
                font-weight: 600;
                margin-bottom: 5px;
                text-transform: uppercase;
                letter-spacing: 0.5px;
            }
            .info-value {
                font-size: 13px;
                font-weight: 700;
                color: #374426;
            }
            .info-value.font-mono {
                font-family: monospace;
                font-size: 14px;
            }
            .info-value.text-large {
                font-size: 14px;
                line-height: 1.3;
            }
            .timeline {
                display: grid;
                grid-template-columns: 20px 1fr;
                grid-template-rows: auto 12px auto;
                align-items: center;
            }
            .timeline-dot {
                width: 8px;
                height: 8px;
                border-radius: 50%;
                justify-self: center;
            }
            .timeline-dot.solid {
                background-color: #8B6E4B;
            }
            .timeline-dot.hollow {
                border: 2px solid #8B6E4B;
                background-color: #FFFEF8;
                box-sizing: border-box;
                width: 8px;
                height: 8px;
            }
            .timeline-line {
                grid-column: 1;
                width: 2px;
                height: 100%;
                background-color: #D6CCAF;
                justify-self: center;
            }
            .timeline-text {
                font-size: 12.5px;
                font-weight: 600;
                color: #374426;
            }
            .notice-banner {
                background-color: #6B553D;
                color: #F4F1E6;
                border-radius: 12px;
                padding: 12px 18px;
                display: flex;
                align-items: center;
                gap: 15px;
                font-size: 11px;
                font-weight: 600;
                margin-bottom: 25px;
                line-height: 1.4;
            }
            .ticket-icon {
                width: 24px;
                height: 24px;
                color: #F4F1E6;
                flex-shrink: 0;
            }
            .table-title {
                font-size: 13px;
                font-weight: 700;
                color: #374426;
                margin: 0 0 12px 0;
            }
            .members-table {
                width: 100%;
                border-collapse: separate;
                border-spacing: 5px 0;
                font-size: 11px;
                margin-bottom: 25px;
            }
            .members-table th {
                background-color: #6B553D;
                color: #FFF;
                font-weight: 600;
                padding: 6px 10px;
                border-radius: 15px;
                font-size: 10px;
            }
            .members-table td {
                padding: 10px;
                color: #374426;
                font-weight: 500;
            }
            .qr-section {
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 12px;
                margin-top: 15px;
                border-top: 2px dashed #D6CCAF;
                padding-top: 30px;
            }
            .qr-title {
                font-size: 16px;
                font-weight: 700;
                color: #8B6E4B;
                letter-spacing: 1px;
                margin: 0;
            }
            .qr-code {
                border: 2px solid #8B6E4B;
                padding: 8px;
                border-radius: 12px;
                background-color: white;
                width: 150px;
                height: 150px;
            }
            .print-btn {
                display: block;
                width: 100%;
                max-width: 200px;
                margin: 15px auto 0 auto;
                background-color: #D6CCAF;
                color: #374426;
                border: none;
                padding: 12px 20px;
                font-size: 14px;
                font-weight: 700;
                border-radius: 10px;
                cursor: pointer;
                text-align: center;
                box-shadow: 0 4px 6px rgba(0,0,0,0.05);
                transition: all 0.2s;
            }
            .print-btn:hover {
                background-color: #c4b998;
            }
            @media print {
                body { background-color: #fff; padding: 0; }
                .ticket-card { box-shadow: none; border: none; padding: 0; }
                .print-btn { display: none; }
            }
        </style>
    </head>
    <body>
        <div class="ticket-card">
            <h1 class="ticket-title">ALTIGUIDE E-TICKET</h1>
            <p class="ticket-subtitle">Surat Izin Masuk Kawasan Konservasi (SIMAKSI)</p>

            <div class="cover-image-container">
                <img src="${coverImage}" alt="Mountain" class="cover-image" />
                <div class="logo-overlay">
                    <img src="/images/logo_2.png" alt="Logo" />
                    <span>AltiGuide</span>
                </div>
            </div>

            <div class="info-row-3">
                <div>
                    <div class="info-label">Order ID</div>
                    <div class="info-value font-mono">#${props.orderId}</div>
                </div>
                <div>
                    <div class="info-label">Nama Rombongan</div>
                    <div class="info-value">${props.groupName}</div>
                </div>
                <div>
                    <div class="info-label">Ketua Rombongan</div>
                    <div class="info-value">${props.user?.name || '-'}</div>
                </div>
            </div>

            <div class="info-row-2">
                <div>
                    <div class="info-label">Tujuan</div>
                    <div class="info-value text-large">${props.selectedMountain?.name} (${props.selectedRoute?.name})</div>
                </div>
                <div>
                    <div class="info-label" style="margin-left: 20px;">Tanggal Pendakian</div>
                    <div class="timeline">
                        <div class="timeline-dot solid"></div>
                        <div class="timeline-text">${formatFullDate(props.startDate)}</div>
                        <div class="timeline-line"></div>
                        <div class="timeline-dot hollow"></div>
                        <div class="timeline-text">${formatFullDate(props.endDate)}</div>
                    </div>
                </div>
            </div>

            <div class="notice-banner">
                <svg class="ticket-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
                </svg>
                <div>Tunjukkan E-Ticket dan Kartu Identitas kepada petugas Basecamp sebelum melakukan pendakian.</div>
            </div>

            <h3 class="table-title">Daftar Anggota Rombongan</h3>
            <table class="members-table">
                <thead>
                    <tr>
                        <th style="width: 10%;">NO</th>
                        <th style="width: 40%; text-align: left; padding-left: 20px;">NAMA LENGKAP</th>
                        <th style="width: 25%;">NOMOR IDENTITAS (NIK)</th>
                        <th style="width: 25%;">NOMOR TELEPON</th>
                    </tr>
                </thead>
                <tbody>
                    ${membersHtml}
                </tbody>
            </table>

            <div class="qr-section">
                <h3 class="qr-title">QRIS</h3>
                <img src="https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${props.orderId}" alt="Check-in QR" class="qr-code" />
            </div>

            <button class="print-btn" onclick="window.print()">Download PDF</button>
        </div>
    </body> 
    </html>`

    const w = window.open('', '_blank')
    w.document.write(ticketHtml)
    w.document.close()
}
</script>

<template>
    <div class="payment-overlay">
        <div class="payment-modal" :class="{ 'modal-success': isPaymentSuccess }">
            <div class="modal-logo">
                <img src="/images/logo_2.png" alt="AltiGuide" class="logo-icon" />
                <span class="logo-text">AltiGuide</span>
            </div>

            <p class="qris-label">QRIS (Midtrans)</p>
            <h2 class="order-id">#{{ orderId }}</h2>

            <div class="qr-wrapper">
                <div v-show="!isPaymentSuccess" class="qr-display">
                    <div v-if="snapToken" id="snap-container" class="snap-embed-container"></div>
                    <div v-else class="qr-placeholder">
                        <div class="spinner-lg"></div>
                        <p>Memuat Token Pembayaran...</p>
                    </div>
                </div>

                <div v-if="isPaymentSuccess" class="success-icon-wrapper">
                    <svg xmlns="http://www.w3.org/2000/svg" width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="#66533A" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                        <path d="m9 11 3 3L22 4"/>
                    </svg>
                </div>
            </div>

            <p class="total-label">Total Biaya</p>
            <div class="total-badge">{{ formatRupiah(grossAmount) }}</div>

            <div v-if="!isPaymentSuccess && !isExpired" class="countdown-row">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                Selesaikan pembayaran dalam {{ countdown }}
            </div>
            <div v-if="isExpired && !isPaymentSuccess" class="expired-label">
                Waktu pembayaran telah habis.
            </div>

            <a v-if="!isPaymentSuccess && qrDataUrl" :href="qrDataUrl" target="_blank" class="save-qr-link">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" x2="12" y1="15" y2="3"/></svg>
                Buka Halaman Pembayaran (Tab Baru)
            </a>

            <transition name="fade">
                <div v-if="isPaymentSuccess" class="success-actions">
                    <div class="success-badge">Pembayaran Berhasil!</div>
                    <button class="download-btn" @click="downloadETicket">
                        Download E-Ticket
                    </button>
                    <div class="actions-row">
                        <Link href="/dashboard" class="go-dashboard-btn text-center">
                            Ke Dashboard Saya
                        </Link>
                        <Link href="/" class="go-home-btn text-center">
                            Kembali ke Home
                        </Link>
                    </div>
                </div>
            </transition>
        </div>
    </div>
</template>

<style scoped>
.payment-overlay {
    position: relative;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    padding: 20px;
    min-height: 600px;
}

.payment-modal {
    background: #FFFEF8;
    border-radius: 20px;
    padding: 36px 40px;
    width: 100%;
    max-width: 420px;
    box-shadow: 0 12px 48px rgba(0, 0, 0, 0.18);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    transition: all 0.4s ease;
}

.modal-success {
    box-shadow: 0 12px 48px rgba(102, 83, 58, 0.25);
}

.modal-logo {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;
}

.logo-icon {
    width: 28px;
    height: 28px;
    object-fit: contain;
}

.logo-text {
    font-family: 'Montserrat', sans-serif;
    font-weight: 700;
    font-size: 20px;
    color: #374426;
}

.qris-label {
    font-family: 'Montserrat', sans-serif;
    font-weight: 500;
    font-size: 13px;
    color: #9F8C74;
}

.order-id {
    font-family: 'Montserrat', sans-serif;
    font-weight: 700;
    font-size: 18px;
    color: #66533A;
    margin-bottom: 8px;
}

.qr-wrapper {
    width: 100%;
    min-height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid #E5DDD0;
    border-radius: 12px;
    overflow: hidden;
    background: #fff;
}

.snap-embed-container {
    width: 100%;
    height: 100%;
    min-height: 400px;
}

.qr-display {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.qr-iframe {
    width: 100%;
    height: 100%;
    border: none;
}

.qr-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    color: #9F8C74;
    font-family: 'Montserrat', sans-serif;
    font-size: 13px;
}

.success-icon-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    animation: scaleIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) both;
}

@keyframes scaleIn {
    from { opacity: 0; transform: scale(0.5); }
    to { opacity: 1; transform: scale(1); }
}

.total-label {
    font-family: 'Montserrat', sans-serif;
    font-weight: 700;
    font-size: 14px;
    color: #66533A;
    margin-top: 8px;
}

.total-badge {
    display: inline-block;
    padding: 8px 24px;
    background: #66533A;
    color: #FFFEF0;
    border-radius: 8px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 700;
    font-size: 18px;
}

.countdown-row {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 8px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 500;
    font-size: 13px;
    color: #9F8C74;
}

.expired-label {
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 14px;
    color: #dc2626;
    margin-top: 8px;
}

.save-qr-link {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    margin-top: 6px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 500;
    font-size: 13px;
    color: #9F8C74;
    text-decoration: none;
    border: 1px solid #D6CCAF;
    padding: 6px 16px;
    border-radius: 8px;
    transition: all 0.2s ease;
}
.save-qr-link:hover { background: rgba(159,140,116,0.1); color: #66533A; }

.success-actions {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    margin-top: 12px;
}

.success-badge {
    padding: 12px 32px;
    background: #F4F1E6;
    border: 2px solid #C4B99A;
    border-radius: 10px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 15px;
    color: #374426;
}

.download-btn {
    padding: 14px 36px;
    background: #8B2020;
    color: #FFFEF0;
    border: none;
    border-radius: 10px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 700;
    font-size: 15px;
    cursor: pointer;
    transition: background-color 0.25s ease, transform 0.15s ease;
    box-shadow: 0 4px 16px rgba(139, 32, 32, 0.3);
}
.download-btn:hover {
    background: #6e1919;
    transform: translateY(-1px);
}

.actions-row {
    display: flex;
    gap: 12px;
    width: 100%;
    justify-content: center;
    margin-top: 4px;
}

.go-dashboard-btn {
    padding: 14px 28px;
    background: #374426;
    color: #FFFEF0;
    border: none;
    border-radius: 10px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 700;
    font-size: 15px;
    cursor: pointer;
    text-decoration: none;
    transition: background-color 0.25s ease, transform 0.15s ease;
    box-shadow: 0 4px 16px rgba(55, 68, 38, 0.3);
}
.go-dashboard-btn:hover {
    background: #2c361e;
    transform: translateY(-1px);
}

.go-home-btn {
    padding: 14px 28px;
    background: #E5DCC5;
    color: #374426;
    border: 1px solid rgba(102, 83, 58, 0.15);
    border-radius: 10px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 700;
    font-size: 15px;
    cursor: pointer;
    text-decoration: none;
    transition: all 0.25s ease;
    box-shadow: 0 4px 12px rgba(102, 83, 58, 0.1);
}
.go-home-btn:hover {
    background: #D8CDB2;
    transform: translateY(-1px);
}

.pay-snap-btn {
    padding: 14px 28px;
    background: #66533A;
    color: #FFFEF0;
    border: none;
    border-radius: 12px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 700;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 4px 12px rgba(102, 83, 58, 0.2);
}
.pay-snap-btn:hover {
    background: #54432d;
    transform: translateY(-1px);
}
.pay-snap-subtext {
    font-family: 'Montserrat', sans-serif;
    font-size: 11px;
    color: #9F8C74;
    max-width: 220px;
    line-height: 1.4;
    text-align: center;
    margin-top: 12px;
}

.spinner-lg { width: 32px; height: 32px; border: 3px solid #E5DDD0; border-top-color: #66533A; border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.fade-enter-active { animation: fadeIn 0.4s ease both; }
.fade-leave-active { animation: fadeIn 0.3s ease reverse both; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 480px) {
    .payment-modal { padding: 24px 20px; }
    .qr-wrapper { width: 240px; height: 240px; }
}
</style>
