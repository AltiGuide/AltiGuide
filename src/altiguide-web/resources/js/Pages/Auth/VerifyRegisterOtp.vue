<script setup>
import { Head, Link, useForm } from '@inertiajs/vue3'

const props = defineProps({
    email: String,
})

const form = useForm({
    email: props.email || '',
    code: '',
})

const resendForm = useForm({
    email: props.email || '',
})

const submit = () => {
    form.post('/register/verify')
}

const resendOtp = () => {
    resendForm.post('/register/verify/resend')
}
</script>

<template>
    <div
        class="min-h-screen w-full bg-cover bg-center bg-no-repeat relative flex flex-col"
        :style="{ backgroundImage: `url('/images/mountain_bg.png')` }"
    >
        <Head title="Verifikasi Akun - AltiGuide">
            <link rel="preconnect" href="https://fonts.googleapis.com" />
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
            <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700&display=swap" rel="stylesheet" />
        </Head>
        <div class="absolute inset-0 bg-black/10 z-0"></div>

        <!-- Navbar -->
        <nav class="w-full flex justify-between items-center px-4 md:px-8 xl:px-12 py-4 relative z-50 text-[#3b4b3b] font-semibold bg-[#374426]/20 backdrop-blur-md border-b border-white/20 shadow-sm">
            <div class="flex items-center gap-2 xl:gap-3">
                <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 md:w-10 md:h-10 object-contain" />
                <span class="text-xl md:text-2xl xl:text-3xl font-bold text-[#3b4b3b] tracking-tight">AltiGuide</span>
            </div>
            <div class="flex items-center gap-4 md:gap-6 xl:gap-9 text-sm md:text-base">
                <Link href="/" class="hover:text-black transition">Home</Link>
                <Link href="/login" class="border border-[#3b4b3b] px-4 md:px-6 py-2 rounded-lg hover:bg-[#3b4b3b] hover:text-white transition duration-200 whitespace-nowrap">
                    Login
                </Link>
            </div>
        </nav>

        <!-- Content -->
        <div class="flex-1 flex items-center justify-center z-10 py-8">
            <div class="w-[560px] bg-[#F8F3E4] rounded-[20px] border border-[#D6CCAF] shadow-2xl flex flex-col items-center px-16 py-12" style="font-family: 'Montserrat', sans-serif;">

                <div class="flex items-center justify-center gap-3 mb-8">
                    <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-10 h-10 object-contain" />
                    <span class="text-[32px] font-semibold text-[#374426]">AltiGuide</span>
                </div>

                <h2 class="text-[20px] font-medium text-[#333333] text-center mb-2">Verifikasi Kode OTP</h2>
                <p class="text-[14px] text-[#666666] text-center mb-8">
                    Kami telah mengirimkan kode verifikasi OTP ke <strong>{{ email }}</strong>. Silakan masukkan kode tersebut di bawah ini.
                </p>

                <!-- Success/Status message -->
                <div v-if="$page.props.flash?.success" class="w-full max-w-[380px] mb-4 p-3 bg-green-100 border border-green-300 rounded-lg text-green-800 text-sm text-center">
                    {{ $page.props.flash.success }}
                </div>
                
                <!-- Error flash message -->
                <div v-if="$page.props.flash?.error" class="w-full max-w-[380px] mb-4 p-3 bg-red-100 border border-red-300 rounded-lg text-red-800 text-sm text-center">
                    {{ $page.props.flash.error }}
                </div>

                <form @submit.prevent="submit" class="w-full max-w-[380px] flex flex-col gap-4">
                    <input type="hidden" v-model="form.email" />

                    <div class="flex flex-col gap-1">
                        <label for="code" class="text-[16px] font-normal text-[#666666]">Kode Verifikasi</label>
                        <input
                            id="code"
                            type="text"
                            v-model="form.code"
                            required
                            maxlength="6"
                            placeholder="Masukkan 6 digit kode"
                            class="w-full h-[56px] border border-[#D7DDC2] rounded-xl px-4 bg-[#F8F3E4] text-[#333333] text-center outline-none focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 transition"
                            :class="form.code ? 'text-2xl tracking-[0.5em]' : 'text-base tracking-normal'"
                        />
                        <div v-if="form.errors.code" class="text-red-500 text-xs mt-1">{{ form.errors.code }}</div>
                    </div>

                    <button
                        type="submit"
                        class="w-full h-[56px] rounded-[40px] bg-[#374426] text-white font-semibold text-[18px] mt-4 hover:bg-[#2c361e] transition-colors disabled:opacity-60 disabled:cursor-not-allowed cursor-pointer flex items-center justify-center gap-2"
                        :disabled="form.processing"
                    >
                        <svg v-if="form.processing" class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        <span v-if="form.processing">Memverifikasi...</span>
                        <span v-else>Verifikasi Akun</span>
                    </button>
                </form>

                <div class="text-center mt-6">
                    <button 
                        @click="resendOtp" 
                        class="text-[#64823E] text-sm font-medium hover:underline bg-none border-none cursor-pointer inline-flex items-center gap-2"
                        :disabled="resendForm.processing"
                    >
                        <svg v-if="resendForm.processing" class="animate-spin h-4 w-4 text-[#64823E]" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        <span v-if="resendForm.processing">Mengirim ulang...</span>
                        <span v-else>Kirim ulang kode OTP</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>
