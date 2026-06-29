<script setup>
import { computed, ref, watch } from 'vue'
import { Head, Link, useForm, usePage, router } from '@inertiajs/vue3'

const props = defineProps({
    bookings: {
        type: Array,
        default: () => [],
    },
})

const page = usePage()
const user = computed(() => page.props.auth.user)

// ── Section toggle ──────────────────────────────────────────────────────
const activeSection = ref('profile') // 'profile' | 'password'

// ── Profile form ────────────────────────────────────────────────────────
const profileForm = useForm({
    name: user.value?.name || '',
    phone_number: user.value?.phone_number || '',
    age: user.value?.age || '',
    address: user.value?.address || '',
    emergency_contact: user.value?.emergency_contact || '',
    nik: user.value?.nik || '',
    avatar: null,
})

const avatarPreview = ref(user.value?.avatar_url || null)

// ── Avatar crop state ─────────────────────────────────────────────
const showCropModal = ref(false)
const cropSrc = ref(null)
const zoom = ref(1)
const position = ref({ x: 0, y: 0 })
const dragStart = ref({ x: 0, y: 0 })
const isDragging = ref(false)
const imageWidth = ref(0)
const imageHeight = ref(0)
const imgRef = ref(null)

const handleAvatarChange = (event) => {
    const file = event.target.files[0]
    if (!file) return
    
    const reader = new FileReader()
    reader.onload = (e) => {
        cropSrc.value = e.target.result
        showCropModal.value = true
    }
    reader.readAsDataURL(file)
}

const clamp = (val, min, max) => Math.min(Math.max(val, min), max)

const onImageLoaded = () => {
    if (!imgRef.value) return
    const img = imgRef.value
    const naturalWidth = img.naturalWidth
    const naturalHeight = img.naturalHeight
    const imgRatio = naturalWidth / naturalHeight
    
    const viewSize = 256
    if (imgRatio > 1) {
        imageHeight.value = viewSize
        imageWidth.value = viewSize * imgRatio
    } else {
        imageWidth.value = viewSize
        imageHeight.value = viewSize / imgRatio
    }
    
    zoom.value = 1
    position.value = { x: 0, y: 0 }
}

const imageStyle = computed(() => {
    const w = imageWidth.value * zoom.value
    const h = imageHeight.value * zoom.value
    return {
        width: `${w}px`,
        height: `${h}px`,
        left: `calc(50% + ${position.value.x}px - ${w / 2}px)`,
        top: `calc(50% + ${position.value.y}px - ${h / 2}px)`,
    }
})

const startDrag = (e) => {
    isDragging.value = true
    const clientX = e.touches ? e.touches[0].clientX : e.clientX
    const clientY = e.touches ? e.touches[0].clientY : e.clientY
    dragStart.value = {
        x: clientX - position.value.x,
        y: clientY - position.value.y
    }
    
    window.addEventListener('mousemove', onDrag)
    window.addEventListener('mouseup', endDrag)
    window.addEventListener('touchmove', onDrag)
    window.addEventListener('touchend', endDrag)
}

const onDrag = (e) => {
    if (!isDragging.value) return
    const clientX = e.touches ? e.touches[0].clientX : e.clientX
    const clientY = e.touches ? e.touches[0].clientY : e.clientY
    
    const viewSize = 256
    const w = imageWidth.value * zoom.value
    const h = imageHeight.value * zoom.value
    
    const maxX = Math.max(0, (w - viewSize) / 2)
    const maxY = Math.max(0, (h - viewSize) / 2)
    
    position.value.x = clamp(clientX - dragStart.value.x, -maxX, maxX)
    position.value.y = clamp(clientY - dragStart.value.y, -maxY, maxY)
}

const endDrag = () => {
    isDragging.value = false
    window.removeEventListener('mousemove', onDrag)
    window.removeEventListener('mouseup', endDrag)
    window.removeEventListener('touchmove', onDrag)
    window.removeEventListener('touchend', endDrag)
}

const clampPosition = () => {
    const viewSize = 256
    const w = imageWidth.value * zoom.value
    const h = imageHeight.value * zoom.value
    
    const maxX = Math.max(0, (w - viewSize) / 2)
    const maxY = Math.max(0, (h - viewSize) / 2)
    
    position.value.x = clamp(position.value.x, -maxX, maxX)
    position.value.y = clamp(position.value.y, -maxY, maxY)
}

watch(zoom, () => {
    clampPosition()
})

const closeCropModal = () => {
    showCropModal.value = false
    cropSrc.value = null
    const input = document.getElementById('avatar-upload')
    if (input) input.value = ''
}

const saveCrop = () => {
    const canvas = document.createElement('canvas')
    canvas.width = 400
    canvas.height = 400
    const ctx = canvas.getContext('2d')
    if (!ctx || !imgRef.value) return

    const img = imgRef.value
    const naturalWidth = img.naturalWidth
    const naturalHeight = img.naturalHeight
    const imgRatio = naturalWidth / naturalHeight

    const viewSize = 256
    const canvasSize = 400
    const scale = canvasSize / viewSize

    let displayWidth, displayHeight
    if (imgRatio > 1) {
        displayHeight = viewSize
        displayWidth = viewSize * imgRatio
    } else {
        displayWidth = viewSize
        displayHeight = viewSize / imgRatio
    }

    const z = zoom.value
    const x = position.value.x
    const y = position.value.y

    const drawWidth = displayWidth * z * scale
    const drawHeight = displayHeight * z * scale
    const drawLeft = (canvasSize / 2) + (x * scale) - (drawWidth / 2)
    const drawTop = (canvasSize / 2) + (y * scale) - (drawHeight / 2)

    ctx.drawImage(img, drawLeft, drawTop, drawWidth, drawHeight)

    canvas.toBlob((blob) => {
        if (!blob) return
        const croppedFile = new File([blob], 'avatar.png', { type: 'image/png' })
        profileForm.avatar = croppedFile
        avatarPreview.value = URL.createObjectURL(blob)
        closeCropModal()
    }, 'image/png')
}

const submitProfile = () => {
    profileForm.post('/profile/update', {
        forceFormData: true,
        onSuccess: () => {
            // Refresh avatar from server
            avatarPreview.value = user.value?.avatar_url || null
        },
    })
}

// ── Password form ───────────────────────────────────────────────────────
const passwordForm = useForm({
    current_password: '',
    password: '',
    password_confirmation: '',
})

const submitPassword = () => {
    passwordForm.post('/profile/password', {
        onSuccess: () => {
            passwordForm.reset()
        },
    })
}

// ── Forgot password modal ───────────────────────────────────────
const showForgotModal = ref(false)
const forgotForm = useForm({
    email: '',
})

