<script setup>
import { ref, computed, reactive } from 'vue'
import { Head, Link } from '@inertiajs/vue3'

// ═══════════════════════════════════════════════════════════
// MOCK DATA
// ═══════════════════════════════════════════════════════════

const adminName = ref('Diva Valencia C')

// Weather status: 'cerah' | 'berawan' | 'hujan' | 'extreme'
const weatherStatus = ref('cerah')
const weatherLabels = {
  cerah: { label: 'Cerah', icon: '☀️', color: 'text-green-700 bg-green-50 border-green-200' },
  berawan: { label: 'Berawan', icon: '⛅', color: 'text-gray-700 bg-gray-50 border-gray-200' },
  hujan: { label: 'Hujan', icon: '🌧️', color: 'text-amber-700 bg-amber-50 border-amber-200' },
  extreme: { label: 'Cuaca Ekstrem', icon: '⛈️', color: 'text-red-700 bg-red-50 border-red-300' },
}
const isWeatherExtreme = computed(() => weatherStatus.value === 'extreme')

// Stat cards
const stats = reactive({
  totalRombongan: 42,
  totalRombonganDelta: '+13%',
  pendakiDiGunung: 158,
  pendakiInfo: 'Di Jalur & Puncak',
  checkInHariIni: 14,
  checkInInfo: '5 Rombongan terjadwal',
  checkOutHariIni: 8,
  checkOutInfo: '3 Rombongan terjadwal',
})

// Administration table data
const orders = reactive([
  {
    id: '#AG-88210',
    groupName: 'Pecinta Alam UI',
    mountain: 'Gn. Andong via Temu Kidu',
    schedule: '12 - 14 Mei 2026',
    members: 5,
    memberCount: '5 Orang',
    paymentStatus: 'lunas',
    verificationStatus: 'terverifikasi',
    dailyQuota: { used: 23, total: 70 },
    memberDetails: [
      { name: 'Ahmad Suparjo', ktp: 'verified', suratSehat: 'verified' },
      { name: 'Budi Nugroho', ktp: 'verified', suratSehat: 'review' },
      { name: 'Citra Wulandari', ktp: 'revisi', suratSehat: 'verified' },
      { name: 'Dewi Sartika', ktp: 'verified', suratSehat: 'verified' },
      { name: 'Eko Prasetyo', ktp: 'verified', suratSehat: 'verified' },
    ],
    amount: 150000,
    paymentMethod: 'QRIS',
  },
  {
    id: '#AG-88211',
    groupName: 'Mapala Adventure',
    mountain: 'Gn. Slamet via Pemalang Guci',
    schedule: '15 - 18 Mei 2026',
    members: 4,
    memberCount: '4 Orang',
    paymentStatus: 'pending',
    verificationStatus: 'review',
    dailyQuota: { used: 55, total: 70 },
    memberDetails: [
      { name: 'Farhan Rizky', ktp: 'verified', suratSehat: 'review' },
      { name: 'Gita Ananda', ktp: 'review', suratSehat: 'review' },
      { name: 'Haris Maulana', ktp: 'verified', suratSehat: 'verified' },
      { name: 'Indah Permata', ktp: 'verified', suratSehat: 'review' },
    ],
    amount: 200000,
    paymentMethod: 'QRIS',
  },
  {
    id: '#AG-88212',
    groupName: 'Solo Hiker Bandung',
    mountain: 'Gn. Lawu via Candi Cetho',
    schedule: '20 Mei 2026',
    members: 1,
    memberCount: '1 Orang',
    paymentStatus: 'lunas',
    verificationStatus: 'terverifikasi',
    dailyQuota: { used: 10, total: 70 },
    memberDetails: [
      { name: 'Joko Widodo', ktp: 'verified', suratSehat: 'verified' },
    ],
    amount: 50000,
    paymentMethod: 'QRIS',
  },
])

// Check-in groups
const checkInGroups = reactive([
  {
    id: 1,
    name: 'Grup Tapak Jagad',
    leader: 'Ahmad Suparjo',
    time: '08:15',
    date: 'ATB',
    note: 'Perlengkapan lengkap, aman untuk naik. Semua anggota sehat.',
    checkedIn: false,
    isOverdue: false,
    endDate: '2026-05-14',
    wasteData: null,
  },
  {
    id: 2,
    name: 'Sindoro Squad',
    leader: 'Budi Santoso',
    time: '07:45',
    date: 'ATB',
    note: 'Briefing keselamatan selesai. Cuaca cerah di Basecamp.',
    checkedIn: false,
    isOverdue: false,
    endDate: '2026-05-16',
    wasteData: null,
  },
])

// Check-out groups
const checkOutGroups = reactive([
  {
    id: 3,
    name: 'Adventure Seekers',
    leader: 'Rendi Mahendra',
    time: '09:10',
    date: 'ATB',
    healthStatus: 'selamat',
    healthBadge: 'SELAMAT',
    wasteStatus: 'ok',
    note: 'Satu anggota luka ringan (lecet), sudah ditangani tim medis basecamp.',
    isOverdue: false,
    wasteDataCheckin: { botolPlastik: 5, bungkusMakanan: 12, kaleng: 3, lainnya: 2 },
  },
  {
    id: 4,
    name: 'Keluarga Cemara',
    leader: 'Siti Aminah',
    time: '08:50',
    date: 'ATB',
    healthStatus: 'selamat',
    healthBadge: 'SELAMAT',
    wasteStatus: 'ok',
    note: 'Anggota lengkap. Turun via Jalur Puteri.',
    isOverdue: true,
    wasteDataCheckin: { botolPlastik: 3, bungkusMakanan: 8, kaleng: 1, lainnya: 0 },
  },
])

// ═══════════════════════════════════════════════════════════
// MODAL STATES
// ═══════════════════════════════════════════════════════════

// Detail Modal
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

const quotaPercentage = computed(() => {
  if (!selectedOrder.value) return 0
  const q = selectedOrder.value.dailyQuota
  return Math.round(((q.total - q.used) / q.total) * 100)
})
const quotaColor = computed(() => {
  const pct = quotaPercentage.value
  if (pct > 50) return 'bg-green-500'
  if (pct > 20) return 'bg-amber-500'
  return 'bg-red-500'
})
const quotaTextColor = computed(() => {
  const pct = quotaPercentage.value
  if (pct > 50) return 'text-green-700'
  if (pct > 20) return 'text-amber-700'
  return 'text-red-700'
})

