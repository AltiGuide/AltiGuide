<script setup>
import { Link, Head, usePage } from '@inertiajs/vue3'
import { computed } from 'vue'

const props = defineProps({
  mountains: {
    type: Array,
    default: () => []
  }
})

const page = usePage()
const authUser = computed(() => page.props.auth?.user)
</script>

<template>
  <div class="min-h-screen bg-[#F8F3E4] font-sans flex flex-col">
    <Head title="Daftar Gunung Jawa Tengah - AltiGuide" />

    <nav class="w-full flex justify-between items-center px-4 md:px-8 xl:px-12 py-4 border-b border-[#D7DDC2]/50 shadow-sm bg-[#374426]/20">
      <div class="flex items-center gap-2 xl:gap-3">
        <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 md:w-10 md:h-10 object-contain" />
        <span class="text-xl md:text-2xl xl:text-3xl font-bold text-[#374426] tracking-tight">AltiGuide</span>
      </div>

      <div class="flex items-center gap-4 md:gap-6 xl:gap-9 text-sm md:text-base font-semibold text-[#374426]">
        <Link href="/" class="hover:text-black transition">Home</Link>
        <Link href="/article" class="hover:text-black transition">Article</Link>
        <Link href="/booking" class="hover:text-black transition">Booking</Link>
        <!-- Auth Button -->
        <template v-if="authUser">
          <Link
            href="/dashboard"
            class="flex items-center gap-2 border border-[#374426] px-4 md:px-5 py-2 rounded-lg hover:bg-[#374426] hover:text-white transition duration-200 whitespace-nowrap"
          >
            <img
              v-if="authUser.avatar"
              :src="`/storage/${authUser.avatar}`"
              alt="Avatar"
              class="w-6 h-6 rounded-full object-cover"
            />
            <svg
              v-else
              xmlns="http://www.w3.org/2000/svg"
              class="w-5 h-5 md:w-6 md:h-6 text-current"
              viewBox="0 0 24 24"
              fill="currentColor"
            >
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
            </svg>
            <span class="hidden md:inline">{{ authUser.name.split(' ')[0] }}</span>
          </Link>
        </template>
        <template v-else>
          <Link
            href="/login"
            class="border border-[#374426] px-4 md:px-5 py-2 rounded-lg hover:bg-[#374426] hover:text-white transition duration-200"
          >
            Login
          </Link>
        </template>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="w-full flex-1 flex flex-col items-center mt-12 pb-24 px-4">
      <div class="w-full max-w-[1340px] flex flex-col">
        <h2 class="text-[#64823E] font-bold text-[48px] mb-8 text-center drop-shadow-md" style="font-family: 'Montserrat', sans-serif;">Daftar Gunung</h2>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10 justify-between">
          <div
            v-for="dest in mountains"
            :key="dest.slug"
            class="w-full h-[450px] rounded-[30px] overflow-hidden bg-[#D6CCAF] flex flex-col shadow-lg transition duration-300 hover:scale-[1.03] hover:shadow-xl"
          >
            <div class="relative w-full h-[210px] shrink-0">
              <img :src="dest.image" :alt="dest.name" class="w-full h-full object-cover" />
              <Link
                :href="`/article?mountain=${dest.slug}`"
                class="absolute bottom-4 left-1/2 -translate-x-1/2 bg-[#374426]/20 backdrop-blur-[4px] shadow-sm text-[#E3E9CD] font-bold px-8 py-2 rounded-full text-[20px] whitespace-nowrap hover:bg-[#374426]/40 transition-colors"
                style="font-family: 'Montserrat', sans-serif;"
              >
                Read More
              </Link>
            </div>

            <div class="flex-1 p-6 flex flex-col gap-3">
              <h4 class="text-[#374426] font-bold text-[28px] leading-tight" style="font-family: 'Montserrat', sans-serif;">{{ dest.name }}</h4>
              <p class="text-[16px] font-normal leading-relaxed text-[#5A684C] line-clamp-3" style="font-family: 'Montserrat', sans-serif;">
                {{ dest.content?.[0]?.text || 'Informasi gunung ini belum tersedia. Silakan klik Read More untuk melihat detail lebih lanjut.' }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <div class="w-full flex flex-col mt-auto">
      <div class="w-full bg-[#E0DBBE] py-10 px-8 md:px-16 xl:px-24 flex flex-col md:flex-row items-center justify-between gap-6">
        <div class="flex items-center gap-3">
          <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-10 h-10 md:w-12 md:h-12 object-contain" />
          <span class="text-2xl md:text-3xl xl:text-[32px] font-bold text-[#374426] tracking-tight">AltiGuide</span>
        </div>

        <div class="flex items-center gap-4">
          <Link href="#" class="inline-block bg-[#374426] text-[#F8F3E4] text-[24px] font-medium rounded-xl px-8 py-4 hover:opacity-90 transition-opacity" style="font-family: 'Montserrat', sans-serif;">
            Contact Us
          </Link>
          <Link href="/booking" class="inline-block bg-[#F8F3E4] text-[#374426] text-[24px] font-medium rounded-xl px-8 py-4 hover:opacity-90 transition-opacity shadow-sm" style="font-family: 'Montserrat', sans-serif;">
            Start Summit
          </Link>
        </div>
      </div>

      <footer class="w-full bg-[#FFFFFF] px-8 md:px-16 xl:px-24 py-10 flex flex-col">
        <div class="flex flex-col lg:flex-row justify-between items-start gap-12 mb-8">
          <div class="flex flex-col gap-[96px]">
            <Link href="/" class="text-[24px] font-medium text-[#374426] underline underline-offset-8">
              AltiGuide.com
            </Link>
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

          <div class="flex flex-col sm:flex-row gap-12 md:gap-24 xl:gap-32">
            <div class="flex flex-col gap-5">
              <h5 class="text-[#374426] font-semibold text-[18px]">Jelajahi</h5>
              <div class="flex flex-col gap-4 text-[#5A684C] font-medium text-[15px]">
                <Link href="/mountains" class="hover:text-[#374426] transition-colors">Daftar Gunung</Link>
                <Link href="/weather-analytics" class="hover:text-[#374426] transition-colors">Weather Analytics</Link>
              </div>
            </div>

            <div class="flex flex-col gap-5">
              <h5 class="text-[#374426] font-semibold text-[18px]">Informasi</h5>
              <div class="flex flex-col gap-4 text-[#5A684C] font-medium text-[15px]">
                <Link href="/tata-tertib" class="hover:text-[#374426] transition-colors">Tata Tertib</Link>
                <Link href="/booking" class="hover:text-[#374426] transition-colors">Booking Simaksi</Link>
                <Link href="/tips-keamanan" class="hover:text-[#374426] transition-colors">Tips Keamanan</Link>
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
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700;800;900&family=Poppins:wght@400;500;600&display=swap');
</style>
