<script setup>
import { ref, computed } from 'vue'
import { Head, Link, useForm, router } from '@inertiajs/vue3'
import axios from 'axios'

const loadGoogleSdk = () => {
    return new Promise((resolve, reject) => {
        if (window.google) {
            resolve();
            return;
        }
        const script = document.createElement('script');
        script.src = 'https://accounts.google.com/gsi/client';
        script.async = true;
        script.defer = true;
        script.onload = () => resolve();
        script.onerror = () => reject(new Error('Google SDK load failed'));
        document.head.appendChild(script);
    });
};

const handleGoogleCallback = async (accessToken) => {
    try {
        const response = await axios.post('/auth/google', { token: accessToken });
        const { role, user_status, redirect, temp_user } = response.data;

        if (role === 'admin') {
            router.visit(redirect);
            return;
        }

        if (user_status === 'old') {
            const intendedUrl = localStorage.getItem('intended_url') || '/dashboard';
            localStorage.removeItem('intended_url');
            router.visit(intendedUrl);
            return;
        }

        if (user_status === 'new') {
            sessionStorage.setItem('temp_google_user', JSON.stringify(temp_user));
            router.visit('/complete-profile');
        }
    } catch (error) {
        console.error(error);
    }
};

const initGoogleLogin = async () => {
    try {
        await loadGoogleSdk();
        const client = google.accounts.oauth2.initTokenClient({
            client_id: import.meta.env.VITE_GOOGLE_CLIENT_ID || '1096057398188-12cfa0cpltr44g3l3d9hmoe7c4f6990j.apps.googleusercontent.com',
            scope: 'email profile openid',
            callback: async (response) => {
                if (response.access_token) {
                    await handleGoogleCallback(response.access_token);
                }
            }
        });
        client.requestAccessToken();
    } catch (error) {
        console.error(error);
    }
};


const props = defineProps({
    defaultTab: {
        type: String,
        default: 'login'
    }
})

const activeTab = ref(props.defaultTab)
const showPassword = ref(false)

/* ── Login form ───────────────────────────────────────────────── */
const loginForm = useForm({
    email: '',
    password: '',
    remember: false,
})

const submitLogin = () => {
    loginForm.post('/login', {
        onFinish: () => loginForm.reset('password'),
    })
}

/* ── Sign-up form ─────────────────────────────────────────────── */
const signupForm = useForm({
    name: '',
    email: '',
    phone_number: '',
    password: '',
})

const submitSignup = () => {
    signupForm.post('/register', {
        onFinish: () => {
            signupForm.reset('password')
        },
    })
}

const togglePassword = () => {
    showPassword.value = !showPassword.value
}

const isLoginValid = computed(() => {
    return loginForm.email.trim() !== '' && loginForm.password.trim() !== ''
})

const isSignupValid = computed(() => {
    return signupForm.name.trim() !== '' &&
           signupForm.email.trim() !== '' &&
           signupForm.phone_number.trim() !== '' &&
           signupForm.password.trim() !== ''
})

/* ── Forgot password modal ────────────────────────────────────── */
const showForgotModal = ref(false)
const forgotForm = useForm({
    email: '',
})

const submitForgot = () => {
    forgotForm.post('/forgot-password/email')
}
</script>

