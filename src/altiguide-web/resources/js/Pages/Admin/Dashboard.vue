<script setup>
import { ref, computed, reactive, watch, nextTick } from 'vue'
import { Head, Link, router, useForm } from '@inertiajs/vue3'
import axios from 'axios'

// ═══════════════════════════════════════════════════════════
// TOAST NOTIFICATION SYSTEM
// ═══════════════════════════════════════════════════════════
const toast = reactive({
  show: false,
  message: '',
  type: 'success',
})
const showToast = (message, type = 'success') => {
  toast.message = message
  toast.type = type
  toast.show = true
  setTimeout(() => {
    toast.show = false
  }, 4500)
}

// ═══════════════════════════════════════════════════════════
// PROPS — Data nyata dari backend (DashboardController)
// ═══════════════════════════════════════════════════════════
const props = defineProps({
  bookings:             { type: Array,  default: () => [] },
  stats:                { type: Object, default: () => ({ totalRombongan: 0, pendakiDiGunung: 0, checkInHariIni: 0, checkOutHariIni: 0 }) },
  mountainsWithContent: { type: Array,  default: () => [] },
  mountains:            { type: Array,  default: () => [] },
  adminName:            { type: String, default: 'Admin' },
})

// ═══════════════════════════════════════════════════════════
// SIDEBAR
// ═══════════════════════════════════════════════════════════
const sidebarActive = ref('dashboard')

// ═══════════════════════════════════════════════════════════
// BOOKING TABLE
// ═══════════════════════════════════════════════════════════
const bookingSearch = ref('')
const filteredBookings = computed(() => {
  if (!bookingSearch.value) return props.bookings
  const q = bookingSearch.value.toLowerCase()
  return props.bookings.filter(b =>
    b.group_name?.toLowerCase().includes(q) ||
    b.order_id?.toLowerCase().includes(q) ||
    b.mountain?.toLowerCase().includes(q)
  )
})

// Helper badges
const paymentBadge = (status) => {
  switch (status) {
    case 'settlement': return { label: 'LUNAS',   cls: 'bg-green-100 text-green-800 border border-green-300' }
    case 'pending':    return { label: 'PENDING',  cls: 'bg-amber-100 text-amber-800 border border-amber-300' }
    case 'expire':     return { label: 'BATAL',    cls: 'bg-red-100 text-red-800 border border-red-300' }
    default:           return { label: status,     cls: 'bg-gray-100 text-gray-800' }
  }
}
const verificationBadge = (status) => {
  switch (status) {
    case 'terverifikasi': return { label: 'TERVERIFIKASI', cls: 'bg-green-100 text-green-800 border border-green-300' }
    case 'review':        return { label: 'REVIEW',        cls: 'bg-amber-100 text-amber-800 border border-amber-300' }
    case 'revisi':        return { label: 'REVISI',        cls: 'bg-red-100 text-red-800 border border-red-300' }
    case 'pending_review':
    default:              return { label: 'MENUNGGU',      cls: 'bg-gray-100 text-gray-700 border border-gray-300' }
  }
}
const formatRupiah = (num) =>
  new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(num)
const formatDate = (d) => d ? new Date(d + 'T00:00:00').toLocaleDateString('id-ID', { day: 'numeric', month: 'long', year: 'numeric' }) : '-'
const hikeTypeLabel = (t) => t === 'camp' ? 'Camp' : 'Tektok'

// ═══════════════════════════════════════════════════════════
// DETAIL MODAL
// ═══════════════════════════════════════════════════════════
const showDetailModal = ref(false)
const selectedOrder = ref(null)

const openDetailModal = (order) => {
  selectedOrder.value = order
  showDetailModal.value = true
}
const closeDetailModal = () => {
  showDetailModal.value = false
  selectedOrder.value = null
}

// Update verification status via Inertia
const verificationLoading = ref(false)
const updateVerification = (bookingId, newStatus) => {
  verificationLoading.value = true
  router.put(`/admin/bookings/${bookingId}/verify`, { verification_status: newStatus }, {
    preserveScroll: true,
    onSuccess: () => {
      if (selectedOrder.value?.id === bookingId) {
        selectedOrder.value = { ...selectedOrder.value, verification_status: newStatus }
      }
    },
    onFinish: () => { verificationLoading.value = false },
  })
}

// ═══════════════════════════════════════════════════════════
// CHECK-IN / CHECK-OUT GROUPS (derived from real bookings)
// ═══════════════════════════════════════════════════════════
const todayStr = new Date().toLocaleDateString('sv-SE')

const todayHikingSessions = computed(() => {
  return props.bookings.filter(b => {
    if (b.payment_status !== 'settlement') return false

    if (b.status === 'on_track') {
      return b.end_date === todayStr
    }
    return false
  })
})

// ─── Check-in Modal ────────────────────────────────────────
const showCheckinModal = ref(false)
const selectedCheckinGroup = ref(null)
const logisticsDeclaration = reactive({
  tenda: 0,
  sleepingBag: 0,
  p3k: 0,
  peralatanMasak: 0,
  senter: 0,
  jasHujan: 0,
})
const wasteDeclaration = reactive({ botolPlastik: 0, bungkusMakanan: 0, kaleng: 0, lainnya: 0 })
const totalWaste    = computed(() => wasteDeclaration.botolPlastik + wasteDeclaration.bungkusMakanan + wasteDeclaration.kaleng + wasteDeclaration.lainnya)
const canCheckin    = computed(() => true)

const openCheckinModalDirect = (session) => {
  selectedCheckinGroup.value = session
  Object.assign(logisticsDeclaration, { tenda: 0, sleepingBag: 0, p3k: 0, peralatanMasak: 0, senter: 0, jasHujan: 0 })
  Object.assign(wasteDeclaration, { botolPlastik: 0, bungkusMakanan: 0, kaleng: 0, lainnya: 0 })
  showCheckinModal.value = true
}

const closeCheckinModal = () => { showCheckinModal.value = false; selectedCheckinGroup.value = null }

const processCheckin = async () => {
  if (!canCheckin.value || !selectedCheckinGroup.value) return
  const group = selectedCheckinGroup.value
  
  // Safely normalize numeric inputs
  Object.keys(logisticsDeclaration).forEach(k => {
    logisticsDeclaration[k] = Math.max(0, Math.floor(Number(logisticsDeclaration[k]) || 0))
  })
  Object.keys(wasteDeclaration).forEach(k => {
    wasteDeclaration[k] = Math.max(0, Math.floor(Number(wasteDeclaration[k]) || 0))
  })
  
  verificationLoading.value = true
  try {
    const { data } = await axios.put(`/admin/checkin/status/${group.order_id}`, { status: 'on_track' })
    if (data.status === 'success') {
      closeCheckinModal()
      showToast(`Check-in berhasil! Rombongan "${group.group_name}" sekarang terdaftar mendaki gunung.`, 'success')
      router.reload({ only: ['bookings', 'stats'] })
    } else {
      showToast(data.message || "Gagal memproses check-in.", 'error')
    }
  } catch (err) {
    showToast(err.response?.data?.message || "Terjadi kesalahan koneksi.", 'error')
  } finally {
    verificationLoading.value = false
  }
}

// ─── Check-out Modal ───────────────────────────────────────
const showCheckoutModal = ref(false)
const selectedCheckoutGroup = ref(null)
const wasteCheckout = reactive({ botolPlastik: 0, bungkusMakanan: 0, kaleng: 0, lainnya: 0 })
const healthCondition = ref('sehat')
const chronology = ref('')

const wasteItems = computed(() => {
  if (!selectedCheckoutGroup.value) return []
  // Real checkin waste is mock-derived if not present
  const c = selectedCheckoutGroup.value.wasteDataCheckin || { botolPlastik: 2, bungkusMakanan: 4, kaleng: 1, lainnya: 0 }
  return [
    { label: 'Botol Plastik',   naik: c.botolPlastik,   turun: wasteCheckout.botolPlastik,   key: 'botolPlastik' },
    { label: 'Bungkus Makanan', naik: c.bungkusMakanan, turun: wasteCheckout.bungkusMakanan, key: 'bungkusMakanan' },
    { label: 'Kaleng',          naik: c.kaleng,          turun: wasteCheckout.kaleng,          key: 'kaleng' },
    { label: 'Lainnya',         naik: c.lainnya,         turun: wasteCheckout.lainnya,         key: 'lainnya' },
  ]
})
const allWasteOk       = computed(() => wasteItems.value.every(i => i.turun >= i.naik))
const needsChronology  = computed(() => healthCondition.value !== 'sehat')
const canCheckout      = computed(() => !(needsChronology.value && !chronology.value.trim()))
const healthConditionColor = computed(() => {
  switch (healthCondition.value) {
    case 'sehat':      return 'border-green-300 bg-green-50'
    case 'luka_ringan':return 'border-amber-300 bg-amber-50'
    case 'luka_berat': return 'border-red-300 bg-red-50'
    case 'meninggal':  return 'border-red-500 bg-red-100'
    default:           return 'border-gray-300 bg-gray-50'
  }
})

