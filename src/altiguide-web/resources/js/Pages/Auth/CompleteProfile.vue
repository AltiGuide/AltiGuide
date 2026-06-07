<script setup>
import { reactive, onMounted } from 'vue'
import { Head, Link, router } from '@inertiajs/vue3'
import axios from 'axios'

const form = reactive({
    name: '',
    email: '',
    nik: '',
    phone_number: '',
    emergency_contact: '',
    address: '',
    age: null
})

const errors = reactive({
    nik: '',
    phone_number: '',
    emergency_contact: '',
    address: '',
    age: ''
})

const processing = reactive({
    status: false
})

onMounted(() => {
    const tempUserStr = sessionStorage.getItem('temp_google_user')
    if (!tempUserStr) {
        router.visit('/login')
        return;
    }
    const tempUser = JSON.parse(tempUserStr)
    form.name = tempUser.name || ''
    form.email = tempUser.email || ''
})

const submitProfile = async () => {
    processing.status = true
    errors.nik = ''
    errors.phone_number = ''
    errors.emergency_contact = ''
    errors.address = ''
    errors.age = ''

    try {
        const response = await axios.post('/complete-profile', form)
        if (response.data.success) {
            sessionStorage.removeItem('temp_google_user')
            const intendedUrl = localStorage.getItem('intended_url') || '/dashboard'
            localStorage.removeItem('intended_url')
            router.visit(intendedUrl)
        }
    } catch (error) {
        if (error.response && error.response.status === 422) {
            const validationErrors = error.response.data.errors
            if (validationErrors.nik) errors.nik = validationErrors.nik[0]
            if (validationErrors.phone_number) errors.phone_number = validationErrors.phone_number[0]
            if (validationErrors.emergency_contact) errors.emergency_contact = validationErrors.emergency_contact[0]
            if (validationErrors.address) errors.address = validationErrors.address[0]
            if (validationErrors.age) errors.age = validationErrors.age[0]
        }
    } finally {
        processing.status = false
    }
}
</script>