<template>
    <div
        class="min-h-screen w-full bg-cover bg-center bg-no-repeat relative flex flex-col"
        :style="{ backgroundImage: `url('/images/mountain_bg.png')` }"
    >
        <Head title="Login - AltiGuide">
            <link rel="preconnect" href="https://fonts.googleapis.com" />
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
            <link href="https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&family=Montserrat:wght@400;500;600;700&display=swap" rel="stylesheet" />
        </Head>
        <!-- Overlay -->
        <div class="absolute inset-0 bg-black/10 z-0"></div>

        <!-- ══════════════ Navbar (identical to Welcome.vue) ══════════════ -->
        <nav class="w-full flex justify-between items-center px-4 md:px-8 xl:px-12 py-4 relative z-50 text-[#3b4b3b] font-semibold bg-[#374426]/20 backdrop-blur-md border-b border-white/20 shadow-sm">
            <div class="flex items-center gap-2 xl:gap-3">
                <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 md:w-10 md:h-10 object-contain" />
                <span class="text-xl md:text-2xl xl:text-3xl font-bold text-[#3b4b3b] tracking-tight">AltiGuide</span>
            </div>

            <div class="flex items-center gap-4 md:gap-6 xl:gap-9 text-sm md:text-base">
                <Link href="/" class="hover:text-black transition">Home</Link>
                <Link href="#" class="hover:text-black transition">Article</Link>
                <Link href="/booking" class="hover:text-black transition">Booking</Link>
                <Link href="/login" class="border border-[#3b4b3b] px-4 md:px-6 py-2 rounded-lg hover:bg-[#3b4b3b] hover:text-white transition duration-200 whitespace-nowrap">
                    Login
                </Link>
            </div>
        </nav>

        <!-- ══════════════ Main content ══════════════ -->
        <div class="flex-1 flex items-center justify-center z-10 py-8">

            <!-- Login / Sign-up Container — 732 × 820 -->
            <div class="auth-container">

                <!-- ── Logo + Title ── -->
                <div class="flex items-center justify-center gap-3 mb-8">
                    <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-10 h-10 object-contain" />
                    <span class="auth-title">AltiGuide</span>
                </div>

                <!-- ── Tabs ── -->
                <div class="flex justify-center mb-6">
                    <button
                        @click="activeTab = 'signup'; showPassword = false"
                        :class="activeTab === 'signup' ? 'auth-tab-active' : 'auth-tab-inactive'"
                        class="auth-tab rounded-l-xl"
                    >
                        Sign up
                    </button>
                    <button
                        @click="activeTab = 'login'; showPassword = false"
                        :class="activeTab === 'login' ? 'auth-tab-active' : 'auth-tab-inactive'"
                        class="auth-tab rounded-r-xl"
                    >
                        Log in
                    </button>
                </div>

                <!-- ── Heading ── -->
                <h2 class="auth-heading">
                    {{ activeTab === 'login' ? 'Login' : 'Sign up' }}
                </h2>

                <button class="google-btn" type="button" @click="initGoogleLogin">
                    <svg class="w-5 h-5" viewBox="0 0 24 24">
                        <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
                        <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                        <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                        <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                    </svg>
                    <span>{{ activeTab === 'login' ? 'Login with Google' : 'Sign up with Google' }}</span>
                </button>

                <!-- ── OR divider ── -->
                <div class="or-divider">
                    <div class="or-line"></div>
                    <span class="or-text">OR</span>
                    <div class="or-line"></div>
                </div>

                <!-- ═══════════════════════════════════════════════════════ -->
                <!--  LOGIN FORM                                            -->
                <!-- ═══════════════════════════════════════════════════════ -->
                <form v-if="activeTab === 'login'" @submit.prevent="submitLogin" class="auth-form">

                    <!-- Email -->
                    <div class="field-group">
                        <label for="login-email" class="field-label">Email address</label>
                        <input
                            id="login-email"
                            type="email"
                            v-model="loginForm.email"
                            required
                            class="field-input"
                        />
                        <div v-if="loginForm.errors.email" class="field-error">{{ loginForm.errors.email }}</div>
                    </div>

                    <!-- Password -->
                    <div class="field-group">
                        <div class="flex items-center justify-between">
                            <label for="login-password" class="field-label">Your Password</label>
                            <button type="button" @click="togglePassword" class="password-toggle">
                                <!-- Eye icon -->
                                <svg v-if="!showPassword" xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                                </svg>
                                <svg v-else xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M13.875 18.825A10.05 10.05 0 0 1 12 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 0 1 1.563-3.029m5.858.908a3 3 0 1 1 4.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88 3 3m6.878 6.879L21 21"/>
                                </svg>
                                <span class="text-xs">{{ showPassword ? 'Hide' : 'Show' }}</span>
                            </button>
                        </div>
                        <input
                            id="login-password"
                            :type="showPassword ? 'text' : 'password'"
                            v-model="loginForm.password"
                            required
                            class="field-input"
                        />
                        <div v-if="loginForm.errors.password" class="field-error">{{ loginForm.errors.password }}</div>
                    </div>

                    <!-- Forgot password -->
                    <div class="flex justify-end -mt-1">
                        <button type="button" @click="showForgotModal = true" class="forgot-link bg-none border-none p-0 outline-none cursor-pointer">Forget your password</button>
                    </div>

                    <!-- Submit -->
                    <div class="flex justify-center mt-6">
                        <button
                            type="submit"
                            class="submit-btn"
                            :class="{ 'btn-ready': isLoginValid }"
                            :disabled="loginForm.processing"
                        >
                            <span v-if="loginForm.processing">Logging in...</span>
                            <span v-else>Login</span>
                        </button>
                    </div>
                </form>

                <!-- ═══════════════════════════════════════════════════════ -->
                <!--  SIGN-UP FORM                                          -->
                <!-- ═══════════════════════════════════════════════════════ -->
                <form v-else @submit.prevent="submitSignup" class="auth-form">

                    <!-- Nama Lengkap -->
                    <div class="field-group">
                        <label for="signup-name" class="field-label">Nama Lengkap</label>
                        <input
                            id="signup-name"
                            type="text"
                            v-model="signupForm.name"
                            required
                            class="field-input"
                        />
                        <div v-if="signupForm.errors.name" class="field-error">{{ signupForm.errors.name }}</div>
                    </div>

                    <!-- Email -->
                    <div class="field-group">
                        <label for="signup-email" class="field-label">Email address</label>
                        <input
                            id="signup-email"
                            type="email"
                            v-model="signupForm.email"
                            required
                            class="field-input"
                        />
                        <div v-if="signupForm.errors.email" class="field-error">{{ signupForm.errors.email }}</div>
                    </div>

                    <!-- No. HP -->
                    <div class="field-group">
                        <label for="signup-phone" class="field-label">No. Handphone</label>
                        <input
                            id="signup-phone"
                            type="text"
                            v-model="signupForm.phone_number"
                            required
                            class="field-input"
                        />
                        <div v-if="signupForm.errors.phone_number" class="field-error">{{ signupForm.errors.phone_number }}</div>
                    </div>

                    <!-- Password -->
                    <div class="field-group">
                        <div class="flex items-center justify-between">
                            <label for="signup-password" class="field-label">Password</label>
                            <button type="button" @click="togglePassword" class="password-toggle">
                                <svg v-if="!showPassword" xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                                </svg>
                                <svg v-else xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M13.875 18.825A10.05 10.05 0 0 1 12 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 0 1 1.563-3.029m5.858.908a3 3 0 1 1 4.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88 3 3m6.878 6.879L21 21"/>
                                </svg>
                                <span class="text-xs">{{ showPassword ? 'Hide' : 'Show' }}</span>
                            </button>
                        </div>
                        <input
                            id="signup-password"
                            :type="showPassword ? 'text' : 'password'"
                            v-model="signupForm.password"
                            required
                            class="field-input"
                        />
                        <div v-if="signupForm.errors.password" class="field-error">{{ signupForm.errors.password }}</div>
                        <p class="text-xs text-[#666666] mt-1" style="font-family: 'Montserrat', sans-serif;">
                            Use 8 or more characters with a mix of letters, numbers &amp; symbols
                        </p>
                    </div>



                    <!-- Submit -->
                    <div class="flex justify-center mt-6">
                        <button
                            type="submit"
                            class="submit-btn"
                            :class="{ 'btn-ready': isSignupValid }"
                            :disabled="signupForm.processing"
                        >
                            <span v-if="signupForm.processing">Creating account...</span>
                            <span v-else>Sign up</span>
                        </button>
                    </div>
                </form>

            </div>
        </div>
    </div>

    <!-- Forgot Password Modal -->
    <div v-if="showForgotModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="showForgotModal = false"></div>
        
        <!-- Modal Card -->
        <div class="relative w-full max-w-[480px] bg-[#F8F3E4] rounded-[20px] border border-[#D6CCAF] shadow-2xl p-8 md:p-10 flex flex-col items-center z-10 animate-fade-in animate-duration-200" style="font-family: 'Montserrat', sans-serif;">
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
</template>

