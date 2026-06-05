<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import axios from 'axios'

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

onMounted(() => {
    startCountdown()
    startPolling()
    generateQR()
})

onUnmounted(() => {
    if (pollInterval) clearInterval(pollInterval)
    if (countdownInterval) clearInterval(countdownInterval)
})

const isPaymentSuccess = computed(() => paymentStatus.value === 'settlement')

const downloadETicket = () => {
    const ticketHtml = `
    <!DOCTYPE html>
    <html>
    <head>
        <title>E-Ticket AltiGuide - ${props.orderId}</title>
        <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&display=swap" rel="stylesheet">
        <style>
            body { font-family: 'Montserrat', sans-serif; padding: 40px; max-width: 600px; margin: 0 auto; color: #374426; }
            .header { text-align: center; margin-bottom: 24px; border-bottom: 2px solid #66533A; padding-bottom: 16px; }
            .header h1 { font-size: 24px; color: #66533A; }
            .header p { color: #9F8C74; font-size: 14px; }
            .order-id { font-size: 20px; font-weight: 700; color: #66533A; text-align: center; margin: 16px 0; }
            .details { margin: 20px 0; }
            .detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #E5DDD0; }
            .detail-label { font-weight: 600; font-size: 13px; color: #66533A; }
            .detail-value { font-size: 13px; color: #374426; }
            .members { margin-top: 20px; }
            .members h3 { font-size: 14px; color: #66533A; margin-bottom: 8px; }
            .member-item { padding: 6px 0; border-bottom: 1px solid #F0EBE0; font-size: 13px; }
            .footer { text-align: center; margin-top: 30px; padding-top: 16px; border-top: 2px solid #66533A; font-size: 12px; color: #9F8C74; }
            @media print { body { padding: 20px; } }
        </style>
    </head>
    <body>
        <div class="header">
            <h1>🏔️ AltiGuide E-Ticket</h1>
            <p>Surat Izin Masuk Kawasan Konservasi (SIMAKSI)</p>
        </div>
        <div class="order-id">#${props.orderId}</div>
        <div class="details">
            <div class="detail-row"><span class="detail-label">Gunung & Jalur</span><span class="detail-value">${props.selectedMountain?.name} — ${props.selectedRoute?.name}</span></div>
            <div class="detail-row"><span class="detail-label">Nama Kelompok</span><span class="detail-value">${props.groupName}</span></div>
            <div class="detail-row"><span class="detail-label">Tanggal</span><span class="detail-value">${formatDate(props.startDate)} — ${formatDate(props.endDate)}</span></div>
            <div class="detail-row"><span class="detail-label">Jumlah Anggota</span><span class="detail-value">${props.memberCount} Orang</span></div>
            <div class="detail-row"><span class="detail-label">Total Pembayaran</span><span class="detail-value">${formatRupiah(props.grossAmount)}</span></div>
            <div class="detail-row"><span class="detail-label">Ketua Rombongan</span><span class="detail-value">${props.user?.name} (${props.user?.nik})</span></div>
        </div>
        <div class="members">
            <h3>Daftar Anggota:</h3>
            ${props.members.map((m, i) => `<div class="member-item">${i + 1}. ${m.full_name} — ${m.identity_number}</div>`).join('')}
        </div>
        <div class="footer">
            <p>E-Ticket ini sah sebagai bukti pendaftaran SIMAKSI digital.</p>
            <p>Tunjukkan tiket ini (cetak/digital) saat melapor di basecamp.</p>
            <p>© 2026 AltiGuide Team</p>
        </div>
        <script>window.onload = () => window.print();<\/script>
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
                <div v-if="!isPaymentSuccess" class="qr-display">
                    <iframe
                        v-if="qrDataUrl"
                        :src="qrDataUrl"
                        class="qr-iframe"
                        frameborder="0"
                        scrolling="no"
                    ></iframe>
                    <div v-else class="qr-placeholder">
                        <div class="spinner-lg"></div>
                        <p>Memuat QR Code...</p>
                    </div>
                </div>

                <div v-else class="success-icon-wrapper">
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
                Simpan QR Code
            </a>

            <transition name="fade">
                <div v-if="isPaymentSuccess" class="success-actions">
                    <div class="success-badge">Pembayaran Berhasil!</div>
                    <button class="download-btn" @click="downloadETicket">
                        Download E-Ticket
                    </button>
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
    width: 280px;
    height: 280px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid #E5DDD0;
    border-radius: 12px;
    overflow: hidden;
    background: #fff;
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