const openCheckoutModalDirect = (session) => {
  selectedCheckoutGroup.value = session
  const c = session.wasteDataCheckin || { botolPlastik: 2, bungkusMakanan: 4, kaleng: 1, lainnya: 0 }
  Object.assign(wasteCheckout, { botolPlastik: c.botolPlastik, bungkusMakanan: c.bungkusMakanan, kaleng: c.kaleng, lainnya: c.lainnya })
  healthCondition.value = 'sehat'
  chronology.value = ''
  showCheckoutModal.value = true
}

const closeCheckoutModal = () => { showCheckoutModal.value = false; selectedCheckoutGroup.value = null }

const processCheckout = async () => {
  if (!canCheckout.value || !selectedCheckoutGroup.value) return
  const group = selectedCheckoutGroup.value
  
  // Safely normalize numeric inputs
  Object.keys(wasteCheckout).forEach(k => {
    wasteCheckout[k] = Math.max(0, Math.floor(Number(wasteCheckout[k]) || 0))
  })
  
  verificationLoading.value = true
  try {
    const { data } = await axios.put(`/admin/checkin/status/${group.order_id}`, { status: 'finished' })
    if (data.status === 'success') {
      closeCheckoutModal()
      showToast(`Check-out berhasil! Rombongan "${group.group_name}" telah kembali dengan selamat.`, 'success')
      router.reload({ only: ['bookings', 'stats'] })
    } else {
      showToast(data.message || "Gagal memproses check-out.", 'error')
    }
  } catch (err) {
    showToast(err.response?.data?.message || "Terjadi kesalahan koneksi.", 'error')
  } finally {
    verificationLoading.value = false
  }
}

// ═══════════════════════════════════════════════════════════
// ARTICLES PANEL — Mountain Content & Route Info Editor
// ═══════════════════════════════════════════════════════════
const contentSearch        = ref('')
const expandedMountainId   = ref(null)  // Which mountain row is expanded

// ─ Mountain Article Editor ─────────────────────────────────────────
const showMtnModal    = ref(false)
const editingMtn      = ref(null)
const mtnForm = useForm({
  description: '',
  content: [],           // [{title?, text}]
})

const filteredMountains = computed(() => {
  if (!contentSearch.value) return props.mountainsWithContent
  const q = contentSearch.value.toLowerCase()
  return props.mountainsWithContent.filter(m => m.name?.toLowerCase().includes(q) || m.location?.toLowerCase().includes(q))
})

const openMtnModal = (mtn) => {
  editingMtn.value       = mtn
  mtnForm.description    = mtn.description || ''
  // Deep copy content array so we can mutate it
  mtnForm.content        = (mtn.content || []).map(s => ({ title: s.title || '', text: s.text || '' }))
  if (mtnForm.content.length === 0) mtnForm.content.push({ title: '', text: '' })
  showMtnModal.value = true
}
const closeMtnModal = () => { showMtnModal.value = false; editingMtn.value = null }

const addSection = () => mtnForm.content.push({ title: '', text: '' })
const removeSection = (idx) => mtnForm.content.splice(idx, 1)

const submitMtnContent = () => {
  if (!editingMtn.value) return
  mtnForm.put(`/admin/content/mountains/${editingMtn.value.id}`, {
    preserveScroll: true,
    onSuccess: () => closeMtnModal(),
  })
}

// ─ Route Info Editor ─────────────────────────────────────────────
const showRouteModal  = ref(false)
const editingRoute    = ref(null)
const routeForm = useForm({
  basecamp_address:        '',
  basecamp_altitude:       '',
  simaksi_price:           '',
  ojek_price:              '',
  ojek_description:        '',
  facilities_description:  '',
  logistics_description:   '',
})

const openRouteModal = (route) => {
  editingRoute.value = route
  const ri = route.route_info || {}
  routeForm.basecamp_address       = ri.basecamp_address       || ''
  routeForm.basecamp_altitude      = ri.basecamp_altitude      ? String(ri.basecamp_altitude) : ''
  routeForm.simaksi_price          = ri.simaksi_price          ? String(ri.simaksi_price) : ''
  routeForm.ojek_price             = ri.ojek_price             ? String(ri.ojek_price) : ''
  routeForm.ojek_description       = ri.ojek_description       || ''
  routeForm.facilities_description = ri.facilities_description || ''
  routeForm.logistics_description  = ri.logistics_description  || ''
  showRouteModal.value = true
}
const closeRouteModal = () => { showRouteModal.value = false; editingRoute.value = null }

const submitRouteInfo = () => {
  if (!editingRoute.value) return
  routeForm.put(`/admin/content/routes/${editingRoute.value.id}/info`, {
    preserveScroll: true,
    onSuccess: () => closeRouteModal(),
  })
}

// ═══════════════════════════════════════════════════════════
// CAMERA QR SCANNER
// ═══════════════════════════════════════════════════════════
const showScanModal   = ref(false)
const scanLoading     = ref(false)
const scanResult      = ref(null)
const scanError       = ref(null)
let html5QrcodeScanner = null

const loadHtml5Qrcode = () => {
  return new Promise((resolve, reject) => {
    if (window.Html5Qrcode) {
      resolve()
      return
    }
    const script = document.createElement('script')
    script.src = "https://unpkg.com/html5-qrcode"
    script.onload = resolve
    script.onerror = () => reject(new Error('Gagal memuat pustaka scanner'))
    document.head.appendChild(script)
  })
}

const openScanModal = async () => {
  showScanModal.value = true
  scanResult.value    = null
  scanError.value     = null
  
  await nextTick()
  try {
    await loadHtml5Qrcode()
    
    html5QrcodeScanner = new window.Html5Qrcode("reader")
    const config = { 
      fps: 25, 
      qrbox: (width, height) => {
        const size = Math.min(width, height) * 0.85
        return { width: size, height: size }
      },
      aspectRatio: 1.0,
      experimentalFeatures: {
        useBarCodeDetectorIfSupported: true
      }
    }
    
    try {
      // Try back camera with automatic fallback
      await html5QrcodeScanner.start(
        { facingMode: "environment" }, 
        config,
        onScanSuccess,
        onScanFailure
      )
    } catch (err) {
      console.warn("Failed starting back camera, retrying with fresh default scanner...", err)
      try {
        if (html5QrcodeScanner && html5QrcodeScanner.isScanning) {
          await html5QrcodeScanner.stop().catch(() => {})
        }
      } catch (e) {}
      
      // Instantiate a fresh scanner to avoid state transition errors
      html5QrcodeScanner = new window.Html5Qrcode("reader")
      await html5QrcodeScanner.start(
        {}, 
        config,
        onScanSuccess,
        onScanFailure
      )
    }
  } catch (err) {
    scanError.value = "Kamera gagal diakses: " + (err.message || err)
  }
}

const closeScanModal = () => {
  if (html5QrcodeScanner && html5QrcodeScanner.isScanning) {
    html5QrcodeScanner.stop().catch(err => console.error(err))
  }
  showScanModal.value = false
  scanResult.value    = null
  scanError.value     = null
}

const onScanSuccess = async (decodedText) => {
  try {
    const context = new (window.AudioContext || window.webkitAudioContext)()
    const osc = context.createOscillator()
    osc.type = "sine"
    osc.frequency.setValueAtTime(800, context.currentTime)
    osc.connect(context.destination)
    osc.start()
    osc.stop(context.currentTime + 0.1)
  } catch (e) {}

  if (html5QrcodeScanner) {
    html5QrcodeScanner.stop().catch(err => console.error(err))
  }

  scanLoading.value = true
  scanError.value   = null
  
  let orderId = decodedText.trim()
  
  // Extract order ID if QR code contains a URL
  if (orderId.includes('/') || orderId.includes('\\')) {
    const segments = orderId.split(/[\/\\]/)
    orderId = segments[segments.length - 1]
  }

  // Strip leading hash character
  if (orderId.startsWith('#')) {
    orderId = orderId.substring(1)
  }

  try {
    const { data } = await axios.get(`/admin/checkin/scan/${orderId}`)
    if (data.status === 'success') {
      scanResult.value = data.data
    } else {
      scanError.value = data.message || "Gagal memproses tiket."
    }
  } catch (err) {
    scanError.value = err.response?.data?.message || "Koneksi terputus."
  } finally {
    scanLoading.value = false
  }
}

const onScanFailure = (error) => {
  // Ignored
}

const openFromScan = (txRef) => {
  if (!txRef) return
  const tx = txRef.value !== undefined ? txRef.value : txRef
  if (!tx) return

  const hs = tx.hiking_session || tx.hikingSession
  const sessionData = {
    id: hs?.id,
    order_id: tx.order_id,
    group_name: hs?.group_name,
    mountain: hs?.route?.mountain?.name,
    route: hs?.route?.name,
    start_date: hs?.start_date,
    end_date: hs?.end_date,
    member_count: hs?.members?.length || 0,
    members: hs?.members || [],
    leader: { name: tx.user?.name },
    status: hs?.status,
  }
  closeScanModal()
  if (sessionData.status === 'prepared' || sessionData.status === 'finished') {
    openCheckinModalDirect(sessionData)
  } else if (sessionData.status === 'on_track') {
    openCheckoutModalDirect(sessionData)
  }
}



