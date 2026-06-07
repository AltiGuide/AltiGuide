<script setup>
import { Link, Head, usePage, router } from '@inertiajs/vue3'
import { computed, watch, ref, onMounted } from 'vue'

const props = defineProps({
  mountains: {
    type: Array,
    default: () => []
  }
})

const allMountains = computed(() => props.mountains)

const page = usePage()
const authUser = computed(() => page.props.auth?.user)

const currentSlug = computed(() => {
  const url = new URL(page.url, window.location.origin)
  return url.searchParams.get('mountain')
})

const currentMountain = computed(() => {
  if (!currentSlug.value) return null
  return allMountains.value.find(m => m.slug === currentSlug.value) || null
})

const selectedRoute = ref(null)
const mapLightbox = ref(false)

const routeName = computed(() => {
  if (!selectedRoute.value) return ''
  return selectedRoute.value.short_name || selectedRoute.value.name || ''
})

const otherDestinations = computed(() => {
  if (!currentSlug.value) return []
  return allMountains.value.filter(m => m.slug !== currentSlug.value).slice(0, 6)
})

const weather = ref(null)
const weatherLoading = ref(false)
const weatherError = ref(null)

const WMO_CODES = {
  0:  { label: 'Cerah', icon: '/images/weather-icon/sun.png' },
  1:  { label: 'Hampir Cerah', icon: '/images/weather-icon/cloudysunny.png' },
  2:  { label: 'Berawan Sebagian', icon: '/images/weather-icon/cloudysunny.png' },
  3:  { label: 'Berawan Penuh', icon: '/images/weather-icon/clouds.png' },
  45: { label: 'Berkabut', icon: '/images/weather-icon/clouds.png' },
  48: { label: 'Kabut Beku', icon: '/images/weather-icon/clouds.png' },
  51: { label: 'Gerimis Ringan', icon: '/images/weather-icon/sun-clouds-rain.png' },
  53: { label: 'Gerimis', icon: '/images/weather-icon/sun-clouds-rain.png' },
  55: { label: 'Gerimis Lebat', icon: '/images/weather-icon/sun-clouds-rain.png' },
  61: { label: 'Hujan Ringan', icon: '/images/weather-icon/sun-clouds-rain.png' },
  63: { label: 'Hujan Sedang', icon: '/images/weather-icon/sun-clouds-rain.png' },
  65: { label: 'Hujan Lebat', icon: '/images/weather-icon/sun-clouds-rain.png' },
  71: { label: 'Salju Ringan', icon: '/images/weather-icon/clouds-snow.png' },
  80: { label: 'Hujan Lokal', icon: '/images/weather-icon/sun-clouds-rain.png' },
  95: { label: 'Badai Petir', icon: '/images/weather-icon/lightning.png' },
}

function getWmoLabel(code) {
  return WMO_CODES[code]?.label ?? 'Tidak Diketahui'
}
function getWmoIcon(code) {
  return WMO_CODES[code]?.icon ?? '/images/weather-icon/sun.png'
}

function formatHour(isoStr) {
  if (!isoStr) return '--:--'
  return isoStr.slice(11, 16)
}

const HARI = ['Minggu', 'Senin', 'Selasa', 'Rabu', 'Kamis', 'Jumat', 'Sabtu']

async function fetchWeather(lat, lon) {
  weatherLoading.value = true
  weatherError.value = null
  weather.value = null
  try {
    const url = new URL('https://api.open-meteo.com/v1/forecast')
    url.searchParams.set('latitude', lat)
    url.searchParams.set('longitude', lon)
    url.searchParams.set('current', [
      'temperature_2m',
      'apparent_temperature',
      'cloudcover',
      'weathercode',
      'windspeed_10m',
      'relativehumidity_2m',
      'precipitation',
      'visibility',
      'surface_pressure',
    ].join(','))
    url.searchParams.set('hourly', [
      'temperature_2m',
      'weathercode'
    ].join(','))
    url.searchParams.set('daily', [
      'weathercode',
      'temperature_2m_max',
      'temperature_2m_min',
      'windspeed_10m_max',
      'sunrise',
      'sunset',
      'precipitation_sum',
    ].join(','))
    url.searchParams.set('timezone', 'Asia/Jakarta')
    url.searchParams.set('forecast_days', '6')

    const res = await fetch(url.toString())
    if (!res.ok) throw new Error('Gagal mengambil data cuaca')
    const data = await res.json()
    weather.value = data
  } catch (err) {
    weatherError.value = err.message || 'Terjadi kesalahan'
  } finally {
    weatherLoading.value = false
  }
}

const hourlyForecast = computed(() => {
  if (!weather.value?.hourly?.time) return []
  const now = new Date()
  const idx = weather.value.hourly.time.findIndex(t => new Date(t) >= now)
  const startIndex = idx !== -1 ? idx : 0

  return weather.value.hourly.time.slice(startIndex, startIndex + 8).map((timeStr, i) => {
    const actualIndex = startIndex + i
    const d = new Date(timeStr)
    return {
      time: d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true }),
      temp: Math.round(weather.value.hourly.temperature_2m[actualIndex]),
      code: weather.value.hourly.weathercode[actualIndex]
    }
  })
})

