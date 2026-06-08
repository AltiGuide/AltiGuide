<script setup>
import { onMounted, ref, onUnmounted } from 'vue'
import { Link, Head, usePage } from '@inertiajs/vue3'
import { computed } from 'vue'

const page = usePage()
const authUser = computed(() => page.props.auth?.user)

const slides = [
  { image: '/images/gunung_andong_1.png', name: 'Gunung Andong', description: 'Pilihan tepat untuk pendakian santai di akhir pekan bersama teman dengan pemandangan 360 derajat yang memperlihatkan deretan gunung di Jawa Tengah.' },
  { image: '/images/gunung_lawu_2.png', name: 'Gunung Lawu', description: 'Rasakan perpaduan unik antara petualangan mendaki dan wisata religi di jalur penuh sejarah dengan pemandangan matahari terbit yang melegenda.' },
  { image: '/images/gunung_slamet_3.png', name: 'Gunung Slamet', description: 'Taklukkan "Atap Jawa Tengah", puncak tertinggi yang menantang adrenalin dengan medan pasir dan bebatuan khas gunung api purba.' },
  { image: '/images/gunung_sumbing_4.png', name: 'Gunung Sumbing', description: 'Jelajahi "Nepal van Java" dan taklukkan puncak tertinggi kedua di Jawa Tengah dengan jalur yang menantang namun eksotis.' },
  { image: '/images/gunung_ungaran_5.png', name: 'Gunung Ungaran', description: 'Eksplorasi jalur hutan yang asri dan temukan kedamaian di puncak terdekat dari pusat kota Semarang yang menawarkan pemandangan lampu kota di malam hari.' },
  { image: '/images/gunung_merbabu_6.png', name: 'Gunung Merbabu', description: 'Nikmati sensasi mendaki di atas hamparan sabana hijau yang luas dan pandangi kemegahan Gunung Merapi tepat di depan matamu.' },
  { image: '/images/gunung_prau_7.png', name: 'Gunung Prau', description: 'Nikmati panorama "Golden Sunrise" terbaik di Asia Tenggara dengan trek yang ramah bagi pemula namun tetap memberikan pemandangan yang mewah.' },
  { image: '/images/gunung_sindoro_8.png', name: 'Gunung Sindoro', description: 'Uji fisikmu di jalur menanjak yang menantang menuju puncak kawah vulkanik aktif yang menawarkan pemandangan "saudara kembarnya", Gunung Sumbing.' },
]

const faqs = ref([
  {
    question: "Apa itu aplikasi AltiGuide?",
    answer: "AltiGuide adalah platform panduan pendakian gunung digital yang menyediakan informasi rute pendakian, peta offline, pemesanan tiket SIMAKSI secara online, serta pemantauan posisi GPS untuk menunjang keamanan dan kenyamanan pendakian Anda.",
    open: false
  },
  {
    question: "Apakah peta navigasi tetap berfungsi jika tidak ada sinyal?",
    answer: "Ya! Aplikasi mobile AltiGuide mendukung fitur peta offline (Offline Maps) yang terintegrasi dengan GPS smartphone Anda. Anda dapat mengunduh peta rute gunung terlebih dahulu sebelum memulai pendakian di area tanpa sinyal.",
    open: false
  },
  {
    question: "Bagaimana cara mendapatkan info terbaru mengenai status jalur?",
    answer: "Informasi status jalur pendakian (buka/tutup, cuaca ekstrem, atau kendala lainnya) akan diperbarui secara berkala oleh pengelola basecamp resmi dan ditampilkan langsung pada halaman informasi detail gunung di aplikasi AltiGuide.",
    open: false
  },
  {
    question: "Apakah data estimasi waktu tempuh sudah akurat?",
    answer: "Estimasi waktu tempuh dihitung menggunakan algoritma khusus yang mempertimbangkan jarak trek, tingkat kemiringan elevasi rute, dan rata-rata kecepatan pendaki umum. Namun, waktu tempuh aktual tetap bergantung pada kondisi fisik dan durasi istirahat rombongan Anda.",
    open: false
  },
  {
    question: "Bisakah saya memesan simaksi atau tiket melalui web AltiGuide?",
    answer: "Tentu saja! Anda dapat melakukan booking tiket SIMAKSI secara mudah dan melakukan pembayaran online secara instan melalui web AltiGuide. E-Ticket resmi akan langsung diterbitkan ke akun Anda setelah pembayaran selesai diverifikasi.",
    open: false
  },
  {
    question: "Bagaimana jika saya tersesat atau keluar dari jalur yang ditentukan?",
    answer: "Aplikasi kami dilengkapi fitur pelacak posisi (Track Position) real-time menggunakan GPS. Jika Anda terdeteksi keluar dari jalur resmi, sistem navigasi akan memberikan peringatan dan membantu memandu Anda kembali ke jalur pendakian yang aman.",
    open: false
  },
  {
    question: "Apakah data sumber air dan pos pendakian selalu diperbarui?",
    answer: "Ya, semua data titik penting seperti pos pendakian, shelter camp, sumber air terdekat, hingga area rawan bahaya selalu diperbarui berdasarkan laporan basecamp resmi serta kontribusi komunitas pendaki terverifikasi.",
    open: false
  }
])