<style scoped>
/* ── Container ───────────────────────────────────────────────── */
.auth-container {
    width: 732px;
    min-height: 820px;
    background: #F8F3E4;
    border-radius: 20px;
    border: 1.5px solid #D6CCAF;
    box-shadow: 0 25px 60px rgba(0, 0, 0, 0.25);
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px 80px;
}

/* ── Title (logo text) ───────────────────────────────────────── */
.auth-title {
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 40px;
    color: #374426;
    letter-spacing: -0.02em;
}

/* ── Tabs ─────────────────────────────────────────────────────── */
.auth-tab {
    width: 302px;
    height: 56px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 500;
    font-size: 18px;
    cursor: pointer;
    transition: background-color 0.25s ease, color 0.25s ease;
    border: none;
    outline: none;
}

.auth-tab-active {
    background-color: #374426;
    color: #ffffff;
}

.auth-tab-inactive {
    background-color: #BEBAAF;
    color: #333333;
}

.auth-heading {
    font-family: 'Montserrat', sans-serif;
    font-weight: 400;
    font-size: 24px;
    color: #333333;
    text-align: center;
    margin-bottom: 20px;
}

/* ── Google button ────────────────────────────────────────────── */
.google-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    width: 100%;
    max-width: 454px;
    height: 56px;
    border: 1.5px solid #D7DDC2;
    border-radius: 40px;
    background: #ffffff;
    font-family: 'Montserrat', sans-serif;
    font-weight: 500;
    font-size: 16px;
    color: #333333;
    cursor: pointer;
    transition: background-color 0.2s ease, box-shadow 0.2s ease;
}

