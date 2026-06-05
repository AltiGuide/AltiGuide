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
const isProfileIncomplete = computed(() => {
    return !user.value?.age || !user.value?.address || !user.value?.emergency_contact || !user.value?.nik
})

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
</script>

<template>
    <Head title="Dashboard - AltiGuide" />

    <div class="min-h-screen bg-[#F4F1E6] flex flex-col font-sans">

        <!-- ══════════════ Navbar ══════════════ -->
        <nav class="w-full flex justify-between items-center px-4 md:px-8 xl:px-12 py-4 text-[#3b4b3b] font-semibold bg-[#374426]/10 backdrop-blur-md border-b border-[#D6CCAF] shadow-sm relative z-50">
            <div class="flex items-center gap-2 xl:gap-3">
                <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 md:w-10 md:h-10 object-contain" />
                <span class="text-xl md:text-2xl xl:text-3xl font-bold text-[#374426] tracking-tight">AltiGuide</span>
            </div>

            <div class="flex items-center gap-4 md:gap-6 xl:gap-9 text-sm md:text-base">
                <Link href="/" class="hover:text-black transition">Home</Link>
                <Link href="/booking" class="hover:text-black transition">Booking</Link>
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
                            <Link href="/" class="h-11 px-6 rounded-full border border-[#D6CCAF] flex items-center text-gray-600 font-semibold hover:bg-gray-50 transition">
                                Kembali
                            </Link>
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
                    <form @submit.prevent="submitPassword" class="flex flex-col gap-5 max-w-lg">

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
                        <Link href="/booking" class="mt-3 inline-block text-sm text-[#64823E] hover:underline font-medium">Mulai Booking →</Link>
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
                                v-for="booking in registeredBookings"
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
                                    <button class="w-full py-2 bg-[#374426] text-white text-xs font-semibold rounded-lg hover:bg-[#2c361e] transition">
                                        View Details
                                    </button>
                                    <button class="w-full py-2 border border-[#374426] text-[#374426] text-xs font-semibold rounded-lg hover:bg-[#374426]/5 transition">
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
                                v-for="booking in pendingBookings"
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
                                    <button class="w-full py-2 bg-amber-500 text-white text-xs font-semibold rounded-lg hover:bg-amber-600 transition">
                                        Complete Payment
                                    </button>
                                    <button class="w-full py-2 border border-amber-400 text-amber-700 text-xs font-semibold rounded-lg hover:bg-amber-100 transition">
                                        Cancel Booking
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
                                v-for="booking in canceledBookings"
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
                                <div class="pt-1 border-t border-red-100">
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
            <div class="w-full bg-[#E0DBBE] py-8 px-8 md:px-16 flex flex-col md:flex-row items-center justify-between gap-6">
                <div class="flex items-center gap-3">
                    <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-10 h-10 object-contain" />
                    <span class="text-2xl font-bold text-[#374426] tracking-tight">AltiGuide</span>
                </div>
                <div class="flex items-center gap-4">
                    <Link href="#" class="bg-[#374426] text-[#F8F3E4] text-base font-medium rounded-xl px-8 py-3 hover:opacity-90 transition">Contact Us</Link>
                    <Link href="/booking" class="bg-[#F8F3E4] text-[#374426] text-base font-medium rounded-xl px-8 py-3 hover:opacity-90 transition shadow-sm">Start Summit</Link>
                </div>
            </div>

            <footer class="w-full bg-white px-8 md:px-16 py-8 flex flex-col">
                <div class="flex flex-col lg:flex-row justify-between items-start gap-10 mb-8">
                    <div class="flex flex-col gap-16">
                        <Link href="/" class="text-xl font-medium text-[#374426] underline underline-offset-8">AltiGuide.com</Link>
                        <div class="flex items-center gap-5 text-[#828282]">
                            <a href="#" class="hover:text-[#374426] transition">
                                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12c0 4.991 3.657 9.128 8.438 9.878v-6.987h-2.54V12h2.54V9.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V12h2.773l-.443 2.89h-2.33v6.988C18.343 21.128 22 16.991 22 12z"/></svg>
                            </a>
                            <a href="#" class="hover:text-[#374426] transition">
                                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M21.582 6.186a2.66 2.66 0 0 0-1.875-1.884C18.053 3.86 12 3.86 12 3.86s-6.053 0-7.707.442a2.66 2.66 0 0 0-1.875 1.884C2 7.854 2 12 2 12s0 4.146.418 5.814a2.66 2.66 0 0 0 1.875 1.884C5.947 20.14 12 20.14 12 20.14s6.053 0 7.707-.442a2.66 2.66 0 0 0 1.875-1.884C22 16.146 22 12 22 12s0-4.146-.418-5.814zM9.88 15.15V8.85l6.32 3.15-6.32 3.15z"/></svg>
                            </a>
                            <a href="#" class="hover:text-[#374426] transition">
                                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zM12 0C8.741 0 8.333.014 7.053.072 2.695.272.273 2.69.073 7.052.014 8.333 0 8.741 0 12c0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98C8.333 23.986 8.741 24 12 24c3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98C15.668.014 15.259 0 12 0zm0 5.838a6.162 6.162 0 1 0 0 12.324 6.162 6.162 0 0 0 0-12.324zM12 16a4 4 0 1 1 0-8 4 4 0 0 1 0 8zm6.406-11.845a1.44 1.44 0 1 0 0 2.881 1.44 1.44 0 0 0 0-2.881z"/></svg>
                            </a>
                        </div>
                    </div>
                    <div class="flex flex-col sm:flex-row gap-10 md:gap-20">
                        <div class="flex flex-col gap-4">
                            <h5 class="text-[#374426] font-semibold text-base">Jelajahi</h5>
                            <div class="flex flex-col gap-3 text-[#5A684C] font-medium text-sm">
                                <Link href="#" class="hover:text-[#374426] transition">Daftar Gunung</Link>
                                <Link href="#" class="hover:text-[#374426] transition">Jalur Pendakian</Link>
                                <Link href="#" class="hover:text-[#374426] transition">Weather Analytics</Link>
                            </div>
                        </div>
                        <div class="flex flex-col gap-4">
                            <h5 class="text-[#374426] font-semibold text-base">Informasi</h5>
                            <div class="flex flex-col gap-3 text-[#5A684C] font-medium text-sm">
                                <Link href="#" class="hover:text-[#374426] transition">Tata Tertib</Link>
                                <Link href="/booking" class="hover:text-[#374426] transition">Booking Simaksi</Link>
                                <Link href="#" class="hover:text-[#374426] transition">Tips Keamanan</Link>
                            </div>
                        </div>
                        <div class="flex flex-col gap-4">
                            <h5 class="text-[#374426] font-semibold text-base">Komunitas</h5>
                            <div class="flex flex-col gap-3 text-[#5A684C] font-medium text-sm">
                                <Link href="#" class="hover:text-[#374426] transition">Event Mendaki</Link>
                                <Link href="#" class="hover:text-[#374426] transition">Forum Diskusi</Link>
                                <Link href="#" class="hover:text-[#374426] transition">Tentang Kami</Link>
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
</template>