const submitForgot = () => {
    forgotForm.post('/forgot-password/email')
}

// ── Profile completeness ─────────────────────────────────────────────────
// ── Profile completeness ─────────────────────────────────────────────────
const isProfileIncomplete = computed(() => {
    return !user.value?.age || !user.value?.address || !user.value?.emergency_contact || !user.value?.nik
})

// JS Error Catcher for remote debugging
const jsError = ref(null)
if (typeof window !== 'undefined') {
    window.onerror = (message, source, lineno, colno, error) => {
        jsError.value = `${message} at ${source}:${lineno}:${colno} | Stack: ${error?.stack || ''}`
        return false
    }
}

// ── Booking history ──────────────────────────────────────────────────────
const bookingSearch = ref('')

const formatDate = (dateStr) => {
    if (!dateStr) return '-'
    const d = new Date(dateStr)
    return d.toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' })
}

const formatCurrency = (amount) => {
    if (!amount) return '-'
    return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(amount)
}

const getStatusLabel = (status) => {
    const map = {
        settlement: 'REGISTERED',
        capture: 'REGISTERED',
        pending: 'PENDING',
        cancel: 'CANCELED',
        expire: 'CANCELED',
        failure: 'CANCELED',
        deny: 'CANCELED',
    }
    return map[status] || status?.toUpperCase() || 'UNKNOWN'
}

const registeredBookings = computed(() =>
    props.bookings.filter(b => ['settlement', 'capture'].includes(b.status) &&
        (!bookingSearch.value || b.mountain_name?.toLowerCase().includes(bookingSearch.value.toLowerCase())))
)
const pendingBookings = computed(() =>
    props.bookings.filter(b => b.status === 'pending' &&
        (!bookingSearch.value || b.mountain_name?.toLowerCase().includes(bookingSearch.value.toLowerCase())))
)
const canceledBookings = computed(() =>
    props.bookings.filter(b => ['cancel', 'expire', 'failure', 'deny'].includes(b.status) &&
        (!bookingSearch.value || b.mountain_name?.toLowerCase().includes(bookingSearch.value.toLowerCase())))
)

// Sliced lists for display
const displayedRegisteredBookings = computed(() => {
    return bookingSearch.value.trim() ? registeredBookings.value : registeredBookings.value.slice(0, 3)
})
const displayedPendingBookings = computed(() => {
    return bookingSearch.value.trim() ? pendingBookings.value : pendingBookings.value.slice(0, 3)
})
const displayedCanceledBookings = computed(() => {
    return bookingSearch.value.trim() ? canceledBookings.value : canceledBookings.value.slice(0, 3)
})

// Details Modal State
const showDetailsModal = ref(false)
const selectedBooking = ref(null)

const viewBookingDetails = (booking) => {
    selectedBooking.value = booking
    showDetailsModal.value = true
}

