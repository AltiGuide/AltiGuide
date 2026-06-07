<script setup>
import { reactive, computed, onMounted } from 'vue'
import { Head, Link, usePage } from '@inertiajs/vue3'
import BookingStepper from './Booking/BookingStepper.vue'
import StepDestination from './Booking/StepDestination.vue'
import StepGroupInit from './Booking/StepGroupInit.vue'
import StepMemberValidation from './Booking/StepMemberValidation.vue'
import StepReview from './Booking/StepReview.vue'
import StepPayment from './Booking/StepPayment.vue'

const props = defineProps({
    mountains: { type: Array, default: () => [] },
    preloadedPayment: { type: Object, default: null },
})

const page = usePage()
const user = computed(() => page.props.auth?.user)

const state = reactive({
    currentStep: 1,
    // Step 1
    selectedMountain: null,
    selectedRoute: null,
    // Step 2
    groupName: '',
    hikeType: 'tektok',
    startDate: '',
    endDate: '',
    memberCount: 2,
    // Step 3
    members: [],
    // Step 5
    orderId: null,
    snapToken: null,
    paymentUrl: null,
    grossAmount: null,
    expiryTime: null,
})

onMounted(() => {
    if (props.preloadedPayment) {
        state.selectedMountain = props.preloadedPayment.selectedMountain
        state.selectedRoute = props.preloadedPayment.selectedRoute
        state.groupName = props.preloadedPayment.groupName
        state.startDate = props.preloadedPayment.startDate
        state.endDate = props.preloadedPayment.endDate
        state.hikeType = props.preloadedPayment.startDate === props.preloadedPayment.endDate ? 'tektok' : 'camp'
        state.memberCount = props.preloadedPayment.memberCount
        state.members = props.preloadedPayment.members
        state.orderId = props.preloadedPayment.orderId
        state.snapToken = props.preloadedPayment.snapToken
        state.paymentUrl = props.preloadedPayment.paymentUrl
        state.grossAmount = props.preloadedPayment.grossAmount
        state.expiryTime = props.preloadedPayment.expiryTime
        state.currentStep = 5
    }
})

/* ── Step navigation ── */
const goToStep = (step) => {
    if (step < state.currentStep) {
        state.currentStep = step
    }
}

/* ── Step 1 handlers ── */
const onSelectDestination = (mountain, route) => {
    state.selectedMountain = mountain
    state.selectedRoute = route
}

const onStep1Next = () => {
    state.currentStep = 2
    scrollToTop()
}

/* ── Step 2 handlers ── */
const onStep2Update = (data) => {
    state.groupName = data.groupName
    state.startDate = data.startDate
    state.endDate = data.endDate
    state.hikeType = data.hikeType
    state.memberCount = data.memberCount
}

/* ── Step 3 handlers ── */
const onStep3Update = (data) => {
    state.members = data.members
}

/* ── Step 4 checkout handler ── */
const onCheckout = (data) => {
    state.orderId = data.orderId
    state.snapToken = data.snapToken
    state.paymentUrl = data.paymentUrl
    state.grossAmount = data.grossAmount
    state.expiryTime = data.expiryTime
    state.currentStep = 5
    scrollToTop()
}

const nextStep = () => {
    state.currentStep++
    scrollToTop()
}

const prevStep = () => {
    state.currentStep--
    scrollToTop()
}

