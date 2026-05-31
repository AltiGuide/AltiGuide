<script setup>
import { Link, Head, usePage, router } from '@inertiajs/vue3'
import { computed, watch, ref } from 'vue'

// ── Data semua gunung ────────────────────────────────────────────────────
const allMountains = [
  {
    slug: 'gunung-sumbing',
    name: 'Gunung Sumbing',
    image: '/images/gunung_sumbing_4.png',
    routes: ['Jalur Garung', 'Jalur Bowongso', 'Jalur Cepit', 'Jalur Banaran', 'Jalur Mangli', 'Jalur Gajah Mungkur', 'Jalur Batusari'],
    content: [
      { title: null, text: 'Gunung Sumbing dengan ketinggian menjulang 3.371 mdpl, merupakan gunung tertinggi kedua di Jawa Tengah setelah Gunung Slamet. Terletak megah di antara Kabupaten Magelang, Temanggung, dan Wonosobo, gunung api aktif ini menawarkan panorama kawah yang eksotis dan sabana luas yang menantang. Bagi pengguna AltiGuide, Sumbing adalah medan pembuktian fisik yang sesungguhnya. Jalur pendakiannya dikenal memiliki kecuraman yang konsisten dari awal hingga puncak, sehingga fitur Real-time Elevation pada aplikasi kami akan sangat membantu kamu memantau sisa ketinggian yang harus ditempuh.' },
      { title: 'Persiapan Fisik & Logistik', text: 'Medan Sumbing didominasi oleh tanjakan terjal dengan sedikit bonus jalur landai. Sangat disarankan untuk melakukan latihan fisik intensif minimal dua minggu sebelum pendakian. Tergantung jalur yang dipilih (seperti Garung atau Bowongso), sumber air sangat terbatas di area atas. Pastikan logistik air kamu tercukupi dan pantau fitur Weather Analytics AltiGuide, karena badai angin seringkali menerjang area punggungan menuju puncak saat cuaca tidak menentu.' },
      { title: 'Keajaiban Kawah & Puncak Sejati', text: 'Berbeda dengan Merbabu yang hijau, Sumbing menawarkan keindahan kawah belerang yang aktif dan sangat luas. Dari Puncak Rajawali atau Puncak Sejati, kamu bisa melihat lubang kawah yang mengeluarkan asap solfatara dengan latar belakang Gunung Sindoro yang berdiri sejajar di utara. Keindahan ini menjadikannya magnet bagi fotografer lanskap, terutama saat momen sunrise di mana cahaya keemasan menyapu dinding kawah yang tandus dan gagah.' },
      { title: 'Konservasi & Etika Pendakian', text: 'Gunung Sumbing memiliki ekosistem yang sensitif, terutama di area padang sabana dan hutan lamtoro. AltiGuide berkomitmen mendukung pelestarian alam dengan fitur Check-list Trash, yang membantu pendaki mencatat barang bawaan potensial sampah. Kami menghimbau setiap pendaki untuk tidak meninggalkan apapun selain jejak kaki dan tidak merusak vegetasi langka seperti bunga Edelweiss yang tumbuh subur di lereng-lereng menuju puncak.' },
      { title: 'Puncak Tertinggi: Rajawali & Puncak Sejati', text: 'Momen paling magis di Gunung Sumbing adalah saat fajar menyingsing di bibir kawah yang luas. Dari Puncak Rajawali atau Puncak Sejati, kamu akan disuguhi pemandangan spektakuler berupa lubang kawah aktif yang mengeluarkan asap solfatara dengan latar belakang "Samudra Awan". Keindahan jajaran gunung kembar Sindoro, Merapi, dan Merbabu dari titik ini menjadikannya salah satu pemandangan paling gagah dan dicari oleh para pecinta petualangan tinggi.' },
    ],
  },
  {
    slug: 'gunung-sindoro',
    name: 'Gunung Sindoro',
    image: '/images/gunung_sindoro_8.png',
    routes: ['Jalur Kledung', 'Jalur Sigedang', 'Jalur Buntu', 'Jalur Tambi'],
    content: [
      { title: null, text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.' },
      { title: 'Persiapan Fisik & Logistik', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin tincidunt, nunc vel tincidunt lacinia, nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Donec euismod, nisl eget aliquam tincidunt, nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae.' },
      { title: 'Keindahan Puncak', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus imperdiet, nulla et dictum interdum, nisi lorem egestas vitae scel erisque enim ligula venenatis dolor. Maecenas nisl est, ultrices nec congue eget, auctor vitae massa. Fusce luctus vestibulum augue ut facilisis. Cras placerat accumsan nulla. Aenean volutpat faucibus eros in condimentum.' },
      { title: 'Konservasi & Etika Pendakian', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium tincidunt lacus. Nulla gravida orci a odio. Nullam varius, turpis et commodo pharetra, est eros bibendum elit, nec luctus magna felis sollicitudin mauris. Integer in mauris eu nibh euismod gravida. Duis ac tellus et risus vulputate vehicula.' },
    ],
  },
  {
    slug: 'gunung-prau',
    name: 'Gunung Prau',
    image: '/images/gunung_prau_7.png',
    routes: ['Jalur Dieng', 'Jalur Patak Banteng', 'Jalur Kalilembu', 'Jalur Igirmranak'],
    content: [
      { title: null, text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.' },
      { title: 'Persiapan Fisik & Logistik', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin tincidunt, nunc vel tincidunt lacinia, nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Donec euismod, nisl eget aliquam tincidunt, nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae.' },
      { title: 'Keindahan Puncak', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus imperdiet, nulla et dictum interdum, nisi lorem egestas vitae scel erisque enim ligula venenatis dolor. Maecenas nisl est, ultrices nec congue eget, auctor vitae massa. Fusce luctus vestibulum augue ut facilisis. Cras placerat accumsan nulla. Aenean volutpat faucibus eros in condimentum.' },
      { title: 'Konservasi & Etika Pendakian', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium tincidunt lacus. Nulla gravida orci a odio. Nullam varius, turpis et commodo pharetra, est eros bibendum elit, nec luctus magna felis sollicitudin mauris. Integer in mauris eu nibh euismod gravida. Duis ac tellus et risus vulputate vehicula.' },
    ],
  },
  {
    slug: 'gunung-merbabu',
    name: 'Gunung Merbabu',
    image: '/images/gunung_merbabu_6.png',
    routes: ['Jalur Selo', 'Jalur Suwanting', 'Jalur Thekelan', 'Jalur Cuntel', 'Jalur Wekas'],
    content: [
      { title: null, text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.' },
      { title: 'Persiapan Fisik & Logistik', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin tincidunt, nunc vel tincidunt lacinia, nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Donec euismod, nisl eget aliquam tincidunt, nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae.' },
      { title: 'Keindahan Puncak', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus imperdiet, nulla et dictum interdum, nisi lorem egestas vitae scel erisque enim ligula venenatis dolor. Maecenas nisl est, ultrices nec congue eget, auctor vitae massa. Fusce luctus vestibulum augue ut facilisis. Cras placerat accumsan nulla. Aenean volutpat faucibus eros in condimentum.' },
      { title: 'Konservasi & Etika Pendakian', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium tincidunt lacus. Nulla gravida orci a odio. Nullam varius, turpis et commodo pharetra, est eros bibendum elit, nec luctus magna felis sollicitudin mauris. Integer in mauris eu nibh euismod gravida. Duis ac tellus et risus vulputate vehicula.' },
    ],
  },
  {
    slug: 'gunung-lawu',
    name: 'Gunung Lawu',
    image: '/images/gunung_lawu_2.png',
    routes: ['Jalur Cemoro Sewu', 'Jalur Cemoro Kandang', 'Jalur Candi Cetho', 'Jalur Singolangu'],
    content: [
      { title: null, text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.' },
      { title: 'Persiapan Fisik & Logistik', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin tincidunt, nunc vel tincidunt lacinia, nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Donec euismod, nisl eget aliquam tincidunt, nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae.' },
      { title: 'Keindahan Puncak', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus imperdiet, nulla et dictum interdum, nisi lorem egestas vitae scel erisque enim ligula venenatis dolor. Maecenas nisl est, ultrices nec congue eget, auctor vitae massa. Fusce luctus vestibulum augue ut facilisis. Cras placerat accumsan nulla. Aenean volutpat faucibus eros in condimentum.' },
      { title: 'Konservasi & Etika Pendakian', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium tincidunt lacus. Nulla gravida orci a odio. Nullam varius, turpis et commodo pharetra, est eros bibendum elit, nec luctus magna felis sollicitudin mauris. Integer in mauris eu nibh euismod gravida. Duis ac tellus et risus vulputate vehicula.' },
    ],
  },
  {
    slug: 'gunung-andong',
    name: 'Gunung Andong',
    image: '/images/gunung_andong_1.png',
    routes: ['Jalur Sawit', 'Jalur Pendem'],
    content: [
      { title: null, text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.' },
      { title: 'Persiapan Fisik & Logistik', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin tincidunt, nunc vel tincidunt lacinia, nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Donec euismod, nisl eget aliquam tincidunt, nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae.' },
      { title: 'Keindahan Puncak', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus imperdiet, nulla et dictum interdum, nisi lorem egestas vitae scel erisque enim ligula venenatis dolor. Maecenas nisl est, ultrices nec congue eget, auctor vitae massa. Fusce luctus vestibulum augue ut facilisis. Cras placerat accumsan nulla. Aenean volutpat faucibus eros in condimentum.' },
      { title: 'Konservasi & Etika Pendakian', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium tincidunt lacus. Nulla gravida orci a odio. Nullam varius, turpis et commodo pharetra, est eros bibendum elit, nec luctus magna felis sollicitudin mauris. Integer in mauris eu nibh euismod gravida. Duis ac tellus et risus vulputate vehicula.' },
    ],
  },
  {
    slug: 'gunung-ungaran',
    name: 'Gunung Ungaran',
    image: '/images/gunung_ungaran_5.png',
    routes: ['Jalur Mawar', 'Jalur Medini', 'Jalur Promasan'],
    content: [
      { title: null, text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.' },
      { title: 'Persiapan Fisik & Logistik', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin tincidunt, nunc vel tincidunt lacinia, nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Donec euismod, nisl eget aliquam tincidunt, nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae.' },
      { title: 'Keindahan Puncak', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus imperdiet, nulla et dictum interdum, nisi lorem egestas vitae scel erisque enim ligula venenatis dolor. Maecenas nisl est, ultrices nec congue eget, auctor vitae massa. Fusce luctus vestibulum augue ut facilisis. Cras placerat accumsan nulla. Aenean volutpat faucibus eros in condimentum.' },
      { title: 'Konservasi & Etika Pendakian', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium tincidunt lacus. Nulla gravida orci a odio. Nullam varius, turpis et commodo pharetra, est eros bibendum elit, nec luctus magna felis sollicitudin mauris. Integer in mauris eu nibh euismod gravida. Duis ac tellus et risus vulputate vehicula.' },
    ],
  },
  {
    slug: 'gunung-slamet',
    name: 'Gunung Slamet',
    image: '/images/gunung_slamet_3.png',
    routes: ['Jalur Bambangan', 'Jalur Dipajaya', 'Jalur Jurangmangu', 'Jalur Guci'],
    content: [
      { title: null, text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.' },
      { title: 'Persiapan Fisik & Logistik', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin tincidunt, nunc vel tincidunt lacinia, nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Donec euismod, nisl eget aliquam tincidunt, nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae.' },
      { title: 'Keindahan Puncak', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus imperdiet, nulla et dictum interdum, nisi lorem egestas vitae scel erisque enim ligula venenatis dolor. Maecenas nisl est, ultrices nec congue eget, auctor vitae massa. Fusce luctus vestibulum augue ut facilisis. Cras placerat accumsan nulla. Aenean volutpat faucibus eros in condimentum.' },
      { title: 'Konservasi & Etika Pendakian', text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium tincidunt lacus. Nulla gravida orci a odio. Nullam varius, turpis et commodo pharetra, est eros bibendum elit, nec luctus magna felis sollicitudin mauris. Integer in mauris eu nibh euismod gravida. Duis ac tellus et risus vulputate vehicula.' },
    ],
  },
]

// ── Ambil slug dari query parameter URL ──────────────────────────────────
const page = usePage()

const currentSlug = computed(() => {
  const url = new URL(page.url, window.location.origin)
  return url.searchParams.get('mountain') || 'gunung-sumbing'
})

const currentMountain = computed(() => {
  return allMountains.find(m => m.slug === currentSlug.value) || allMountains[0]
})

const selectedRoute = ref(null)

const otherDestinations = computed(() => {
  return allMountains.filter(m => m.slug !== currentSlug.value).slice(0, 6)
})

watch(currentSlug, () => {
  selectedRoute.value = null
  window.scrollTo({ top: 0, behavior: 'smooth' })
})
</script>

<template>
  <Head :title="`Article - ${currentMountain.name}`" />

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
    <div v-if="!selectedRoute" class="w-full flex justify-center py-10">
      <!-- Outer Container -->
      <div 
        class="relative rounded-[40px] shadow-2xl p-10 flex flex-col items-center"
        style="
          width: 1180px; 
          min-height: 1820px; 
          background: linear-gradient(180deg, rgba(126,98,63,0.8) 26%, rgba(60,38,12,0.8) 87%);
        "
      >
        <!-- If Mountain View -->
        <template v-if="!selectedRoute">
          <!-- Title -->
          <h1 class="text-[#F8F3E4] text-[64px] font-semibold mb-10 drop-shadow-[0_2px_8px_rgba(255,255,255,0.6)] tracking-wide font-sans text-center">
            {{ currentMountain.name }}
          </h1>

          <!-- Content Area -->
          <div class="w-full flex gap-10">
            
            <!-- Left Column -->
            <div class="w-[504px] flex flex-col gap-6 shrink-0">
              <!-- Image -->
              <div class="w-full h-[1176px] rounded-[30px] overflow-hidden shadow-lg border-2 border-white/10">
                <img :src="currentMountain.image" :alt="currentMountain.name" class="w-full h-full object-cover" />
              </div>

              <!-- Routes Section -->
              <div class="w-full flex flex-col gap-3">
                <!-- Title Container -->
                <div class="w-full bg-[#FFFFFF]/20 backdrop-blur-[4px] border border-white/20 shadow-sm rounded-[20px] py-4 px-6">
                  <h3 class="text-[#EDE6D2] font-semibold text-[20px]" style="font-family: 'Montserrat', sans-serif;">Jalur Pendakian Resmi</h3>
                </div>
                
                <!-- Routes Container -->
                <div class="w-full bg-[#FFFFFF]/20 backdrop-blur-[4px] border border-white/20 shadow-sm rounded-[20px] p-6 flex flex-col gap-3">
                  <button 
                    v-for="(route, idx) in currentMountain.routes" 
                    :key="idx"
                    @click="selectedRoute = route"
                    class="w-full bg-[#D6CCAF] text-[#7E623F] text-[17px] font-medium py-3 px-5 rounded-xl shadow-sm text-left hover:bg-[#c4b998] hover:scale-[1.02] transition-all cursor-pointer" 
                    style="font-family: 'Montserrat', sans-serif;"
                  >
                    {{ route }}
                  </button>
                </div>
              </div>
            </div>

            <!-- Right Column (Text Content) -->
            <div class="flex-1 flex flex-col gap-8 text-[#F8F3E4] leading-relaxed pr-4" style="font-family: 'Poppins', sans-serif;">
              
              <template v-for="(section, idx) in currentMountain.content" :key="idx">
                <!-- First paragraph (with bold mountain name) -->
                <p v-if="!section.title" class="text-justify text-[20px]">
                  <span class="font-semibold">{{ currentMountain.name }}</span> {{ section.text.substring(section.text.indexOf(' ') + 1) }}
                </p>

                <!-- Sub-sections with title -->
                <div v-else class="flex flex-col gap-2">
                  <h3 class="font-semibold text-xl text-[#F8F3E4]">{{ section.title }}</h3>
                  <p class="text-justify text-[20px]">{{ section.text }}</p>
                </div>
              </template>

            </div>
          </div>
        </template>

        <!-- If Route View -->
        <template v-else>
        </template>
      </div>
    </div>

    <!-- ═══════════════════════════════════════════════════════════════════ -->
    <!-- ROUTE DETAIL PAGE (full-width, outside the mountain container)    -->
    <!-- ═══════════════════════════════════════════════════════════════════ -->
    <template v-if="selectedRoute">

      <!-- ── Section 1: Curved Title ────────────────────────────────── -->
      <section class="route-hero">
        <div class="route-hero__inner">
          <!-- Curved Title using SVG textPath -->
          <svg class="route-hero__svg" :viewBox="`0 0 900 220`" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <!-- Circular arc with larger radius for an even flatter curve -->
              <path
                id="title-curve"
                d="M -100,240 A 2200,2200 0 0,1 1000,240"
                fill="none"
              />
              <!-- Drop shadow filter -->
              <filter id="title-shadow" x="-10%" y="-10%" width="130%" height="150%">
                <feDropShadow dx="0" dy="4" stdDeviation="6" flood-color="rgba(0,0,0,0.25)" />
              </filter>
            </defs>

            <!-- Stroke outline layer (behind) -->
            <text
              class="route-hero__text-stroke"
              filter="url(#title-shadow)"
            >
              <textPath href="#title-curve" startOffset="51.5%" text-anchor="middle">JALUR PENDAKIAN {{ selectedRoute.toUpperCase().replace('JALUR ', '') }}</textPath>
            </text>

            <!-- Fill layer (on top) -->
            <text
              class="route-hero__text-fill"
            >
              <textPath href="#title-curve" startOffset="51.5%" text-anchor="middle">JALUR PENDAKIAN {{ selectedRoute.toUpperCase().replace('JALUR ', '') }}</textPath>
            </text>
          </svg>
        </div>
      </section>

      <!-- ── Section 2: Peta Jalur (placeholder) ─────────────────────── -->
      <section class="route-section route-map-section">
        <div class="route-container">
          <div class="route-map-frame">
            <!-- Empty map placeholder -->
            <div class="route-map-empty">
              <svg xmlns="http://www.w3.org/2000/svg" class="w-24 h-24 text-[#A2825B]/20" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 6.75V15m6-6v8.25m.503 3.498 4.875-2.437c.381-.19.622-.58.622-1.006V4.82c0-.836-.88-1.38-1.628-1.006l-3.869 1.934c-.317.159-.69.159-1.006 0L9.503 3.252a1.125 1.125 0 0 0-1.006 0L3.622 5.689C3.24 5.88 3 6.27 3 6.695V19.18c0 .836.88 1.38 1.628 1.006l3.869-1.934c.317-.159.69-.159 1.006 0l4.994 2.497c.317.158.69.158 1.006 0Z" />
              </svg>
              <span class="route-map-empty__text">Peta Jalur Pendakian akan ditampilkan di sini</span>
            </div>
          </div>
        </div>
      </section>

      <!-- ── Section 3: Deskripsi ────────────────────────────────────── -->
      <section class="route-section route-deskripsi-section">
        <div class="route-container">
          <div class="route-card">
            <h2 class="route-card__title mb-8">Deskripsi</h2>
            <p class="route-card__text">
              Jalur Cepit merupakan rute pendakian Gunung Sumbing yang dikenal sebagai jalur tercepat namun memiliki tantangan fisik yang luar biasa karena sudut kemiringannya yang sangat ekstrem. Terletak di Desa Cepit, Kabupaten Temanggung, jalur ini menawarkan suasana yang jauh lebih tenang dan sunyi dibandingkan jalur Garung, melewati hutan alam yang masih sangat asri hingga mencapai area kawah yang luas.
            </p>
          </div>
        </div>
      </section>

      <!-- ── Section 4: Estimasi Waktu per Pos ───────────────────────── -->
      <section class="route-section route-estimasi-section">
        <div class="route-container">
          <div class="route-card">
            <h2 class="route-card__title text-center mb-8">Estimasi waktu per Pos</h2>
            <div class="route-estimasi-grid">
              <!-- Pos items -->
              <div v-for="pos in 6" :key="pos" class="route-pos-item">
                <div class="route-pos-item__icon mt-1">
                  <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6 text-[#374426]" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                  </svg>
                </div>
                <div class="route-pos-item__content">
                  <div class="flex items-center gap-2 mb-1">
                    <h4 class="route-pos-item__title">Pos {{ pos }} Lorem Ipsum (1.000 mdpl)</h4>
                  </div>
                  <p class="route-pos-item__desc">
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                  </p>
                  <div class="route-pos-item__time mt-1 flex items-center gap-1.5">
                    <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
                    </svg>
                    <span>{{ pos * 15 }} Menit</span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- Legends -->
            <div class="route-estimasi-legends mt-10 flex flex-col gap-1">
              <div class="flex items-center gap-2">
                <svg class="w-5 h-5 text-[#8CB4D6]" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 21a9.004 9.004 0 0 0 8.716-6.747M12 21a9.004 9.004 0 0 1-8.716-6.747M12 21c2.485 0 4.5-4.03 4.5-9S12 3 12 3s-4.5 4.03-4.5 9 2.015 9 4.5 9Z" /></svg>
                <span class="text-[#8CB4D6] font-medium text-[18px]" style="font-family: 'Montserrat', sans-serif;">Water Source</span>
              </div>
              <div class="flex items-center gap-2">
                <svg class="w-5 h-5 text-[#374426]" viewBox="0 0 24 24" fill="currentColor"><path d="M17.49 17L12 6.5 6.51 17h10.98zM12 2L1 21h22L12 2z"/></svg>
                <span class="text-[#374426] font-medium text-[18px]" style="font-family: 'Montserrat', sans-serif;">Campsite</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ── Section 5: Gallery + Statistik & Tips ───────────────────── -->
      <section class="route-section route-stats-section">
        <div class="route-container">
          <div class="route-stats-wrapper">
            <!-- Left: Image Gallery -->
            <div class="route-stats-gallery">
              <div v-for="n in 2" :key="n" class="relative w-full h-[280px] rounded-[30px] overflow-hidden flex flex-col bg-[#F0ECD8] border-2 border-dashed border-[#D7DDC2]">
                <!-- Image Placeholder -->
                <div class="flex-1 flex items-center justify-center">
                  <svg xmlns="http://www.w3.org/2000/svg" class="w-16 h-16 text-[#A2825B]/30" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1">
                    <path stroke-linecap="round" stroke-linejoin="round" d="m2.25 15.75 5.159-5.159a2.25 2.25 0 0 1 3.182 0l5.159 5.159m-1.5-1.5 1.409-1.409a2.25 2.25 0 0 1 3.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0 0 22.5 18.75V5.25A2.25 2.25 0 0 0 20.25 3H3.75A2.25 2.25 0 0 0 1.5 5.25v13.5A2.25 2.25 0 0 0 3.75 21Z" />
                  </svg>
                </div>
                <!-- Text Container -->
                <div class="w-full bg-[#7E623F] px-6 py-4 flex justify-between items-center z-10">
                  <span class="font-['Montserrat'] font-semibold text-[22px] text-[#F8F3E4]">{{ selectedRoute }}</span>
                  <span class="font-['Montserrat'] font-medium text-[16px] text-[#F8F3E4]">{{ currentMountain.name.replace('Gunung ', '') }}</span>
                </div>
              </div>
            </div>

            <!-- Right: Statistik & Tips -->
            <div class="route-stats-info route-card">
              <div class="route-stats-block">
                <h3 class="route-stats-block__title">Statistik Jalur</h3>
                <ul class="route-stats-list">
                  <li><strong>Jarak Tempuh:</strong> ± 7.5 Km</li>
                  <li><strong>Ketinggian Maksimal:</strong> 3.371 mdpl</li>
                  <li><strong>Kemiringan Rata-rata:</strong> 31.5 % (Cukup terjal)</li>
                  <li><strong>Sumber Air:</strong> Tersedia (Pos 2 dan Pos 3)</li>
                </ul>
              </div>

              <div class="route-stats-block">
                <h3 class="route-stats-block__title">Tips Khusus</h3>
                <p class="route-stats-block__text">
                  Karakteristik: <br>
                  Dikenal sebagai "Jalur Spiritual" karena banyaknya petilasan keramat. Medannya didominasi oleh hutan rimbun di awal dan berubah menjadi lautan pasir putih (Segoro Wedi) saat mendekati kawah.
                </p>
                <p class="route-stats-block__text">
                  Saran: <br>
                  Gunakan jasa ojek dari Basecamp ke Pos 1 jika ingin menghemat waktu 80 menit.
                </p>
                <p class="route-stats-block__text">
                  Moment Terbaik: <br>
                  Menikmati suasana tenang di Segoro Wedi, sebuah lembah pasir tersembunyi yang dikelilingi tebing megah Sumbing, memberikan sensasi pendakian yang berbeda dari jalur lainnya.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ── Section 6: Cuaca / Weather ──────────────────────────────── -->
      <section class="route-section route-weather-section">
        <div class="route-container">
          <div class="route-weather-card">
            <div class="route-weather-card__header">
              <span class="route-weather-card__badge">Cuaca</span>
            </div>
            <div class="route-weather-card__body">
              <!-- Left: Current Weather -->
              <div class="route-weather-current">
                <div class="route-weather-current__mountain">
                  <h3>Gunung</h3>
                  <h2>Sumbing</h2>
                </div>
                <div class="route-weather-current__icon">
                  <!-- Sun + cloud icon placeholder -->
                  <svg xmlns="http://www.w3.org/2000/svg" class="w-16 h-16" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 15a4.5 4.5 0 0 0 4.5 4.5H18a3.75 3.75 0 0 0 1.332-7.257 3 3 0 0 0-3.758-3.848 5.25 5.25 0 0 0-10.233 2.33A4.502 4.502 0 0 0 2.25 15Z" />
                  </svg>
                </div>
                <div class="route-weather-current__temp">
                  <span class="route-weather-current__degrees">24</span>
                  <span class="route-weather-current__unit">°C</span>
                </div>
                <div class="route-weather-current__condition">Berawan Sebagian</div>
              </div>

              <!-- Right: Forecast Table -->
              <div class="route-weather-forecast">
                <table class="route-weather-table">
                  <thead>
                    <tr>
                      <th>Hari</th>
                      <th>Suhu</th>
                      <th>Cuaca</th>
                      <th>Angin</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="day in ['Senin', 'Selasa', 'Rabu', 'Kamis', 'Jumat']" :key="day">
                      <td>{{ day }}</td>
                      <td>22-26°C</td>
                      <td>Cerah Berawan</td>
                      <td>15 km/jam</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Bottom: Additional Info -->
            <div class="route-weather-card__footer">
              <div class="route-weather-info-item">
                <span class="route-weather-info-item__label">🌡 Kelembaban</span>
                <span class="route-weather-info-item__value">75%</span>
              </div>
              <div class="route-weather-info-item">
                <span class="route-weather-info-item__label">🌧 Curah Hujan</span>
                <span class="route-weather-info-item__value">12mm</span>
              </div>
              <div class="route-weather-info-item">
                <span class="route-weather-info-item__label">👁 Visibilitas</span>
                <span class="route-weather-info-item__value">8 km</span>
              </div>
              <div class="route-weather-info-item">
                <span class="route-weather-info-item__label">🌅 Sunrise</span>
                <span class="route-weather-info-item__value">05:42</span>
              </div>
              <div class="route-weather-info-item">
                <span class="route-weather-info-item__label">🌇 Sunset</span>
                <span class="route-weather-info-item__value">17:28</span>
              </div>
              <div class="route-weather-info-item">
                <span class="route-weather-info-item__label">💨 Tekanan</span>
                <span class="route-weather-info-item__value">1013 hPa</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ── Section 7: Footer ───────────────────────────────────────── -->
      <footer class="route-footer">
        <div class="route-footer__inner">
          <!-- Left: Brand -->
          <div class="route-footer__brand">
            <div class="route-footer__logo">
              <img src="/images/logo_2.png" alt="AltiGuide" class="w-10 h-10 object-contain" />
              <span class="route-footer__logo-text">AltiGuide</span>
            </div>
          </div>

          <!-- Center: Action Buttons -->
          <div class="route-footer__actions">
            <button class="route-footer__btn route-footer__btn--outline">Selengkapnya</button>
            <button class="route-footer__btn route-footer__btn--primary">Start Guiding</button>
          </div>

          <!-- Right: Links -->
          <div class="route-footer__links">
            <div class="route-footer__link-group">
              <h4>Fitur</h4>
              <a href="#">Lorem Ipsum</a>
              <a href="#">Lorem Ipsum</a>
              <a href="#">Lorem Ipsum</a>
            </div>
            <div class="route-footer__link-group">
              <h4>Tentang</h4>
              <a href="#">Lorem Ipsum</a>
              <a href="#">Lorem Ipsum</a>
              <a href="#">Lorem Ipsum</a>
            </div>
            <div class="route-footer__link-group">
              <h4>Bantuan</h4>
              <a href="#">Lorem Ipsum</a>
              <a href="#">Lorem Ipsum</a>
              <a href="#">Lorem Ipsum</a>
            </div>
          </div>
        </div>

        <!-- Bottom Bar -->
        <div class="route-footer__bottom">
          <span>AltiGuide.com</span>
          <div class="route-footer__social">
            <a href="#" aria-label="Instagram">📷</a>
            <a href="#" aria-label="Twitter">🐦</a>
            <a href="#" aria-label="Facebook">📘</a>
          </div>
        </div>
      </footer>

    </template>

    <!-- Other Destinations Section -->
    <div v-if="!selectedRoute" class="w-full flex flex-col items-center mt-[40px] pb-24 px-4">
      <div class="w-full max-w-[1180px] flex flex-col">
        <h2 class="text-[#64823E] font-bold text-[48px] mb-[32px]" style="font-family: 'Montserrat', sans-serif;">Other Destinations</h2>
        
        <div class="flex flex-wrap gap-[50px] justify-center md:justify-start">
          <div 
            v-for="dest in otherDestinations" 
            :key="dest.slug"
            class="w-[360px] h-[450px] rounded-[30px] overflow-hidden bg-[#D6CCAF] flex flex-col shadow-lg"
          >
            <!-- Image Area -->
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
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700;800;900&family=Poppins:wght@400;500;600&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Jost:wght@700;800;900&display=swap');

/* ═══════════════════════════════════════════════════════════════════════
   BASE / SHARED
   ═══════════════════════════════════════════════════════════════════════ */

h1 {
  font-family: 'Montserrat', sans-serif;
}

.route-container {
  width: 100%;
  max-width: 1180px;
  margin: 0 auto;
  padding: 0 24px;
}

.route-section {
  width: 100%;
  padding: 40px 0;
}

.route-section-title {
  font-family: 'Montserrat', sans-serif;
  font-weight: 700;
  font-size: 32px;
  color: #374426;
  margin-bottom: 24px;
  position: relative;
}

.route-section-title--center {
  text-align: center;
}

/* ═══════════════════════════════════════════════════════════════════════
   SECTION 1: HERO — CURVED TITLE
   ═══════════════════════════════════════════════════════════════════════ */

.route-hero {
  width: 100%;
  background: #F8F3E4;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 50px 24px 60px;
}

.route-hero__inner {
  width: 100%;
  max-width: 1180px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.route-hero__svg {
  width: 100%;
  max-width: 900px;
  height: auto;
  overflow: visible;
}

/* Stroke outline layer — renders behind the fill */
.route-hero__text-stroke {
  font-family: 'Jost', sans-serif;
  font-weight: 700;
  font-size: 75px;
  letter-spacing: -0.02em;
  fill: #64823E;
  stroke: #64823E;
  stroke-width: 10px;
  stroke-linejoin: round;
  paint-order: stroke fill;
}

/* Fill layer — renders on top */
.route-hero__text-fill {
  font-family: 'Jost', sans-serif;
  font-weight: 700;
  font-size: 75px;
  letter-spacing: -0.02em;
  fill: #FFFEF0;
}

/* ═══════════════════════════════════════════════════════════════════════
   SECTION 2: MAP PLACEHOLDER
   ═══════════════════════════════════════════════════════════════════════ */

.route-map-section {
  background: #F8F3E4;
  padding-top: 0;
}

.route-map-frame {
  width: 100%;
  border: 2px solid #C8C4A9;
  border-radius: 4px;
  background: #FFFEF5;
  overflow: hidden;
}

.route-map-empty {
  width: 100%;
  min-height: 700px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #F5F0DC;
  gap: 12px;
}

.route-map-empty__text {
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  color: #A2825B;
  opacity: 0.5;
}

/* ═══════════════════════════════════════════════════════════════════════
   COMMON CARD STYLES
   ═══════════════════════════════════════════════════════════════════════ */

.route-card {
  background: linear-gradient(135deg, #E5E6D5 0%, #C3CE8F 100%);
  border-radius: 40px;
  box-shadow: 0px 4px 15px rgba(0, 0, 0, 0.25);
  padding: 32px 48px;
  display: flex;
  flex-direction: column;
}

.route-card__title {
  font-family: 'Montserrat', sans-serif;
  font-weight: 700;
  font-size: 40px;
  color: #4B632B;
  line-height: 1.2;
}

.route-card__text {
  font-family: 'Montserrat', sans-serif;
  font-weight: 400;
  font-size: 22px;
  color: #374426;
  line-height: 1.6;
  margin: 0;
  text-align: justify;
}


.route-deskripsi-section {
  background: #F8F3E4;
  padding-top: 45px;
}

.route-deskripsi-section .route-card {
  gap: 12px;
}

.route-estimasi-section {
  background: #F8F3E4;
  padding-top: 45px;
}

.route-estimasi-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  row-gap: 24px;
  column-gap: 40px;
}

.route-pos-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.route-pos-item__icon {
  flex-shrink: 0;
}

.route-pos-item__title {
  font-family: 'Montserrat', sans-serif;
  font-weight: 600;
  font-size: 22px;
  color: #374426;
  margin: 0;
  line-height: 1.4;
}

.route-pos-item__desc {
  font-family: 'Montserrat', sans-serif;
  font-weight: 400;
  font-size: 22px;
  color: #374426;
  line-height: 1.5;
  margin: 0;
  text-align: justify;
}

.route-pos-item__time {
  font-family: 'Montserrat', sans-serif;
  font-weight: 400;
  font-size: 18px;
  color: #374426;
}

/* ═══════════════════════════════════════════════════════════════════════
   SECTION 5: GALLERY + STATISTIK & TIPS
   ═══════════════════════════════════════════════════════════════════════ */

.route-stats-section {
  background: #F8F3E4;
}

.route-stats-wrapper {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.route-stats-gallery {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.route-stats-gallery__main {
  width: 100%;
}

.route-stats-gallery__thumbs {
  display: flex;
  gap: 12px;
}

.route-gallery-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F0ECD8;
  border: 2px dashed #D7DDC2;
  border-radius: 12px;
}

.route-gallery-placeholder--large {
  width: 100%;
  height: 280px;
}

.route-gallery-placeholder--small {
  flex: 1;
  height: 140px;
}

.route-stats-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.route-stats-block__title {
  font-family: 'Montserrat', sans-serif;
  font-weight: 700;
  font-size: 22px;
  color: #374426;
  margin-bottom: 12px;
}

.route-stats-list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  color: #4A4A3A;
}

.route-stats-list li {
  padding: 4px 0;
}

.route-stats-block__text {
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  line-height: 1.7;
  color: #4A4A3A;
  margin-bottom: 10px;
  text-align: justify;
}

/* ═══════════════════════════════════════════════════════════════════════
   SECTION 6: WEATHER / CUACA
   ═══════════════════════════════════════════════════════════════════════ */

.route-weather-section {
  background: #F8F3E4;
  padding-bottom: 60px;
}

.route-weather-card {
  background: linear-gradient(135deg, #4A6FA5 0%, #6B4FA0 50%, #3D2C7C 100%);
  border-radius: 24px;
  overflow: hidden;
  color: #fff;
}

.route-weather-card__header {
  padding: 16px 24px 0;
}

.route-weather-card__badge {
  display: inline-block;
  font-family: 'Montserrat', sans-serif;
  font-weight: 700;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 20px;
  border-radius: 20px;
  letter-spacing: 1px;
}

.route-weather-card__body {
  display: flex;
  padding: 24px;
  gap: 32px;
  align-items: flex-start;
}

.route-weather-current {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 200px;
}

.route-weather-current__mountain h3 {
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
}

.route-weather-current__mountain h2 {
  font-family: 'Montserrat', sans-serif;
  font-weight: 800;
  font-size: 32px;
  color: #fff;
  line-height: 1.1;
}

.route-weather-current__icon {
  color: #FFD700;
}

.route-weather-current__temp {
  display: flex;
  align-items: flex-start;
}

.route-weather-current__degrees {
  font-family: 'Montserrat', sans-serif;
  font-weight: 800;
  font-size: 56px;
  line-height: 1;
}

.route-weather-current__unit {
  font-family: 'Montserrat', sans-serif;
  font-weight: 600;
  font-size: 20px;
  margin-top: 8px;
}

.route-weather-current__condition {
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.route-weather-forecast {
  flex: 1;
}

.route-weather-table {
  width: 100%;
  border-collapse: collapse;
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
}

.route-weather-table thead th {
  text-align: left;
  font-weight: 600;
  padding: 8px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.route-weather-table tbody td {
  padding: 8px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.9);
}

.route-weather-card__footer {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px 24px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.route-weather-info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 120px;
}

.route-weather-info-item__label {
  font-family: 'Poppins', sans-serif;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.route-weather-info-item__value {
  font-family: 'Montserrat', sans-serif;
  font-weight: 700;
  font-size: 16px;
  color: #fff;
}

/* ═══════════════════════════════════════════════════════════════════════
   SECTION 7: FOOTER
   ═══════════════════════════════════════════════════════════════════════ */

.route-footer {
  width: 100%;
  background: #2C3E1E;
  color: #D7DDC2;
  padding: 40px 24px 0;
}

.route-footer__inner {
  max-width: 1180px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 40px;
  padding-bottom: 32px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.route-footer__brand {
  flex-shrink: 0;
}

.route-footer__logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.route-footer__logo-text {
  font-family: 'Montserrat', sans-serif;
  font-weight: 800;
  font-size: 24px;
  color: #F8F3E4;
}

.route-footer__actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.route-footer__btn {
  font-family: 'Montserrat', sans-serif;
  font-weight: 600;
  font-size: 14px;
  padding: 10px 24px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.route-footer__btn--outline {
  background: transparent;
  border: 1px solid #D7DDC2;
  color: #D7DDC2;
}

.route-footer__btn--outline:hover {
  background: rgba(255, 255, 255, 0.1);
}

.route-footer__btn--primary {
  background: #5F8D4E;
  border: 1px solid #5F8D4E;
  color: #fff;
}

.route-footer__btn--primary:hover {
  background: #4E7A3E;
}

.route-footer__links {
  display: flex;
  gap: 40px;
}

.route-footer__link-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.route-footer__link-group h4 {
  font-family: 'Montserrat', sans-serif;
  font-weight: 700;
  font-size: 14px;
  color: #F8F3E4;
  margin-bottom: 4px;
}

.route-footer__link-group a {
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  color: #A8B497;
  text-decoration: none;
  transition: color 0.2s;
}

.route-footer__link-group a:hover {
  color: #F8F3E4;
}

.route-footer__bottom {
  max-width: 1180px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  color: #A8B497;
}

.route-footer__social {
  display: flex;
  gap: 12px;
}

.route-footer__social a {
  font-size: 18px;
  text-decoration: none;
  transition: opacity 0.2s;
}

.route-footer__social a:hover {
  opacity: 0.7;
}
</style>