watch(currentSlug, () => {
  selectedRoute.value = null
  weather.value = null
  window.scrollTo({ top: 0, behavior: 'smooth' })
})

watch(selectedRoute, (route) => {
  mapLightbox.value = false
  if (route && route.latitude && route.longitude) {
    fetchWeather(route.latitude, route.longitude)
  }
})

function difficultyLabel(d) {
  if (d === 'easy') return 'Mudah'
  if (d === 'moderate') return 'Menengah'
  if (d === 'hard') return 'Sulit'
  return d || '-'
}

function formatDuration(minutes) {
  if (!minutes) return '-'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h === 0) return `${m} menit`
  if (m === 0) return `${h} jam`
  return `${h} jam ${m} menit`
}

function formatRupiah(val) {
  if (!val) return '-'
  return 'Rp ' + Number(val).toLocaleString('id-ID')
}

const waterSourceWaypoints = computed(() => {
  if (!selectedRoute.value?.waypoints) return []
  return selectedRoute.value.waypoints.filter(wp => wp.has_water_source)
})
</script>

<template>
  <div class="min-h-screen bg-[#F8F3E4] font-sans">
    <Head :title="currentMountain ? `Article - ${currentMountain.name}` : 'Artikel Gunung Jawa Tengah - AltiGuide'" />

    <nav class="w-full flex justify-between items-center px-4 md:px-8 xl:px-12 py-4 border-b border-[#D7DDC2]/50 shadow-sm bg-[#374426]/20">
      <div class="flex items-center gap-2 xl:gap-3">
        <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 md:w-10 md:h-10 object-contain" />
        <span class="text-xl md:text-2xl xl:text-3xl font-bold text-[#374426] tracking-tight">AltiGuide</span>
      </div>

      <div class="flex items-center gap-4 md:gap-6 xl:gap-9 text-sm md:text-base font-semibold text-[#374426]">
        <a href="/" class="hover:text-black transition">Home</a>
        <a href="/article" class="hover:text-black transition">Article</a>
        <a href="/booking" class="hover:text-black transition">Booking</a>
                <template v-if="authUser">
          <a
            href="/dashboard"
            class="flex items-center gap-2 border border-[#374426] px-4 md:px-5 py-2 rounded-lg hover:bg-[#374426] hover:text-white transition duration-200 whitespace-nowrap"
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
          <a href="/login" class="border border-[#374426] px-4 md:px-6 py-2 rounded-lg hover:bg-[#374426] hover:text-white transition duration-200 whitespace-nowrap">
            Login
          </a>
        </template>
      </div>
    </nav>

    <div v-if="!currentSlug" class="w-full flex flex-col items-center py-12">
      <div class="w-full max-w-[1340px] px-6 mb-12 flex flex-col items-center">
        <h1 class="text-[#374426] text-[48px] font-bold tracking-tight mb-4 text-center" style="font-family: 'Montserrat', sans-serif;">
          Artikel Gunung Jawa Tengah
        </h1>
        <p class="text-[#5A684C] text-[18px] font-medium text-center max-w-[650px] leading-relaxed" style="font-family: 'Montserrat', sans-serif;">
          Temukan info rute pendakian resmi, estimasi waktu perjalanan, informasi cuaca real-time, dan tips keselamatan penting untuk petualangan summit Anda berikutnya.
        </p>
      </div>

      <div class="w-full max-w-[1340px] px-6">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10 justify-between">
          <div
            v-for="dest in allMountains"
            :key="dest.slug"
            class="w-full h-[450px] rounded-[30px] overflow-hidden bg-[#D6CCAF] flex flex-col shadow-lg transition duration-300 hover:scale-[1.03] hover:shadow-xl"
          >
            <div class="relative w-full h-[210px] shrink-0 group overflow-hidden">
              <img :src="dest.image" :alt="dest.name" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" @error="e => e.target.style.display='none'" />
              <Link
                :href="`/article?mountain=${dest.slug}`"
                class="absolute bottom-6 left-1/2 -translate-x-1/2 bg-[#374426]/20 backdrop-blur-[6px] border border-white/15 shadow-md text-white font-bold px-8 py-2.5 rounded-full text-[18px] hover:bg-[#374426]/40 transition duration-200 whitespace-nowrap cursor-pointer"
                style="font-family: 'Montserrat', sans-serif;"
              >
                Read More
              </Link>
            </div>

            <div class="flex-1 p-6 flex flex-col justify-between bg-[#D6CCAF]">
              <div class="flex flex-col gap-3">
                <h4 class="text-[#374426] font-bold text-[28px] leading-tight" style="font-family: 'Montserrat', sans-serif;">{{ dest.name }}</h4>
                <p class="text-[15px] font-normal leading-relaxed text-[#5A684C] line-clamp-3" style="font-family: 'Montserrat', sans-serif;">
                  {{ dest.content[0]?.text }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

        <template v-else-if="currentMountain">
            <div v-if="!selectedRoute" class="w-full flex justify-center py-10">
        <div
          class="relative rounded-[40px] shadow-2xl p-10 flex flex-col items-center"
          style="max-width: 1340px; width: 100%; min-height: 1000px; background: linear-gradient(180deg, rgba(126,98,63,0.8) 26%, rgba(60,38,12,0.8) 87%);"
        >
          <h1 class="text-[#F8F3E4] text-[64px] font-semibold mb-10 drop-shadow-[0_2px_8px_rgba(255,255,255,0.6)] tracking-wide font-sans text-center">
            {{ currentMountain.name }}
          </h1>

          <div class="w-full flow-root text-[#F8F3E4] leading-relaxed pr-4" style="font-family: 'Poppins', sans-serif;">
                        <div class="float-left w-[504px] mr-10 mb-6 flex flex-col gap-6">
              <div class="w-full h-[600px] rounded-[30px] overflow-hidden shadow-lg border-2 border-white/10">
                <img :src="currentMountain.image" :alt="currentMountain.name" class="w-full h-full object-cover" @error="e => e.target.style.opacity='0'" />
              </div>

              <div class="w-full flex flex-col gap-3">
                <div class="w-full bg-[#FFFFFF]/20 backdrop-blur-[4px] border border-white/20 shadow-sm rounded-[20px] py-4 px-6">
                  <h3 class="text-[#EDE6D2] font-semibold text-[20px]" style="font-family: 'Montserrat', sans-serif;">Jalur Pendakian Resmi</h3>
                </div>

                <div class="w-full bg-[#FFFFFF]/20 backdrop-blur-[4px] border border-white/20 shadow-sm rounded-[20px] p-6 flex flex-col gap-3">
                  <button
                    v-for="(route, idx) in currentMountain.routes"
                    :key="route.id"
                    @click="selectedRoute = route"
                    class="w-full bg-[#D6CCAF] text-[#7E623F] text-[17px] font-medium py-3 px-5 rounded-xl shadow-sm text-left hover:bg-[#c4b998] hover:scale-[1.02] transition-all cursor-pointer"
                    style="font-family: 'Montserrat', sans-serif;"
                  >
                    {{ route.short_name }}
                  </button>
                </div>
              </div>
            </div>

                        <template v-for="(section, idx) in currentMountain.content" :key="idx">
              <p v-if="!section.title" class="text-justify text-[18px] mb-8">
                <span class="font-semibold">{{ currentMountain.name }}</span> {{ section.text.substring(section.text.indexOf(' ') + 1) }}
              </p>

              <div v-else class="flex flex-col gap-2 mb-8">
                <h3 class="font-semibold text-xl text-[#F8F3E4]">{{ section.title }}</h3>
                <p class="text-justify text-[18px]">{{ section.text }}</p>
              </div>
            </template>
          </div>
        </div>
      </div>

            <template v-else>

                <section class="route-hero">
          <div class="route-hero__inner">
            <svg class="route-hero__svg" :viewBox="`0 0 900 220`" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <path id="title-curve" d="M -500,340 A 2200,2200 0 0,1 1400,340" fill="none" />
                <filter id="title-shadow" x="-10%" y="-10%" width="130%" height="150%">
                  <feDropShadow dx="0" dy="4" stdDeviation="6" flood-color="rgba(0,0,0,0.25)" />
                </filter>
              </defs>
              <text class="route-hero__text-stroke" filter="url(#title-shadow)">
                <textPath href="#title-curve" startOffset="50%" text-anchor="middle">{{ routeName.toUpperCase() }}</textPath>
              </text>
              <text class="route-hero__text-fill">
                <textPath href="#title-curve" startOffset="50%" text-anchor="middle">{{ routeName.toUpperCase() }}</textPath>
              </text>
            </svg>
          </div>
        </section>

                <section class="route-section route-map-section">
          <div class="route-container">
            <div class="route-map-frame">
                            <template v-if="selectedRoute.map_image">
                <div class="route-map-img-wrapper">
                  <img
                    :src="selectedRoute.map_image"
                    :alt="`Peta jalur ${routeName}`"
                    class="route-map-img"
                    @error="e => e.target.closest('.route-map-img-wrapper').innerHTML = '<div class=\'route-map-empty\'><span class=\'route-map-empty__title\'>Peta tidak dapat dimuat</span></div>'"
                  />
                                    <button
                    @click="mapLightbox = true"
                    class="route-map-zoom-btn"
                    title="Perbesar peta"
                    aria-label="Perbesar peta jalur"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                      <path stroke-linecap="round" stroke-linejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607ZM10.5 7.5v6m3-3h-6" />
                    </svg>
                    Perbesar
                  </button>
                </div>

                                <Teleport to="body">
                  <Transition name="lightbox">
                    <div
                      v-if="mapLightbox"
                      class="route-lightbox"
                      @click.self="mapLightbox = false"
                    >
                      <div class="route-lightbox__inner">
                        <button
                          @click="mapLightbox = false"
                          class="route-lightbox__close"
                          aria-label="Tutup"
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
                          </svg>
                        </button>
                        <img
                          :src="selectedRoute.map_image"
                          :alt="`Peta jalur ${routeName}`"
                          class="route-lightbox__img"
                        />
                        <p class="route-lightbox__caption">Peta Jalur — {{ routeName }}</p>
                      </div>
                    </div>
                  </Transition>
                </Teleport>
              </template>

                            <div v-else class="route-map-empty">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-24 h-24 text-[#A2825B]/20" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 6.75V15m6-6v8.25m.503 3.498 4.875-2.437c.381-.19.622-.58.622-1.006V4.82c0-.836-.88-1.38-1.628-1.006l-3.869 1.934c-.317.159-.69.159-1.006 0L9.503 3.252a1.125 1.125 0 0 0-1.006 0L3.622 5.689C3.24 5.88 3 6.27 3 6.695V19.18c0 .836.88 1.38 1.628 1.006l3.869-1.934c.317-.159.69-.159 1.006 0l4.994 2.497c.317.158.69.158 1.006 0Z" />
                </svg>
                <span class="route-map-empty__title">Peta Jalur Pendakian</span>
                <span class="route-map-empty__text">Peta jalur <strong>{{ routeName }}</strong> sedang dalam proses. Segera tersedia.</span>
              </div>
            </div>
          </div>
        </section>

                <section class="route-section route-deskripsi-section">
          <div class="route-container">
            <div class="route-card">
              <h2 class="route-card__title mb-6">Informasi Basecamp</h2>

                            <div class="route-info-grid">
                <div class="route-info-item">
                  <span class="route-info-item__label">📍 Alamat Basecamp</span>
                  <span class="route-info-item__value">{{ selectedRoute.route_info?.basecamp_address ?? '-' }}</span>
                </div>
                <div class="route-info-item">
                  <span class="route-info-item__label">⛰️ Ketinggian Basecamp</span>
                  <span class="route-info-item__value">{{ selectedRoute.route_info?.basecamp_altitude ? selectedRoute.route_info.basecamp_altitude + ' mdpl' : '-' }}</span>
                </div>
                <div class="route-info-item">
                  <span class="route-info-item__label">🎫 Simaksi</span>
                  <span class="route-info-item__value">{{ formatRupiah(selectedRoute.route_info?.simaksi_price) }}</span>
                </div>
                <div class="route-info-item" v-if="selectedRoute.route_info?.ojek_description">
                  <span class="route-info-item__label">🛵 Ojek</span>
                  <span class="route-info-item__value">{{ selectedRoute.route_info.ojek_description }}</span>
                </div>
              </div>

                            <div v-if="selectedRoute.route_info?.logistics_description" class="mt-8">
                <h3 class="route-card__subtitle">Karakteristik Jalur</h3>
                <p class="route-card__text mt-3">{{ selectedRoute.route_info.logistics_description }}</p>
              </div>
            </div>
          </div>
        </section>

                <section class="route-section route-estimasi-section">
          <div class="route-container">
            <div class="route-card">
              <h2 class="route-card__title text-center mb-8">Estimasi Waktu per Pos</h2>

              <div v-if="selectedRoute.waypoints?.length" class="route-estimasi-grid">
                <div v-for="wp in selectedRoute.waypoints" :key="wp.order_index" class="route-pos-item">
                  <div class="route-pos-item__icon mt-1">
                                        <svg v-if="wp.has_water_source" xmlns="http://www.w3.org/2000/svg" class="w-6 h-6 text-[#4A90D9]" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15H9V8h2v9zm4 0h-2V8h2v9z"/>
                    </svg>
                                        <svg v-else xmlns="http://www.w3.org/2000/svg" class="w-6 h-6 text-[#374426]" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                    </svg>
                  </div>

                  <div class="route-pos-item__content">
                    <div class="flex items-center gap-2 mb-1 flex-wrap">
                      <h4 class="route-pos-item__title">{{ wp.name }}</h4>
                      <span v-if="wp.altitude" class="route-pos-item__badge">{{ wp.altitude }} mdpl</span>
                      <span v-if="wp.has_water_source" class="route-pos-item__water-badge">💧 Sumber Air</span>
                    </div>
                    <p class="route-pos-item__desc">{{ wp.description }}</p>
                    <div class="route-pos-item__time mt-2 flex items-center gap-1.5">
                      <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
                      </svg>
                      <span>{{ formatDuration(wp.estimated_time_minutes) }} dari pos sebelumnya</span>
                    </div>
                  </div>
                </div>
              </div>

              <div v-else class="text-center text-[#A2825B] py-8" style="font-family: 'Poppins', sans-serif;">
                Data waypoint untuk jalur ini belum tersedia.
              </div>

                            <div class="route-estimasi-legends mt-10 flex flex-col gap-1">
                <div class="flex items-center gap-2">
                  <svg class="w-5 h-5 text-[#4A90D9]" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15H9V8h2v9zm4 0h-2V8h2v9z"/></svg>
                  <span class="text-[#4A90D9] font-medium text-[18px]" style="font-family: 'Montserrat', sans-serif;">Sumber Air</span>
                </div>
                <div class="flex items-center gap-2">
                  <svg class="w-5 h-5 text-[#374426]" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/></svg>
                  <span class="text-[#374426] font-medium text-[18px]" style="font-family: 'Montserrat', sans-serif;">Pos / Campsite</span>
                </div>
              </div>
            </div>
          </div>
        </section>

                <section class="route-section route-stats-section">
          <div class="route-container">
            <div class="route-stats-wrapper">

                            <div class="route-stats-gallery">
                                <div class="relative w-full flex-1 rounded-[30px] overflow-hidden flex flex-col bg-[#F0ECD8] border-2 border-dashed border-[#D7DDC2]" style="min-height: 260px;">
                  <img
                    v-if="selectedRoute.image"
                    :src="selectedRoute.image"
                    :alt="`Foto jalur ${routeName}`"
                    class="w-full flex-1 object-cover"
                  />
                  <div v-else class="flex-1 flex items-center justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" class="w-16 h-16 text-[#A2825B]/30" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1">
                      <path stroke-linecap="round" stroke-linejoin="round" d="m2.25 15.75 5.159-5.159a2.25 2.25 0 0 1 3.182 0l5.159 5.159m-1.5-1.5 1.409-1.409a2.25 2.25 0 0 1 3.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0 0 22.5 18.75V5.25A2.25 2.25 0 0 0 20.25 3H3.75A2.25 2.25 0 0 0 1.5 5.25v13.5A2.25 2.25 0 0 0 3.75 21Z" />
                    </svg>
                  </div>
                  <div class="w-full bg-[#7E623F] px-6 py-3 flex justify-between items-center">
                    <span class="font-['Montserrat'] font-semibold text-[18px] text-[#F8F3E4]">{{ routeName }}</span>
                    <span class="font-['Montserrat'] font-medium text-[14px] text-[#F8F3E4]/80">Foto Jalur</span>
                  </div>
                </div>

                                <div class="relative w-full flex-1 rounded-[30px] overflow-hidden flex flex-col bg-[#F0ECD8] border-2 border-dashed border-[#D7DDC2]" style="min-height: 260px;">
                  <img
                    v-if="currentMountain.image"
                    :src="currentMountain.image"
                    :alt="`Foto ${currentMountain.name}`"
                    class="w-full flex-1 object-cover"
                  />
                  <div v-else class="flex-1 flex items-center justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" class="w-16 h-16 text-[#A2825B]/30" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1">
                      <path stroke-linecap="round" stroke-linejoin="round" d="m2.25 15.75 5.159-5.159a2.25 2.25 0 0 1 3.182 0l5.159 5.159m-1.5-1.5 1.409-1.409a2.25 2.25 0 0 1 3.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0 0 22.5 18.75V5.25A2.25 2.25 0 0 0 20.25 3H3.75A2.25 2.25 0 0 0 1.5 5.25v13.5A2.25 2.25 0 0 0 3.75 21Z" />
                    </svg>
                  </div>
                  <div class="w-full bg-[#7E623F] px-6 py-3 flex justify-between items-center">
                    <span class="font-['Montserrat'] font-semibold text-[18px] text-[#F8F3E4]">{{ currentMountain.name }}</span>
                    <span class="font-['Montserrat'] font-medium text-[14px] text-[#F8F3E4]/80">Foto Gunung</span>
                  </div>
                </div>
              </div>

                            <div class="route-stats-info route-card">
                                <div class="route-stats-block">
                  <h3 class="route-stats-block__title">Statistik Jalur</h3>
                  <ul class="route-stats-list">
                    <li v-if="selectedRoute.distance">
                      <strong>Jarak Tempuh:</strong> ± {{ selectedRoute.distance }} km
                    </li>
                    <li v-if="selectedRoute.estimated_time">
                      <strong>Estimasi Total:</strong> {{ formatDuration(selectedRoute.estimated_time) }}
                    </li>
                    <li v-if="selectedRoute.difficulty">
                      <strong>Tingkat Kesulitan:</strong> {{ difficultyLabel(selectedRoute.difficulty) }}
                    </li>
                    <li v-if="currentMountain.altitude">
                      <strong>Ketinggian Puncak:</strong> {{ currentMountain.altitude }} mdpl
                    </li>
                    <li v-if="selectedRoute.route_info?.basecamp_altitude">
                      <strong>Ketinggian Basecamp:</strong> {{ selectedRoute.route_info.basecamp_altitude }} mdpl
                    </li>
                    <li v-if="waterSourceWaypoints.length">
                      <strong>Sumber Air:</strong>
                      {{ waterSourceWaypoints.map(w => w.name).join(', ') }}
                    </li>
                    <li v-else>
                      <strong>Sumber Air:</strong> Tidak ada di jalur — bawa dari basecamp
                    </li>
                  </ul>
                </div>

                                <div class="route-stats-block">
                  <h3 class="route-stats-block__title">Tips Khusus</h3>
                  <div v-if="selectedRoute.route_info?.logistics_description" class="route-stats-tip">
                    <span class="route-stats-tip__label">🏔️ Karakteristik:</span>
                    <p class="route-stats-block__text">{{ selectedRoute.route_info.logistics_description }}</p>
                  </div>
                  <div v-if="selectedRoute.route_info?.ojek_description" class="route-stats-tip">
                    <span class="route-stats-tip__label">🛵 Transportasi Ojek:</span>
                    <p class="route-stats-block__text">{{ selectedRoute.route_info.ojek_description }}</p>
                  </div>
                  <div v-if="selectedRoute.route_info?.facilities_description" class="route-stats-tip">
                    <span class="route-stats-tip__label">🏠 Fasilitas Basecamp:</span>
                    <p class="route-stats-block__text">{{ selectedRoute.route_info.facilities_description }}</p>
                  </div>
                  <p v-if="!selectedRoute.route_info" class="route-stats-block__text text-[#A2825B]">
                    Informasi tips belum tersedia untuk jalur ini.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>

                <section class="route-section route-weather-section">
          <div class="route-container">

                        <div v-if="weatherLoading" class="route-weather-card flex items-center justify-center" style="min-height: 220px;">
              <div class="flex flex-col items-center gap-4 text-white/70">
                <svg class="w-10 h-10 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
                </svg>
                <span style="font-family: 'Poppins', sans-serif;">Memuat data cuaca...</span>
              </div>
            </div>

                        <div v-else-if="weatherError" class="w-full flex justify-center py-20">
              <div class="text-center text-[#E6E6E6]/70 px-8" style="font-family: 'Montserrat', sans-serif;">
                <p class="text-lg mb-2">⚠️ Gagal memuat data cuaca</p>
                <p class="text-sm">{{ weatherError }}</p>
              </div>
            </div>

            <div v-else-if="weather" class="w-full mt-8 mb-12 rounded-[40px] p-8 md:p-12 text-[#E6E6E6] relative overflow-hidden shadow-[0_20px_50px_rgba(20,30,80,0.5)]" style="background: linear-gradient(to top left, #4021CB 0%, #7176C9 25%, #122E80 100%); font-family: 'Montserrat', sans-serif;">
                            <h2 class="text-center font-bold text-2xl mb-8 tracking-wide">Cuaca</h2>

                            <div class="flex flex-col lg:flex-row justify-between items-center lg:items-stretch gap-8 mb-8">
                                <div class="flex flex-1 flex-col sm:flex-row items-center sm:items-center justify-start gap-4 sm:gap-12 w-full">
                                    <div class="flex flex-col justify-center">
                    <h3 class="text-[32px] md:text-[40px] font-bold leading-[1.1] text-center sm:text-left">Gunung<br/>{{ currentMountain.name.replace('Gunung ', '') }}</h3>
                    <div class="mt-1 text-center sm:text-left">
                      <span class="text-[56px] md:text-[72px] font-bold leading-none">{{ Math.round(weather.current?.temperature_2m ?? 0) }}°C</span>
                    </div>
                    <div class="text-[#E6E6E6]/80 text-lg font-medium -mt-2 text-center sm:text-left">Real feel {{ Math.round(weather.current?.apparent_temperature ?? 0) }}°C</div>
                  </div>

                                    <div class="flex justify-center items-center relative">
                    <img :src="getWmoIcon(weather.current?.weathercode)" alt="Current Weather" class="w-48 h-48 md:w-56 md:h-56 object-contain drop-shadow-[0_10px_20px_rgba(0,0,0,0.3)] z-10" />
                  </div>
                </div>

                                <div class="w-full lg:w-[400px] xl:w-[450px] bg-[#123767]/10 backdrop-blur-md rounded-2xl p-5 border border-white/20 shrink-0">
                  <h4 class="text-sm font-semibold mb-4 text-[#E6E6E6]/90">3 Days Forecast</h4>
                  <div class="flex flex-col gap-4">
                    <div v-for="(date, i) in weather.daily?.time?.slice(0, 3)" :key="date" class="flex items-center justify-between border-b border-white/10 pb-3 last:border-0 last:pb-0">
                      <span class="w-16 text-sm font-medium">{{ i === 0 ? 'Today' : HARI[new Date(date).getDay()].substring(0, 3) }}</span>
                      <img :src="getWmoIcon(weather.daily.weathercode[i])" class="w-8 h-8 object-contain drop-shadow-sm" />
                      <span class="flex-1 text-center text-sm font-medium">{{ getWmoLabel(weather.daily.weathercode[i]) }}</span>
                      <span class="w-20 text-right text-sm font-semibold">{{ Math.round(weather.daily.temperature_2m_max[i]) }}°C / {{ Math.round(weather.daily.temperature_2m_min[i]) }}°C</span>
                    </div>
                  </div>
                </div>
              </div>

                            <div class="bg-[#123767]/10 backdrop-blur-md rounded-2xl p-5 border border-white/20 mb-6">
                <div class="flex justify-between items-center mb-4 text-sm font-medium">
                  <span class="text-[#E6E6E6]/90">Hourly Forecast</span>
                  <div class="flex items-center gap-2 text-[#E6E6E6]/80">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
                    <span>{{ new Date().toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }) }} {{ new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true }) }}</span>
                  </div>
                </div>
                <div class="flex justify-between gap-4 overflow-x-auto pb-2 scrollbar-thin scrollbar-thumb-white/20 scrollbar-track-transparent">
                  <div v-for="hour in hourlyForecast" :key="hour.time" class="flex-1 min-w-[80px] flex flex-col items-center gap-3 border-r border-white/10 last:border-0">
                    <span class="text-xs font-medium text-[#E6E6E6]/80 whitespace-nowrap">{{ hour.time }}</span>
                    <img :src="getWmoIcon(hour.code)" class="w-10 h-10 object-contain drop-shadow-sm" />
                    <span class="text-sm font-bold">{{ hour.temp }}°C</span>
                  </div>
                </div>
              </div>

                            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div class="bg-[#123767]/10 backdrop-blur-md rounded-2xl p-4 border border-white/20 flex flex-col items-center justify-center gap-2">
                  <div class="flex items-center gap-2 text-[#E6E6E6]/80 text-sm font-medium">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
                    <span>Wind Speed</span>
                  </div>
                  <span class="text-lg font-semibold">{{ Math.round(weather.current?.windspeed_10m ?? 0) }} km/h</span>
                </div>

                <div class="bg-[#123767]/10 backdrop-blur-md rounded-2xl p-4 border border-white/20 flex flex-col items-center justify-center gap-2">
                  <div class="flex items-center gap-2 text-[#E6E6E6]/80 text-sm font-medium">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17a5 5 0 106 0v-9a3 3 0 00-6 0v9z" /></svg>
                    <span>Temperature</span>
                  </div>
                  <span class="text-lg font-semibold">{{ Math.round(weather.current?.temperature_2m ?? 0) }} °C</span>
                </div>

                <div class="bg-[#123767]/10 backdrop-blur-md rounded-2xl p-4 border border-white/20 flex flex-col items-center justify-center gap-2">
                  <div class="flex items-center gap-2 text-[#E6E6E6]/80 text-sm font-medium">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3c0 0-4 5-4 9a4 4 0 008 0c0-4-4-9-4-9z" /></svg>
                    <span>Humidity</span>
                  </div>
                  <span class="text-lg font-semibold">{{ weather.current?.relativehumidity_2m ?? 0 }} %</span>
                </div>

                <div class="bg-[#123767]/10 backdrop-blur-md rounded-2xl p-4 border border-white/20 flex flex-col items-center justify-center gap-2">
                  <div class="flex items-center gap-2 text-[#E6E6E6]/80 text-sm font-medium">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 15a4 4 0 004 4h9a5 5 0 10-.1-9.999 5.002 5.002 0 10-9.78 2.096A4.001 4.001 0 003 15z" /></svg>
                    <span>Clouds</span>
                  </div>
                  <span class="text-lg font-semibold">{{ weather.current?.cloudcover ?? 0 }} %</span>
                </div>
              </div>
            </div>

          </div>
        </section>

                <div class="w-full max-w-[1340px] mx-auto px-6 pb-16 flex justify-center mt-8">
          <button
            @click="selectedRoute = null"
            class="flex items-center gap-2 bg-[#374426] text-[#F8F3E4] font-medium text-[14px] px-5 py-2.5 rounded-lg hover:opacity-90 transition-all shadow-sm hover:scale-[1.02]"
            style="font-family: 'Montserrat', sans-serif;"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
            </svg>
            Kembali ke Informasi {{ currentMountain.name }}
          </button>
        </div>

      </template>

            <div v-if="!selectedRoute" class="w-full flex flex-col items-center mt-[40px] pb-24 px-4">
        <div class="w-full max-w-[1340px] flex flex-col">
          <h2 class="text-[#64823E] font-bold text-[48px] mb-[32px]" style="font-family: 'Montserrat', sans-serif;">Other Destinations</h2>

          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10 justify-between">
            <div
              v-for="dest in otherDestinations"
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
                  {{ dest.content?.[0]?.text }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

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
          <a href="/booking" class="inline-block bg-[#F8F3E4] text-[#374426] text-[24px] font-medium rounded-xl px-8 py-4 hover:opacity-90 transition-opacity shadow-sm" style="font-family: 'Montserrat', sans-serif;">
            Start Summit
          </a>
        </div>
      </div>

      <footer class="w-full bg-[#FFFFFF] px-8 md:px-16 xl:px-24 py-10 flex flex-col">
        <div class="flex flex-col lg:flex-row justify-between items-start gap-12 mb-8">
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

          <div class="flex flex-col sm:flex-row gap-12 md:gap-24 xl:gap-32">
            <div class="flex flex-col gap-5">
              <h5 class="text-[#374426] font-semibold text-[18px]">Jelajahi</h5>
              <div class="flex flex-col gap-4 text-[#5A684C] font-medium text-[15px]">
                <Link href="/mountains" class="hover:text-[#374426] transition-colors">Daftar Gunung</Link>
                <a href="/" class="hover:text-[#374426] transition-colors">Weather Analytics</a>
              </div>
            </div>

            <div class="flex flex-col gap-5">
              <h5 class="text-[#374426] font-semibold text-[18px]">Informasi</h5>
              <div class="flex flex-col gap-4 text-[#5A684C] font-medium text-[15px]">
                <a href="/article" class="hover:text-[#374426] transition-colors">Tata Tertib</a>
                <a href="/booking" class="hover:text-[#374426] transition-colors">Booking Simaksi</a>
                <a href="/article" class="hover:text-[#374426] transition-colors">Tips Keamanan</a>
              </div>
            </div>

            <div class="flex flex-col gap-5">
              <h5 class="text-[#374426] font-semibold text-[18px]">Komunitas</h5>
              <div class="flex flex-col gap-4 text-[#5A684C] font-medium text-[15px]">
                <a href="/" class="hover:text-[#374426] transition-colors">Forum Diskusi</a>
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
@import url('https://fonts.googleapis.com/css2?family=Jost:wght@700;800;900&display=swap');

h1 {
  font-family: 'Montserrat', sans-serif;
}

.route-container {
  width: 100%;
  max-width: 1340px;
  margin: 0 auto;
  padding: 0 24px;
}

.route-section {
  width: 100%;
  padding: 40px 0;
}

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
  max-width: 1340px;
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

.route-hero__text-fill {
  font-family: 'Jost', sans-serif;
  font-weight: 700;
  font-size: 75px;
  letter-spacing: -0.02em;
  fill: #FFFEF0;
}

.route-map-section {
  background: #F8F3E4;
  padding-top: 0;
}

.route-map-frame {
  width: 100%;
  border: 2px solid #C8C4A9;
  border-radius: 16px;
  background: #FFFEF5;
  overflow: hidden;
  position: relative;
}

.route-map-img-wrapper {
  position: relative;
  width: 100%;
  background: #F5F0DC;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
}

.route-map-img {
  max-height: 520px;
  width: auto;
  max-width: 100%;
  object-fit: contain;
  border-radius: 8px;
  display: block;
  box-shadow: 0 4px 24px rgba(0,0,0,0.12);
}

.route-map-zoom-btn {
  position: absolute;
  bottom: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(55, 68, 38, 0.85);
  backdrop-filter: blur(8px);
  color: #F8F3E4;
  border: none;
  border-radius: 10px;
  padding: 8px 14px;
  font-family: 'Montserrat', sans-serif;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;
  letter-spacing: 0.3px;
}

.route-map-zoom-btn:hover {
  background: rgba(55, 68, 38, 1);
  transform: scale(1.04);
}

.route-lightbox {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.80);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.route-lightbox__inner {
  position: relative;
  max-width: min(1100px, 95vw);
  max-height: 92vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.route-lightbox__img {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
  border-radius: 12px;
  box-shadow: 0 8px 60px rgba(0,0,0,0.5);
}

.route-lightbox__caption {
  font-family: 'Montserrat', sans-serif;
  font-size: 14px;
  color: rgba(255,255,255,0.7);
  letter-spacing: 0.5px;
}

.route-lightbox__close {
  position: absolute;
  top: -44px;
  right: 0;
  background: rgba(255,255,255,0.15);
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s;
}

.route-lightbox__close:hover {
  background: rgba(255,255,255,0.3);
}

.lightbox-enter-active,
.lightbox-leave-active {
  transition: opacity 0.25s ease;
}
.lightbox-enter-from,
.lightbox-leave-to {
  opacity: 0;
}

.route-map-empty {
  width: 100%;
  min-height: 500px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #F5F0DC;
  gap: 12px;
  padding: 40px;
  text-align: center;
}

.route-map-empty__title {
  font-family: 'Montserrat', sans-serif;
  font-size: 22px;
  font-weight: 700;
  color: #A2825B;
  opacity: 0.7;
}

.route-map-empty__text {
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  color: #A2825B;
  opacity: 0.55;
  max-width: 440px;
  line-height: 1.6;
}

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

.route-card__subtitle {
  font-family: 'Montserrat', sans-serif;
  font-weight: 700;
  font-size: 22px;
  color: #4B632B;
}

.route-card__text {
  font-family: 'Montserrat', sans-serif;
  font-weight: 400;
  font-size: 18px;
  color: #374426;
  line-height: 1.6;
  margin: 0;
  text-align: justify;
}

.route-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.route-info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  background: rgba(255,255,255,0.4);
  border-radius: 16px;
  padding: 14px 18px;
}

.route-info-item__label {
  font-family: 'Montserrat', sans-serif;
  font-weight: 600;
  font-size: 13px;
  color: #5A6840;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.route-info-item__value {
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  color: #374426;
  line-height: 1.5;
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
  font-size: 18px;
  color: #374426;
  margin: 0;
  line-height: 1.4;
}

.route-pos-item__badge {
  font-family: 'Poppins', sans-serif;
  font-size: 12px;
  background: rgba(55, 68, 38, 0.15);
  color: #374426;
  padding: 2px 10px;
  border-radius: 20px;
  font-weight: 600;
}

.route-pos-item__water-badge {
  font-family: 'Poppins', sans-serif;
  font-size: 12px;
  background: rgba(74, 144, 217, 0.15);
  color: #2563EB;
  padding: 2px 10px;
  border-radius: 20px;
  font-weight: 600;
}

.route-pos-item__desc {
  font-family: 'Montserrat', sans-serif;
  font-weight: 400;
  font-size: 16px;
  color: #374426;
  line-height: 1.5;
  margin: 0;
  text-align: justify;
}

.route-pos-item__time {
  font-family: 'Montserrat', sans-serif;
  font-weight: 400;
  font-size: 14px;
  color: #7E623F;
}

.route-estimasi-legends {
  border-top: 1px solid rgba(55, 68, 38, 0.2);
  padding-top: 16px;
}

.route-stats-section {
  background: #F8F3E4;
}

.route-stats-wrapper {
  display: flex;
  gap: 32px;
  align-items: stretch;
}

.route-stats-gallery {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.route-stats-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.route-stats-block {
  display: flex;
  flex-direction: column;
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
  padding: 6px 10px;
  background: rgba(255,255,255,0.35);
  border-radius: 8px;
}

.route-stats-tip {
  margin-bottom: 12px;
}

.route-stats-tip__label {
  display: block;
  font-family: 'Montserrat', sans-serif;
  font-weight: 600;
  font-size: 13px;
  color: #5A6840;
  margin-bottom: 4px;
}

.route-stats-block__text {
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  line-height: 1.7;
  color: #4A4A3A;
  margin: 0;
  text-align: justify;
}

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
  line-height: 1;
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
</style>