const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
    <div class="booking-page">
        <Head title="Booking Simaksi - AltiGuide">
            <link rel="preconnect" href="https://fonts.googleapis.com" />
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
            <link href="https://fonts.googleapis.com/css2?family=Jost:wght@700&family=Montserrat:wght@400;500;600;700&family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet" />
        </Head>
        <!-- ══════════════ Navbar ══════════════ -->
        <nav class="booking-navbar">
            <div class="nav-left">
                <img src="/images/logo_2.png" alt="AltiGuide Logo" class="nav-logo-img" />
                <span class="nav-logo-text">AltiGuide</span>
            </div>

            <div class="nav-right">
                <Link href="/" class="nav-link-item">Home</Link>
                <Link href="/article" class="nav-link-item">Article</Link>
                <Link href="/booking" class="nav-link-item">Booking</Link>
                <template v-if="user">
                    <Link
                        href="/logout"
                        method="post"
                        as="button"
                        class="nav-btn-logout"
                    >
                        Logout
                    </Link>
                </template>
                <template v-else>
                    <Link href="/login" class="nav-btn-login">Login</Link>
                </template>
            </div>
        </nav>

        <!-- ══════════════ Page Title ══════════════ -->
        <div class="page-header">
            <h1 class="page-title">Alur Simaksi Online</h1>
        </div>

        <!-- ══════════════ Stepper ══════════════ -->
        <div class="stepper-section">
            <BookingStepper :currentStep="state.currentStep" @goTo="goToStep" />
        </div>

        <!-- ══════════════ Step Content ══════════════ -->
        <main class="step-content">
            <transition name="step-fade" mode="out-in">
                <!-- STEP 1: Destinasi & Jalur -->
                <StepDestination
                    v-if="state.currentStep === 1"
                    key="step1"
                    :mountains="mountains"
                    :selectedMountain="state.selectedMountain"
                    :selectedRoute="state.selectedRoute"
                    @select="onSelectDestination"
                    @next="onStep1Next"
                />

                <!-- STEP 2: Inisiasi Grup -->
                <StepGroupInit
                    v-else-if="state.currentStep === 2"
                    key="step2"
                    :selectedMountain="state.selectedMountain"
                    :selectedRoute="state.selectedRoute"
                    :user="user"
                    :groupName="state.groupName"
                    :hikeType="state.hikeType"
                    :startDate="state.startDate"
                    :endDate="state.endDate"
                    :memberCount="state.memberCount"
                    @update="onStep2Update"
                    @next="nextStep"
                    @prev="prevStep"
                />

                <!-- STEP 3: Validasi Anggota -->
                <StepMemberValidation
                    v-else-if="state.currentStep === 3"
                    key="step3"
                    :selectedMountain="state.selectedMountain"
                    :selectedRoute="state.selectedRoute"
                    :memberCount="state.memberCount"
                    :startDate="state.startDate"
                    :hikeType="state.hikeType"
                    :existingMembers="state.members"
                    @update="onStep3Update"
                    @next="nextStep"
                    @prev="prevStep"
                />

                <!-- STEP 4: Review & Pembayaran -->
                <StepReview
                    v-else-if="state.currentStep === 4"
                    key="step4"
                    :selectedMountain="state.selectedMountain"
                    :selectedRoute="state.selectedRoute"
                    :groupName="state.groupName"
                    :startDate="state.startDate"
                    :endDate="state.endDate"
                    :memberCount="state.memberCount"
                    :members="state.members"
                    :user="user"
                    @checkout="onCheckout"
                    @prev="prevStep"
                />

                <!-- STEP 5: Pembayaran QRIS -->
                <StepPayment
                    v-else-if="state.currentStep === 5"
                    key="step5"
                    :orderId="state.orderId"
                    :snapToken="state.snapToken"
                    :paymentUrl="state.paymentUrl"
                    :grossAmount="state.grossAmount"
                    :expiryTime="state.expiryTime"
                    :selectedMountain="state.selectedMountain"
                    :selectedRoute="state.selectedRoute"
                    :groupName="state.groupName"
                    :startDate="state.startDate"
                    :endDate="state.endDate"
                    :memberCount="state.memberCount"
                    :members="state.members"
                    :user="user"
                />
            </transition>
        </main>

        <!-- ══════════════ Footer ══════════════ -->
        <div class="footer-section">
            <!-- CTA Banner -->
            <div class="footer-cta">
                <div class="cta-brand">
                    <img src="/images/logo_2.png" alt="AltiGuide Logo" class="cta-logo" />
                    <span class="cta-brand-name">AltiGuide</span>
                </div>
                <div class="cta-buttons">
                    <Link href="mailto:support@altiguide.com" class="cta-btn cta-btn-primary">Contact Us</Link>
                    <Link href="/booking" class="cta-btn cta-btn-secondary">Start Summit</Link>
                </div>
            </div>

            <!-- Footer Links -->
            <footer class="footer-links">
                <div class="footer-top">
                    <div class="footer-left">
                        <Link href="/" class="footer-site-link">AltiGuide.com</Link>
                        <div class="footer-socials">
                            <a href="#" class="social-icon">
                                <svg class="w-7 h-7" fill="currentColor" viewBox="0 0 24 24"><path d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12c0 4.991 3.657 9.128 8.438 9.878v-6.987h-2.54V12h2.54V9.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V12h2.773l-.443 2.89h-2.33v6.988C18.343 21.128 22 16.991 22 12z"/></svg>
                            </a>
                            <a href="#" class="social-icon">
                                <svg class="w-7 h-7" fill="currentColor" viewBox="0 0 24 24"><path d="M21.582 6.186a2.66 2.66 0 0 0-1.875-1.884C18.053 3.86 12 3.86 12 3.86s-6.053 0-7.707.442a2.66 2.66 0 0 0-1.875 1.884C2 7.854 2 12 2 12s0 4.146.418 5.814a2.66 2.66 0 0 0 1.875 1.884C5.947 20.14 12 20.14 12 20.14s6.053 0 7.707-.442a2.66 2.66 0 0 0 1.875-1.884C22 16.146 22 12 22 12s0-4.146-.418-5.814zM9.88 15.15V8.85l6.32 3.15-6.32 3.15z"/></svg>
                            </a>
                            <a href="https://www.instagram.com/altiguide___?igsh=MTRwbW8zbW8wZDVubg==" target="_blank" rel="noopener noreferrer" class="social-icon">
                                <svg class="w-7 h-7" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zM12 0C8.741 0 8.333.014 7.053.072 2.695.272.273 2.69.073 7.052.014 8.333 0 8.741 0 12c0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98C8.333 23.986 8.741 24 12 24c3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98C15.668.014 15.259 0 12 0zm0 5.838a6.162 6.162 0 1 0 0 12.324 6.162 6.162 0 0 0 0-12.324zM12 16a4 4 0 1 1 0-8 4 4 0 0 1 0 8zm6.406-11.845a1.44 1.44 0 1 0 0 2.881 1.44 1.44 0 0 0 0-2.881z"/></svg>
                            </a>
                        </div>
                    </div>

                    <div class="footer-columns">
                        <div class="footer-col">
                            <h5 class="footer-col-title">Jelajahi</h5>
                            <div class="footer-col-links">
                                <Link href="/mountains">Daftar Gunung</Link>
                                <Link href="/">Weather Analytics</Link>
                            </div>
                        </div>
                        <div class="footer-col">
                            <h5 class="footer-col-title">Informasi</h5>
                            <div class="footer-col-links">
                                <Link href="/article">Tata Tertib</Link>
                                <Link href="/booking">Booking Simaksi</Link>
                                <Link href="/article">Tips Keamanan</Link>
                            </div>
                        </div>
                        <div class="footer-col">
                            <h5 class="footer-col-title">Komunitas</h5>
                            <div class="footer-col-links">
                                <Link href="/">Forum Diskusi</Link>
                                <!-- <Link href="/about">Tentang Kami</Link> -->
                            </div>
                        </div>
                    </div>
                </div>

                <div class="footer-bottom">
                    <p>© 2026 AltiGuide Team. All rights reserved.</p>
                </div>
            </footer>
        </div>
    </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700&display=swap');

