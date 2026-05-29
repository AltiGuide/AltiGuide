<script setup>
import { Link, Head } from '@inertiajs/vue3'

const otherDestinations = [
  { name: 'Gunung Sindoro', image: '/images/gunung_sindoro_8.png' },
  { name: 'Gunung Prau', image: '/images/gunung_prau_7.png' },
  { name: 'Gunung Merbabu', image: '/images/gunung_merbabu_6.png' },
  { name: 'Gunung Lawu', image: '/images/gunung_lawu_2.png' },
  { name: 'Gunung Andong', image: '/images/gunung_andong_1.png' },
  { name: 'Gunung Ungaran', image: '/images/gunung_ungaran_5.png' },
]
</script>

<template>
  <Head title="Article - Gunung Sumbing" />

  <div class="min-h-screen bg-[#F8F3E4] font-sans">
    <!-- Navbar -->
    <nav class="w-full flex justify-between items-center px-4 md:px-8 xl:px-12 py-4 border-b border-[#D7DDC2]/50 shadow-sm bg-[#374426]/20">
      <div class="flex items-center gap-2 xl:gap-3">
        <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 md:w-10 md:h-10 object-contain" />
        <span class="text-xl md:text-2xl xl:text-3xl font-bold text-[#374426] tracking-tight">AltiGuide</span>
      </div>

      <div class="flex items-center gap-4 md:gap-6 xl:gap-9 text-sm md:text-base font-semibold text-[#374426]">
        <Link href="/" class="hover:text-black transition">Home</Link>
        <Link href="/article" class="hover:text-black transition">Article</Link>
        <Link href="#" class="hover:text-black transition">Booking</Link>
        <Link href="/login" class="border border-[#374426] px-4 md:px-6 py-2 rounded-lg hover:bg-[#374426] hover:text-white transition duration-200 whitespace-nowrap">
          Login
        </Link>
      </div>
    </nav>

    <!-- Main Content Container -->
    <div class="w-full flex justify-center py-10">
      <!-- Outer Container: width 1180px, height 1820px as requested -->
      <div 
        class="relative rounded-[40px] shadow-2xl p-10 flex flex-col items-center"
        style="
          width: 1180px; 
          min-height: 1820px; 
          background: linear-gradient(180deg, rgba(126,98,63,0.8) 26%, rgba(60,38,12,0.8) 87%);
        "
      >
        <!-- Title -->
        <h1 class="text-[#F8F3E4] text-[64px] font-semibold mb-10 drop-shadow-[0_2px_8px_rgba(255,255,255,0.6)] tracking-wide font-sans text-center">
          Gunung Sumbing
        </h1>

        <!-- Content Area -->
        <div class="w-full flex gap-10">
          
          <!-- Left Column -->
          <div class="w-[504px] flex flex-col gap-6 shrink-0">
            <!-- Image -->
            <div class="w-full h-[1176px] rounded-[30px] overflow-hidden shadow-lg border-2 border-white/10">
              <img src="/images/gunung_sumbing_4.png" alt="Gunung Sumbing" class="w-full h-full object-cover" />
            </div>

            <!-- Routes Section -->
            <div class="w-full flex flex-col gap-3">
              <!-- Title Container -->
              <div class="w-full bg-[#FFFFFF]/20 backdrop-blur-[4px] border border-white/20 shadow-sm rounded-[20px] py-4 px-6">
                <h3 class="text-[#EDE6D2] font-semibold text-[20px]" style="font-family: 'Montserrat', sans-serif;">Jalur Pendakian Resmi</h3>
              </div>
              
              <!-- Routes Container -->
              <div class="w-full bg-[#FFFFFF]/20 backdrop-blur-[4px] border border-white/20 shadow-sm rounded-[20px] p-6 flex flex-col gap-3">
                <div class="w-full bg-[#D6CCAF] text-[#7E623F] text-[17px] font-medium py-3 px-5 rounded-xl shadow-sm" style="font-family: 'Montserrat', sans-serif;">Jalur Garung</div>
                <div class="w-full bg-[#D6CCAF] text-[#7E623F] text-[17px] font-medium py-3 px-5 rounded-xl shadow-sm" style="font-family: 'Montserrat', sans-serif;">Jalur Bowongso</div>
                <div class="w-full bg-[#D6CCAF] text-[#7E623F] text-[17px] font-medium py-3 px-5 rounded-xl shadow-sm" style="font-family: 'Montserrat', sans-serif;">Jalur Cepit</div>
                <div class="w-full bg-[#D6CCAF] text-[#7E623F] text-[17px] font-medium py-3 px-5 rounded-xl shadow-sm" style="font-family: 'Montserrat', sans-serif;">Jalur Banaran</div>
                <div class="w-full bg-[#D6CCAF] text-[#7E623F] text-[17px] font-medium py-3 px-5 rounded-xl shadow-sm" style="font-family: 'Montserrat', sans-serif;">Jalur Mangli</div>
                <div class="w-full bg-[#D6CCAF] text-[#7E623F] text-[17px] font-medium py-3 px-5 rounded-xl shadow-sm" style="font-family: 'Montserrat', sans-serif;">Jalur Gajah Mungkur</div>
                <div class="w-full bg-[#D6CCAF] text-[#7E623F] text-[17px] font-medium py-3 px-5 rounded-xl shadow-sm" style="font-family: 'Montserrat', sans-serif;">Jalur Batusari</div>
              </div>
            </div>
          </div>

          <!-- Right Column (Text Content) -->
          <div class="flex-1 flex flex-col gap-8 text-[#F8F3E4] leading-relaxed pr-4" style="font-family: 'Poppins', sans-serif;">
            
            <p class="text-justify text-[20px]">
              <span class="font-semibold">Gunung Sumbing</span> dengan ketinggian menjulang 3.371 mdpl, merupakan gunung tertinggi kedua di Jawa Tengah setelah Gunung Slamet. Terletak megah di antara Kabupaten Magelang, Temanggung, dan Wonosobo, gunung api aktif ini menawarkan panorama kawah yang eksotis dan sabana luas yang menantang. Bagi pengguna AltiGuide, Sumbing adalah medan pembuktian fisik yang sesungguhnya. Jalur pendakiannya dikenal memiliki kecuraman yang konsisten dari awal hingga puncak, sehingga fitur Real-time Elevation pada aplikasi kami akan sangat membantu kamu memantau sisa ketinggian yang harus ditempuh.
            </p>

            <div class="flex flex-col gap-2">
              <h3 class="font-semibold text-xl text-[#F8F3E4]">Persiapan Fisik & Logistik</h3>
              <p class="text-justify text-[20px]">
                Medan Sumbing didominasi oleh tanjakan terjal dengan sedikit bonus jalur landai. Sangat disarankan untuk melakukan latihan fisik intensif minimal dua minggu sebelum pendakian. Tergantung jalur yang dipilih (seperti Garung atau Bowongso), sumber air sangat terbatas di area atas. Pastikan logistik air kamu tercukupi dan pantau fitur Weather Analytics AltiGuide, karena badai angin seringkali menerjang area punggungan menuju puncak saat cuaca tidak menentu.
              </p>
            </div>

            <div class="flex flex-col gap-2">
              <h3 class="font-semibold text-xl text-[#F8F3E4]">Keajaiban Kawah & Puncak Sejati</h3>
              <p class="text-justify text-[20px]">
                Berbeda dengan Merbabu yang hijau, Sumbing menawarkan keindahan kawah belerang yang aktif dan sangat luas. Dari Puncak Rajawali atau Puncak Sejati, kamu bisa melihat lubang kawah yang mengeluarkan asap solfatara dengan latar belakang Gunung Sindoro yang berdiri sejajar di utara. Keindahan ini menjadikannya magnet bagi fotografer lanskap, terutama saat momen sunrise di mana cahaya keemasan menyapu dinding kawah yang tandus dan gagah.
              </p>
            </div>

            <div class="flex flex-col gap-2">
              <h3 class="font-semibold text-xl text-[#F8F3E4]">Konservasi & Etika Pendakian</h3>
              <p class="text-justify text-[20px]">
                Gunung Sumbing memiliki ekosistem yang sensitif, terutama di area padang sabana dan hutan lamtoro. AltiGuide berkomitmen mendukung pelestarian alam dengan fitur Check-list Trash, yang membantu pendaki mencatat barang bawaan potensial sampah. Kami menghimbau setiap pendaki untuk tidak meninggalkan apapun selain jejak kaki dan tidak merusak vegetasi langka seperti bunga Edelweiss yang tumbuh subur di lereng-lereng menuju puncak.
              </p>
            </div>

            <div class="flex flex-col gap-2">
              <h3 class="font-semibold text-xl text-[#F8F3E4]">Puncak Tertinggi: Rajawali & Puncak Sejati</h3>
              <p class="text-justify text-[20px]">
                Momen paling magis di Gunung Sumbing adalah saat fajar menyingsing di bibir kawah yang luas. Dari Puncak Rajawali atau Puncak Sejati, kamu akan disuguhi pemandangan spektakuler berupa lubang kawah aktif yang mengeluarkan asap solfatara dengan latar belakang "Samudra Awan". Keindahan jajaran gunung kembar Sindoro, Merapi, dan Merbabu dari titik ini menjadikannya salah satu pemandangan paling gagah dan dicari oleh para pecinta petualangan tinggi.
              </p>
            </div>

          </div>

        </div>
      </div>
    </div>

    <!-- Other Destinations Section -->
    <div class="w-full flex flex-col items-center mt-[40px] pb-24 px-4">
      <div class="w-full max-w-[1180px] flex flex-col">
        <h2 class="text-[#64823E] font-bold text-[48px] mb-[32px]" style="font-family: 'Montserrat', sans-serif;">Other Destinations</h2>
        
        <div class="flex flex-wrap gap-[50px] justify-center md:justify-start">
          <div 
            v-for="(dest, index) in otherDestinations" 
            :key="index"
            class="w-[360px] h-[450px] rounded-[30px] overflow-hidden bg-[#D6CCAF] flex flex-col shadow-lg"
          >
            <!-- Image Area -->
            <div class="relative w-full h-[210px] shrink-0">
              <img :src="dest.image" :alt="dest.name" class="w-full h-full object-cover" />
              <Link href="/article" class="absolute bottom-4 left-1/2 -translate-x-1/2 bg-[#374426]/20 backdrop-blur-[4px] shadow-sm text-[#E3E9CD] font-bold px-8 py-2 rounded-full text-[20px] whitespace-nowrap hover:bg-[#374426]/40 transition-colors" style="font-family: 'Montserrat', sans-serif;">
                Read More
              </Link>
            </div>
            
            <!-- Content Area -->
            <div class="flex-1 p-6 flex flex-col gap-3">
              <h4 class="text-[#374426] font-bold text-[28px] leading-tight" style="font-family: 'Montserrat', sans-serif;">{{ dest.name }}</h4>
              <p class="text-[16px] font-normal leading-relaxed text-[#5A684C]" style="font-family: 'Montserrat', sans-serif;">
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap');
h1 {
  font-family: 'Montserrat', sans-serif;
}
</style>