// Check-in Modal
const showCheckinModal = ref(false)
const selectedCheckinGroup = ref(null)
const sopChecklist = ref([
  { id: 1, label: 'Tenda (minimal 1 per 3 orang)', checked: false },
  { id: 2, label: 'Sleeping Bag (setiap anggota)', checked: false },
  { id: 3, label: 'Kotak P3K', checked: false },
  { id: 4, label: 'Logistik (air min. 2L/orang)', checked: false },
  { id: 5, label: 'Peralatan Masak', checked: false },
  { id: 6, label: 'Headlamp / Senter', checked: false },
  { id: 7, label: 'Jas Hujan / Poncho', checked: false },
])
const wasteDeclaration = reactive({
  botolPlastik: 0,
  bungkusMakanan: 0,
  kaleng: 0,
  lainnya: 0,
})
const totalWaste = computed(() => {
  return wasteDeclaration.botolPlastik + wasteDeclaration.bungkusMakanan + wasteDeclaration.kaleng + wasteDeclaration.lainnya
})
const allSopChecked = computed(() => sopChecklist.value.every(item => item.checked))
const canCheckin = computed(() => allSopChecked.value && !isWeatherExtreme.value)

const openCheckinModal = (group) => {
  selectedCheckinGroup.value = group
  sopChecklist.value.forEach(item => item.checked = false)
  wasteDeclaration.botolPlastik = 0
  wasteDeclaration.bungkusMakanan = 0
  wasteDeclaration.kaleng = 0
  wasteDeclaration.lainnya = 0
  showCheckinModal.value = true
}
const closeCheckinModal = () => {
  showCheckinModal.value = false
  selectedCheckinGroup.value = null
}
const processCheckin = () => {
  if (!canCheckin.value || !selectedCheckinGroup.value) return
  const group = selectedCheckinGroup.value
  group.checkedIn = true
  group.wasteData = { ...wasteDeclaration }
  const now = new Date()
  group.time = now.toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' })
  group.note = `Check-in berhasil. ${totalWaste.value} item sampah dideklarasikan.`
  closeCheckinModal()
}

// Check-out Modal
const showCheckoutModal = ref(false)
const selectedCheckoutGroup = ref(null)
const wasteCheckout = reactive({
  botolPlastik: 0,
  bungkusMakanan: 0,
  kaleng: 0,
  lainnya: 0,
})
const healthCondition = ref('sehat')
const chronology = ref('')

const wasteItems = computed(() => {
  if (!selectedCheckoutGroup.value) return []
  const checkin = selectedCheckoutGroup.value.wasteDataCheckin
  return [
    { label: 'Botol Plastik', naik: checkin.botolPlastik, turun: wasteCheckout.botolPlastik, key: 'botolPlastik' },
    { label: 'Bungkus Makanan', naik: checkin.bungkusMakanan, turun: wasteCheckout.bungkusMakanan, key: 'bungkusMakanan' },
    { label: 'Kaleng', naik: checkin.kaleng, turun: wasteCheckout.kaleng, key: 'kaleng' },
    { label: 'Lainnya', naik: checkin.lainnya, turun: wasteCheckout.lainnya, key: 'lainnya' },
  ]
})
const allWasteOk = computed(() => wasteItems.value.every(item => item.turun >= item.naik))
const needsChronology = computed(() => healthCondition.value !== 'sehat')
const canCheckout = computed(() => {
  if (needsChronology.value && chronology.value.trim() === '') return false
  return true
})

const healthConditionColor = computed(() => {
  switch (healthCondition.value) {
    case 'sehat': return 'border-green-300 bg-green-50'
    case 'luka_ringan': return 'border-amber-300 bg-amber-50'
    case 'luka_berat': return 'border-red-300 bg-red-50'
    case 'meninggal': return 'border-red-500 bg-red-100'
    default: return 'border-gray-300 bg-gray-50'
  }
})

const openCheckoutModal = (group) => {
  selectedCheckoutGroup.value = group
  const c = group.wasteDataCheckin
  wasteCheckout.botolPlastik = c.botolPlastik
  wasteCheckout.bungkusMakanan = c.bungkusMakanan
  wasteCheckout.kaleng = c.kaleng
  wasteCheckout.lainnya = c.lainnya
  healthCondition.value = 'sehat'
  chronology.value = ''
  showCheckoutModal.value = true
}
const closeCheckoutModal = () => {
  showCheckoutModal.value = false
  selectedCheckoutGroup.value = null
}
const processCheckout = () => {
  if (!canCheckout.value || !selectedCheckoutGroup.value) return
  const group = selectedCheckoutGroup.value
  group.wasteStatus = allWasteOk.value ? 'ok' : 'selisih'
  group.healthStatus = healthCondition.value
  const labels = { sehat: 'SELAMAT', luka_ringan: 'LUKA RINGAN', luka_berat: 'LUKA BERAT', meninggal: 'MENINGGAL' }
  group.healthBadge = labels[healthCondition.value] || 'SELAMAT'
  const now = new Date()
  group.time = now.toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' })
  closeCheckoutModal()
}

// Sidebar
const sidebarActive = ref('dashboard')

// Format currency
const formatRupiah = (num) => {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(num)
}