/* ══════ Page ══════ */
.booking-page {
    min-height: 100vh;
    background: #F4F1E6;
    display: flex;
    flex-direction: column;
    font-family: 'Montserrat', sans-serif;
}

/* ══════ Navbar ══════ */
.booking-navbar {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 32px;
    background: rgba(55, 68, 38, 0.08);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border-bottom: 1px solid #D6CCAF;
    position: sticky;
    top: 0;
    z-index: 100;
}

.nav-left {
    display: flex;
    align-items: center;
    gap: 10px;
}

.nav-logo-img {
    width: 36px;
    height: 36px;
    object-fit: contain;
}

.nav-logo-text {
    font-weight: 700;
    font-size: 24px;
    color: #374426;
    letter-spacing: -0.02em;
}

.nav-right {
    display: flex;
    align-items: center;
    gap: 28px;
    font-size: 15px;
    font-weight: 600;
}

.nav-link-item {
    color: #3b4b3b;
    text-decoration: none;
    transition: color 0.2s ease;
}

.nav-link-item:hover {
    color: #000;
}

.nav-btn-login {
    border: 1.5px solid #3b4b3b;
    padding: 8px 22px;
    border-radius: 8px;
    color: #3b4b3b;
    text-decoration: none;
    transition: all 0.2s ease;
    white-space: nowrap;
}