const downloadBookingETicket = (booking) => {
    if (!booking) return
    
    const formatFullDate = (dateStr) => {
        if (!dateStr) return '-'
        const cleanStr = String(dateStr).substring(0, 10)
        const options = { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }
        const d = new Date(cleanStr + 'T00:00:00')
        if (isNaN(d.getTime())) {
            const fallback = new Date(dateStr)
            return isNaN(fallback.getTime()) ? dateStr : fallback.toLocaleDateString('id-ID', options)
        }
        return d.toLocaleDateString('id-ID', options)
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
    const coverImage = mountainImages[booking.mountain_name] || '/images/logo_2.png';

    const membersHtml = booking.members && booking.members.length > 0
        ? booking.members.map((m, i) => `
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
        <title>E-Ticket AltiGuide - ${booking.order_id}</title>
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
                grid-template-columns: 1.2fr 1fr 1fr;
                gap: 15px;
                margin-bottom: 20px;
            }
            .info-row-2 {
                display: grid;
                grid-template-columns: 1.2fr 1fr;
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
                grid-template-columns: 24px 1fr;
                gap: 4px 10px;
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
                grid-column: 1;
                grid-row: 1;
            }
            .timeline-dot.hollow {
                border: 2px solid #8B6E4B;
                background-color: #FFFEF8;
                box-sizing: border-box;
                width: 8px;
                height: 8px;
                grid-column: 1;
                grid-row: 3;
            }
            .timeline-line {
                grid-column: 1;
                grid-row: 2;
                width: 2px;
                height: 16px;
                background-color: #D6CCAF;
                justify-self: center;
            }
            .timeline-text.start {
                grid-column: 2;
                grid-row: 1;
                font-size: 12.5px;
                font-weight: 600;
                color: #374426;
                white-space: nowrap;
            }
            .timeline-text.end {
                grid-column: 2;
                grid-row: 3;
                font-size: 12.5px;
                font-weight: 600;
                color: #374426;
                white-space: nowrap;
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
            <div style="text-align: center; margin-top: -15px; margin-bottom: 25px;">
                <span style="display: inline-block; padding: 6px 16px; border-radius: 20px; font-size: 11px; font-weight: 700; text-transform: uppercase; tracking-wider; 
                             background-color: ${booking.verification_status === 'terverifikasi' ? '#DCFCE7' : '#FEE2E2'}; 
                             color: ${booking.verification_status === 'terverifikasi' ? '#15803D' : '#991B1B'};
                             border: 1px solid ${booking.verification_status === 'terverifikasi' ? '#BBF7D0' : '#FECACA'};">
                    ${booking.verification_status === 'terverifikasi' ? '✓ DOKUMEN TERVERIFIKASI' : '⏳ MENUNGGU VERIFIKASI / BELUM DIVERIFIKASI'}
                </span>
            </div>

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
                    <div class="info-value font-mono">#${booking.order_id}</div>
                </div>
                <div>
                    <div class="info-label">Nama Rombongan</div>
                    <div class="info-value">${booking.group_name}</div>
                </div>
                <div>
                    <div class="info-label">Ketua Rombongan</div>
                    <div class="info-value">${booking.leader?.name || '-'}</div>
                </div>
            </div>

            <div class="info-row-2">
                <div>
                    <div class="info-label">Tujuan</div>
                    <div class="info-value text-large">${booking.mountain_name} (${booking.route_name})</div>
                </div>
                <div>
                    <div class="info-label" style="margin-left: 20px;">Tanggal Pendakian</div>
                    <div class="timeline">
                        <div class="timeline-dot solid"></div>
                        <div class="timeline-text start">${formatFullDate(booking.start_date)}</div>
                        <div class="timeline-line"></div>
                        <div class="timeline-dot hollow"></div>
                        <div class="timeline-text end">${formatFullDate(booking.end_date)}</div>
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
                <img src="https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${encodeURIComponent(booking.order_id)}" alt="Check-in QR" class="qr-code" />
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
    <div class="min-h-screen bg-[#F4F1E6] flex flex-col font-sans">
        <Head title="Dashboard - AltiGuide" />

        <!-- ══════════════ Navbar ══════════════ -->
        <nav class="w-full flex justify-between items-center px-4 md:px-8 xl:px-12 py-4 text-[#3b4b3b] font-semibold bg-[#374426]/10 backdrop-blur-md border-b border-[#D6CCAF] shadow-sm relative z-50">
            <div class="flex items-center gap-2 xl:gap-3">
                <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 md:w-10 md:h-10 object-contain" />
                <span class="text-xl md:text-2xl xl:text-3xl font-bold text-[#374426] tracking-tight">AltiGuide</span>
            </div>

            <div class="flex items-center gap-4 md:gap-6 xl:gap-9 text-sm md:text-base">
                <a href="/" class="hover:text-black transition">Home</a>
                <a href="/booking" class="hover:text-black transition">Booking</a>
                <Link
                    href="/logout"
                    method="post"
                    as="button"
                    class="border border-red-600 text-red-600 px-4 py-1.5 rounded-lg hover:bg-red-600 hover:text-white transition duration-200"
                >
                    Logout
                </Link>
            </div>
        </nav>

        <!-- ══════════════ Flash Messages ══════════════ -->
        <div class="max-w-5xl w-full mx-auto px-4 pt-4">
            <div v-if="$page.props.flash?.success" class="mb-2 p-3 bg-green-50 border border-green-300 rounded-xl text-green-900 text-sm flex gap-3 items-center">
                <span class="text-lg">✅</span>
                <span>{{ $page.props.flash.success }}</span>
            </div>
            <div v-if="$page.props.flash?.error" class="mb-2 p-3 bg-red-50 border border-red-300 rounded-xl text-red-900 text-sm flex gap-3 items-center">
                <span class="text-lg">❌</span>
                <span>{{ $page.props.flash.error }}</span>
            </div>
            <div v-if="$page.props.flash?.warning" class="mb-2 p-3 bg-yellow-50 border border-yellow-300 rounded-xl text-yellow-900 text-sm flex gap-3 items-center">
                <span class="text-lg">⚠️</span>
                <span>{{ $page.props.flash.warning }}</span>
            </div>
        </div>

        <!-- JS Error Log for Debugging -->
        <div v-if="jsError" class="max-w-5xl w-full mx-auto px-4 pt-2">
            <div class="p-4 bg-red-600 border border-red-800 rounded-xl text-white text-xs font-mono whitespace-pre-wrap break-all shadow-md">
                <strong>JS Rendering Error Detected:</strong><br>
                {{ jsError }}
            </div>
        </div>

        <!-- ══════════════ Main Content ══════════════ -->
        <main class="flex-1 max-w-5xl w-full mx-auto px-4 py-6 flex flex-col gap-6">

            <!-- ── Profile Incomplete Warning ── -->
            <div v-if="isProfileIncomplete" class="p-4 bg-amber-50 border border-amber-300 rounded-xl text-amber-900 text-sm flex gap-3">
                <div class="text-xl">⚠️</div>
                <div>
                    <p class="font-bold">Profil Belum Lengkap!</p>
                    <p class="mt-0.5">Lengkapi NIK, Umur, Alamat, dan Kontak Darurat agar dapat melakukan booking pendakian.</p>
                </div>
            </div>

            <!-- ══════════════ SECTION 1: Informasi Pribadi ══════════════ -->
            <div class="bg-white rounded-2xl border border-[#D6CCAF] shadow-md overflow-hidden">
                <!-- Header -->
                <div class="p-5 md:p-6 border-b border-[#EDE9D8]">
                    <h2 class="text-xl font-bold text-[#374426]">Informasi Pribadi</h2>
                </div>

                <!-- Avatar + Name Row -->
                <div class="p-5 md:p-6 flex items-center gap-4 border-b border-[#EDE9D8]">
                    <!-- Avatar -->
                    <div class="relative shrink-0">
                        <div class="w-16 h-16 md:w-20 md:h-20 rounded-full overflow-hidden border-2 border-[#D6CCAF] bg-[#E8E4D4]">
                            <img
                                v-if="avatarPreview"
                                :src="avatarPreview"
                                alt="Foto Profil"
                                class="w-full h-full object-cover"
                            />
                            <div v-else class="w-full h-full flex items-center justify-center text-[#374426] text-2xl font-bold">
                                {{ user?.name?.charAt(0)?.toUpperCase() }}
                            </div>
                        </div>
                        <!-- Upload trigger -->
                        <label
                            for="avatar-upload"
                            class="absolute -bottom-1 -right-1 w-6 h-6 bg-[#374426] rounded-full flex items-center justify-center cursor-pointer hover:bg-[#2c361e] transition"
                            title="Ganti foto profil"
                        >
                            <svg class="w-3.5 h-3.5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M3 16.5V19a1 1 0 001 1h3.5M21 16.5V19a1 1 0 01-1 1h-3.5M12 3v12m0-12L8.5 6.5M12 3l3.5 3.5" />
                                <path stroke-linecap="round" stroke-linejoin="round" d="M16 3.13a4 4 0 010 7.75M3 12h.01" />
                            </svg>
                        </label>
                        <input id="avatar-upload" type="file" accept="image/*" class="hidden" @change="handleAvatarChange" />
                    </div>

                    <!-- Name & Email -->
                    <div class="flex-1 min-w-0">
                        <p class="font-bold text-[#374426] text-lg truncate">{{ user?.name }}</p>
                        <p class="text-gray-500 text-sm truncate">{{ user?.email }}</p>
                    </div>

                    <!-- Section Toggle Buttons -->
                    <div class="flex gap-2 shrink-0">
                        <button
                            @click="activeSection = 'profile'"
                            :class="activeSection === 'profile' ? 'bg-[#374426] text-white' : 'border border-[#374426] text-[#374426] hover:bg-[#374426]/10'"
                            class="px-4 py-1.5 rounded-lg text-sm font-medium transition"
                        >
                            Edit Profil
                        </button>
                        <button
                            @click="activeSection = 'password'"
                            :class="activeSection === 'password' ? 'bg-[#374426] text-white' : 'border border-[#374426] text-[#374426] hover:bg-[#374426]/10'"
                            class="px-4 py-1.5 rounded-lg text-sm font-medium transition"
                        >
                            Ganti Password
                        </button>
                    </div>
                </div>

                <!-- ── Edit Profile Form ── -->
                <div v-if="activeSection === 'profile'" class="p-5 md:p-6">
                    <form @submit.prevent="submitProfile" enctype="multipart/form-data" class="grid grid-cols-1 md:grid-cols-2 gap-5">

                        <!-- Name -->
                        <div class="flex flex-col gap-1.5">
                            <label for="p-name" class="text-sm font-semibold text-gray-700">Nama Lengkap</label>
                            <input id="p-name" type="text" v-model="profileForm.name" required
                                class="w-full h-11 border border-[#D7DDC2] rounded-xl px-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition" />
                            <div v-if="profileForm.errors.name" class="text-red-600 text-xs">{{ profileForm.errors.name }}</div>
                        </div>

                        <!-- Email (readonly) -->
                        <div class="flex flex-col gap-1.5">
                            <label for="p-email" class="text-sm font-semibold text-gray-400">Email (Tidak Dapat Diubah)</label>
                            <input id="p-email" type="email" :value="user?.email" disabled
                                class="w-full h-11 border border-gray-200 rounded-xl px-4 text-gray-400 bg-gray-50 cursor-not-allowed outline-none" />
                        </div>

                        <!-- NIK -->
                        <div class="flex flex-col gap-1.5">
                            <label for="p-nik" class="text-sm font-semibold text-gray-700">NIK (16 Digit)</label>
                            <input id="p-nik" type="text" v-model="profileForm.nik" required maxlength="16" placeholder="Masukkan 16 digit NIK"
                                class="w-full h-11 border border-[#D7DDC2] rounded-xl px-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition" />
                            <div v-if="profileForm.errors.nik" class="text-red-600 text-xs">{{ profileForm.errors.nik }}</div>
                        </div>

                        <!-- Age -->
                        <div class="flex flex-col gap-1.5">
                            <label for="p-age" class="text-sm font-semibold text-gray-700">Umur (Tahun)</label>
                            <input id="p-age" type="number" v-model="profileForm.age" required min="1" max="120"
                                class="w-full h-11 border border-[#D7DDC2] rounded-xl px-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition" />
                            <div v-if="profileForm.errors.age" class="text-red-600 text-xs">{{ profileForm.errors.age }}</div>
                        </div>

                        <!-- Phone -->
                        <div class="flex flex-col gap-1.5">
                            <label for="p-phone" class="text-sm font-semibold text-gray-700">Nomor Telepon</label>
                            <input id="p-phone" type="text" v-model="profileForm.phone_number" required
                                class="w-full h-11 border border-[#D7DDC2] rounded-xl px-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition" />
                            <div v-if="profileForm.errors.phone_number" class="text-red-600 text-xs">{{ profileForm.errors.phone_number }}</div>
                        </div>

                        <!-- Emergency Contact -->
                        <div class="flex flex-col gap-1.5">
                            <label for="p-emergency" class="text-sm font-semibold text-gray-700">Nomor Telepon Darurat</label>
                            <input id="p-emergency" type="text" v-model="profileForm.emergency_contact" required
                                class="w-full h-11 border border-[#D7DDC2] rounded-xl px-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition" />
                            <div v-if="profileForm.errors.emergency_contact" class="text-red-600 text-xs">{{ profileForm.errors.emergency_contact }}</div>
                        </div>

                        <!-- Address -->
                        <div class="flex flex-col gap-1.5 md:col-span-2">
                            <label for="p-address" class="text-sm font-semibold text-gray-700">Alamat Lengkap</label>
                            <textarea id="p-address" v-model="profileForm.address" required rows="3"
                                class="w-full border border-[#D7DDC2] rounded-xl p-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition resize-none"></textarea>
                            <div v-if="profileForm.errors.address" class="text-red-600 text-xs">{{ profileForm.errors.address }}</div>
                        </div>

                        <!-- Avatar error -->
                        <div v-if="profileForm.errors.avatar" class="md:col-span-2 text-red-600 text-xs">{{ profileForm.errors.avatar }}</div>

                        <!-- Actions -->
                        <div class="md:col-span-2 flex justify-end gap-3 mt-2">
                            <a href="/" class="h-11 px-6 rounded-full border border-[#D6CCAF] flex items-center text-gray-600 font-semibold hover:bg-gray-50 transition">
                                Kembali
                            </a>
                            <button type="submit" :disabled="profileForm.processing"
                                class="h-11 px-8 rounded-full bg-[#374426] text-white font-semibold hover:bg-[#2c361e] transition disabled:opacity-60 disabled:cursor-not-allowed">
                                <span v-if="profileForm.processing">Menyimpan...</span>
                                <span v-else>Simpan Profil</span>
                            </button>
                        </div>
                    </form>
                </div>

                <!-- ── Change Password Form ── -->
                <div v-if="activeSection === 'password'" class="p-5 md:p-6">
                    <form @submit.prevent="submitPassword" class="flex flex-col gap-4 max-w-lg">

                        <!-- Current Password -->
                        <div class="flex flex-col gap-1.5">
                            <label for="pw-current" class="text-sm font-semibold text-gray-700">Password Lama</label>
                            <input id="pw-current" type="password" v-model="passwordForm.current_password" required autocomplete="current-password"
                                class="w-full h-11 border border-[#D7DDC2] rounded-xl px-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition" />
                            <div v-if="passwordForm.errors.current_password" class="text-red-600 text-xs">{{ passwordForm.errors.current_password }}</div>
                            <!-- Forgot password link -->
                            <button type="button" @click="showForgotModal = true" class="text-xs text-[#64823E] hover:underline self-end mt-0.5 bg-none border-none p-0 outline-none cursor-pointer">
                                Lupa password?
                            </button>
                        </div>

                        <!-- New Password -->
                        <div class="flex flex-col gap-1.5">
                            <label for="pw-new" class="text-sm font-semibold text-gray-700">Password Baru</label>
                            <input id="pw-new" type="password" v-model="passwordForm.password" required autocomplete="new-password" minlength="8"
                                class="w-full h-11 border border-[#D7DDC2] rounded-xl px-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition" />
                            <div v-if="passwordForm.errors.password" class="text-red-600 text-xs">{{ passwordForm.errors.password }}</div>
                        </div>

                        <!-- Confirm New Password -->
                        <div class="flex flex-col gap-1.5">
                            <label for="pw-confirm" class="text-sm font-semibold text-gray-700">Konfirmasi Password Baru</label>
                            <input id="pw-confirm" type="password" v-model="passwordForm.password_confirmation" required autocomplete="new-password"
                                class="w-full h-11 border border-[#D7DDC2] rounded-xl px-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition" />
                            <div v-if="passwordForm.errors.password_confirmation" class="text-red-600 text-xs">{{ passwordForm.errors.password_confirmation }}</div>
                        </div>

                        <!-- Actions -->
                        <div class="flex justify-end gap-3 mt-2">
                            <button type="button" @click="passwordForm.reset()"
                                class="h-11 px-6 rounded-full border border-[#D6CCAF] text-gray-600 font-semibold hover:bg-gray-50 transition">
                                Reset
                            </button>
                            <button type="submit" :disabled="passwordForm.processing"
                                class="h-11 px-8 rounded-full bg-[#374426] text-white font-semibold hover:bg-[#2c361e] transition disabled:opacity-60 disabled:cursor-not-allowed">
                                <span v-if="passwordForm.processing">Menyimpan...</span>
                                <span v-else>Ubah Password</span>
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- ══════════════ SECTION 2: Riwayat Pemesanan ══════════════ -->
            <div class="bg-white rounded-2xl border border-[#D6CCAF] shadow-md overflow-hidden">
                <!-- Header -->
                <div class="p-5 md:p-6 border-b border-[#EDE9D8] flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
                    <h2 class="text-xl font-bold text-[#374426]">Riwayat Pemesanan</h2>
                    <!-- Search -->
                    <div class="relative">
                        <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-4.35-4.35M17 11A6 6 0 1 1 5 11a6 6 0 0 1 12 0z" />
                        </svg>
                        <input
                            v-model="bookingSearch"
                            type="text"
                            placeholder="Cari booking..."
                            id="booking-search"
                            class="pl-9 pr-4 h-9 border border-[#D7DDC2] rounded-lg text-sm bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition w-48"
                        />
                    </div>
                </div>

                <!-- Booking Cards — 3 Column Layout -->
                <div class="p-5 md:p-6">
                    <div v-if="bookings.length === 0" class="text-center py-12 text-gray-400">
                        <svg class="w-12 h-12 mx-auto mb-3 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                        </svg>
                        <p class="font-medium">Belum ada riwayat pemesanan</p>
                        <a href="/booking" class="mt-3 inline-block text-sm text-[#64823E] hover:underline font-medium">Mulai Booking →</a>
                    </div>

                    <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-5">

                        <!-- ── REGISTERED Column ── -->
                        <div class="flex flex-col gap-4">
                            <div class="flex items-center gap-2">
                                <span class="px-2.5 py-0.5 bg-[#374426] text-white text-[10px] font-bold rounded tracking-widest">REGISTERED</span>
                                <span class="text-xs text-gray-400">({{ registeredBookings.length }})</span>
                            </div>
                            <div v-if="registeredBookings.length === 0" class="text-center py-8 text-gray-300 text-sm border border-dashed border-gray-200 rounded-xl">
                                Tidak ada
                            </div>
                            <div
                                v-for="booking in displayedRegisteredBookings"
                                :key="booking.id"
                                class="border border-[#374426]/30 rounded-xl p-4 flex flex-col gap-3 bg-[#f6f9f2] hover:shadow-sm transition"
                            >
                                <div class="flex items-start justify-between gap-2">
                                    <div>
                                        <p class="font-bold text-[#374426] text-sm">{{ booking.mountain_name || 'Gunung' }}</p>
                                        <p class="text-xs text-gray-500 mt-0.5 flex items-center gap-1">
                                            <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                                            {{ booking.route_name || '-' }}
                                        </p>
                                    </div>
                                    <svg class="w-4 h-4 text-[#374426] shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"/></svg>
                                </div>
                                <div class="flex flex-col gap-1 text-xs text-gray-500">
                                    <span class="flex items-center gap-1">
                                        <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                                        {{ formatDate(booking.start_date) }} – {{ formatDate(booking.end_date) }}
                                    </span>
                                    <span class="flex items-center gap-1">
                                        <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                                        {{ booking.member_count }} Member
                                    </span>
                                </div>
                                <div class="flex flex-col gap-2 pt-1 border-t border-[#D6CCAF]">
                                    <button @click="viewBookingDetails(booking)" class="w-full py-2 bg-[#374426] text-white text-xs font-semibold rounded-lg hover:bg-[#2c361e] transition cursor-pointer">
                                        View Details
                                    </button>
                                    <button @click="downloadBookingETicket(booking)" class="w-full py-2 border border-[#374426] text-[#374426] text-xs font-semibold rounded-lg hover:bg-[#374426]/5 transition cursor-pointer">
                                        Download E-Ticket
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- ── PENDING Column ── -->
                        <div class="flex flex-col gap-4">
                            <div class="flex items-center gap-2">
                                <span class="px-2.5 py-0.5 bg-amber-500 text-white text-[10px] font-bold rounded tracking-widest">PENDING</span>
                                <span class="text-xs text-gray-400">({{ pendingBookings.length }})</span>
                            </div>
                            <div v-if="pendingBookings.length === 0" class="text-center py-8 text-gray-300 text-sm border border-dashed border-gray-200 rounded-xl">
                                Tidak ada
                            </div>
                            <div
                                v-for="booking in displayedPendingBookings"
                                :key="booking.id"
                                class="border border-amber-300 rounded-xl p-4 flex flex-col gap-3 bg-amber-50 hover:shadow-sm transition"
                            >
                                <div class="flex items-start justify-between gap-2">
                                    <div>
                                        <p class="font-bold text-amber-900 text-sm">{{ booking.mountain_name || 'Gunung' }}</p>
                                        <p class="text-xs text-amber-600 mt-0.5 flex items-center gap-1">
                                            <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                                            {{ booking.route_name || '-' }}
                                        </p>
                                    </div>
                                    <svg class="w-4 h-4 text-amber-500 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                                </div>
                                <div class="flex flex-col gap-1 text-xs text-amber-700">
                                    <span class="flex items-center gap-1">
                                        <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                                        {{ formatDate(booking.start_date) }} – {{ formatDate(booking.end_date) }}
                                    </span>
                                    <span class="flex items-center gap-1">
                                        <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                                        {{ booking.member_count }} Member
                                    </span>
                                    <span v-if="booking.expiry_time" class="text-amber-500 font-medium">
                                        Bayar sebelum: {{ formatDate(booking.expiry_time) }}
                                    </span>
                                </div>
                                <div class="flex flex-col gap-2 pt-1 border-t border-amber-200">
                                    <Link
                                        :href="`/booking/pay/${booking.order_id}`"
                                        class="w-full py-2 bg-amber-500 text-white text-xs font-semibold rounded-lg hover:bg-amber-600 transition text-center block animate-pulse"
                                    >
                                        Complete Payment
                                    </Link>
                                    <button @click="viewBookingDetails(booking)" class="w-full py-2 border border-amber-400 text-amber-700 text-xs font-semibold rounded-lg hover:bg-amber-100 transition cursor-pointer">
                                        View Details
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- ── CANCELED Column ── -->
                        <div class="flex flex-col gap-4">
                            <div class="flex items-center gap-2">
                                <span class="px-2.5 py-0.5 bg-red-600 text-white text-[10px] font-bold rounded tracking-widest">CANCELED</span>
                                <span class="text-xs text-gray-400">({{ canceledBookings.length }})</span>
                            </div>
                            <div v-if="canceledBookings.length === 0" class="text-center py-8 text-gray-300 text-sm border border-dashed border-gray-200 rounded-xl">
                                Tidak ada
                            </div>
                            <div
                                v-for="booking in displayedCanceledBookings"
                                :key="booking.id"
                                class="border border-red-200 rounded-xl p-4 flex flex-col gap-3 bg-red-50 hover:shadow-sm transition"
                            >
                                <div class="flex items-start justify-between gap-2">
                                    <div>
                                        <p class="font-bold text-red-900 text-sm">{{ booking.mountain_name || 'Gunung' }}</p>
                                        <p class="text-xs text-red-400 mt-0.5 flex items-center gap-1">
                                            <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                                            {{ booking.route_name || '-' }}
                                        </p>
                                    </div>
                                    <svg class="w-4 h-4 text-red-400 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                                </div>
                                <div class="flex flex-col gap-1 text-xs text-red-400">
                                    <span class="flex items-center gap-1">
                                        <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                                        {{ formatDate(booking.start_date) }} – {{ formatDate(booking.end_date) }}
                                    </span>
                                    <span class="flex items-center gap-1">
                                        <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                                        {{ booking.member_count }} Member
                                    </span>
                                </div>
                                <div class="flex flex-col gap-2 pt-1 border-t border-red-100">
                                    <button @click="viewBookingDetails(booking)" class="w-full py-2 bg-[#8B2020] text-white text-xs font-semibold rounded-lg hover:bg-red-700 transition cursor-pointer">
                                        View Details
                                    </button>
                                    <Link href="/booking"
                                        class="block w-full py-2 text-center border border-red-400 text-red-600 text-xs font-semibold rounded-lg hover:bg-red-100 transition">
                                        Rebook
                                    </Link>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </div>

        </main>

        <!-- ══════════════ Footer ══════════════ -->
        <div class="w-full flex flex-col">
            <div class="w-full bg-[#E0DBBE] py-5 px-6 md:px-12 flex flex-col md:flex-row items-center justify-between gap-6">
                <div class="flex items-center gap-3">
                    <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-10 h-10 object-contain" />
                    <span class="text-2xl font-bold text-[#374426] tracking-tight">AltiGuide</span>
                </div>
                <div class="flex items-center gap-4">
                    <Link href="mailto:support@altiguide.com" class="bg-[#374426] text-[#F8F3E4] text-sm md:text-base font-medium rounded-lg px-5 py-2.5 hover:opacity-90 transition">Contact Us</Link>
                    <Link href="/booking" class="bg-[#F8F3E4] text-[#374426] text-sm md:text-base font-medium rounded-lg px-5 py-2.5 hover:opacity-90 transition shadow-sm">Start Summit</Link>
                </div>
            </div>

            <footer class="w-full bg-white px-6 md:px-12 py-6 flex flex-col">
                <div class="flex flex-col lg:flex-row justify-between items-start gap-10 mb-6">
                    <div class="flex flex-col gap-16">
                        <a href="/" class="text-xl font-medium text-[#374426] underline underline-offset-8">AltiGuide.com</a>
                        <div class="flex items-center gap-5 text-[#828282]">
                            <a href="#" class="hover:text-[#374426] transition">
                                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12c0 4.991 3.657 9.128 8.438 9.878v-6.987h-2.54V12h2.54V9.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V12h2.773l-.443 2.89h-2.33v6.988C18.343 21.128 22 16.991 22 12z"/></svg>
                            </a>
                            <a href="#" class="hover:text-[#374426] transition">
                                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M21.582 6.186a2.66 2.66 0 0 0-1.875-1.884C18.053 3.86 12 3.86 12 3.86s-6.053 0-7.707.442a2.66 2.66 0 0 0-1.875 1.884C2 7.854 2 12 2 12s0 4.146.418 5.814a2.66 2.66 0 0 0 1.875 1.884C5.947 20.14 12 20.14 12 20.14s6.053 0 7.707-.442a2.66 2.66 0 0 0 1.875-1.884C22 16.146 22 12 22 12s0-4.146-.418-5.814zM9.88 15.15V8.85l6.32 3.15-6.32 3.15z"/></svg>
                            </a>
                            <a href="https://www.instagram.com/altiguide___?igsh=MTRwbW8zbW8wZDVubg==" target="_blank" rel="noopener noreferrer" class="hover:text-[#374426] transition">
                                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zM12 0C8.741 0 8.333.014 7.053.072 2.695.272.273 2.69.073 7.052.014 8.333 0 8.741 0 12c0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98C8.333 23.986 8.741 24 12 24c3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98C15.668.014 15.259 0 12 0zm0 5.838a6.162 6.162 0 1 0 0 12.324 6.162 6.162 0 0 0 0-12.324zM12 16a4 4 0 1 1 0-8 4 4 0 0 1 0 8zm6.406-11.845a1.44 1.44 0 1 0 0 2.881 1.44 1.44 0 0 0 0-2.881z"/></svg>
                            </a>
                        </div>
                    </div>
                    <div class="flex flex-col sm:flex-row gap-10 md:gap-20">
                        <div class="flex flex-col gap-4">
                            <h5 class="text-[#374426] font-semibold text-base">Jelajahi</h5>
                            <div class="flex flex-col gap-3 text-[#5A684C] font-medium text-sm">
                                <Link href="/mountains" class="hover:text-[#374426] transition">Daftar Gunung</Link>
                                <Link href="/weather-analytics" class="hover:text-[#374426] transition">Weather Analytics</Link>
                            </div>
                        </div>
                        <div class="flex flex-col gap-4">
                            <h5 class="text-[#374426] font-semibold text-base">Informasi</h5>
                            <div class="flex flex-col gap-3 text-[#5A684C] font-medium text-sm">
                                <Link href="/tata-tertib" class="hover:text-[#374426] transition">Tata Tertib</Link>
                                <Link href="/booking" class="hover:text-[#374426] transition">Booking Simaksi</Link>
                                <Link href="/tips-keamanan" class="hover:text-[#374426] transition">Tips Keamanan</Link>
                            </div>
                        </div>
                        <div class="flex flex-col gap-4">
                            <h5 class="text-[#374426] font-semibold text-base">Komunitas</h5>
                            <div class="flex flex-col gap-3 text-[#5A684C] font-medium text-sm">
                                <Link href="/" class="hover:text-[#374426] transition">Forum Diskusi</Link>
                                <!-- <Link href="/about" class="hover:text-[#374426] transition">Tentang Kami</Link> -->
                            </div>
                        </div>
                    </div>
                </div>
                <div class="w-full border-t border-[#D7DDC2] pt-5 flex justify-end">
                    <p class="text-[#5A684C] font-medium text-sm">© 2026 AltiGuide Team. All rights reserved.</p>
                </div>
            </footer>
        </div>
    </div>

    <!-- Forgot Password Modal -->
    <div v-if="showForgotModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="showForgotModal = false"></div>
        
        <!-- Modal Card -->
        <div class="relative w-full max-w-[480px] bg-[#F8F3E4] rounded-[20px] border border-[#D6CCAF] shadow-2xl p-8 md:p-10 flex flex-col items-center z-10 animate-fade-in" style="font-family: 'Montserrat', sans-serif;">
            <!-- Close Button -->
            <button @click="showForgotModal = false" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition cursor-pointer" aria-label="Close modal">
                <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>

            <!-- Logo -->
            <div class="flex items-center justify-center gap-3 mb-6">
                <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 object-contain" />
                <span class="text-2xl font-semibold text-[#374426] tracking-tight">AltiGuide</span>
            </div>

            <h2 class="text-xl font-semibold text-[#333333] text-center mb-2">Forgot Password</h2>
            <p class="text-sm text-[#666666] text-center mb-6 leading-relaxed">
                Enter your email address and we'll send you a verification code to reset your password.
            </p>

            <form @submit.prevent="submitForgot" class="w-full flex flex-col gap-4">
                <div class="flex flex-col gap-1.5 w-full">
                    <label for="forgot-email" class="text-sm font-semibold text-[#666666]">Email address</label>
                    <input
                        id="forgot-email"
                        type="email"
                        v-model="forgotForm.email"
                        required
                        class="w-full h-12 border border-[#D7DDC2] rounded-xl px-4 bg-[#F8F3E4] text-[#333333] outline-none focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 transition"
                    />
                    <div v-if="forgotForm.errors.email" class="text-red-500 text-xs mt-1">{{ forgotForm.errors.email }}</div>
                </div>

                <button
                    type="submit"
                    class="w-full h-12 rounded-[40px] bg-[#374426] text-white font-semibold text-base mt-2 hover:bg-[#2c361e] transition disabled:opacity-60 disabled:cursor-not-allowed cursor-pointer"
                    :disabled="forgotForm.processing"
                >
                    <span v-if="forgotForm.processing">Sending...</span>
                    <span v-else>Send Verification Code</span>
                </button>
            </form>
        </div>
    </div>

    <!-- Avatar Crop Modal -->
    <div v-if="showCropModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/85 backdrop-blur-sm" @click="closeCropModal"></div>
        
        <!-- Modal Card -->
        <div class="relative w-full max-w-[420px] bg-white rounded-2xl shadow-2xl p-6 flex flex-col items-center z-10" style="font-family: 'Montserrat', sans-serif;">
            <h3 class="text-lg font-bold text-[#374426] mb-4">Sesuaikan Foto Profil</h3>
            
            <!-- Crop Container (circular viewport) -->
            <div class="relative w-64 h-64 rounded-full overflow-hidden border border-gray-300 bg-[#1e1e1e] cursor-move select-none"
                 @mousedown="startDrag"
                 @touchstart="startDrag">
                <!-- Mask Overlay -->
                <div class="absolute inset-0 rounded-full border-2 border-white/50 pointer-events-none z-10 shadow-[0_0_0_9999px_rgba(0,0,0,0.5)]"></div>
                
                <!-- Cropping Image -->
                <img
                    ref="imgRef"
                    :src="cropSrc"
                    alt="Image to crop"
                    class="absolute origin-center max-w-none select-none pointer-events-none"
                    :style="imageStyle"
                    @load="onImageLoaded"
                />
            </div>
            
            <!-- Zoom Slider -->
            <div class="w-full mt-6 flex flex-col gap-2">
                <div class="flex justify-between text-xs text-gray-500 font-semibold">
                    <span>Perkecil</span>
                    <span>Perbesar</span>
                </div>
                <input
                    type="range"
                    v-model.number="zoom"
                    min="1"
                    max="3"
                    step="0.01"
                    class="w-full accent-[#374426] h-1.5 bg-gray-200 rounded-lg appearance-none cursor-pointer"
                />
            </div>
            
            <!-- Buttons -->
            <div class="w-full flex gap-3 mt-6">
                <button
                    type="button"
                    @click="closeCropModal"
                    class="flex-1 py-2.5 rounded-full border border-gray-300 text-gray-600 text-sm font-semibold hover:bg-gray-50 transition cursor-pointer"
                >
                    Batal
                </button>
                <button
                    type="button"
                    @click="saveCrop"
                    class="flex-1 py-2.5 rounded-full bg-[#374426] text-white text-sm font-semibold hover:bg-[#2c361e] transition cursor-pointer"
                >
                    Simpan
                </button>
            </div>
        </div>
    </div>

    <!-- Detail Booking Modal -->
    <div v-if="showDetailsModal && selectedBooking" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="showDetailsModal = false"></div>
        
        <!-- Modal Card -->
        <div class="relative w-full max-w-2xl bg-[#F8F3E4] rounded-[20px] border border-[#D6CCAF] shadow-2xl p-6 md:p-8 flex flex-col z-10 max-h-[90vh] overflow-y-auto animate-fade-in" style="font-family: 'Montserrat', sans-serif;">
            <!-- Close Button -->
            <button @click="showDetailsModal = false" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition cursor-pointer" aria-label="Close modal">
                <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>

            <!-- Header -->
            <div class="flex items-center gap-3 border-b border-[#D6CCAF] pb-4 mb-6">
                <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 object-contain" />
                <div>
                    <h2 class="text-xl font-bold text-[#374426]">Detail Pemesanan</h2>
                    <p class="text-xs text-[#9F8C74] font-semibold tracking-wider">ORDER ID: #{{ selectedBooking.order_id }}</p>
                </div>
            </div>

            <!-- Modal Content Grid -->
            <div class="flex flex-col gap-6 text-[#374426]">
                <!-- Grid 2 Kolom untuk Info Pendakian & Ketua -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <!-- Info Pendakian -->
                    <div class="bg-white/50 border border-[#D6CCAF] rounded-xl p-4 flex flex-col gap-2.5">
                        <h3 class="text-sm font-bold text-[#66533A] border-b border-[#E5DDD0] pb-1.5 flex items-center gap-1.5">
                            🏔️ Informasi Pendakian
                        </h3>
                        <div class="grid grid-cols-3 text-xs">
                            <span class="text-gray-500 font-semibold col-span-1">Destinasi</span>
                            <span class="col-span-2 font-bold">{{ selectedBooking.mountain_name }}</span>
                        </div>
                        <div class="grid grid-cols-3 text-xs">
                            <span class="text-gray-500 font-semibold col-span-1">Jalur</span>
                            <span class="col-span-2 font-semibold">{{ selectedBooking.route_name }}</span>
                        </div>
                        <div class="grid grid-cols-3 text-xs">
                            <span class="text-gray-500 font-semibold col-span-1">Tanggal</span>
                            <span class="col-span-2 font-semibold">{{ formatDate(selectedBooking.start_date) }} – {{ formatDate(selectedBooking.end_date) }}</span>
                        </div>
                        <div class="grid grid-cols-3 text-xs">
                            <span class="text-gray-500 font-semibold col-span-1">Tipe</span>
                            <span class="col-span-2 font-semibold capitalize">{{ selectedBooking.hike_type || 'Camp' }}</span>
                        </div>
                        <div class="grid grid-cols-3 text-xs">
                            <span class="text-gray-500 font-semibold col-span-1">Kelompok</span>
                            <span class="col-span-2 font-semibold">{{ selectedBooking.group_name }}</span>
                        </div>
                        <div class="grid grid-cols-3 text-xs">
                            <span class="text-gray-500 font-semibold col-span-1">Total Biaya</span>
                            <span class="col-span-2 font-bold text-emerald-700">{{ formatCurrency(selectedBooking.gross_amount) }}</span>
                        </div>
                        <div class="grid grid-cols-3 text-xs items-center">
                            <span class="text-gray-500 font-semibold col-span-1">Status</span>
                            <div class="col-span-2">
                                <span v-if="['settlement', 'capture'].includes(selectedBooking.status)" class="px-2 py-0.5 bg-[#374426] text-white text-[10px] font-bold rounded">REGISTERED</span>
                                <span v-else-if="selectedBooking.status === 'pending'" class="px-2 py-0.5 bg-amber-500 text-white text-[10px] font-bold rounded">PENDING</span>
                                <span v-else class="px-2 py-0.5 bg-red-600 text-white text-[10px] font-bold rounded">CANCELED</span>
                            </div>
                        </div>
                    </div>

                    <!-- Info Ketua -->
                    <div class="bg-white/50 border border-[#D6CCAF] rounded-xl p-4 flex flex-col gap-2.5">
                        <h3 class="text-sm font-bold text-[#66533A] border-b border-[#E5DDD0] pb-1.5 flex items-center gap-1.5">
                            👤 Ketua Rombongan
                        </h3>
                        <div class="grid grid-cols-3 text-xs">
                            <span class="text-gray-500 font-semibold col-span-1">Nama</span>
                            <span class="col-span-2 font-bold">{{ selectedBooking.leader?.name || '-' }}</span>
                        </div>
                        <div class="grid grid-cols-3 text-xs">
                            <span class="text-gray-500 font-semibold col-span-1">NIK</span>
                            <span class="col-span-2 font-semibold">{{ selectedBooking.leader?.nik || '-' }}</span>
                        </div>
                        <div class="grid grid-cols-3 text-xs">
                            <span class="text-gray-500 font-semibold col-span-1">Telepon</span>
                            <span class="col-span-2 font-semibold">{{ selectedBooking.leader?.phone || '-' }}</span>
                        </div>
                        <div class="grid grid-cols-3 text-xs">
                            <span class="text-gray-500 font-semibold col-span-1">Email</span>
                            <span class="col-span-2 font-semibold truncate">{{ selectedBooking.leader?.email || '-' }}</span>
                        </div>
                    </div>
                </div>

                <!-- Daftar Anggota -->
                <div class="flex flex-col gap-3">
                    <h3 class="text-sm font-bold text-[#66533A] flex items-center gap-1.5">
                        👥 Daftar Anggota ({{ selectedBooking.member_count }} Orang)
                    </h3>
                    <div class="border border-[#D6CCAF] rounded-xl overflow-hidden bg-white shadow-sm">
                        <table class="w-full text-left border-collapse text-xs">
                            <thead>
                                <tr class="bg-[#F4F1E6] text-[#66533A] font-bold border-b border-[#D6CCAF]">
                                    <th class="py-2.5 px-4 w-12 text-center">No</th>
                                    <th class="py-2.5 px-4">Nama Lengkap</th>
                                    <th class="py-2.5 px-4">NIK</th>
                                    <th class="py-2.5 px-4">Nomor HP</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(member, idx) in selectedBooking.members" :key="idx" class="border-b border-[#F0EBE0] hover:bg-gray-50">
                                    <td class="py-2.5 px-4 text-center text-gray-500 font-semibold">{{ idx + 1 }}</td>
                                    <td class="py-2.5 px-4 font-bold">{{ member.full_name }}</td>
                                    <td class="py-2.5 px-4 text-gray-600 font-mono">{{ member.identity_number }}</td>
                                    <td class="py-2.5 px-4 text-gray-600">{{ member.phone_number }}</td>
                                </tr>
                                <tr v-if="!selectedBooking.members || selectedBooking.members.length === 0">
                                    <td colspan="4" class="py-6 text-center text-gray-400 font-medium">Tidak ada anggota tambahan.</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- Footer / Actions -->
            <div class="flex flex-col sm:flex-row justify-end gap-3 mt-8 pt-4 border-t border-[#D6CCAF]">
                <button
                    v-if="['settlement', 'capture'].includes(selectedBooking.status)"
                    @click="downloadBookingETicket(selectedBooking)"
                    class="h-11 px-6 bg-[#374426] text-white rounded-full font-semibold text-sm hover:bg-[#2c361e] transition flex items-center justify-center gap-2 cursor-pointer shadow-md"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" x2="12" y1="15" y2="3"/></svg>
                    Download E-Ticket
                </button>
                <Link
                    v-if="selectedBooking.status === 'pending'"
                    :href="`/booking/pay/${selectedBooking.order_id}`"
                    class="h-11 px-6 bg-amber-500 text-white rounded-full font-semibold text-sm hover:bg-amber-600 transition flex items-center justify-center gap-2 shadow-md"
                >
                    Complete Payment
                </Link>
                <button
                    type="button"
                    @click="showDetailsModal = false"
                    class="h-11 px-6 rounded-full border border-gray-300 text-gray-700 text-sm font-semibold hover:bg-gray-50 transition cursor-pointer"
                >
                    Tutup
                </button>
            </div>
        </div>
    </div>
</template>