const toggleFaq = (index) => {
  faqs.value[index].open = !faqs.value[index].open
}

const currentSlide = ref(0)
let slideInterval = null

const nextSlide = () => {
  currentSlide.value = (currentSlide.value + 1) % slides.length
}

const setSlide = (idx) => {
  currentSlide.value = idx
  resetInterval()
}

const startInterval = () => {
  slideInterval = setInterval(nextSlide, 4000)
}

const resetInterval = () => {
  if (slideInterval) clearInterval(slideInterval)
  startInterval()
}

onMounted(() => {
  startInterval()

  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('opacity-100', 'translate-y-0')
        entry.target.classList.remove('opacity-0', 'translate-y-10')
        observer.unobserve(entry.target)
      }
    })
  }, {
    threshold: 0.1
  })

  const revealElements = document.querySelectorAll('.scroll-reveal')
  revealElements.forEach((el) => observer.observe(el))
})

onUnmounted(() => {
  if (slideInterval) clearInterval(slideInterval)
})
</script>

<template>
  <div class="w-full text-slate-800">
    <Head title="AltiGuide">
      <link rel="preconnect" href="https://fonts.googleapis.com" />
      <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
      <link href="https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap" rel="stylesheet" />
    </Head>

    <section 
      :style="{ backgroundImage: `url('/images/mountain_bg.png')` }" 
      class="min-h-screen w-full bg-cover bg-center bg-no-repeat relative flex flex-col font-sans"
    >
      <div class="absolute inset-0 bg-black/10 z-0"></div>

      <nav class="w-full flex justify-between items-center px-4 md:px-8 xl:px-12 py-4 relative z-50 text-[#3b4b3b] font-semibold bg-[#374426]/20 backdrop-blur-md border-b border-white/20 shadow-sm">
        <div class="flex items-center gap-2 xl:gap-3">
          <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 md:w-10 md:h-10 object-contain" />
          <span class="text-xl md:text-2xl xl:text-3xl font-bold text-[#3b4b3b] tracking-tight">AltiGuide</span>
        </div>

        <div class="flex items-center gap-4 md:gap-6 xl:gap-9 text-sm md:text-base">
          <a href="/" class="hover:text-black transition">Home</a>
          <a href="/article" class="hover:text-black transition">Article</a>
          <a href="/booking" class="hover:text-black transition">Booking</a>
          <!-- Auth Button -->
          <template v-if="authUser">
            <a
              href="/dashboard"
              class="flex items-center gap-2 border border-[#3b4b3b] px-4 md:px-5 py-2 rounded-lg hover:bg-[#3b4b3b] hover:text-white transition duration-200 whitespace-nowrap"
            >
              <img
                v-if="authUser.avatar_url"
                :src="authUser.avatar_url"
                class="w-6 h-6 rounded-full object-cover"
                :alt="authUser.name"
              />
              <span v-else class="w-6 h-6 rounded-full bg-[#64823E] flex items-center justify-center text-white text-xs font-bold">
                {{ authUser.name?.charAt(0)?.toUpperCase() }}
              </span>
              <span>My Dashboard</span>
            </a>
          </template>
          <template v-else>
            <a href="/login" class="border border-[#3b4b3b] px-4 md:px-6 py-2 rounded-lg hover:bg-[#3b4b3b] hover:text-white transition duration-200 whitespace-nowrap">
              Login
            </a>
          </template>
        </div>
      </nav>

      <div class="flex-1 w-full flex flex-col items-center justify-start text-center px-6 z-10 pt-9">
        <h1 class="text-[53px] font-bold text-white drop-shadow-[0_4px_6px_rgba(0,0,0,0.5)] leading-tight tracking-tight" style="font-family: 'Montserrat', sans-serif;">
          Your Ultimate Summit Companion
        </h1>
      </div>

      <div class="absolute bottom-6 right-6 md:bottom-10 md:right-10 xl:bottom-12 xl:right-12 z-10">
        <Link href="#" class="inline-flex items-center gap-2 border border-white/30 bg-white/20 backdrop-blur-md shadow-lg text-white px-4 py-2.5 md:px-6 md:py-3 rounded-lg font-medium hover:bg-white/30 transition duration-300 group">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5 transition-transform group-hover:translate-y-0.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3 16.5v2.25A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75V16.5M16.5 12 12 16.5m0 0L7.5 12m4.5 4.5V3" />
          </svg>
          <span class="text-sm md:text-base xl:text-lg whitespace-nowrap">Download AltiGuide</span>
        </Link>
      </div>
    </section>

    <section class="w-full bg-[#F8F3E4] flex flex-col items-center py-16 md:py-20 xl:py-24 px-6 md:px-12 xl:px-24">
      <h2 class="text-3xl md:text-4xl xl:text-5xl font-bold text-[#64823E] mb-8 md:mb-12">
        "Our Feature"
      </h2>
      
      <div class="w-full max-w-7xl grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6 md:gap-8">
        
        <div class="bg-gradient-to-tr from-[#A2825B] to-[#D6CCAF] rounded-2xl xl:rounded-3xl p-6 md:p-8 xl:p-10 shadow-lg scroll-reveal opacity-0 translate-y-10 transition-all duration-1000 ease-out">
          <div class="flex flex-col gap-4">
            <div class="flex items-center justify-center gap-3 md:gap-4">
              <img src="/images/offline_maps.png" alt="Offline Maps Icon" class="w-8 h-8 md:w-10 md:h-10 xl:w-12 xl:h-12 object-contain shrink-0" />
              <h3 class="text-xl md:text-2xl xl:text-3xl font-semibold text-[#374426] leading-tight break-words">Offline Maps</h3>
            </div>
            <p class="text-base md:text-lg xl:text-xl font-medium text-[#374426] text-center leading-snug break-words">
              Access detailed topographic maps and trail routes even without any internet connection.
            </p>
          </div>
        </div>

        <div class="bg-gradient-to-tr from-[#A2825B] to-[#D6CCAF] rounded-2xl xl:rounded-3xl p-6 md:p-8 xl:p-10 shadow-lg scroll-reveal opacity-0 translate-y-10 transition-all duration-1000 ease-out delay-100">
          <div class="flex flex-col gap-4">
            <div class="flex items-center justify-center gap-3 md:gap-4">
              <img src="/images/basecamp_info.png" alt="Basecamp Info Icon" class="w-8 h-8 md:w-10 md:h-10 xl:w-12 xl:h-12 object-contain shrink-0" />
              <h3 class="text-xl md:text-2xl xl:text-3xl font-semibold text-[#374426] leading-tight break-words">Basecamp Info</h3>
            </div>
            <p class="text-base md:text-lg xl:text-xl font-medium text-[#374426] text-center leading-snug break-words">
              Get real-time updates on trail status, camp facilities, and official permit fees from local basecamps.
            </p>
          </div>
        </div>

        <div class="bg-gradient-to-tr from-[#A2825B] to-[#D6CCAF] rounded-2xl xl:rounded-3xl p-6 md:p-8 xl:p-10 shadow-lg scroll-reveal opacity-0 translate-y-10 transition-all duration-1000 ease-out delay-200">
          <div class="flex flex-col gap-4">
            <div class="flex items-center justify-center gap-3 md:gap-4">
              <img src="/images/track_position.png" alt="Track Position Icon" class="w-8 h-8 md:w-10 md:h-10 xl:w-12 xl:h-12 object-contain shrink-0" />
              <h3 class="text-xl md:text-2xl xl:text-3xl font-semibold text-[#374426] leading-tight break-words">Track Position</h3>
            </div>
            <p class="text-base md:text-lg xl:text-xl font-medium text-[#374426] text-center leading-snug break-words">
              Real-time GPS tracking to monitor your exact location and stay safely on the designated trail.
            </p>
          </div>
        </div>

      </div>

      <!-- Find Your Summit Section -->
      <div
        class="w-full max-w-7xl mt-[89px] rounded-[40px] p-8 flex flex-col lg:flex-row items-center justify-center gap-6 lg:gap-8 scroll-reveal opacity-0 translate-y-10 transition-all duration-1000 ease-out shadow-[inset_0_3px_12px_rgba(0,0,0,0.08)]"
        style="background: radial-gradient(ellipse 400px 300px at 20% 50%, #FAEEAD 0%, #FAEEAD 30%, #EEF1DB 100%)"
      >
        <!-- Left: Title -->
        <div class="flex flex-col items-center justify-center shrink-0 -rotate-[8deg] lg:-rotate-[10deg] pl-2 lg:pl-6 relative w-[280px] md:w-[380px] lg:w-[480px] h-[150px] lg:h-[220px]">
          <svg width="100%" height="100%" viewBox="0 0 500 250" class="overflow-visible absolute inset-0">
            <!-- Curve for FIND YOUR -->
            <path id="curve-sub" d="M 0, 100 Q 250, 30 500, 100" fill="transparent" />
            <!-- Curve for SUMMIT ! -->
            <path id="curve-main" d="M 0, 200 Q 250, 115 500, 200" fill="transparent" />
            
            <text class="summit-title-sub">
              <textPath href="#curve-sub" startOffset="50%" text-anchor="middle">FIND YOUR</textPath>
            </text>
            <text class="summit-title-main">
              <textPath href="#curve-main" startOffset="50%" text-anchor="middle">SUMMIT !</textPath>
            </text>
          </svg>
        </div>

        <div class="flex flex-col items-center lg:items-end w-full lg:flex-1">
          <div class="flex flex-col items-center gap-5 w-full max-w-[621px]">
            <div class="relative w-full rounded-[32px] overflow-hidden shadow-xl" style="aspect-ratio: 621 / 477;">
              <div
                class="flex h-full transition-transform duration-700 ease-in-out"
                :style="{ width: slides.length * 100 + '%', transform: 'translateX(-' + (currentSlide * (100 / slides.length)) + '%)' }"
              >
                <div
                  v-for="(slide, idx) in slides"
                  :key="idx"
                  class="h-full flex-shrink-0 relative"
                  :style="{ width: (100 / slides.length) + '%' }"
                >
                  <img :src="slide.image" :alt="slide.name" class="w-full h-full object-cover block" />
                </div>
              </div>

              <div class="absolute bottom-0 left-0 right-0 px-6 py-4 pointer-events-none" style="background: rgba(55, 68, 38, 0.3); backdrop-filter: blur(4px); -webkit-backdrop-filter: blur(4px); box-shadow: inset 0px 1px 0px rgba(255, 255, 255, 0.15), 0 -4px 20px rgba(0, 0, 0, 0.1);">
                <h4 class="text-[#E3E9CD] font-semibold text-[24px] leading-tight" style="font-family: 'Montserrat', sans-serif;">{{ slides[currentSlide].name }}</h4>
                <p class="text-[#E3E9CD] font-normal text-[16px] leading-snug mt-1" style="font-family: 'Montserrat', sans-serif;">{{ slides[currentSlide].description }}</p>
              </div>
            </div>

            <!-- Dots -->
            <div class="flex items-center gap-3 mt-2">
              <button
                v-for="(_, idx) in slides"
                :key="idx"
                @click="setSlide(idx)"
                class="w-3 h-3 rounded-full transition-all duration-300 focus:outline-none"
                :class="currentSlide === idx ? 'bg-[#64823E] scale-125' : 'bg-black/20 hover:bg-[#64823E]/60'"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- FAQ Section -->
      <div class="w-full max-w-7xl mt-[88px] mb-[27px] flex flex-col items-start">
        <div class="flex items-center gap-4 mb-6">
          <img src="/images/Vector.png" alt="FAQ Icon" class="w-[50px] object-contain drop-shadow-md" />
          <h2 class="text-[40px] font-bold text-[#64823E] drop-shadow-md" style="font-family: 'Montserrat', sans-serif;">FAQ</h2>
        </div>

        <button class="w-full bg-[#E1E5CD] hover:bg-[#D7DDC2] transition-colors rounded-full py-5 flex items-center justify-center mb-6 cursor-pointer">
          <span class="text-[24px] font-bold text-[#374426]" style="font-family: 'Montserrat', sans-serif;">+ Ask a question</span>
        </button>

        <div class="w-full flex flex-col">
          <div 
            v-for="(faq, index) in faqs" 
            :key="index" 
            class="w-full border-b border-[#D7DDC2] py-5 cursor-pointer group"
            @click="toggleFaq(index)"
          >
            <!-- Question Header -->
            <div class="w-full flex items-center justify-between">
              <h3 
                class="text-[22px] font-medium text-[#374426] group-hover:text-[#64823E] transition-colors duration-200" 
                style="font-family: 'Montserrat', sans-serif;"
              >
                {{ faq.question }}
              </h3>
              <span 
                class="text-[#374426] text-[32px] font-medium leading-none select-none transition-transform duration-300 ease-in-out origin-center inline-block"
                :class="{ 'rotate-45 text-[#64823E]': faq.open }"
              >
                +
              </span>
            </div>

            <!-- Answer Content -->
            <div 
              class="overflow-hidden transition-all duration-300 ease-in-out"
              :style="{ maxHeight: faq.open ? '200px' : '0px', opacity: faq.open ? '1' : '0', marginTop: faq.open ? '12px' : '0px' }"
            >
              <p class="text-[16px] md:text-[18px] text-[#5A684C] font-normal leading-relaxed" style="font-family: 'Montserrat', sans-serif;">
                {{ faq.answer }}
              </p>
            </div>
          </div>
        </div>
      </div>

    </section>

    <!-- Footer Section -->
    <div class="w-full flex flex-col">
      <!-- CTA Banner -->
      <div class="w-full bg-[#E0DBBE] py-10 px-8 md:px-16 xl:px-24 flex flex-col md:flex-row items-center justify-between gap-6">
        <div class="flex items-center gap-3">
          <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-10 h-10 md:w-12 md:h-12 object-contain" />
          <span class="text-2xl md:text-3xl xl:text-[32px] font-bold text-[#374426] tracking-tight">AltiGuide</span>
        </div>
        
        <div class="flex items-center gap-4">
          <Link href="mailto:support@altiguide.com" class="inline-block bg-[#374426] text-[#F8F3E4] text-[24px] font-medium rounded-xl px-8 py-4 hover:opacity-90 transition-opacity" style="font-family: 'Montserrat', sans-serif;">
            Contact Us
          </Link>
          <a href="/booking" class="inline-block bg-[#F8F3E4] text-[#374426] text-[24px] font-medium rounded-xl px-8 py-4 hover:opacity-90 transition-opacity shadow-sm" style="font-family: 'Montserrat', sans-serif;">
            Start Summit
          </a>
        </div>
      </div>

      <!-- Main Footer Links -->
      <footer class="w-full bg-[#FFFFFF] px-8 md:px-16 xl:px-24 py-10 flex flex-col">
        <div class="flex flex-col lg:flex-row justify-between items-start gap-12 mb-8">
          <!-- Left: Logo & Social -->
          <div class="flex flex-col gap-[96px]">
            <a href="/" class="text-[24px] font-medium text-[#374426] underline underline-offset-8">
              AltiGuide.com
            </a>
            <div class="flex items-center gap-6 text-[#828282]">
              <a href="#" class="hover:text-[#374426] transition-colors">
                <svg class="w-7 h-7" fill="currentColor" viewBox="0 0 24 24"><path d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12c0 4.991 3.657 9.128 8.438 9.878v-6.987h-2.54V12h2.54V9.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V12h2.773l-.443 2.89h-2.33v6.988C18.343 21.128 22 16.991 22 12z"/></svg>
              </a>
              <a href="#" class="hover:text-[#374426] transition-colors">
                <svg class="w-7 h-7" fill="currentColor" viewBox="0 0 24 24"><path d="M21.582 6.186a2.66 2.66 0 0 0-1.875-1.884C18.053 3.86 12 3.86 12 3.86s-6.053 0-7.707.442a2.66 2.66 0 0 0-1.875 1.884C2 7.854 2 12 2 12s0 4.146.418 5.814a2.66 2.66 0 0 0 1.875 1.884C5.947 20.14 12 20.14 12 20.14s6.053 0 7.707-.442a2.66 2.66 0 0 0 1.875-1.884C22 16.146 22 12 22 12s0-4.146-.418-5.814zM9.88 15.15V8.85l6.32 3.15-6.32 3.15z"/></svg>
              </a>
              <a href="https://www.instagram.com/altiguide___?igsh=MTRwbW8zbW8wZDVubg==" target="_blank" rel="noopener noreferrer" class="hover:text-[#374426] transition-colors">
                <svg class="w-7 h-7" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zM12 0C8.741 0 8.333.014 7.053.072 2.695.272.273 2.69.073 7.052.014 8.333 0 8.741 0 12c0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98C8.333 23.986 8.741 24 12 24c3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98C15.668.014 15.259 0 12 0zm0 5.838a6.162 6.162 0 1 0 0 12.324 6.162 6.162 0 0 0 0-12.324zM12 16a4 4 0 1 1 0-8 4 4 0 0 1 0 8zm6.406-11.845a1.44 1.44 0 1 0 0 2.881 1.44 1.44 0 0 0 0-2.881z"/></svg>
              </a>
            </div>
          </div>

          <!-- Right: Links -->
          <div class="flex flex-col sm:flex-row gap-12 md:gap-24 xl:gap-32">
            <div class="flex flex-col gap-5">
              <h5 class="text-[#374426] font-semibold text-[18px]">Jelajahi</h5>
              <div class="flex flex-col gap-4 text-[#5A684C] font-medium text-[15px]">
                <Link href="/mountains" class="hover:text-[#374426] transition-colors">Daftar Gunung</Link>
                <Link href="/" class="hover:text-[#374426] transition-colors">Weather Analytics</Link>
              </div>
            </div>
            
            <div class="flex flex-col gap-5">
              <h5 class="text-[#374426] font-semibold text-[18px]">Informasi</h5>
              <div class="flex flex-col gap-4 text-[#5A684C] font-medium text-[15px]">
                <Link href="/article" class="hover:text-[#374426] transition-colors">Tata Tertib</Link>
                <Link href="/booking" class="hover:text-[#374426] transition-colors">Booking Simaksi</Link>
                <Link href="/article" class="hover:text-[#374426] transition-colors">Tips Keamanan</Link>
              </div>
            </div>

            <div class="flex flex-col gap-5">
              <h5 class="text-[#374426] font-semibold text-[18px]">Komunitas</h5>
              <div class="flex flex-col gap-4 text-[#5A684C] font-medium text-[15px]">
                <Link href="/" class="hover:text-[#374426] transition-colors">Forum Diskusi</Link>
                <!-- <Link href="/about" class="hover:text-[#374426] transition-colors">Tentang Kami</Link> -->
              </div>
            </div>
          </div>
        </div>

        <!-- Copyright -->
        <div class="w-full border-t border-[#D7DDC2] pt-6 flex justify-end">
          <p class="text-[#5A684C] font-medium text-[14px]">
            © 2026 AltiGuide Team. All rights reserved.
          </p>
        </div>
      </footer>
    </div>

  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600&display=swap');

.summit-title-sub {
  font-family: 'Inter', sans-serif;
  font-weight: 700;
  font-style: italic;
  font-size: 30px;
  fill: #FFFFFF;
  stroke: #374426;
  stroke-width: 6px;
  stroke-linejoin: round;
  paint-order: stroke fill;
  filter: drop-shadow(-2px 4px 3px rgba(25, 35, 15, 0.95));
  text-transform: uppercase;
}

.summit-title-main {
  font-family: 'Inter', sans-serif;
  font-weight: 700;
  font-style: italic;
  font-size: 90px;
  fill: #FFFFFF;
  stroke: #374426;
  stroke-width: 12px;
  stroke-linejoin: round;
  paint-order: stroke fill;
  filter: drop-shadow(-5px 8px 6px rgba(25, 35, 15, 0.95));
  text-transform: uppercase;
}

@media (max-width: 1280px) {
  .summit-title-main {
    font-size: 70px;
  }
  .summit-title-sub {
    font-size: 26px;
  }
}

@media (max-width: 768px) {
  .summit-title-main {
    font-size: 55px;
    stroke-width: 6px;
  }
  .summit-title-sub {
    font-size: 22px;
    stroke-width: 4px;
  }
}
</style>