<script setup>
import { ref } from 'vue'
import { Head, Link, useForm } from '@inertiajs/vue3'

const props = defineProps({
    email: String,
    code: String,
})

const showPassword = ref(false)

const form = useForm({
    email: props.email || '',
    code: props.code || '',
    password: '',
    password_confirmation: '',
})

const submit = () => {
    form.post('/reset-password', {
        onFinish: () => {
            form.reset('password')
            form.reset('password_confirmation')
        },
    })
}

const togglePassword = () => {
    showPassword.value = !showPassword.value
}
</script>

<template>
    <Head title="Reset Password - AltiGuide">
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

                <h2 class="text-[20px] font-medium text-[#333333] text-center mb-2">Reset Password</h2>
                <p class="text-[14px] text-[#666666] text-center mb-8">
                    Create a new password for your account.
                </p>

                <form @submit.prevent="submit" class="w-full max-w-[380px] flex flex-col gap-4">
                    <input type="hidden" v-model="form.email" />
                    <input type="hidden" v-model="form.code" />

                    <!-- New Password -->
                    <div class="flex flex-col gap-1">
                        <div class="flex items-center justify-between">
                            <label for="password" class="text-[16px] font-normal text-[#666666]">New Password</label>
                            <button type="button" @click="togglePassword" class="flex items-center gap-1 text-[#666666] text-xs cursor-pointer hover:text-[#374426] transition">
                                <svg v-if="!showPassword" xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                                </svg>
                                <svg v-else xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M13.875 18.825A10.05 10.05 0 0 1 12 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 0 1 1.563-3.029m5.858.908a3 3 0 1 1 4.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88 3 3m6.878 6.879L21 21"/>
                                </svg>
                                <span>{{ showPassword ? 'Show' : 'Hide' }}</span>
                            </button>
                        </div>
                        <input
                            id="password"
                            :type="showPassword ? 'text' : 'password'"
                            v-model="form.password"
                            required
                            class="w-full h-[56px] border border-[#D7DDC2] rounded-xl px-4 bg-[#F8F3E4] text-[#333333] outline-none focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 transition"
                        />
                        <div v-if="form.errors.password" class="text-red-500 text-xs mt-1">{{ form.errors.password }}</div>
                        <p class="text-xs text-[#666666] mt-1">
                            Use 8 or more characters with a mix of letters, numbers &amp; symbols
                        </p>
                    </div>

                    <!-- Confirm Password -->
                    <div class="flex flex-col gap-1">
                        <label for="password_confirmation" class="text-[16px] font-normal text-[#666666]">Confirm Password</label>
                        <input
                            id="password_confirmation"
                            :type="showPassword ? 'text' : 'password'"
                            v-model="form.password_confirmation"
                            required
                            class="w-full h-[56px] border border-[#D7DDC2] rounded-xl px-4 bg-[#F8F3E4] text-[#333333] outline-none focus:border-[#64823E] focus:ring-2 focus:ring-[#64823E]/15 transition"
                        />
                    </div>

                    <button
                        type="submit"
                        class="w-full h-[56px] rounded-[40px] bg-[#374426] text-white font-semibold text-[18px] mt-4 hover:bg-[#2c361e] transition-colors disabled:opacity-60 disabled:cursor-not-allowed cursor-pointer"
                        :disabled="form.processing"
                    >
                        <span v-if="form.processing">Resetting...</span>
                        <span v-else>Reset Password</span>
                    </button>

                    <div class="text-center mt-4">
                        <Link href="/login" class="text-[#64823E] text-sm font-medium hover:underline">
                            ← Back to Login
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>
