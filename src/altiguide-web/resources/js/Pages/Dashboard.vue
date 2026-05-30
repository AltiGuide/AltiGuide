<script setup>
import { computed } from 'vue'
import { Head, Link, useForm, usePage } from '@inertiajs/vue3'

const page = usePage()
const user = computed(() => page.props.auth.user)

// Form data initialized with current user details
const form = useForm({
    name: user.value?.name || '',
    phone_number: user.value?.phone_number || '',
    age: user.value?.age || '',
    address: user.value?.address || '',
    emergency_contact: user.value?.emergency_contact || '',
    nik: user.value?.nik || '',
})

const isProfileIncomplete = computed(() => {
    return !user.value?.age || !user.value?.address || !user.value?.emergency_contact || !user.value?.nik
})

const submitProfile = () => {
    form.put('/profile/update')
}
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

        <!-- ══════════════ Main Content ══════════════ -->
        <main class="flex-1 max-w-4xl w-full mx-auto px-4 py-8">
            <div class="bg-white rounded-2xl border border-[#D6CCAF] shadow-xl overflow-hidden">
                <!-- Header Banner -->
                <div class="bg-[#374426] text-white p-6 md:p-8 flex items-center justify-between">
                    <div>
                        <h1 class="text-2xl md:text-3xl font-bold tracking-tight">Halo, {{ user.name }}!</h1>
                        <p class="text-[#D6CCAF] text-sm md:text-base mt-1">Selamat datang kembali di dashboard AltiGuide.</p>
                    </div>
                    <div class="hidden md:block">
                        <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-16 h-16 opacity-80 object-contain" />
                    </div>
                </div>

                <div class="p-6 md:p-8">
                    <!-- Warning / Alert Banner for Incomplete Profile -->
                    <div v-if="isProfileIncomplete" class="mb-6 p-4 bg-amber-50 border border-amber-300 rounded-xl text-amber-900 text-sm flex gap-3">
                        <div class="text-xl">⚠️</div>
                        <div>
                            <h4 class="font-bold">Profil Belum Lengkap!</h4>
                            <p class="mt-0.5">
                                Anda harus melengkapi NIK, Umur, Alamat, dan Kontak Darurat di formulir bawah ini agar dapat melakukan booking pendakian gunung.
                            </p>
                        </div>
                    </div>

                    <!-- Flash Success/Warning notifications -->
                    <div v-if="$page.props.flash?.success" class="mb-6 p-4 bg-green-50 border border-green-300 rounded-xl text-green-900 text-sm flex gap-3">
                        <div class="text-xl">✅</div>
                        <div class="flex-1 align-middle self-center">{{ $page.props.flash.success }}</div>
                    </div>
                    
                    <div v-if="$page.props.flash?.warning" class="mb-6 p-4 bg-yellow-50 border border-yellow-300 rounded-xl text-yellow-900 text-sm flex gap-3">
                        <div class="text-xl">⚠️</div>
                        <div class="flex-1 align-middle self-center">{{ $page.props.flash.warning }}</div>
                    </div>

                    <div v-if="$page.props.flash?.error" class="mb-6 p-4 bg-red-50 border border-red-300 rounded-xl text-red-900 text-sm flex gap-3">
                        <div class="text-xl">❌</div>
                        <div class="flex-1 align-middle self-center">{{ $page.props.flash.error }}</div>
                    </div>

                    <!-- Profile Form Section -->
                    <h2 class="text-xl font-bold text-[#374426] mb-6 border-b border-gray-100 pb-2">Informasi Profil Pendaki</h2>

                    <form @submit.prevent="submitProfile" class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <!-- Name -->
                        <div class="flex flex-col gap-1.5 col-span-2 md:col-span-1">
                            <label for="profile-name" class="text-sm font-semibold text-gray-700">Nama Lengkap</label>
                            <input
                                id="profile-name"
                                type="text"
                                v-model="form.name"
                                required
                                class="w-full h-11 border border-[#D7DDC2] rounded-xl px-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition"
                            />
                            <div v-if="form.errors.name" class="text-red-600 text-xs mt-1">{{ form.errors.name }}</div>
                        </div>

                        <!-- Email -->
                        <div class="flex flex-col gap-1.5 col-span-2 md:col-span-1">
                            <label for="profile-email" class="text-sm font-semibold text-gray-400">Email Address (Tidak Dapat Diubah)</label>
                            <input
                                id="profile-email"
                                type="email"
                                :value="user.email"
                                disabled
                                class="w-full h-11 border border-gray-200 rounded-xl px-4 text-gray-400 bg-gray-50 cursor-not-allowed outline-none"
                            />
                        </div>

                        <!-- Phone Number -->
                        <div class="flex flex-col gap-1.5 col-span-2 md:col-span-1">
                            <label for="profile-phone" class="text-sm font-semibold text-gray-700">Nomor Telepon / HP</label>
                            <input
                                id="profile-phone"
                                type="text"
                                v-model="form.phone_number"
                                required
                                class="w-full h-11 border border-[#D7DDC2] rounded-xl px-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition"
                            />
                            <div v-if="form.errors.phone_number" class="text-red-600 text-xs mt-1">{{ form.errors.phone_number }}</div>
                        </div>

                        <!-- NIK (Deferred) -->
                        <div class="flex flex-col gap-1.5 col-span-2 md:col-span-1">
                            <label for="profile-nik" class="text-sm font-semibold text-gray-700">Nomor Induk Kependudukan (NIK)</label>
                            <input
                                id="profile-nik"
                                type="text"
                                v-model="form.nik"
                                required
                                maxlength="16"
                                placeholder="Masukkan 16 digit NIK"
                                class="w-full h-11 border border-[#D7DDC2] rounded-xl px-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition"
                            />
                            <div v-if="form.errors.nik" class="text-red-600 text-xs mt-1">{{ form.errors.nik }}</div>
                        </div>

                        <!-- Age (Deferred) -->
                        <div class="flex flex-col gap-1.5 col-span-2 md:col-span-1">
                            <label for="profile-age" class="text-sm font-semibold text-gray-700">Umur (Tahun)</label>
                            <input
                                id="profile-age"
                                type="number"
                                v-model="form.age"
                                required
                                min="1"
                                max="120"
                                class="w-full h-11 border border-[#D7DDC2] rounded-xl px-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition"
                            />
                            <div v-if="form.errors.age" class="text-red-600 text-xs mt-1">{{ form.errors.age }}</div>
                        </div>

                        <!-- Emergency Contact (Deferred) -->
                        <div class="flex flex-col gap-1.5 col-span-2 md:col-span-1">
                            <label for="profile-emergency" class="text-sm font-semibold text-gray-700">Kontak Darurat (No HP Kerabat/Keluarga)</label>
                            <input
                                id="profile-emergency"
                                type="text"
                                v-model="form.emergency_contact"
                                required
                                class="w-full h-11 border border-[#D7DDC2] rounded-xl px-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition"
                            />
                            <div v-if="form.errors.emergency_contact" class="text-red-600 text-xs mt-1">{{ form.errors.emergency_contact }}</div>
                        </div>

                        <!-- Address (Deferred) -->
                        <div class="flex flex-col gap-1.5 col-span-2">
                            <label for="profile-address" class="text-sm font-semibold text-gray-700">Alamat Lengkap</label>
                            <textarea
                                id="profile-address"
                                v-model="form.address"
                                required
                                rows="3"
                                class="w-full border border-[#D7DDC2] rounded-xl p-4 text-gray-800 bg-[#FBFBFB] focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 outline-none transition resize-none"
                            ></textarea>
                            <div v-if="form.errors.address" class="text-red-600 text-xs mt-1">{{ form.errors.address }}</div>
                        </div>

                        <!-- Form Actions -->
                        <div class="col-span-2 flex justify-end gap-4 mt-4">
                            <Link
                                href="/"
                                class="h-11 px-6 rounded-full border border-[#D6CCAF] flex items-center justify-center text-gray-600 font-semibold hover:bg-gray-50 transition cursor-pointer"
                            >
                                Kembali ke Beranda
                            </Link>

                            <button
                                type="submit"
                                class="h-11 px-8 rounded-full bg-[#374426] text-white font-semibold hover:bg-[#2c361e] transition-colors disabled:opacity-60 disabled:cursor-not-allowed cursor-pointer"
                                :disabled="form.processing"
                            >
                                <span v-if="form.processing">Menyimpan...</span>
                                <span v-else>Simpan Profil</span>
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </main>
    </div>
</template>
