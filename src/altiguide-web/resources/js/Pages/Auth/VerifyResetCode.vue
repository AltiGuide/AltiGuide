<script setup>
import { Head, Link, useForm } from '@inertiajs/vue3'

const props = defineProps({
    email: String,
})

const form = useForm({
    email: props.email || '',
    code: '',
})

const submit = () => {
    form.post('/forgot-password/verify')
}
</script>

<template>
    <Head title="Verify OTP - AltiGuide">
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
        <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700&display=swap" rel="stylesheet" />
    </Head>

    <div
        class="min-h-screen w-full bg-cover bg-center bg-no-repeat relative flex flex-col"
        :style="{ backgroundImage: `url('/images/mountain_bg.png')` }"
    >
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

                <h2 class="text-[20px] font-medium text-[#333333] text-center mb-2">Verify OTP Code</h2>
                <p class="text-[14px] text-[#666666] text-center mb-8">
                    We've sent a verification code to <strong>{{ email }}</strong>. Please enter it below.
                </p>

                <!-- Success/Status message -->
                <div v-if="$page.props.flash?.status" class="w-full max-w-[380px] mb-4 p-3 bg-green-100 border border-green-300 rounded-lg text-green-800 text-sm text-center">
                    {{ $page.props.flash.status }}
                </div>

                <form @submit.prevent="submit" class="w-full max-w-[380px] flex flex-col gap-4">
                    <input type="hidden" v-model="form.email" />

                    <div class="flex flex-col gap-1">
                        <label for="code" class="text-[16px] font-normal text-[#666666]">Verification Code</label>
                        <input
                            id="code"
                            type="text"
                            v-model="form.code"
                            required
                            maxlength="6"
                            placeholder="Enter 6-digit code"
                            class="w-full h-[56px] border border-[#D7DDC2] rounded-xl px-4 bg-[#F8F3E4] text-[#333333] text-center text-2xl tracking-[0.5em] outline-none focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 transition"
                        />
                        <div v-if="form.errors.code" class="text-red-500 text-xs mt-1">{{ form.errors.code }}</div>
                    </div>

                    <button
                        type="submit"
                        class="w-full h-[56px] rounded-[40px] bg-[#374426] text-white font-semibold text-[18px] mt-4 hover:bg-[#2c361e] transition-colors disabled:opacity-60 disabled:cursor-not-allowed cursor-pointer"
                        :disabled="form.processing"
                    >
                        <span v-if="form.processing">Verifying...</span>
                        <span v-else>Verify Code</span>
                    </button>

                    <div class="text-center mt-4">
                        <Link href="/forgot-password" class="text-[#64823E] text-sm font-medium hover:underline">
                            ← Resend code
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>