</script>

<template>
  <div class="min-h-screen bg-[#F4F1E6] flex flex-col font-sans">
    <Head title="Admin Dashboard" />

    <!-- ══ Navbar ══ -->
    <nav class="w-full flex justify-between items-center px-6 py-3 bg-[#F4F1E6] border-b border-[#D6CCAF] relative z-50">
      <div class="flex items-center gap-2">
        <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 object-contain" />
        <span class="text-xl font-bold text-[#374426] tracking-tight">AltiGuide</span>
      </div>
      <div class="flex items-center gap-6 text-sm font-medium text-[#5A684C]">
        <a href="/" class="hover:text-[#374426] transition-colors">Home</a>
        <span class="px-4 py-1.5 rounded-lg border border-[#374426] text-[#374426] font-semibold bg-white/50">Admin Dashboard</span>
      </div>
    </nav>

    <!-- ══ Main Layout ══ -->
    <div class="flex flex-1 overflow-hidden">

      <!-- ═ Sidebar ═ -->
      <aside class="w-56 bg-[#F4F1E6] border-r border-[#D6CCAF] flex flex-col py-6 px-4 shrink-0">
        <!-- Admin Profile -->
        <div class="flex items-center gap-3 mb-6 px-2">
          <div class="w-10 h-10 rounded-full bg-[#374426] flex items-center justify-center text-white font-bold text-sm">
            {{ adminName.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase() }}
          </div>
          <div>
            <p class="text-[10px] text-[#8B9A7B] font-medium uppercase tracking-wider">Admin</p>
            <p class="text-sm font-semibold text-[#374426] leading-tight truncate max-w-[110px]">{{ adminName }}</p>
          </div>
        </div>

        <!-- Nav -->
        <div class="mb-6">
          <p class="text-[10px] text-[#8B9A7B] font-bold uppercase tracking-wider px-2 mb-3">Main</p>

          <button @click="sidebarActive = 'dashboard'"
            :class="sidebarActive === 'dashboard' ? 'bg-[#374426] text-white shadow-md' : 'text-[#5A684C] hover:bg-[#E8E3D3]'"
            class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 cursor-pointer mb-1">
            <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-4 0a1 1 0 01-1-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 01-1 1" /></svg>
            Dashboard
          </button>

          <button @click="sidebarActive = 'checkin'"
            :class="sidebarActive === 'checkin' ? 'bg-[#374426] text-white shadow-md' : 'text-[#5A684C] hover:bg-[#E8E3D3]'"
            class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 cursor-pointer mb-1">
            <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" /></svg>
            Check-in / Out
            <span v-if="todayHikingSessions.length" class="ml-auto text-[10px] font-bold px-1.5 py-0.5 rounded-full" :class="sidebarActive==='checkin'?'bg-white/20 text-white':'bg-[#374426]/10 text-[#374426]'">{{ todayHikingSessions.length }}</span>
          </button>

          <button @click="sidebarActive = 'articles'"
            :class="sidebarActive === 'articles' ? 'bg-[#374426] text-white shadow-md' : 'text-[#5A684C] hover:bg-[#E8E3D3]'"
            class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 cursor-pointer mb-1">
            <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" /></svg>
            Articles
            <span v-if="props.mountainsWithContent.length" class="ml-auto text-[10px] font-bold px-1.5 py-0.5 rounded-full" :class="sidebarActive==='articles'?'bg-white/20 text-white':'bg-[#374426]/10 text-[#374426]'">{{ props.mountainsWithContent.length }}</span>
          </button>


        </div>

        <div class="mt-auto">
          <p class="text-[10px] text-[#8B9A7B] font-bold uppercase tracking-wider px-2 mb-3">Account</p>
          <button @click="router.post('/admin/logout')"
            class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-red-600 hover:bg-red-50 transition-all duration-200 cursor-pointer text-left bg-transparent border-0 outline-none">
            <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" /></svg>
            Logout
          </button>
        </div>
      </aside>

      <!-- ═ Main Content ═ -->
      <main class="flex-1 overflow-y-auto px-6 py-6">

        <!-- ══════════════════════════════════
             PANEL 1: DASHBOARD (BOOKING)
        ══════════════════════════════════ -->
        <div v-if="sidebarActive === 'dashboard'">

          <!-- Stats Cards -->
          <div class="grid grid-cols-4 gap-4 mb-6">
            <!-- Total Rombongan Aktif -->
            <div class="bg-[#e9e2d3] rounded-2xl p-5 border border-[#ded5c4] shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300">
              <div class="flex items-center justify-between mb-2">
                <div class="w-9 h-9 rounded-xl bg-[#ded5c4] flex items-center justify-center">
                  <img src="/images/admin-icon/Icon.png" alt="Total Rombongan" class="w-5 h-5 object-contain" />
                </div>
                <span class="text-xs font-semibold" :class="stats.totalRombonganChange.startsWith('-') ? 'text-red-600' : 'text-[#4d6639]'">
                  {{ stats.totalRombonganChange }}
                </span>
              </div>
              <p class="text-xs text-[#5A684C] font-semibold mt-3">Total Rombongan Aktif</p>
              <p class="text-4xl font-bold text-[#374426] mt-1">{{ stats.totalRombongan }}</p>
              <p class="text-[9px] font-bold text-[#8B9A7B] tracking-wider uppercase mt-3">HINGGA MINGGU INI</p>
            </div>

            <!-- Pendaki di Gunung -->
            <div class="bg-[#e9e2d3] rounded-2xl p-5 border border-[#ded5c4] shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300">
              <div class="flex items-center justify-between mb-2">
                <div class="w-9 h-9 rounded-xl bg-[#ded5c4] flex items-center justify-center">
                  <img src="/images/admin-icon/Icon (1).png" alt="Pendaki di Gunung" class="w-5 h-5 object-contain" />
                </div>
                <span class="flex items-center gap-1.5 text-xs font-semibold text-[#374426]">
                  <span class="w-2 h-2 rounded-full bg-[#4d6639] animate-pulse"></span> Live
                </span>
              </div>
              <p class="text-xs text-[#5A684C] font-semibold mt-3">Pendaki di Gunung</p>
              <p class="text-4xl font-bold text-[#374426] mt-1">{{ stats.pendakiDiGunung }}</p>
              <p class="text-[9px] font-bold text-[#8B9A7B] tracking-wider uppercase mt-3">PUNCAK & JALUR</p>
            </div>

            <!-- Check-in Hari Ini -->
            <div class="bg-[#e9e2d3] rounded-2xl p-5 border border-[#ded5c4] shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300">
              <div class="flex items-center justify-between mb-2">
                <div class="w-9 h-9 rounded-xl bg-[#ded5c4] flex items-center justify-center">
                  <img src="/images/admin-icon/Icon (2).png" alt="Check-in Hari Ini" class="w-5 h-5 object-contain" />
                </div>
              </div>
              <p class="text-xs text-[#5A684C] font-semibold mt-3">Check-in Hari Ini</p>
              <p class="text-4xl font-bold text-[#374426] mt-1">{{ stats.checkInHariIni }}</p>
              <p class="text-[9px] font-bold text-[#8B9A7B] tracking-wider uppercase mt-3">KELOMPOK TERDAFTAR</p>
            </div>

            <!-- Check-out Hari Ini -->
            <div class="bg-[#e9e2d3] rounded-2xl p-5 border border-[#ded5c4] shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300">
              <div class="flex items-center justify-between mb-2">
                <div class="w-9 h-9 rounded-xl bg-[#ded5c4] flex items-center justify-center">
                  <img src="/images/admin-icon/Icon (3).png" alt="Check-out Hari Ini" class="w-5 h-5 object-contain" />
                </div>
              </div>
              <p class="text-xs text-[#5A684C] font-semibold mt-3">Check-out Hari Ini</p>
              <p class="text-4xl font-bold text-[#374426] mt-1">{{ stats.checkOutHariIni }}</p>
              <p class="text-[9px] font-bold text-[#8B9A7B] tracking-wider uppercase mt-3">BERHASIL KEMBALI</p>
            </div>
          </div>

          <!-- Booking Table -->
          <div class="bg-[#374426] rounded-2xl overflow-hidden shadow-md mb-6">
            <div class="flex items-center justify-between px-6 py-4 gap-4">
              <h2 class="text-white font-bold text-base shrink-0">Laporan Administrasi & Pembayaran</h2>
              <div class="flex items-center gap-3 flex-1 justify-end">
                <div class="relative">
                  <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-white/50" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
                  <input v-model="bookingSearch" placeholder="Cari rombongan..." class="pl-8 pr-3 py-1.5 text-xs bg-white/10 text-white placeholder-white/40 border border-white/20 rounded-lg focus:outline-none focus:bg-white/20 w-44" />
                </div>
              </div>
            </div>

            <div class="bg-[#F4F1E6] rounded-t-2xl overflow-hidden">
              <div v-if="filteredBookings.length === 0" class="py-16 text-center text-[#8B9A7B] text-sm">
                <svg class="w-10 h-10 mx-auto mb-3 opacity-30" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" /></svg>
                Belum ada data booking.
              </div>
              <table v-else class="w-full text-sm">
                <thead>
                  <tr class="bg-[#5A684C] text-white text-xs font-semibold uppercase tracking-wider">
                    <th class="text-left px-4 py-3">Order ID</th>
                    <th class="text-left px-4 py-3">Rombongan</th>
                    <th class="text-left px-4 py-3">Gunung & Jalur</th>
                    <th class="text-left px-4 py-3">Jadwal</th>
                    <th class="text-center px-4 py-3">Anggota</th>
                    <th class="text-center px-4 py-3">Pembayaran</th>
                    <th class="text-center px-4 py-3">Verifikasi</th>
                    <th class="text-center px-4 py-3">Aksi</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="booking in filteredBookings" :key="booking.id"
                    class="border-b border-[#E8E3D3] hover:bg-[#EDEADB] transition-colors">
                    <td class="px-4 py-4 text-[#5A684C] font-mono text-xs">{{ booking.order_id }}</td>
                    <td class="px-4 py-4">
                      <p class="font-medium text-[#374426]">{{ booking.group_name }}</p>
                      <p class="text-[10px] text-[#8B9A7B]">{{ hikeTypeLabel(booking.hike_type) }}</p>
                    </td>
                    <td class="px-4 py-4 text-[#5A684C] text-xs max-w-[160px]">
                      <p class="font-medium">{{ booking.mountain }}</p>
                      <p class="text-[#8B9A7B] truncate">{{ booking.route }}</p>
                    </td>
                    <td class="px-4 py-4 text-[#5A684C] text-xs">
                      <p>{{ formatDate(booking.start_date) }}</p>
                      <p v-if="booking.end_date && booking.end_date !== booking.start_date" class="text-[#8B9A7B]">s/d {{ formatDate(booking.end_date) }}</p>
                    </td>
                    <td class="px-4 py-4 text-center text-[#5A684C]">{{ booking.member_count }} orang</td>
                    <td class="px-4 py-4 text-center">
                      <span :class="paymentBadge(booking.payment_status).cls" class="inline-block text-[10px] font-bold uppercase px-3 py-1 rounded-full tracking-wide">
                        {{ paymentBadge(booking.payment_status).label }}
                      </span>
                    </td>
                    <td class="px-4 py-4 text-center">
                      <span :class="verificationBadge(booking.verification_status).cls" class="inline-block text-[10px] font-bold uppercase px-2 py-1 rounded-full tracking-wide">
                        {{ verificationBadge(booking.verification_status).label }}
                      </span>
                    </td>
                    <td class="px-4 py-4 text-center">
                      <button @click="openDetailModal(booking)" class="text-[#64823E] hover:text-[#374426] text-xs font-semibold hover:underline transition cursor-pointer">
                        Lihat Detail
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Check-in / Check-out Panels Removed From Here -->
        </div>

        <!-- ══════════════════════════════════
             PANEL 1.5: CHECK-IN & CHECK-OUT MANAGEMENT
        ══════════════════════════════════ -->
        <div v-else-if="sidebarActive === 'checkin'">
          <!-- Header -->
          <div class="flex items-center justify-between mb-6">
            <div>
              <h1 class="text-xl font-bold text-[#374426]">Manajemen Check-in & Check-out</h1>
              <p class="text-xs text-[#8B9A7B] mt-0.5">Lakukan pencatatan logistik pendakian dan verifikasi sampah turun saat check-out</p>
            </div>
            <button @click="openScanModal"
              class="flex items-center gap-2 bg-[#374426] text-white hover:bg-[#2c361e] text-sm font-semibold px-4 py-2.5 rounded-xl transition cursor-pointer shadow-sm">
              📸 Scan Tiket QR
            </button>
          </div>

          <!-- Table/Content -->
          <div class="bg-white rounded-2xl border border-[#D6CCAF] shadow-sm overflow-hidden mb-6">
            <div class="bg-[#374426] px-6 py-4 flex justify-between items-center">
              <h2 class="text-white font-bold text-base">Daftar Aktivitas Hari Ini</h2>
              <span class="text-xs text-white/70 font-semibold">{{ todayHikingSessions.length }} rombongan aktif</span>
            </div>

            <div class="bg-[#F4F1E6] overflow-hidden">
              <div v-if="todayHikingSessions.length === 0" class="py-16 text-center text-[#8B9A7B] text-sm">
                <svg class="w-10 h-10 mx-auto mb-3 opacity-30" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" /></svg>
                Belum ada aktivitas check-in atau check-out untuk hari ini.
              </div>
              <table v-else class="w-full text-sm">
                <thead>
                  <tr class="bg-[#5A684C] text-white text-xs font-semibold uppercase tracking-wider">
                    <th class="text-left px-4 py-3">Order ID</th>
                    <th class="text-left px-4 py-3">Rombongan</th>
                    <th class="text-left px-4 py-3">Gunung & Jalur</th>
                    <th class="text-left px-4 py-3">Jadwal</th>
                    <th class="text-center px-4 py-3">Anggota</th>
                    <th class="text-center px-4 py-3">Status</th>
                    <th class="text-center px-4 py-3">Aksi</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="booking in todayHikingSessions" :key="booking.id"
                    class="border-b border-[#E8E3D3] hover:bg-[#EDEADB] transition-colors">
                    <td class="px-4 py-4 text-[#5A684C] font-mono text-xs">{{ booking.order_id }}</td>
                    <td class="px-4 py-4">
                      <p class="font-medium text-[#374426]">{{ booking.group_name }}</p>
                      <p class="text-[10px] text-[#8B9A7B]">{{ hikeTypeLabel(booking.hike_type) }}</p>
                    </td>
                    <td class="px-4 py-4 text-[#5A684C] text-xs max-w-[160px]">
                      <p class="font-medium">{{ booking.mountain }}</p>
                      <p class="text-[#8B9A7B] truncate">{{ booking.route }}</p>
                    </td>
                    <td class="px-4 py-4 text-[#5A684C] text-xs">
                      <p>{{ formatDate(booking.start_date) }}</p>
                      <p v-if="booking.end_date && booking.end_date !== booking.start_date" class="text-[#8B9A7B]">s/d {{ formatDate(booking.end_date) }}</p>
                    </td>
                    <td class="px-4 py-4 text-center text-[#5A684C]">{{ booking.member_count }} orang</td>
                    <td class="px-4 py-4 text-center">
                      <span v-if="booking.status === 'prepared'" class="inline-block text-[10px] font-bold uppercase px-2.5 py-1 rounded-full tracking-wide bg-amber-100 text-amber-800 border border-amber-300">
                        Belum Naik
                      </span>
                      <span v-else-if="booking.status === 'on_track'" class="inline-block text-[10px] font-bold uppercase px-2.5 py-1 rounded-full tracking-wide bg-blue-100 text-blue-800 border border-blue-300">
                        Di Gunung
                      </span>
                      <span v-else-if="booking.status === 'finished'" class="inline-block text-[10px] font-bold uppercase px-2.5 py-1 rounded-full tracking-wide bg-green-100 text-green-800 border border-green-300">
                        Selesai
                      </span>
                      <span v-else class="inline-block text-[10px] font-bold uppercase px-2.5 py-1 rounded-full tracking-wide bg-gray-100 text-gray-800 border border-gray-300">
                        {{ booking.status }}
                      </span>
                    </td>
                    <td class="px-4 py-4 text-center">
                      <button v-if="booking.status === 'prepared'" @click="openCheckinModalDirect(booking)"
                        class="bg-[#374426] text-white hover:bg-[#2c361e] px-3.5 py-1.5 rounded-xl text-xs font-semibold transition cursor-pointer shadow-sm">
                        Check-in
                      </button>
                      <button v-else-if="booking.status === 'on_track'" @click="openCheckoutModalDirect(booking)"
                        class="bg-[#5A684C] text-white hover:bg-[#4a5840] px-3.5 py-1.5 rounded-xl text-xs font-semibold transition cursor-pointer shadow-sm">
                        Check-out
                      </button>
                      <span v-else class="text-[#8B9A7B] font-bold">—</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- ══════════════════════════════════
             PANEL 2: ARTICLES MANAGEMENT
        ══════════════════════════════════ -->
        <div v-else-if="sidebarActive === 'articles'">
          <!-- Header -->
          <div class="flex items-center justify-between mb-6">
            <div>
              <h1 class="text-xl font-bold text-[#374426]">Manajemen Konten Artikel</h1>
              <p class="text-xs text-[#8B9A7B] mt-0.5">Edit deskripsi artikel gunung dan informasi jalur pendakian yang tampil di halaman publik</p>
            </div>
            <a href="/article" target="_blank"
              class="flex items-center gap-2 border border-[#374426] text-[#374426] hover:bg-[#374426] hover:text-white text-sm font-semibold px-4 py-2.5 rounded-xl transition-colors cursor-pointer">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"/></svg>
              Lihat Halaman Publik
            </a>
          </div>

          <!-- Search -->
          <div class="relative max-w-sm mb-5">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#8B9A7B]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
            <input v-model="contentSearch" placeholder="Cari gunung..." class="w-full pl-10 pr-4 py-2 text-sm border border-[#D6CCAF] rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-[#374426]/20 text-[#374426]" />
          </div>

          <!-- Mountains List -->
          <div class="space-y-3">
            <div v-if="filteredMountains.length === 0" class="py-16 text-center text-[#8B9A7B] text-sm">
              Tidak ada gunung ditemukan.
            </div>

            <div v-for="mtn in filteredMountains" :key="mtn.id"
              class="bg-white rounded-2xl border border-[#D6CCAF] shadow-sm overflow-hidden">

              <!-- Mountain Header Row -->
              <div class="flex items-center gap-4 p-4">
                <!-- Image thumbnail -->
                <div class="w-14 h-14 rounded-xl overflow-hidden shrink-0 bg-[#E8E3D3]">
                  <img v-if="mtn.image" :src="mtn.image" :alt="mtn.name" class="w-full h-full object-cover" />
                  <div v-else class="w-full h-full flex items-center justify-center text-2xl">🏔️</div>
                </div>

                <!-- Info -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2">
                    <h3 class="font-bold text-[#374426] text-sm">{{ mtn.name }}</h3>
                    <span v-if="mtn.altitude" class="text-[10px] bg-[#E8E3D3] text-[#5A684C] px-2 py-0.5 rounded-full font-medium">{{ mtn.altitude }} mdpl</span>
                  </div>
                  <p class="text-[11px] text-[#8B9A7B] mt-0.5">{{ mtn.location }}</p>
                  <p class="text-[11px] text-[#A8B89C] mt-0.5 truncate max-w-[380px]">
                    {{ mtn.content?.length ? `${mtn.content.length} seksi konten` : 'Belum ada konten artikel' }} •
                    {{ mtn.routes?.length ?? 0 }} jalur pendakian
                  </p>
                </div>

                <!-- Actions -->
                <div class="flex items-center gap-2 shrink-0">
                  <button @click="openMtnModal(mtn)"
                    class="flex items-center gap-1.5 text-xs font-semibold bg-[#374426] text-white px-3 py-2 rounded-lg hover:bg-[#2c361e] transition cursor-pointer">
                    <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
                    Edit Artikel
                  </button>
                  <button @click="expandedMountainId = expandedMountainId === mtn.id ? null : mtn.id"
                    :class="expandedMountainId === mtn.id ? 'bg-[#E8E3D3] text-[#374426]' : 'text-[#5A684C] hover:bg-[#F4F1E6]'"
                    class="flex items-center gap-1.5 text-xs font-semibold px-3 py-2 rounded-lg border border-[#D6CCAF] transition cursor-pointer">
                    <svg class="w-3.5 h-3.5 transition-transform duration-200" :class="expandedMountainId === mtn.id ? 'rotate-180' : ''" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7"/></svg>
                    Info Jalur
                  </button>
                </div>
              </div>

              <!-- Expanded Routes Section -->
              <Transition name="slide-down">
                <div v-if="expandedMountainId === mtn.id" class="border-t border-[#E8E3D3] bg-[#FAFAF7] px-4 pb-4 pt-3">
                  <p class="text-xs font-semibold text-[#374426] mb-3">Jalur Pendakian — {{ mtn.name }}</p>
                  <div v-if="!mtn.routes?.length" class="text-xs text-[#8B9A7B] py-2">Belum ada jalur pendakian terdaftar.</div>
                  <div v-else class="grid grid-cols-1 gap-2">
                    <div v-for="route in mtn.routes" :key="route.id"
                      class="flex items-center justify-between bg-white rounded-xl px-4 py-3 border border-[#E8E3D3]">
                      <div class="flex-1 min-w-0">
                        <p class="font-medium text-sm text-[#374426]">{{ route.name }}</p>
                        <div v-if="route.route_info" class="text-[11px] text-[#8B9A7B] mt-0.5">
                          📍 {{ route.route_info.basecamp_address || 'Alamat basecamp belum diisi' }} •
                          🎫 {{ route.route_info.simaksi_price ? formatRupiah(route.route_info.simaksi_price) : 'Simaksi belum diisi' }}
                        </div>
                        <p v-else class="text-[11px] text-[#C0B090] mt-0.5">Info jalur belum diisi</p>
                      </div>
                      <button @click="openRouteModal(route)"
                        class="ml-3 flex items-center gap-1.5 text-[11px] font-semibold text-[#64823E] border border-[#64823E]/30 px-2.5 py-1.5 rounded-lg hover:bg-[#64823E]/10 transition cursor-pointer shrink-0">
                        <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
                        Edit Info Jalur
                      </button>
                    </div>
                  </div>
                </div>
              </Transition>
            </div>
          </div>
        </div>



      </main>
    </div>

    <!-- ══ Footer ══ -->
    <footer class="w-full border-t border-[#D6CCAF] bg-white">
      <div class="w-full px-8 py-4 flex justify-center">
        <p class="text-[#5A684C] text-xs">© 2026 AltiGuide Team. All rights reserved.</p>
      </div>
    </footer>

    <!-- ══════════════════════════════════════════════════════
         MODAL 1: DETAIL BOOKING
    ══════════════════════════════════════════════════════ -->
    <Transition name="modal-fade">
      <div v-if="showDetailModal && selectedOrder" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click.self="closeDetailModal">
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="closeDetailModal"></div>
        <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto z-10">
          <!-- Header -->
          <div class="sticky top-0 bg-[#374426] text-white px-6 py-4 rounded-t-3xl flex items-center justify-between z-10">
            <div>
              <h3 class="font-bold text-lg">Detail Pendaftaran</h3>
              <p class="text-xs text-white/60 mt-0.5">{{ selectedOrder.order_id }} • {{ selectedOrder.group_name }}</p>
            </div>
            <button @click="closeDetailModal" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition cursor-pointer">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>

          <div class="p-6 space-y-6">
            <!-- Info Umum -->
            <div class="bg-[#F4F1E6] rounded-2xl p-5 border border-[#D6CCAF]">
              <div class="flex items-center gap-2 mb-3">
                <span class="text-lg">🏔️</span>
                <div>
                  <p class="font-semibold text-sm text-[#374426]">{{ selectedOrder.mountain }}</p>
                  <p class="text-[11px] text-[#8B9A7B]">{{ selectedOrder.route }}</p>
                </div>
              </div>
              <div class="grid grid-cols-3 gap-3 text-xs">
                <div class="bg-white rounded-xl p-3 border border-[#E8E3D3]">
                  <p class="text-[#8B9A7B] mb-1">Tanggal Mulai</p>
                  <p class="font-semibold text-[#374426]">{{ formatDate(selectedOrder.start_date) }}</p>
                </div>
                <div class="bg-white rounded-xl p-3 border border-[#E8E3D3]">
                  <p class="text-[#8B9A7B] mb-1">Tanggal Selesai</p>
                  <p class="font-semibold text-[#374426]">{{ formatDate(selectedOrder.end_date) }}</p>
                </div>
                <div class="bg-white rounded-xl p-3 border border-[#E8E3D3]">
                  <p class="text-[#8B9A7B] mb-1">Tipe Pendakian</p>
                  <p class="font-semibold text-[#374426]">{{ hikeTypeLabel(selectedOrder.hike_type) }}</p>
                </div>
              </div>
            </div>

            <!-- Validasi Pembayaran -->
            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-6 h-6 rounded-full bg-[#374426] text-white flex items-center justify-center text-xs font-bold">1</div>
                <h4 class="font-bold text-sm text-[#374426]">Validasi Pembayaran</h4>
              </div>
              <div class="bg-[#FBFBFB] rounded-xl p-4 border border-[#E8E3D3] space-y-3">
                <div class="flex items-center justify-between">
                  <span class="text-xs text-[#8B9A7B]">Status Transaksi</span>
                  <span :class="paymentBadge(selectedOrder.payment_status).cls" class="text-[10px] font-bold uppercase px-3 py-1 rounded-full">{{ paymentBadge(selectedOrder.payment_status).label }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-xs text-[#8B9A7B]">Order ID</span>
                  <span class="text-xs font-mono font-semibold text-[#374426]">{{ selectedOrder.order_id }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-xs text-[#8B9A7B]">Metode Pembayaran</span>
                  <span class="text-xs font-semibold text-[#374426]">QRIS (Midtrans)</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-xs text-[#8B9A7B]">Total</span>
                  <span class="text-xs font-bold text-[#374426]">{{ formatRupiah(selectedOrder.gross_amount) }}</span>
                </div>
              </div>
            </div>

            <!-- Verifikasi Dokumen -->
            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-6 h-6 rounded-full bg-[#374426] text-white flex items-center justify-center text-xs font-bold">2</div>
                <h4 class="font-bold text-sm text-[#374426]">Verifikasi Dokumen Pendaki</h4>
              </div>
              <div class="bg-[#FBFBFB] rounded-xl border border-[#E8E3D3] overflow-hidden mb-4">
                <table class="w-full text-xs">
                  <thead>
                    <tr class="bg-[#E8E3D3] text-[#5A684C] font-semibold">
                      <th class="text-left px-4 py-2.5">Nama Anggota</th>
                      <th class="text-left px-4 py-2.5">No. KTP</th>
                      <th class="text-left px-4 py-2.5">No. HP</th>
                      <th class="text-left px-4 py-2.5">Kontak Darurat</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(member, i) in selectedOrder.members" :key="i" class="border-b border-[#F4F1E6] last:border-0">
                      <td class="px-4 py-3 font-medium text-[#374426]">{{ member.full_name }}</td>
                      <td class="px-4 py-3 font-mono text-[#5A684C]">{{ member.identity_number }}</td>
                      <td class="px-4 py-3 text-[#5A684C]">{{ member.phone_number }}</td>
                      <td class="px-4 py-3 text-[#5A684C]">{{ member.emergency_contact }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <!-- Update Status Verifikasi -->
              <div class="bg-[#F4F1E6] rounded-xl p-4 border border-[#D6CCAF]">
                <p class="text-xs font-semibold text-[#374426] mb-3">Perbarui Status Verifikasi</p>
                <div class="flex flex-wrap gap-2">
                  <button v-for="opt in [
                    { val: 'terverifikasi', label: '✓ Terverifikasi', cls: 'border-green-300 text-green-700 hover:bg-green-50' },
                    { val: 'review',        label: '⏳ Perlu Review',   cls: 'border-amber-300 text-amber-700 hover:bg-amber-50' },
                    { val: 'revisi',        label: '⚠ Perlu Revisi',   cls: 'border-red-300 text-red-700 hover:bg-red-50' },
                    { val: 'pending_review',label: '○ Reset',           cls: 'border-gray-300 text-gray-600 hover:bg-gray-50' },
                  ]" :key="opt.val"
                    @click="updateVerification(selectedOrder.id, opt.val)"
                    :disabled="verificationLoading"
                    :class="[opt.cls, selectedOrder.verification_status === opt.val ? 'ring-2 ring-offset-1 ring-current opacity-70' : '']"
                    class="text-xs font-semibold px-3 py-1.5 rounded-lg border transition cursor-pointer disabled:opacity-50">
                    {{ opt.label }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ══════════════════════════════════════════════════════
         MODAL: Edit Artikel Gunung
    ══════════════════════════════════════════════════════ -->
    <Transition name="modal-fade">
      <div v-if="showMtnModal && editingMtn" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click.self="closeMtnModal">
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="closeMtnModal"></div>
        <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-2xl max-h-[92vh] overflow-y-auto z-10">
          <!-- Header -->
          <div class="sticky top-0 bg-[#374426] text-white px-6 py-4 rounded-t-3xl flex items-center justify-between z-10">
            <div>
              <h3 class="font-bold text-lg">Edit Artikel — {{ editingMtn.name }}</h3>
              <p class="text-xs text-white/60">Konten tampil di halaman /article?mountain={{ editingMtn.slug }}</p>
            </div>
            <button type="button" @click="closeMtnModal" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center cursor-pointer">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>
          <form @submit.prevent="submitMtnContent" class="p-6 space-y-5">
            <!-- Description -->
            <div>
              <label class="block text-xs font-semibold text-[#374426] mb-1.5">Deskripsi Singkat</label>
              <textarea v-model="mtnForm.description" rows="3" placeholder="Deskripsi singkat gunung..."
                class="w-full px-4 py-3 text-sm border border-[#D6CCAF] rounded-xl focus:outline-none focus:ring-2 focus:ring-[#374426]/20 text-[#374426] resize-y"></textarea>
            </div>
            <!-- Content Sections -->
            <div>
              <div class="flex items-center justify-between mb-3">
                <label class="text-xs font-semibold text-[#374426]">Seksi Konten Artikel</label>
                <button type="button" @click="addSection" class="text-xs text-[#64823E] font-semibold hover:underline cursor-pointer">+ Tambah Seksi</button>
              </div>
              <div class="space-y-4">
                <div v-for="(section, idx) in mtnForm.content" :key="idx" class="bg-[#F4F1E6] rounded-xl p-4 border border-[#D6CCAF]">
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-[11px] font-bold text-[#8B9A7B] uppercase tracking-wide">Seksi {{ idx + 1 }}</span>
                    <button type="button" @click="removeSection(idx)" class="text-red-400 hover:text-red-600 text-xs cursor-pointer" v-if="mtnForm.content.length > 1">Hapus</button>
                  </div>
                  <input v-model="section.title" type="text" placeholder="Judul seksi (opsional, contoh: Sejarah, Flora & Fauna...)"
                    class="w-full px-3 py-2 text-sm border border-[#D6CCAF] rounded-lg mb-2 focus:outline-none focus:ring-2 focus:ring-[#374426]/20 bg-white text-[#374426]" />
                  <textarea v-model="section.text" rows="4" placeholder="Isi paragraf konten..." required
                    class="w-full px-3 py-3 text-sm border border-[#D6CCAF] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#374426]/20 bg-white text-[#374426] resize-y"></textarea>
                </div>
              </div>
            </div>
            <!-- Actions -->
            <div class="flex justify-end gap-3 pt-2">
              <button type="button" @click="closeMtnModal" class="px-5 py-2.5 text-sm font-medium text-[#5A684C] border border-[#D6CCAF] rounded-xl hover:bg-[#F4F1E6] transition cursor-pointer">Batal</button>
              <button type="submit" :disabled="mtnForm.processing" class="flex items-center gap-2 px-6 py-2.5 text-sm font-semibold bg-[#374426] text-white rounded-xl hover:bg-[#2c361e] transition cursor-pointer disabled:opacity-60">
                <svg v-if="mtnForm.processing" class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                Simpan Artikel
              </button>
            </div>
          </form>
        </div>
      </div>
    </Transition>

    <!-- ══════════════════════════════════════════════════════
         MODAL: Edit Info Jalur
    ══════════════════════════════════════════════════════ -->
    <Transition name="modal-fade">
      <div v-if="showRouteModal && editingRoute" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click.self="closeRouteModal">
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="closeRouteModal"></div>
        <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-xl max-h-[92vh] overflow-y-auto z-10">
          <div class="sticky top-0 bg-[#5A684C] text-white px-6 py-4 rounded-t-3xl flex items-center justify-between z-10">
            <div>
              <h3 class="font-bold text-lg">Edit Info Jalur</h3>
              <p class="text-xs text-white/60">{{ editingRoute.name }}</p>
            </div>
            <button type="button" @click="closeRouteModal" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center cursor-pointer">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>
          <form @submit.prevent="submitRouteInfo" class="p-6 space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div class="col-span-2">
                <label class="block text-xs font-semibold text-[#374426] mb-1.5">Alamat Basecamp</label>
                <input v-model="routeForm.basecamp_address" type="text" placeholder="Jl. Raya Basecamp..." class="w-full px-4 py-2.5 text-sm border border-[#D6CCAF] rounded-xl focus:outline-none focus:ring-2 focus:ring-[#374426]/20 text-[#374426]" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-[#374426] mb-1.5">Ketinggian Basecamp (mdpl)</label>
                <input v-model="routeForm.basecamp_altitude" type="number" placeholder="1500" class="w-full px-4 py-2.5 text-sm border border-[#D6CCAF] rounded-xl focus:outline-none focus:ring-2 focus:ring-[#374426]/20 text-[#374426]" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-[#374426] mb-1.5">Harga Simaksi (Rp)</label>
                <input v-model="routeForm.simaksi_price" type="number" placeholder="25000" class="w-full px-4 py-2.5 text-sm border border-[#D6CCAF] rounded-xl focus:outline-none focus:ring-2 focus:ring-[#374426]/20 text-[#374426]" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-[#374426] mb-1.5">Harga Ojek (Rp, opsional)</label>
                <input v-model="routeForm.ojek_price" type="number" placeholder="30000" class="w-full px-4 py-2.5 text-sm border border-[#D6CCAF] rounded-xl focus:outline-none focus:ring-2 focus:ring-[#374426]/20 text-[#374426]" />
              </div>
              <div class="col-span-2">
                <label class="block text-xs font-semibold text-[#374426] mb-1.5">Deskripsi Ojek (opsional)</label>
                <input v-model="routeForm.ojek_description" type="text" placeholder="Ojek tersedia mulai pukul 05.00..." class="w-full px-4 py-2.5 text-sm border border-[#D6CCAF] rounded-xl focus:outline-none focus:ring-2 focus:ring-[#374426]/20 text-[#374426]" />
              </div>
            </div>
            <div>
              <label class="block text-xs font-semibold text-[#374426] mb-1.5">Fasilitas Basecamp</label>
              <textarea v-model="routeForm.facilities_description" rows="3" placeholder="Toilet, mushola, warung, area parkir..." class="w-full px-4 py-3 text-sm border border-[#D6CCAF] rounded-xl focus:outline-none focus:ring-2 focus:ring-[#374426]/20 text-[#374426] resize-y"></textarea>
            </div>
            <div>
              <label class="block text-xs font-semibold text-[#374426] mb-1.5">Karakteristik Jalur</label>
              <textarea v-model="routeForm.logistics_description" rows="3" placeholder="Jalur berbatu, banyak vegetasi lebat di km 2-4..." class="w-full px-4 py-3 text-sm border border-[#D6CCAF] rounded-xl focus:outline-none focus:ring-2 focus:ring-[#374426]/20 text-[#374426] resize-y"></textarea>
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <button type="button" @click="closeRouteModal" class="px-5 py-2.5 text-sm font-medium text-[#5A684C] border border-[#D6CCAF] rounded-xl hover:bg-[#F4F1E6] transition cursor-pointer">Batal</button>
              <button type="submit" :disabled="routeForm.processing" class="flex items-center gap-2 px-6 py-2.5 text-sm font-semibold bg-[#5A684C] text-white rounded-xl hover:bg-[#4a5840] transition cursor-pointer disabled:opacity-60">
                <svg v-if="routeForm.processing" class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                Simpan Info Jalur
              </button>
            </div>
          </form>
        </div>
      </div>
    </Transition>

    <!-- ══════════════════════════════════════════════════════
         MODAL 3: CHECK-IN
    ══════════════════════════════════════════════════════ -->
    <Transition name="modal-fade">
      <div v-if="showCheckinModal && selectedCheckinGroup" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click.self="closeCheckinModal">
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="closeCheckinModal"></div>
        <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto z-10">
          <div class="sticky top-0 bg-[#A2825B] text-white px-6 py-4 rounded-t-3xl flex items-center justify-between z-10">
            <div>
              <h3 class="font-bold text-base">Proses Check-in</h3>
              <p class="text-xs text-white/70">{{ selectedCheckinGroup.group_name || selectedCheckinGroup.name }}</p>
            </div>
            <button @click="closeCheckinModal" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center cursor-pointer"><svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg></button>
          </div>
          <div class="p-6 space-y-5">
            <!-- Pencatatan Logistik Bawaan -->
            <div>
              <p class="text-sm font-bold text-[#374426] mb-3">Pencatatan Logistik Bawaan</p>
              <div class="grid grid-cols-2 gap-3">
                <div v-for="(label, key) in { tenda: 'Tenda', sleepingBag: 'Sleeping Bag', p3k: 'Kotak P3K', peralatanMasak: 'Peralatan Masak', senter: 'Senter / Headlamp', jasHujan: 'Jas Hujan' }" :key="key" class="flex items-center justify-between bg-[#F4F1E6] rounded-xl px-4 py-2.5">
                  <span class="text-xs text-[#5A684C]">{{ label }}</span>
                  <div class="flex items-center gap-2">
                    <button type="button" @click="logisticsDeclaration[key] = Math.max(0, (Number(logisticsDeclaration[key]) || 0) - 1)" class="w-6 h-6 rounded-full bg-white border border-[#D6CCAF] text-[#374426] text-sm font-bold flex items-center justify-center cursor-pointer hover:bg-[#E8E3D3]">-</button>
                    <input type="number" min="0" v-model.number="logisticsDeclaration[key]" class="w-12 text-center font-bold bg-transparent text-[#374426] focus:outline-none border-b border-[#D6CCAF] focus:border-[#374426] [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none" />
                    <button type="button" @click="logisticsDeclaration[key] = (Number(logisticsDeclaration[key]) || 0) + 1" class="w-6 h-6 rounded-full bg-[#374426] text-white text-sm font-bold flex items-center justify-center cursor-pointer hover:bg-[#2c361e]">+</button>
                  </div>
                </div>
              </div>
            </div>
            <!-- Waste Declaration -->
            <div>
              <p class="text-sm font-bold text-[#374426] mb-3">Deklarasi Sampah Bawaan Naik</p>
              <div class="grid grid-cols-2 gap-3">
                <div v-for="(label, key) in { botolPlastik: 'Botol Plastik', bungkusMakanan: 'Bungkus Makanan', kaleng: 'Kaleng', lainnya: 'Lainnya' }" :key="key" class="flex items-center justify-between bg-[#F4F1E6] rounded-xl px-4 py-2.5">
                  <span class="text-xs text-[#5A684C]">{{ label }}</span>
                  <div class="flex items-center gap-2">
                    <button type="button" @click="wasteDeclaration[key] = Math.max(0, (Number(wasteDeclaration[key]) || 0) - 1)" class="w-6 h-6 rounded-full bg-white border border-[#D6CCAF] text-[#374426] text-sm font-bold flex items-center justify-center cursor-pointer hover:bg-[#E8E3D3]">-</button>
                    <input type="number" min="0" v-model.number="wasteDeclaration[key]" class="w-12 text-center font-bold bg-transparent text-[#374426] focus:outline-none border-b border-[#D6CCAF] focus:border-[#374426] [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none" />
                    <button type="button" @click="wasteDeclaration[key] = (Number(wasteDeclaration[key]) || 0) + 1" class="w-6 h-6 rounded-full bg-[#374426] text-white text-sm font-bold flex items-center justify-center cursor-pointer hover:bg-[#2c361e]">+</button>
                  </div>
                </div>
              </div>
              <p class="text-xs text-[#8B9A7B] mt-2">Total: {{ totalWaste }} item sampah</p>
            </div>
            <button @click="processCheckin"
              class="w-full py-3 text-sm font-bold text-white rounded-xl transition bg-[#374426] hover:bg-[#2c361e] cursor-pointer">
              Konfirmasi Check-in
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ══════════════════════════════════════════════════════
         MODAL 4: CHECK-OUT
    ══════════════════════════════════════════════════════ -->
    <Transition name="modal-fade">
      <div v-if="showCheckoutModal && selectedCheckoutGroup" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click.self="closeCheckoutModal">
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="closeCheckoutModal"></div>
        <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto z-10">
          <div class="sticky top-0 bg-[#6B8E4E] text-white px-6 py-4 rounded-t-3xl flex items-center justify-between z-10">
            <div>
              <h3 class="font-bold text-base">Proses Check-out</h3>
              <p class="text-xs text-white/70">{{ selectedCheckoutGroup.group_name || selectedCheckoutGroup.name }}</p>
            </div>
            <button @click="closeCheckoutModal" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center cursor-pointer"><svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg></button>
          </div>
          <div class="p-6 space-y-5">
            <!-- Waste Check -->
            <div>
              <p class="text-sm font-bold text-[#374426] mb-3">Verifikasi Sampah Turun</p>
              <div class="space-y-3">
                <div v-for="item in wasteItems" :key="item.key" class="flex items-center justify-between bg-[#F4F1E6] rounded-xl px-4 py-3">
                  <span class="text-xs text-[#5A684C] flex-1">{{ item.label }}</span>
                  <span class="text-xs text-[#8B9A7B] mr-3">Naik: {{ item.naik }}</span>
                  <div class="flex items-center gap-2">
                    <button type="button" @click="wasteCheckout[item.key] = Math.max(0, (Number(wasteCheckout[item.key]) || 0) - 1)" class="w-6 h-6 rounded-full bg-white border border-[#D6CCAF] text-sm font-bold cursor-pointer hover:bg-[#E8E3D3] flex items-center justify-center">-</button>
                    <input type="number" min="0" v-model.number="wasteCheckout[item.key]" 
                      :class="item.turun < item.naik ? 'text-red-600 border-red-300 focus:border-red-500' : 'text-green-600 border-green-300 focus:border-green-500'" 
                      class="w-12 text-center font-bold bg-transparent focus:outline-none border-b [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none" />
                    <button type="button" @click="wasteCheckout[item.key] = (Number(wasteCheckout[item.key]) || 0) + 1" class="w-6 h-6 rounded-full bg-[#374426] text-white text-sm font-bold cursor-pointer hover:bg-[#2c361e] flex items-center justify-center">+</button>
                  </div>
                </div>
              </div>
              <p v-if="!allWasteOk" class="text-xs text-amber-700 mt-2 font-medium">⚠ Jumlah sampah turun kurang dari saat naik.</p>
            </div>
            <!-- Health Condition -->
            <div>
              <p class="text-sm font-bold text-[#374426] mb-3">Kondisi Kesehatan Anggota</p>
              <select v-model="healthCondition" :class="healthConditionColor" class="w-full px-4 py-2.5 text-sm border-2 rounded-xl focus:outline-none cursor-pointer">
                <option value="sehat">✅ Sehat / Selamat</option>
                <option value="luka_ringan">🟡 Luka Ringan</option>
                <option value="luka_berat">🔴 Luka Berat</option>
                <option value="meninggal">⚫ Meninggal Dunia</option>
              </select>
            </div>
            <!-- Chronology -->
            <div v-if="needsChronology">
              <p class="text-sm font-bold text-[#374426] mb-2">Kronologi Kejadian <span class="text-red-500">*</span></p>
              <textarea v-model="chronology" rows="3" placeholder="Jelaskan kronologi kejadian secara singkat..."
                class="w-full px-4 py-2.5 text-sm border border-[#D6CCAF] rounded-xl focus:outline-none focus:ring-2 focus:ring-red-500/30 resize-none"></textarea>
            </div>
            <button @click="processCheckout" :disabled="!canCheckout"
              :class="canCheckout ? 'bg-[#6B8E4E] hover:bg-[#5a7841] cursor-pointer' : 'bg-gray-300 cursor-not-allowed'"
              class="w-full py-3 text-sm font-bold text-white rounded-xl transition">
              Konfirmasi Check-out
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- MODAL: SCAN QR TIKET -->
    <Transition name="modal-fade">
      <div v-if="showScanModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click.self="closeScanModal">
        <div class="fixed inset-0 bg-black/60 backdrop-blur-sm" @click="closeScanModal"></div>
        <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-lg max-h-[92vh] overflow-y-auto z-10 flex flex-col">
          <!-- Header -->
          <div class="sticky top-0 bg-[#374426] text-white px-6 py-4 rounded-t-3xl flex items-center justify-between z-10">
            <div>
              <h3 class="font-bold text-lg">Scan Tiket QR</h3>
              <p class="text-xs text-white/60">Arahkan kamera ke QR Code E-Ticket Pendaki</p>
            </div>
            <button @click="closeScanModal" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center cursor-pointer">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>

          <div class="p-6 space-y-5 flex-1 overflow-y-auto">
            <!-- Camera area -->
            <div v-show="!scanResult" class="relative bg-black rounded-2xl overflow-hidden aspect-square border border-[#D6CCAF] flex flex-col items-center justify-center">
              <div id="reader" class="w-full h-full"></div>
              <div v-if="scanLoading" class="absolute inset-0 bg-black/50 flex flex-col items-center justify-center text-white gap-3">
                <svg class="animate-spin h-8 w-8 text-white" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                <p class="text-xs font-semibold">Mencari data booking...</p>
              </div>
            </div>

            <!-- Error Notification -->
            <div v-if="scanError" class="p-4 bg-red-50 border border-red-200 rounded-2xl text-red-800 text-xs leading-relaxed">
              <strong>Error:</strong> {{ scanError }}
              <div class="mt-2" v-if="!scanResult">
                <button @click="openScanModal" class="px-3 py-1 bg-red-600 text-white rounded-lg font-semibold hover:bg-red-700 transition cursor-pointer">Ulangi Scan</button>
              </div>
            </div>

            <!-- Scanned Result Details -->
            <div v-if="scanResult" class="space-y-4">
              <div class="bg-[#F4F1E6] rounded-2xl p-5 border border-[#D6CCAF] space-y-3">
                <div class="flex items-center justify-between border-b border-[#D6CCAF]/40 pb-2">
                  <div>
                    <h4 class="font-bold text-[#374426] text-sm">{{ scanResult.hiking_session?.group_name }}</h4>
                    <p class="text-xs text-[#8B9A7B]">Order ID: #{{ scanResult.order_id }}</p>
                  </div>
                  <span class="text-xs bg-green-100 text-green-700 px-2.5 py-0.5 rounded-full font-bold border border-green-200">LUNAS</span>
                </div>

                <div class="grid grid-cols-2 gap-3 text-xs">
                  <div>
                    <p class="text-[#8B9A7B]">Gunung / Jalur</p>
                    <p class="font-semibold text-[#374426]">{{ scanResult.hiking_session?.route?.mountain?.name }} — {{ scanResult.hiking_session?.route?.name }}</p>
                  </div>
                  <div>
                    <p class="text-[#8B9A7B]">Ketua Rombongan</p>
                    <p class="font-semibold text-[#374426]">{{ scanResult.user?.name }}</p>
                  </div>
                  <div>
                    <p class="text-[#8B9A7B]">Tanggal Pendakian</p>
                    <p class="font-semibold text-[#374426]">{{ formatDate(scanResult.hiking_session?.start_date) }} s/d {{ formatDate(scanResult.hiking_session?.end_date) }}</p>
                  </div>
                  <div>
                    <p class="text-[#8B9A7B]">Status Perjalanan</p>
                    <p class="font-semibold text-[#374426] uppercase">{{ scanResult.hiking_session?.status }}</p>
                  </div>
                </div>
              </div>

              <!-- Members Checklist -->
              <div>
                <p class="text-xs font-bold text-[#374426] mb-2">Daftar Anggota Rombongan ({{ scanResult.hiking_session?.members?.length }} orang)</p>
                <div class="border border-[#D6CCAF] rounded-2xl overflow-hidden max-h-48 overflow-y-auto">
                  <table class="w-full text-xs text-left bg-white">
                    <thead class="bg-[#F4F1E6] text-[#374426] font-bold">
                      <tr>
                        <th class="px-3 py-2 border-b border-[#D6CCAF]">Nama</th>
                        <th class="px-3 py-2 border-b border-[#D6CCAF]">NIK</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="member in scanResult.hiking_session?.members" :key="member.id" class="border-b border-gray-100 last:border-0 hover:bg-gray-50">
                        <td class="px-3 py-2.5 font-medium text-[#374426]">{{ member.full_name }}</td>
                        <td class="px-3 py-2.5 font-mono text-[#5A684C]">{{ member.identity_number }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <!-- Action Confirmation Buttons -->
              <div class="flex gap-3 pt-2">
                <button type="button" @click="openScanModal" class="flex-1 py-3 text-sm font-semibold border border-[#D6CCAF] text-[#5A684C] rounded-xl hover:bg-[#F4F1E6] transition cursor-pointer">
                  Scan Ulang
                </button>
                <button v-if="scanResult.hiking_session?.status === 'prepared' || scanResult.hiking_session?.status === 'finished'" 
                  @click="openFromScan(scanResult)" :disabled="scanLoading"
                  class="flex-1 py-3 text-sm font-semibold bg-[#374426] text-white rounded-xl hover:bg-[#2c361e] transition cursor-pointer disabled:opacity-60 flex items-center justify-center gap-2">
                  <svg v-if="scanLoading" class="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                  Proses Check-in &amp; Logistik
                </button>
                <button v-if="scanResult.hiking_session?.status === 'on_track'" 
                  @click="openFromScan(scanResult)" :disabled="scanLoading"
                  class="flex-1 py-3 text-sm font-semibold bg-[#5A684C] text-white rounded-xl hover:bg-[#4a5840] transition cursor-pointer disabled:opacity-60 flex items-center justify-center gap-2">
                  <svg v-if="scanLoading" class="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                  Proses Check-out &amp; Sampah
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Toast Notification -->
    <Transition name="slide-up">
      <div v-if="toast.show" 
        :class="{
          'bg-green-600 border-green-700 text-white': toast.type === 'success',
          'bg-red-600 border-red-700 text-white': toast.type === 'error',
          'bg-amber-600 border-amber-700 text-white': toast.type === 'warning'
        }"
        class="fixed bottom-6 right-6 z-[200] flex items-center gap-3 px-5 py-3.5 rounded-2xl shadow-xl border font-semibold text-sm max-w-sm">
        <span class="text-lg">
          <span v-if="toast.type === 'success'">✅</span>
          <span v-else-if="toast.type === 'error'">❌</span>
          <span v-else>⚠️</span>
        </span>
        <div class="flex-1 leading-snug">{{ toast.message }}</div>
        <button @click="toast.show = false" class="text-white/60 hover:text-white cursor-pointer text-xs font-bold font-mono ml-2 bg-transparent border-0 outline-none">×</button>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.2s ease; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
.slide-down-enter-active, .slide-down-leave-active { transition: all 0.3s ease; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-12px); }

/* Force QR Scanner video and canvas to cover the container and prevent alignment bugs */
#reader :deep(video) {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
}
#reader :deep(canvas) {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
}

/* slide-up transition for toast */
.slide-up-enter-active, .slide-up-leave-active { transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1); }
.slide-up-enter-from { opacity: 0; transform: translateY(20px) scale(0.95); }
.slide-up-leave-to { opacity: 0; transform: translateY(10px) scale(0.95); }
</style>