<template>
    <div
        class="min-h-screen w-full bg-cover bg-center bg-no-repeat relative flex flex-col"
        :style="{ backgroundImage: `url('/images/mountain_bg.png')` }"
    >
        <Head title="Lengkapi Profil - AltiGuide">
            <link rel="preconnect" href="https://fonts.googleapis.com" />
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
            <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700&display=swap" rel="stylesheet" />
        </Head>
        <div class="absolute inset-0 bg-black/10 z-0"></div>

        <nav class="w-full flex justify-between items-center px-4 md:px-8 xl:px-12 py-4 relative z-50 text-[#3b4b3b] font-semibold bg-[#374426]/20 backdrop-blur-md border-b border-white/20 shadow-sm">
            <div class="flex items-center gap-2 xl:gap-3">
                <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 md:w-10 md:h-10 object-contain" />
                <span class="text-xl md:text-2xl xl:text-3xl font-bold text-[#3b4b3b] tracking-tight">AltiGuide</span>
            </div>
            <div class="flex items-center gap-4 md:gap-6 xl:gap-9 text-sm md:text-base">
                <a href="/" class="hover:text-black transition">Home</a>
                <a href="/login" class="border border-[#3b4b3b] px-4 md:px-6 py-2 rounded-lg hover:bg-[#3b4b3b] hover:text-white transition duration-200 whitespace-nowrap">
                    Login
                </a>
            </div>
        </nav>

        <div class="flex-1 flex items-center justify-center z-10 py-8">
            <div class="w-[600px] bg-[#F8F3E4] rounded-[20px] border border-[#D6CCAF] shadow-2xl flex flex-col items-center px-12 py-10 animate-fade-in" style="font-family: 'Montserrat', sans-serif;">
                <div class="flex items-center justify-center gap-3 mb-6">
                    <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-10 h-10 object-contain" />
                    <span class="text-[32px] font-semibold text-[#374426]">AltiGuide</span>
                </div>

                <h2 class="text-[20px] font-medium text-[#333333] text-center mb-1">Lengkapi Profil Anda</h2>
                <p class="text-[14px] text-[#666666] text-center mb-6">
                    Silakan lengkapi data profil di bawah ini untuk menyelesaikan pendaftaran Anda.
                </p>

                <form @submit.prevent="submitProfile" class="w-full flex flex-col gap-4">
                    <div class="flex flex-col gap-1">
                        <label for="nik" class="text-[15px] font-normal text-[#666666]">NIK (Nomor Induk Kependudukan)</label>
                        <input
                            id="nik"
                            type="text"
                            v-model="form.nik"
                            required
                            maxlength="16"
                            class="w-full h-[52px] border border-[#D7DDC2] rounded-xl px-4 bg-[#F8F3E4] text-[#333333] outline-none focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 transition"
                        />
                        <div v-if="errors.nik" class="text-red-500 text-xs mt-1">{{ errors.nik }}</div>
                    </div>

                    <div class="flex flex-col gap-1">
                        <label for="phone_number" class="text-[15px] font-normal text-[#666666]">Nomor Telepon</label>
                        <input
                            id="phone_number"
                            type="text"
                            v-model="form.phone_number"
                            required
                            class="w-full h-[52px] border border-[#D7DDC2] rounded-xl px-4 bg-[#F8F3E4] text-[#333333] outline-none focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 transition"
                        />
                        <div v-if="errors.phone_number" class="text-red-500 text-xs mt-1">{{ errors.phone_number }}</div>
                    </div>

                    <div class="flex flex-col gap-1">
                        <label for="emergency_contact" class="text-[15px] font-normal text-[#666666]">Nomor Kontak Darurat</label>
                        <input
                            id="emergency_contact"
                            type="text"
                            v-model="form.emergency_contact"
                            required
                            class="w-full h-[52px] border border-[#D7DDC2] rounded-xl px-4 bg-[#F8F3E4] text-[#333333] outline-none focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 transition"
                        />
                        <div v-if="errors.emergency_contact" class="text-red-500 text-xs mt-1">{{ errors.emergency_contact }}</div>
                    </div>

                    <div class="flex flex-col gap-1">
                        <label for="age" class="text-[15px] font-normal text-[#666666]">Umur</label>
                        <input
                            id="age"
                            type="number"
                            v-model="form.age"
                            required
                            class="w-full h-[52px] border border-[#D7DDC2] rounded-xl px-4 bg-[#F8F3E4] text-[#333333] outline-none focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 transition"
                        />
                        <div v-if="errors.age" class="text-red-500 text-xs mt-1">{{ errors.age }}</div>
                    </div>

                    <div class="flex flex-col gap-1">
                        <label for="address" class="text-[15px] font-normal text-[#666666]">Alamat Lengkap</label>
                        <textarea
                            id="address"
                            v-model="form.address"
                            required
                            rows="3"
                            class="w-full border border-[#D7DDC2] rounded-xl p-4 bg-[#F8F3E4] text-[#333333] outline-none focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 transition"
                        ></textarea>
                        <div v-if="errors.address" class="text-red-500 text-xs mt-1">{{ errors.address }}</div>
                    </div>

                    <button
                        type="submit"
                        class="w-full h-[56px] rounded-[40px] bg-[#374426] text-white font-semibold text-[18px] mt-4 hover:bg-[#2c361e] transition-colors disabled:opacity-60 disabled:cursor-not-allowed cursor-pointer flex items-center justify-center gap-2"
                        :disabled="processing.status"
                    >
                        <svg v-if="processing.status" class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        <span v-if="processing.status">Menyimpan...</span>
                        <span v-else>Simpan &amp; Lanjutkan</span>
                    </button>
                </form>
            </div>
        </div>
    </div>
</template>