.nav-btn-login:hover {
    background: #3b4b3b;
    color: #fff;
}

.nav-btn-logout {
    border: 1.5px solid #dc2626;
    padding: 8px 18px;
    border-radius: 8px;
    color: #dc2626;
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 14px;
    background: none;
    cursor: pointer;
    transition: all 0.2s ease;
}

.nav-btn-logout:hover {
    background: #dc2626;
    color: #fff;
}

/* ══════ Page Header ══════ */
.page-header {
    text-align: center;
    padding: 56px 24px 24px;
}

.page-title {
    font-weight: 700;
    font-size: 36px;
    color: #66533A;
    letter-spacing: -0.01em;
    text-shadow: none;
    -webkit-text-stroke: 0.5px #796041;
}

/* ══════ Stepper Section ══════ */
.stepper-section {
    padding: 16px 24px 48px;
}

/* ══════ Step Content ══════ */
.step-content {
    flex: 1;
    padding: 0 32px 64px;
}

/* ── Step transition ── */
.step-fade-enter-active {
    animation: stepIn 0.35s cubic-bezier(0.22, 1, 0.36, 1) both;
}
.step-fade-leave-active {
    animation: stepOut 0.2s cubic-bezier(0.55, 0, 1, 0.45) both;
}

@keyframes stepIn {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

@keyframes stepOut {
    from { opacity: 1; transform: translateY(0); }
    to { opacity: 0; transform: translateY(-12px); }
}

/* ══════ Footer ══════ */
.footer-section {
    margin-top: auto;
}

.footer-cta {
    background: #E0DBBE;
    padding: 32px 48px;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.cta-brand {
    display: flex;
    align-items: center;
    gap: 12px;
}

.cta-logo { width: 40px; height: 40px; object-fit: contain; }
.cta-brand-name { font-weight: 700; font-size: 28px; color: #374426; }

.cta-buttons { display: flex; gap: 12px; }

.cta-btn {
    display: inline-block;
    font-weight: 600;
    font-size: 18px;
    padding: 14px 28px;
    border-radius: 12px;
    text-decoration: none;
    transition: opacity 0.2s ease;
}

.cta-btn-primary { background: #374426; color: #F8F3E4; }
.cta-btn-secondary { background: #F8F3E4; color: #374426; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.cta-btn:hover { opacity: 0.88; }

.footer-links {
    background: #fff;
    padding: 36px 48px;
}

.footer-top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 48px;
    margin-bottom: 28px;
}

.footer-left {
    display: flex;
    flex-direction: column;
    gap: 80px;
}

.footer-site-link {
    font-weight: 500;
    font-size: 20px;
    color: #374426;
    text-decoration: underline;
    text-underline-offset: 6px;
}

.footer-socials { display: flex; gap: 20px; }
.social-icon { color: #828282; transition: color 0.2s ease; }
.social-icon:hover { color: #374426; }
.social-icon svg { width: 24px; height: 24px; }

.footer-columns { display: flex; gap: 64px; }
.footer-col { display: flex; flex-direction: column; gap: 16px; }
.footer-col-title { font-weight: 600; font-size: 16px; color: #374426; }
.footer-col-links { display: flex; flex-direction: column; gap: 12px; }
.footer-col-links a { font-weight: 500; font-size: 14px; color: #5A684C; text-decoration: none; transition: color 0.2s ease; }
.footer-col-links a:hover { color: #374426; }

.footer-bottom {
    border-top: 1px solid #D7DDC2;
    padding-top: 20px;
    display: flex;
    justify-content: flex-end;
}

.footer-bottom p { font-weight: 500; font-size: 13px; color: #5A684C; }

/* ══════ Responsive ══════ */
@media (max-width: 768px) {
    .booking-navbar { padding: 12px 16px; }
    .nav-right { gap: 14px; font-size: 13px; }
    .nav-logo-text { font-size: 20px; }
    .page-title { font-size: 28px; }
    .step-content { padding: 0 16px 32px; }
    .footer-cta { flex-direction: column; gap: 16px; padding: 24px; text-align: center; }
    .footer-top { flex-direction: column; }
    .footer-columns { flex-direction: column; gap: 24px; }
    .footer-links { padding: 24px; }
}
</style>
