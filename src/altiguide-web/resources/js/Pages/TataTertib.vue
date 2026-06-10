<script setup>
import { Link, Head, usePage } from '@inertiajs/vue3'
import { computed } from 'vue'

const page = usePage()
const authUser = computed(() => page.props.auth?.user)
</script>

<template>
  <div class="flex-1 w-full bg-[#F8F3E4] font-sans flex flex-col transition-all duration-300 ease-[cubic-bezier(0.4,0,0.2,1)] opacity-0 translate-y-4 animate-fadeSlideUp">
    <Head title="Tata Tertib Pendakian - AltiGuide" />

    <nav class="w-full flex justify-between items-center px-4 md:px-8 xl:px-12 py-4 border-b border-[#D7DDC2]/50 shadow-sm bg-[#374426]/20">
      <div class="flex items-center gap-2 xl:gap-3">
        <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 md:w-10 md:h-10 object-contain" />
        <span class="text-xl md:text-2xl xl:text-3xl font-bold text-[#374426] tracking-tight">AltiGuide</span>
      </div>

      <div class="flex items-center gap-4 md:gap-6 xl:gap-9 text-sm md:text-base font-semibold text-[#374426]">
        <Link href="/" class="hover:text-black transition">Home</Link>
        <Link href="/article" class="hover:text-black transition">Article</Link>
        <Link href="/booking" class="hover:text-black transition">Booking</Link>
        <template v-if="authUser">
          <Link href="/dashboard" class="flex items-center gap-2 border border-[#374426] px-4 md:px-5 py-2 rounded-lg hover:bg-[#374426] hover:text-white transition duration-200 whitespace-nowrap">
            <img v-if="authUser.avatar" :src="`/storage/${authUser.avatar}`" alt="Avatar" class="w-6 h-6 rounded-full object-cover" />
            <svg v-else xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 md:w-6 md:h-6 text-current" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
            </svg>
            <span class="hidden md:inline">{{ authUser.name.split(' ')[0] }}</span>
          </Link>
        </template>
        <template v-else>
          <Link href="/login" class="border border-[#374426] px-4 md:px-5 py-2 rounded-lg hover:bg-[#374426] hover:text-white transition duration-200">Login</Link>
        </template>
      </div>
    </nav>

    <main class="w-full flex-1 flex flex-col items-center mt-12 pb-24 px-4">
      <div class="w-full max-w-[1100px] flex flex-col gap-16">
        
        <div class="text-center">
          <h1 class="text-[#64823E] font-bold text-[56px] drop-shadow-md mb-6" style="font-family: 'Montserrat', sans-serif;">Tata Tertib Pendakian Umum</h1>
          <p class="text-[#5A684C] text-[18px] leading-relaxed max-w-3xl mx-auto" style="font-family: 'Montserrat', sans-serif;">
            Demi menjaga kelestarian alam dan keselamatan bersama, setiap pendaki diwajibkan untuk mematuhi regulasi dan norma pendakian universal.
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-10">
          <div class="bg-[#D6CCAF] p-10 rounded-[30px] shadow-lg flex flex-col gap-4">
            <h3 class="text-[#374426] font-bold text-[32px]" style="font-family: 'Montserrat', sans-serif;">Zero Waste Policy</h3>
            <p class="text-[#5A684C] text-[16px] leading-relaxed" style="font-family: 'Montserrat', sans-serif;">
              Setiap pendaki wajib membawa turun semua sampah yang dihasilkan selama pendakian. Dilarang keras membuang sampah sembarangan atau meninggalkan sisa makanan yang dapat mengubah perilaku satwa liar.
            </p>
          </div>
          <div class="bg-[#D6CCAF] p-10 rounded-[30px] shadow-lg flex flex-col gap-4">
            <h3 class="text-[#374426] font-bold text-[32px]" style="font-family: 'Montserrat', sans-serif;">Perlindungan Ekosistem</h3>
            <p class="text-[#5A684C] text-[16px] leading-relaxed" style="font-family: 'Montserrat', sans-serif;">
              Dilarang keras merusak, menebang, atau mengambil flora (seperti Edelweis) dan mengganggu satwa endemik di sepanjang jalur pendakian. Lindungi keanekaragaman hayati kita.
            </p>
          </div>
          <div class="bg-[#D6CCAF] p-10 rounded-[30px] shadow-lg flex flex-col gap-4">
            <h3 class="text-[#374426] font-bold text-[32px]" style="font-family: 'Montserrat', sans-serif;">Aturan Api Unggun</h3>
            <p class="text-[#5A684C] text-[16px] leading-relaxed" style="font-family: 'Montserrat', sans-serif;">
              Pembuatan api unggun dilarang di sebagian besar kawasan taman nasional untuk mencegah kebakaran hutan. Gunakan kompor portabel (nesting) untuk memasak.
            </p>
          </div>
          <div class="bg-[#D6CCAF] p-10 rounded-[30px] shadow-lg flex flex-col gap-4">
            <h3 class="text-[#374426] font-bold text-[32px]" style="font-family: 'Montserrat', sans-serif;">Registrasi Resmi</h3>
            <p class="text-[#5A684C] text-[16px] leading-relaxed" style="font-family: 'Montserrat', sans-serif;">
              Semua pendaki wajib memiliki Simaksi (Surat Izin Masuk Kawasan Konservasi) yang diterbitkan secara resmi melalui AltiGuide atau pos masuk gunung sebelum memulai pendakian.
            </p>
          </div>
        </div>

        <div class="bg-white p-10 rounded-[30px] shadow-lg flex flex-col gap-4 max-w-4xl mx-auto w-full">
          <h3 class="text-[#374426] font-bold text-[32px] text-center" style="font-family: 'Montserrat', sans-serif;">Etika Berpapasan</h3>
          <p class="text-[#5A684C] text-[16px] leading-relaxed text-center" style="font-family: 'Montserrat', sans-serif;">
            Berikan prioritas pada pendaki yang sedang berjalan naik. Ucapkan salam ketika berpapasan dengan pendaki lain. Jaga ketenangan dan hindari membuat kebisingan yang mengganggu kenyamanan.
          </p>
        </div>

      </div>
    </main>

    <div class="w-full flex flex-col mt-auto">
      <footer class="w-full bg-[#FFFFFF] px-8 md:px-16 xl:px-24 py-10 flex flex-col">
        <div class="flex flex-col lg:flex-row justify-between items-start gap-12 mb-8">
          <div class="flex flex-col gap-[96px]">
            <Link href="/" class="text-[24px] font-medium text-[#374426] underline underline-offset-8">
              AltiGuide.com
            </Link>
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
@import url('https://fonts.googleapis.com/css2?family=Jost:wght@400;500;600;700&family=Montserrat:wght@400;500;600;700;800;900&display=swap');

@keyframes fadeSlideUp {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fadeSlideUp {
  animation: fadeSlideUp 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}
</style>