// Payment & verification badge helpers
const paymentBadge = (status) => {
  switch (status) {
    case 'lunas': return { label: 'LUNAS', cls: 'bg-green-100 text-green-800 border border-green-300' }
    case 'pending': return { label: 'PENDING', cls: 'bg-amber-100 text-amber-800 border border-amber-300' }
    case 'batal': return { label: 'BATAL', cls: 'bg-red-100 text-red-800 border border-red-300' }
    default: return { label: status, cls: 'bg-gray-100 text-gray-800' }
  }
}
const verificationBadge = (status) => {
  switch (status) {
    case 'terverifikasi': return { label: 'TERVERIFIKASI', cls: 'bg-green-100 text-green-800 border border-green-300' }
    case 'review': return { label: 'REVIEW', cls: 'bg-amber-100 text-amber-800 border border-amber-300' }
    case 'revisi': return { label: 'REVISI', cls: 'bg-red-100 text-red-800 border border-red-300' }
    default: return { label: status, cls: 'bg-gray-100 text-gray-800' }
  }
}
const docStatusIcon = (status) => {
  switch (status) {
    case 'verified': return { icon: '✓', label: 'Terverifikasi', cls: 'text-green-700 bg-green-50' }
    case 'review': return { icon: '⏳', label: 'Review', cls: 'text-amber-700 bg-amber-50' }
    case 'revisi': return { icon: '⚠', label: 'Revisi', cls: 'text-red-700 bg-red-50' }
    default: return { icon: '?', label: status, cls: 'text-gray-700 bg-gray-50' }
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#F4F1E6] flex flex-col font-sans">
    <Head title="Admin Dashboard — AltiGuide" />
    <!-- ══════════════ Navbar ══════════════ -->
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

    <!-- ══════════════ Main Layout ══════════════ -->
    <div class="flex flex-1 overflow-hidden">

      <!-- ═══ Sidebar ═══ -->
      <aside class="w-56 bg-[#F4F1E6] border-r border-[#D6CCAF] flex flex-col py-6 px-4 shrink-0">
        <!-- Admin Profile -->
        <div class="flex items-center gap-3 mb-8 px-2">
          <div class="w-10 h-10 rounded-full bg-[#374426] flex items-center justify-center text-white font-bold text-sm">
            {{ adminName.split(' ').map(n => n[0]).join('').slice(0, 2) }}
          </div>
          <div>
            <p class="text-[10px] text-[#8B9A7B] font-medium uppercase tracking-wider">Admin</p>
            <p class="text-sm font-semibold text-[#374426] leading-tight">{{ adminName }}</p>
          </div>
        </div>

        <!-- Nav Sections -->
        <div class="mb-6">
          <p class="text-[10px] text-[#8B9A7B] font-bold uppercase tracking-wider px-2 mb-3">Main</p>
          <button
            @click="sidebarActive = 'dashboard'"
            :class="sidebarActive === 'dashboard' ? 'bg-[#374426] text-white shadow-md' : 'text-[#5A684C] hover:bg-[#E8E3D3]'"
            class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 cursor-pointer mb-1"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-4 0a1 1 0 01-1-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 01-1 1" /></svg>
            Dashboard
          </button>
          <button
            @click="sidebarActive = 'articles'"
            :class="sidebarActive === 'articles' ? 'bg-[#374426] text-white shadow-md' : 'text-[#5A684C] hover:bg-[#E8E3D3]'"
            class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 cursor-pointer mb-1"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" /></svg>
            Articles
          </button>
          <button
            @click="sidebarActive = 'weather'"
            :class="sidebarActive === 'weather' ? 'bg-[#374426] text-white shadow-md' : 'text-[#5A684C] hover:bg-[#E8E3D3]'"
            class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 cursor-pointer mb-1"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 15a4 4 0 004 4h9a5 5 0 10-.1-9.999 5.002 5.002 0 10-9.78 2.096A4.001 4.001 0 003 15z" /></svg>
            Weather Alert
          </button>
        </div>

        <div class="mb-6">
          <p class="text-[10px] text-[#8B9A7B] font-bold uppercase tracking-wider px-2 mb-3">Settings</p>
          <button class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-[#5A684C] hover:bg-[#E8E3D3] transition-all duration-200 cursor-pointer mb-1">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" /><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
            Settings
          </button>
          <button class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-red-600 hover:bg-red-50 transition-all duration-200 cursor-pointer">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" /></svg>
            Logout Account
          </button>
        </div>
      </aside>

      <!-- ═══ Main Content ═══ -->
      <main class="flex-1 overflow-y-auto px-6 py-6">

        <!-- Weather Alert Banner (when extreme) -->
        <Transition name="slide-down">
          <div v-if="isWeatherExtreme" class="mb-4 flex items-center gap-3 px-5 py-3.5 bg-red-600 text-white rounded-2xl shadow-lg weather-extreme-banner">
            <span class="text-xl">⛈️</span>
            <div class="flex-1">
              <p class="font-bold text-sm">PERINGATAN CUACA EKSTREM</p>
              <p class="text-xs opacity-90">Seluruh pemberangkatan pendaki ditangguhkan hingga cuaca membaik. Tombol Scan Check-in dinonaktifkan.</p>
            </div>
            <button @click="weatherStatus = 'cerah'" class="text-xs bg-white/20 hover:bg-white/30 px-3 py-1.5 rounded-lg transition cursor-pointer">Ubah Status</button>
          </div>
        </Transition>

        <!-- ═══ Weather Toggle (Dev Control) ═══ -->
        <div class="mb-4 flex items-center gap-2">
          <span class="text-xs text-[#8B9A7B] font-medium">Status Cuaca:</span>
          <select v-model="weatherStatus" class="text-xs border border-[#D6CCAF] rounded-lg px-2 py-1 bg-white text-[#374426] focus:outline-none focus:ring-1 focus:ring-[#64823E] cursor-pointer">
            <option value="cerah">☀️ Cerah</option>
            <option value="berawan">⛅ Berawan</option>
            <option value="hujan">🌧️ Hujan</option>
            <option value="extreme">⛈️ Cuaca Ekstrem</option>
          </select>
          <span :class="weatherLabels[weatherStatus].color" class="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-full border">
            {{ weatherLabels[weatherStatus].icon }} {{ weatherLabels[weatherStatus].label }}
          </span>
        </div>

        <!-- ═══ Stats Cards Row ═══ -->
        <div class="grid grid-cols-4 gap-4 mb-6">
          <!-- Total Rombongan Aktif -->
          <div class="bg-white rounded-2xl p-5 border border-[#D6CCAF] shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300 group">
            <div class="flex items-center justify-between mb-2">
              <div class="w-9 h-9 rounded-xl bg-[#E8E3D3] flex items-center justify-center">
                <svg class="w-5 h-5 text-[#374426]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
              </div>
              <span class="text-xs font-bold text-green-600 bg-green-50 px-2 py-0.5 rounded-full">{{ stats.totalRombonganDelta }}</span>
            </div>
            <p class="text-xs text-[#8B9A7B] font-medium">Total Rombongan Aktif</p>
            <p class="text-3xl font-bold text-[#374426] mt-1">{{ stats.totalRombongan }}</p>
            <p class="text-[10px] text-[#A8B89C] mt-1">Pendakian berlangsung</p>
          </div>

          <!-- Pendaki di Gunung -->
          <div class="bg-[#374426] rounded-2xl p-5 border border-[#2c361e] shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300 text-white">
            <div class="flex items-center justify-between mb-2">
              <div class="w-9 h-9 rounded-xl bg-white/15 flex items-center justify-center">
                <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" /></svg>
              </div>
              <span class="flex items-center gap-1 text-xs font-medium text-green-300">
                <span class="w-2 h-2 rounded-full bg-green-400 animate-pulse"></span>
                Live
              </span>
            </div>
            <p class="text-xs text-white/60 font-medium">Pendaki di Gunung</p>
            <p class="text-3xl font-bold mt-1">{{ stats.pendakiDiGunung }}</p>
            <p class="text-[10px] text-white/40 mt-1">{{ stats.pendakiInfo }}</p>
          </div>

          <!-- Check-in Hari Ini -->
          <div class="bg-[#A2825B] rounded-2xl p-5 border border-[#8B6E4B] shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300 text-white">
            <div class="flex items-center justify-between mb-2">
              <div class="w-9 h-9 rounded-xl bg-white/15 flex items-center justify-center">
                <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              </div>
            </div>
            <p class="text-xs text-white/60 font-medium">Check-in Hari Ini</p>
            <p class="text-3xl font-bold mt-1">{{ stats.checkInHariIni }}</p>
            <p class="text-[10px] text-white/40 mt-1">{{ stats.checkInInfo }}</p>
          </div>

          <!-- Check-out Hari Ini -->
          <div class="bg-white rounded-2xl p-5 border border-[#D6CCAF] shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300">
            <div class="flex items-center justify-between mb-2">
              <div class="w-9 h-9 rounded-xl bg-[#E8E3D3] flex items-center justify-center">
                <svg class="w-5 h-5 text-[#374426]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" /></svg>
              </div>
            </div>
            <p class="text-xs text-[#8B9A7B] font-medium">Check-out Hari Ini</p>
            <p class="text-3xl font-bold text-[#374426] mt-1">{{ stats.checkOutHariIni }}</p>
            <p class="text-[10px] text-[#A8B89C] mt-1">{{ stats.checkOutInfo }}</p>
          </div>
        </div>

        <!-- ═══ Administration Table ═══ -->
        <div class="bg-[#374426] rounded-2xl overflow-hidden shadow-md mb-6">
          <div class="flex items-center justify-between px-6 py-4">
            <h2 class="text-white font-bold text-base">Laporan Administrasi & Pembayaran</h2>
            <button class="flex items-center gap-2 bg-[#A2825B] hover:bg-[#8B6E4B] text-white text-xs font-semibold px-4 py-2 rounded-xl transition-colors cursor-pointer">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
              Ekspor PDF
            </button>
          </div>

          <div class="bg-[#F4F1E6] rounded-t-2xl overflow-hidden">
            <table class="w-full text-sm">
              <thead>
                <tr class="bg-[#5A684C] text-white text-xs font-semibold uppercase tracking-wider">
                  <th class="text-left px-4 py-3">Order ID</th>
                  <th class="text-left px-4 py-3">Rombongan</th>
                  <th class="text-left px-4 py-3">Gunung & Jalur</th>
                  <th class="text-left px-4 py-3">Jadwal</th>
                  <th class="text-center px-4 py-3">Anggota</th>
                  <th class="text-center px-4 py-3">Status</th>
                  <th class="text-center px-4 py-3">Verifikasi</th>
                  <th class="text-center px-4 py-3">Aksi</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(order, idx) in orders"
                  :key="order.id"
                  class="border-b border-[#E8E3D3] hover:bg-[#EDEADB] transition-colors"
                >
                  <td class="px-4 py-4 text-[#5A684C] font-mono text-xs">{{ order.id }}</td>
                  <td class="px-4 py-4 font-medium text-[#374426]">{{ order.groupName }}</td>
                  <td class="px-4 py-4 text-[#5A684C] text-xs">{{ order.mountain }}</td>
                  <td class="px-4 py-4 text-[#5A684C] text-xs">{{ order.schedule }}</td>
                  <td class="px-4 py-4 text-center text-[#5A684C]">{{ order.memberCount }}</td>
                  <td class="px-4 py-4 text-center">
                    <span :class="paymentBadge(order.paymentStatus).cls" class="inline-block text-[10px] font-bold uppercase px-3 py-1 rounded-full tracking-wide">
                      {{ paymentBadge(order.paymentStatus).label }}
                    </span>
                  </td>
                  <td class="px-4 py-4 text-center">
                    <span :class="verificationBadge(order.verificationStatus).cls" class="inline-block text-[10px] font-bold uppercase px-3 py-1 rounded-full tracking-wide">
                      {{ verificationBadge(order.verificationStatus).label }}
                    </span>
                  </td>
                  <td class="px-4 py-4 text-center">
                    <button @click="openDetailModal(order)" class="text-[#64823E] hover:text-[#374426] text-xs font-semibold hover:underline transition cursor-pointer">
                      Lihat Detail
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- ═══ Log Panels (Check-in / Check-out) ═══ -->
        <div class="grid grid-cols-2 gap-4 mb-6">
          <!-- Log Naik (Check-in) -->
          <div class="bg-[#E8E3D3] rounded-2xl overflow-hidden shadow-sm">
            <div class="flex items-center justify-between px-5 py-3 bg-[#A2825B]">
              <div class="flex items-center gap-2 text-white">
                <span class="text-base">🔺</span>
                <h3 class="font-bold text-sm">Log Naik (Check-in)</h3>
              </div>
              <button
                @click="openCheckinModal(checkInGroups[0])"
                :disabled="isWeatherExtreme"
                :class="isWeatherExtreme ? 'opacity-40 cursor-not-allowed' : 'hover:bg-white/30 cursor-pointer'"
                class="flex items-center gap-1.5 bg-white/20 text-white text-[10px] font-semibold px-3 py-1.5 rounded-lg transition"
              >
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z" /></svg>
                Scan Check-in
              </button>
            </div>
            <div class="p-4 space-y-3 max-h-80 overflow-y-auto">
              <div
                v-for="group in checkInGroups"
                :key="group.id"
                :class="group.isOverdue ? 'border-red-400 bg-red-50/60 ring-1 ring-red-200' : 'border-[#D6CCAF] bg-white'"
                class="rounded-xl p-4 border shadow-sm hover:shadow-md transition-all duration-200 relative"
              >
                <!-- Overdue Badge -->
                <div v-if="group.isOverdue" class="absolute -top-2 -right-2 flex items-center gap-1 bg-red-600 text-white text-[9px] font-bold px-2 py-0.5 rounded-full overdue-pulse shadow-md">
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                  OVERDUE
                </div>

                <div class="flex items-start justify-between mb-2">
                  <div>
                    <h4 class="font-bold text-sm text-[#374426]">{{ group.name }}</h4>
                    <p class="text-[11px] text-[#8B9A7B]">👤 Ketua: {{ group.leader }}</p>
                  </div>
                  <span class="text-[11px] font-mono text-[#A2825B] font-bold">{{ group.time }} <span class="text-[#8B9A7B] font-normal">{{ group.date }}</span></span>
                </div>
                <div class="bg-[#F4F1E6] rounded-lg p-3 mt-2">
                  <p class="text-xs text-[#5A684C] italic leading-relaxed">"{{ group.note }}"</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Log Turun (Check-out) -->
          <div class="bg-[#E8E3D3] rounded-2xl overflow-hidden shadow-sm">
            <div class="flex items-center justify-between px-5 py-3 bg-[#6B8E4E]">
              <div class="flex items-center gap-2 text-white">
                <span class="text-base">🔻</span>
                <h3 class="font-bold text-sm">Log Turun (Check-out)</h3>
              </div>
              <button
                @click="openCheckoutModal(checkOutGroups[0])"
                class="flex items-center gap-1.5 bg-white/20 hover:bg-white/30 text-white text-[10px] font-semibold px-3 py-1.5 rounded-lg transition cursor-pointer"
              >
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z" /></svg>
                Scan Check-out
              </button>
            </div>
            <div class="p-4 space-y-3 max-h-80 overflow-y-auto">
              <div
                v-for="group in checkOutGroups"
                :key="group.id"
                :class="group.isOverdue ? 'border-red-400 bg-red-50/60 ring-1 ring-red-200' : 'border-[#D6CCAF] bg-white'"
                class="rounded-xl p-4 border shadow-sm hover:shadow-md transition-all duration-200 relative"
              >
                <!-- Overdue Badge -->
                <div v-if="group.isOverdue" class="absolute -top-2 -right-2 flex items-center gap-1 bg-red-600 text-white text-[9px] font-bold px-2 py-0.5 rounded-full overdue-pulse shadow-md">
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                  OVERDUE
                </div>

                <div class="flex items-start justify-between mb-2">
                  <div>
                    <h4 class="font-bold text-sm text-[#374426]">{{ group.name }}</h4>
                    <p class="text-[11px] text-[#8B9A7B]">👤 Ketua: {{ group.leader }}</p>
                  </div>
                  <span class="text-[11px] font-mono text-[#6B8E4E] font-bold">{{ group.time }} <span class="text-[#8B9A7B] font-normal">{{ group.date }}</span></span>
                </div>
                <!-- Status badges -->
                <div class="flex items-center gap-2 mt-2 mb-2">
                  <span :class="group.healthStatus === 'sehat' || group.healthStatus === 'selamat' ? 'bg-green-100 text-green-800 border-green-300' : group.healthStatus === 'luka_ringan' ? 'bg-amber-100 text-amber-800 border-amber-300' : 'bg-red-100 text-red-800 border-red-300'" class="text-[9px] font-bold uppercase px-2 py-0.5 rounded-full border">
                    {{ group.healthBadge }}
                  </span>
                  <span :class="group.wasteStatus === 'ok' ? 'bg-green-50 text-green-700 border-green-200' : 'bg-amber-50 text-amber-700 border-amber-200'" class="flex items-center gap-1 text-[9px] font-semibold px-2 py-0.5 rounded-full border">
                    {{ group.wasteStatus === 'ok' ? '✓' : '⚠' }} Sampah {{ group.wasteStatus === 'ok' ? 'OK' : 'Selisih' }}
                  </span>
                </div>
                <p class="text-xs text-[#5A684C] leading-relaxed">{{ group.note }}</p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- ══════════════ Footer ══════════════ -->
    <footer class="w-full border-t border-[#D6CCAF]">
      <!-- CTA Banner -->
      <div class="w-full bg-[#E0DBBE] py-8 px-8 md:px-16 flex flex-col md:flex-row items-center justify-between gap-6">
        <div class="flex items-center gap-3">
          <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-10 h-10 object-contain" />
          <span class="text-2xl font-bold text-[#374426] tracking-tight">AltiGuide</span>
        </div>
        <div class="flex items-center gap-4">
          <button class="bg-[#374426] text-[#F8F3E4] font-medium rounded-xl px-6 py-3 hover:opacity-90 transition-opacity cursor-pointer">Contact Us</button>
          <button class="bg-[#F8F3E4] text-[#374426] font-medium rounded-xl px-6 py-3 hover:opacity-90 transition-opacity shadow-sm cursor-pointer">Start Summit</button>
        </div>
      </div>

      <!-- Footer Links -->
      <div class="w-full bg-white px-8 md:px-16 py-8">
        <div class="flex flex-col lg:flex-row justify-between items-start gap-12 mb-6">
          <div class="flex flex-col gap-16">
            <span class="text-lg font-medium text-[#374426] underline underline-offset-8">AltiGuide.com</span>
            <div class="flex items-center gap-5 text-[#828282]">
              <a href="#" class="hover:text-[#374426] transition-colors">
                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12c0 4.991 3.657 9.128 8.438 9.878v-6.987h-2.54V12h2.54V9.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V12h2.773l-.443 2.89h-2.33v6.988C18.343 21.128 22 16.991 22 12z"/></svg>
              </a>
              <a href="#" class="hover:text-[#374426] transition-colors">
                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M21.582 6.186a2.66 2.66 0 0 0-1.875-1.884C18.053 3.86 12 3.86 12 3.86s-6.053 0-7.707.442a2.66 2.66 0 0 0-1.875 1.884C2 7.854 2 12 2 12s0 4.146.418 5.814a2.66 2.66 0 0 0 1.875 1.884C5.947 20.14 12 20.14 12 20.14s6.053 0 7.707-.442a2.66 2.66 0 0 0 1.875-1.884C22 16.146 22 12 22 12s0-4.146-.418-5.814zM9.88 15.15V8.85l6.32 3.15-6.32 3.15z"/></svg>
              </a>
              <a href="https://www.instagram.com/altiguide___?igsh=MTRwbW8zbW8wZDVubg==" target="_blank" rel="noopener noreferrer" class="hover:text-[#374426] transition-colors">
                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zM12 0C8.741 0 8.333.014 7.053.072 2.695.272.273 2.69.073 7.052.014 8.333 0 8.741 0 12c0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98C8.333 23.986 8.741 24 12 24c3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98C15.668.014 15.259 0 12 0zm0 5.838a6.162 6.162 0 1 0 0 12.324 6.162 6.162 0 0 0 0-12.324zM12 16a4 4 0 1 1 0-8 4 4 0 0 1 0 8zm6.406-11.845a1.44 1.44 0 1 0 0 2.881 1.44 1.44 0 0 0 0-2.881z"/></svg>
              </a>
            </div>
          </div>
          <div class="flex gap-16">
            <div class="flex flex-col gap-4">
              <h5 class="text-[#374426] font-semibold text-sm">Jelajahi</h5>
              <div class="flex flex-col gap-3 text-[#5A684C] text-xs">
                <Link href="/mountains" class="hover:text-[#374426] transition-colors">Daftar Gunung</Link>
                <Link href="/" class="hover:text-[#374426] transition-colors">Weather Analytics</Link>
              </div>
            </div>
            <div class="flex flex-col gap-4">
              <h5 class="text-[#374426] font-semibold text-sm">Informasi</h5>
              <div class="flex flex-col gap-3 text-[#5A684C] text-xs">
                <Link href="/article" class="hover:text-[#374426] transition-colors">Tata Tertib</Link>
                <Link href="/booking" class="hover:text-[#374426] transition-colors">Booking Simaksi</Link>
                <Link href="/article" class="hover:text-[#374426] transition-colors">Tips Keamanan</Link>
              </div>
            </div>
            <div class="flex flex-col gap-4">
              <h5 class="text-[#374426] font-semibold text-sm">Komunitas</h5>
              <div class="flex flex-col gap-3 text-[#5A684C] text-xs">
                <Link href="/" class="hover:text-[#374426] transition-colors">Forum Diskusi</Link>
                <!-- <Link href="/about" class="hover:text-[#374426] transition-colors">Tentang Kami</Link> -->
              </div>
            </div>
          </div>
        </div>
        <div class="w-full border-t border-[#D7DDC2] pt-4 flex justify-center">
          <p class="text-[#5A684C] text-xs">© 2026 AltiGuide Team. All rights reserved.</p>
        </div>
      </div>
    </footer>

    <!-- ══════════════════════════════════════════════════════════════ -->
    <!-- MODAL 1: LIHAT DETAIL (Validasi 2 Lapis)                    -->
    <!-- ══════════════════════════════════════════════════════════════ -->
    <Transition name="modal-fade">
      <div v-if="showDetailModal && selectedOrder" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click.self="closeDetailModal">
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="closeDetailModal"></div>
        <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto modal-enter z-10">
          <!-- Header -->
          <div class="sticky top-0 bg-[#374426] text-white px-6 py-4 rounded-t-3xl flex items-center justify-between z-10">
            <div>
              <h3 class="font-bold text-lg">Detail Pendaftaran</h3>
              <p class="text-xs text-white/60 mt-0.5">{{ selectedOrder.id }} • {{ selectedOrder.groupName }}</p>
            </div>
            <button @click="closeDetailModal" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition cursor-pointer">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>

          <div class="p-6 space-y-6">
            <!-- Kuota Harian -->
            <div class="bg-[#F4F1E6] rounded-2xl p-5 border border-[#D6CCAF]">
              <div class="flex items-center gap-2 mb-3">
                <span class="text-lg">🏔️</span>
                <div>
                  <p class="font-semibold text-sm text-[#374426]">{{ selectedOrder.mountain }}</p>
                  <p class="text-[11px] text-[#8B9A7B]">Kuota Harian Pendakian</p>
                </div>
              </div>
              <div class="flex items-center gap-3">
                <div class="flex-1 h-3 bg-[#E8E3D3] rounded-full overflow-hidden">
                  <div :class="quotaColor" :style="{ width: quotaPercentage + '%' }" class="h-full rounded-full transition-all duration-500"></div>
                </div>
                <span :class="quotaTextColor" class="text-xs font-bold whitespace-nowrap">
                  Sisa: {{ selectedOrder.dailyQuota.total - selectedOrder.dailyQuota.used }}/{{ selectedOrder.dailyQuota.total }} ({{ quotaPercentage }}%)
                </span>
              </div>
            </div>

            <!-- LAPIS 1: Validasi Pembayaran -->
            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-6 h-6 rounded-full bg-[#374426] text-white flex items-center justify-center text-xs font-bold">1</div>
                <h4 class="font-bold text-sm text-[#374426]">Validasi Pembayaran</h4>
              </div>
              <div class="bg-[#FBFBFB] rounded-xl p-4 border border-[#E8E3D3] space-y-3">
                <div class="flex items-center justify-between">
                  <span class="text-xs text-[#8B9A7B]">Status Transaksi</span>
                  <span :class="paymentBadge(selectedOrder.paymentStatus).cls" class="text-[10px] font-bold uppercase px-3 py-1 rounded-full">
                    {{ paymentBadge(selectedOrder.paymentStatus).label }}
                  </span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-xs text-[#8B9A7B]">Order ID</span>
                  <span class="text-xs font-mono font-semibold text-[#374426]">{{ selectedOrder.id }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-xs text-[#8B9A7B]">Metode Pembayaran</span>
                  <span class="text-xs font-semibold text-[#374426]">{{ selectedOrder.paymentMethod }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-xs text-[#8B9A7B]">Jumlah</span>
                  <span class="text-xs font-bold text-[#374426]">{{ formatRupiah(selectedOrder.amount) }}</span>
                </div>
              </div>
            </div>

            <!-- LAPIS 2: Verifikasi Dokumen -->
            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-6 h-6 rounded-full bg-[#374426] text-white flex items-center justify-center text-xs font-bold">2</div>
                <h4 class="font-bold text-sm text-[#374426]">Verifikasi Dokumen Pendaki</h4>
              </div>
              <div class="bg-[#FBFBFB] rounded-xl border border-[#E8E3D3] overflow-hidden">
                <table class="w-full text-xs">
                  <thead>
                    <tr class="bg-[#E8E3D3] text-[#5A684C] font-semibold">
                      <th class="text-left px-4 py-2.5">Anggota</th>
                      <th class="text-center px-4 py-2.5">KTP</th>
                      <th class="text-center px-4 py-2.5">Surat Sehat</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(member, mIdx) in selectedOrder.memberDetails" :key="mIdx" class="border-b border-[#F4F1E6] last:border-0">
                      <td class="px-4 py-3 font-medium text-[#374426]">{{ member.name }}</td>
                      <td class="px-4 py-3 text-center">
                        <span :class="docStatusIcon(member.ktp).cls" class="inline-flex items-center gap-1 text-[10px] font-semibold px-2.5 py-1 rounded-full">
                          {{ docStatusIcon(member.ktp).icon }} {{ docStatusIcon(member.ktp).label }}
                        </span>
                      </td>
                      <td class="px-4 py-3 text-center">
                        <span :class="docStatusIcon(member.suratSehat).cls" class="inline-flex items-center gap-1 text-[10px] font-semibold px-2.5 py-1 rounded-full">
                          {{ docStatusIcon(member.suratSehat).icon }} {{ docStatusIcon(member.suratSehat).label }}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex items-center gap-3 pt-2">
              <button
                :disabled="selectedOrder.paymentStatus !== 'lunas'"
                :class="selectedOrder.paymentStatus !== 'lunas' ? 'opacity-40 cursor-not-allowed' : 'hover:bg-[#2c361e] cursor-pointer'"
                class="flex-1 bg-[#374426] text-white font-semibold text-sm py-3 rounded-xl transition-colors"
              >
                ✓ Setujui Semua
              </button>
              <button class="flex-1 border border-[#A2825B] text-[#A2825B] font-semibold text-sm py-3 rounded-xl hover:bg-[#A2825B]/10 transition-colors cursor-pointer">
                Minta Revisi
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ══════════════════════════════════════════════════════════════ -->
    <!-- MODAL 2: SCAN CHECK-IN (SOP + Waste Declaration)            -->
    <!-- ══════════════════════════════════════════════════════════════ -->
    <Transition name="modal-fade">
      <div v-if="showCheckinModal && selectedCheckinGroup" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click.self="closeCheckinModal">
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="closeCheckinModal"></div>
        <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto modal-enter z-10">
          <!-- Header -->
          <div class="sticky top-0 bg-[#A2825B] text-white px-6 py-4 rounded-t-3xl flex items-center justify-between z-10">
            <div>
              <h3 class="font-bold text-lg">Scan Check-in</h3>
              <p class="text-xs text-white/60 mt-0.5">{{ selectedCheckinGroup.name }} • Ketua: {{ selectedCheckinGroup.leader }}</p>
            </div>
            <button @click="closeCheckinModal" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition cursor-pointer">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>

          <div class="p-6 space-y-6">
            <!-- Weather Warning -->
            <div v-if="isWeatherExtreme" class="flex items-center gap-3 p-4 bg-red-50 border border-red-300 rounded-2xl">
              <span class="text-2xl">⛈️</span>
              <div>
                <p class="font-bold text-sm text-red-800">Cuaca Ekstrem — Pemberangkatan Ditangguhkan</p>
                <p class="text-xs text-red-600 mt-0.5">Demi keselamatan pendaki, check-in tidak dapat diproses saat kondisi cuaca ekstrem.</p>
              </div>
            </div>

            <!-- Digital Checklist SOP -->
            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-6 h-6 rounded-full bg-[#A2825B] text-white flex items-center justify-center text-xs font-bold">✓</div>
                <h4 class="font-bold text-sm text-[#374426]">Digital Checklist SOP Kelayakan</h4>
              </div>
              <div class="space-y-2">
                <label
                  v-for="item in sopChecklist"
                  :key="item.id"
                  :class="item.checked ? 'bg-green-50 border-green-200' : 'bg-[#FBFBFB] border-[#E8E3D3]'"
                  class="flex items-center gap-3 p-3.5 rounded-xl border cursor-pointer transition-all duration-200 hover:shadow-sm group"
                >
                  <div class="relative">
                    <input
                      type="checkbox"
                      v-model="item.checked"
                      class="sr-only peer"
                    />
                    <div :class="item.checked ? 'bg-green-600 border-green-600' : 'bg-white border-[#D6CCAF] group-hover:border-[#A2825B]'" class="w-5 h-5 rounded-md border-2 flex items-center justify-center transition-all duration-200">
                      <svg v-if="item.checked" class="w-3 h-3 text-white" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg>
                    </div>
                  </div>
                  <span :class="item.checked ? 'text-green-800' : 'text-[#5A684C]'" class="text-sm font-medium transition-colors">{{ item.label }}</span>
                </label>
              </div>
              <div class="mt-3 flex items-center gap-2">
                <div :class="allSopChecked ? 'bg-green-100 text-green-700' : 'bg-amber-100 text-amber-700'" class="text-[10px] font-semibold px-3 py-1 rounded-full transition-colors">
                  {{ sopChecklist.filter(i => i.checked).length }}/{{ sopChecklist.length }} item terverifikasi
                </div>
              </div>
            </div>

            <!-- Deklarasi Potensi Sampah -->
            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-6 h-6 rounded-full bg-[#A2825B] text-white flex items-center justify-center text-xs font-bold">♻</div>
                <h4 class="font-bold text-sm text-[#374426]">Deklarasi Potensi Sampah</h4>
              </div>
              <div class="bg-[#FBFBFB] rounded-xl border border-[#E8E3D3] p-4 space-y-3">
                <div class="flex items-center justify-between">
                  <label class="text-sm text-[#5A684C] font-medium">Botol Plastik</label>
                  <input type="number" v-model.number="wasteDeclaration.botolPlastik" min="0" class="w-20 h-9 text-center border border-[#D6CCAF] rounded-lg text-sm font-semibold text-[#374426] bg-white focus:outline-none focus:ring-2 focus:ring-[#A2825B]/30 focus:border-[#A2825B]" />
                </div>
                <div class="flex items-center justify-between">
                  <label class="text-sm text-[#5A684C] font-medium">Bungkus Makanan</label>
                  <input type="number" v-model.number="wasteDeclaration.bungkusMakanan" min="0" class="w-20 h-9 text-center border border-[#D6CCAF] rounded-lg text-sm font-semibold text-[#374426] bg-white focus:outline-none focus:ring-2 focus:ring-[#A2825B]/30 focus:border-[#A2825B]" />
                </div>
                <div class="flex items-center justify-between">
                  <label class="text-sm text-[#5A684C] font-medium">Kaleng</label>
                  <input type="number" v-model.number="wasteDeclaration.kaleng" min="0" class="w-20 h-9 text-center border border-[#D6CCAF] rounded-lg text-sm font-semibold text-[#374426] bg-white focus:outline-none focus:ring-2 focus:ring-[#A2825B]/30 focus:border-[#A2825B]" />
                </div>
                <div class="flex items-center justify-between">
                  <label class="text-sm text-[#5A684C] font-medium">Lainnya</label>
                  <input type="number" v-model.number="wasteDeclaration.lainnya" min="0" class="w-20 h-9 text-center border border-[#D6CCAF] rounded-lg text-sm font-semibold text-[#374426] bg-white focus:outline-none focus:ring-2 focus:ring-[#A2825B]/30 focus:border-[#A2825B]" />
                </div>
                <div class="pt-2 border-t border-[#E8E3D3] flex items-center justify-between">
                  <span class="text-xs text-[#8B9A7B]">Total item sampah potensial:</span>
                  <span class="text-sm font-bold text-[#374426]">{{ totalWaste }} item</span>
                </div>
              </div>
            </div>

            <!-- Action Button -->
            <button
              @click="processCheckin"
              :disabled="!canCheckin"
              :class="canCheckin ? 'bg-[#374426] hover:bg-[#2c361e] cursor-pointer' : 'bg-gray-300 cursor-not-allowed'"
              class="w-full text-white font-semibold text-sm py-3.5 rounded-xl transition-colors flex items-center justify-center gap-2"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
              <span v-if="isWeatherExtreme">Check-in Ditangguhkan — Cuaca Ekstrem</span>
              <span v-else-if="!allSopChecked">Lengkapi Checklist SOP Terlebih Dahulu</span>
              <span v-else>Proses Check-in</span>
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ══════════════════════════════════════════════════════════════ -->
    <!-- MODAL 3: SCAN CHECK-OUT (Waste Matching + Triage)           -->
    <!-- ══════════════════════════════════════════════════════════════ -->
    <Transition name="modal-fade">
      <div v-if="showCheckoutModal && selectedCheckoutGroup" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click.self="closeCheckoutModal">
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="closeCheckoutModal"></div>
        <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto modal-enter z-10">
          <!-- Header -->
          <div class="sticky top-0 bg-[#6B8E4E] text-white px-6 py-4 rounded-t-3xl flex items-center justify-between z-10">
            <div>
              <h3 class="font-bold text-lg">Scan Check-out</h3>
              <p class="text-xs text-white/60 mt-0.5">{{ selectedCheckoutGroup.name }} • Ketua: {{ selectedCheckoutGroup.leader }}</p>
            </div>
            <button @click="closeCheckoutModal" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition cursor-pointer">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>

          <div class="p-6 space-y-6">
            <!-- Waste Validation -->
            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-6 h-6 rounded-full bg-[#6B8E4E] text-white flex items-center justify-center text-xs font-bold">♻</div>
                <h4 class="font-bold text-sm text-[#374426]">Validasi Sampah</h4>
              </div>
              <div class="bg-[#FBFBFB] rounded-xl border border-[#E8E3D3] overflow-hidden">
                <table class="w-full text-xs">
                  <thead>
                    <tr class="bg-[#E8E3D3] text-[#5A684C] font-semibold">
                      <th class="text-left px-4 py-2.5">Item</th>
                      <th class="text-center px-4 py-2.5">Naik</th>
                      <th class="text-center px-4 py-2.5">Turun</th>
                      <th class="text-center px-4 py-2.5">Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in wasteItems" :key="item.key" class="border-b border-[#F4F1E6] last:border-0">
                      <td class="px-4 py-3 font-medium text-[#374426]">{{ item.label }}</td>
                      <td class="px-4 py-3 text-center">
                        <span class="inline-block bg-[#E8E3D3] text-[#5A684C] font-semibold px-3 py-1 rounded-lg text-xs">{{ item.naik }}</span>
                      </td>
                      <td class="px-4 py-3 text-center">
                        <input type="number" v-model.number="wasteCheckout[item.key]" min="0" class="w-16 h-8 text-center border border-[#D6CCAF] rounded-lg text-xs font-semibold text-[#374426] bg-white focus:outline-none focus:ring-2 focus:ring-[#6B8E4E]/30 focus:border-[#6B8E4E]" />
                      </td>
                      <td class="px-4 py-3 text-center">
                        <span v-if="item.turun >= item.naik" class="inline-flex items-center gap-1 text-green-700 bg-green-50 text-[10px] font-semibold px-2 py-0.5 rounded-full">✓ OK</span>
                        <span v-else class="inline-flex items-center gap-1 text-red-700 bg-red-50 text-[10px] font-semibold px-2 py-0.5 rounded-full">⚠ {{ item.turun - item.naik }}</span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div class="mt-3">
                <span v-if="allWasteOk" class="inline-flex items-center gap-1.5 bg-green-100 text-green-800 text-xs font-bold px-4 py-2 rounded-full border border-green-200">
                  ✓ SAMPAH OK — Semua item cocok
                </span>
                <span v-else class="inline-flex items-center gap-1.5 bg-red-100 text-red-800 text-xs font-bold px-4 py-2 rounded-full border border-red-200">
                  ⚠ SELISIH DITEMUKAN — Periksa item yang kurang
                </span>
              </div>
            </div>

            <!-- Triage Kesehatan -->
            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-6 h-6 rounded-full bg-[#6B8E4E] text-white flex items-center justify-center text-xs font-bold">🏥</div>
                <h4 class="font-bold text-sm text-[#374426]">Triage Kesehatan</h4>
              </div>
              <div class="space-y-4">
                <div>
                  <label class="text-xs text-[#8B9A7B] font-medium mb-1.5 block">Kondisi Rombongan</label>
                  <select v-model="healthCondition" :class="healthConditionColor" class="w-full h-11 px-4 rounded-xl border text-sm font-semibold text-[#374426] focus:outline-none focus:ring-2 focus:ring-[#6B8E4E]/30 cursor-pointer transition-colors">
                    <option value="sehat">🟢 Sehat / Selamat</option>
                    <option value="luka_ringan">🟡 Luka Ringan</option>
                    <option value="luka_berat">🔴 Luka Berat</option>
                    <option value="meninggal">⚫ Meninggal</option>
                  </select>
                </div>

                <Transition name="slide-down">
                  <div v-if="needsChronology" class="space-y-2">
                    <label class="text-xs font-medium block" :class="healthCondition === 'luka_ringan' ? 'text-amber-700' : 'text-red-700'">
                      Kronologi Kejadian <span class="text-red-500">*</span>
                    </label>
                    <textarea
                      v-model="chronology"
                      rows="4"
                      :class="healthCondition === 'luka_ringan' ? 'border-amber-300 bg-amber-50 focus:ring-amber-400/30' : 'border-red-300 bg-red-50 focus:ring-red-400/30'"
                      class="w-full border rounded-xl p-4 text-sm text-[#374426] focus:outline-none focus:ring-2 resize-none transition-colors"
                      placeholder="Tuliskan kronologi kejadian secara detail..."
                    ></textarea>
                    <p v-if="needsChronology && chronology.trim() === ''" class="text-[10px] text-red-600 font-medium">
                      * Kronologi wajib diisi untuk kondisi selain "Sehat/Selamat"
                    </p>
                  </div>
                </Transition>
              </div>
            </div>

            <!-- Action Button -->
            <button
              @click="processCheckout"
              :disabled="!canCheckout"
              :class="canCheckout ? 'bg-[#6B8E4E] hover:bg-[#5A7A3D] cursor-pointer' : 'bg-gray-300 cursor-not-allowed'"
              class="w-full text-white font-semibold text-sm py-3.5 rounded-xl transition-colors flex items-center justify-center gap-2"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
              Proses Check-out
            </button>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>

<style scoped>
/* ═══ Modal Transitions ═══ */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}
.modal-fade-enter-active .modal-enter {
  transition: transform 0.3s ease, opacity 0.3s ease;
}
.modal-fade-leave-active .modal-enter {
  transition: transform 0.2s ease, opacity 0.2s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
.modal-fade-enter-from .modal-enter {
  transform: scale(0.95) translateY(10px);
  opacity: 0;
}
.modal-fade-leave-to .modal-enter {
  transform: scale(0.97) translateY(5px);
  opacity: 0;
}

/* ═══ Slide Down Transition ═══ */
.slide-down-enter-active {
  transition: all 0.35s ease-out;
}
.slide-down-leave-active {
  transition: all 0.25s ease-in;
}
.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-12px);
  max-height: 0;
}
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-8px);
  max-height: 0;
}

/* ═══ Overdue Pulse Animation ═══ */
.overdue-pulse {
  animation: overduePulse 1.5s ease-in-out infinite;
}
@keyframes overduePulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 0 0 rgba(220, 38, 38, 0.5); }
  50% { opacity: 0.85; box-shadow: 0 0 0 6px rgba(220, 38, 38, 0); }
}

/* ═══ Weather Extreme Banner Animation ═══ */
.weather-extreme-banner {
  animation: weatherGlow 2s ease-in-out infinite;
}
@keyframes weatherGlow {
  0%, 100% { box-shadow: 0 4px 20px rgba(220, 38, 38, 0.25); }
  50% { box-shadow: 0 4px 30px rgba(220, 38, 38, 0.45); }
}

/* ═══ Scrollbar Styling ═══ */
.overflow-y-auto::-webkit-scrollbar {
  width: 5px;
}
.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}
.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #D6CCAF;
  border-radius: 10px;
}
.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #A2825B;
}
</style>