.google-btn:hover {
    background-color: #f5f5f5;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* ── OR divider ───────────────────────────────────────────────── */
.or-divider {
    display: flex;
    align-items: center;
    gap: 16px;
    width: 100%;
    max-width: 454px;
    margin: 20px 0;
}

.or-line {
    flex: 1;
    height: 1px;
    background: #C4C4C4;
}

.or-text {
    font-family: 'Montserrat', sans-serif;
    font-weight: 500;
    font-size: 18px;
    color: #666666;
}

/* ── Form ─────────────────────────────────────────────────────── */
.auth-form {
    width: 100%;
    max-width: 454px;
    display: flex;
    flex-direction: column;
    gap: 16px;
}

/* ── Field group (label + input together, label margin 4px) ──── */
.field-group {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.field-label {
    font-family: 'Montserrat', sans-serif;
    font-weight: 400;
    font-size: 16px;
    color: #666666;
}

.field-input {
    width: 100%;
    height: 56px;
    border: 1.5px solid #D7DDC2;
    border-radius: 12px;
    padding: 0 16px;
    font-family: 'Montserrat', sans-serif;
    font-size: 15px;
    color: #333333;
    background: #F8F3E4;
    outline: none;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.field-input:focus {
    border-color: #64823E;
    box-shadow: 0 0 0 3px rgba(100, 130, 62, 0.15);
}

/* ── Password toggle ──────────────────────────────────────────── */
.password-toggle {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #666666;
    cursor: pointer;
    background: none;
    border: none;
    font-family: 'Montserrat', sans-serif;
    font-weight: 500;
    transition: color 0.2s ease;
}

.password-toggle:hover {
    color: #374426;
}

/* ── Forgot password link ─────────────────────────────────────── */
.forgot-link {
    font-family: 'Montserrat', sans-serif;
    font-weight: 500;
    font-size: 13px;
    color: #64823E;
    text-decoration: underline;
    text-underline-offset: 3px;
    transition: color 0.2s ease;
}

.forgot-link:hover {
    color: #374426;
}

/* ── Submit button ────────────────────────────────────────────── */
.submit-btn {
    width: 454px;
    height: 64px;
    border-radius: 40px;
    background-color: #BEBAAF;
    color: #ffffff;
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 20px;
    border: none;
    cursor: not-allowed;
    transition: background-color 0.25s ease, transform 0.15s ease, box-shadow 0.25s ease;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.submit-btn.btn-ready {
    background-color: #64823E;
    cursor: pointer;
}

.submit-btn.btn-ready:hover:not(:disabled) {
    background-color: #BEBAAF;
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.18);
}

.submit-btn.btn-ready:active:not(:disabled) {
    transform: translateY(0);
}

.submit-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* ── Error text ───────────────────────────────────────────────── */
.field-error {
    font-family: 'Montserrat', sans-serif;
    font-size: 13px;
    color: #dc2626;
    margin-top: 2px;
}

/* ── Responsive fallback for smaller screens ──────────────────── */
@media (max-width: 800px) {
    .auth-container {
        width: 95vw;
        min-height: auto;
        padding: 32px 24px;
    }

    .auth-tab {
        width: 50%;
        font-size: 18px;
        height: 48px;
    }

    .submit-btn {
        width: 100%;
        height: 56px;
        font-size: 18px;
    }

    .google-btn {
        max-width: 100%;
    }

    .or-divider {
        max-width: 100%;
    }

    .auth-form {
        max-width: 100%;
    }
}
</style>
