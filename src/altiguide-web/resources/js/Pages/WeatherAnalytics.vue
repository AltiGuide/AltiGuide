<script setup>
import { ref, computed, watch } from 'vue'
import { Link, Head, usePage } from '@inertiajs/vue3'

const props = defineProps({
  mountains: Array
})

const page = usePage()
const authUser = computed(() => page.props.auth?.user)

const selectedMountainId = ref(null)
const isDropdownOpen = ref(false)
const weather = ref(null)
const weatherLoading = ref(false)
const weatherError = ref(null)

const HARI = ['Minggu', 'Senin', 'Selasa', 'Rabu', 'Kamis', 'Jumat', 'Sabtu']

const wmoLabels = {
  0: 'Cerah',
  1: 'Cerah Berawan', 2: 'Cerah Berawan',
  3: 'Berawan',
  45: 'Kabut', 48: 'Kabut',
  51: 'Gerimis Ringan', 53: 'Gerimis', 55: 'Gerimis Lebat',
  56: 'Gerimis Beku Ringan', 57: 'Gerimis Beku Lebat',
  61: 'Hujan Ringan', 63: 'Hujan', 65: 'Hujan Lebat',
  66: 'Hujan Beku Ringan', 67: 'Hujan Beku Lebat',
  71: 'Salju Ringan', 73: 'Salju', 75: 'Salju Lebat',
  77: 'Hujan Es',
  80: 'Hujan Ringan', 81: 'Hujan', 82: 'Hujan Lebat',
  85: 'Hujan Salju Ringan', 86: 'Hujan Salju Lebat',
  95: 'Badai Petir',
  96: 'Badai Petir Ringan', 99: 'Badai Petir Lebat'
}

const getWmoLabel = (code) => wmoLabels[code] ?? 'Tidak Diketahui'

const getWmoIcon = (code) => {
  if (code === undefined || code === null) return '/images/weather-icon/sun.png'
  if (code === 0) return '/images/weather-icon/sun.png'
  if ([1, 2].includes(code)) return '/images/weather-icon/cloudysunny.png'
  if ([3, 45, 48].includes(code)) return '/images/weather-icon/clouds.png'
  if (code >= 51 && code <= 67) return '/images/weather-icon/sun-clouds-rain.png'
  if (code >= 71 && code <= 77) return '/images/weather-icon/clouds-snow.png'
  if (code >= 80 && code <= 82) return '/images/weather-icon/sun-clouds-rain.png'
  if (code >= 85 && code <= 86) return '/images/weather-icon/clouds-snow.png'
  if ([95, 96, 99].includes(code)) return '/images/weather-icon/lightning.png'
  return '/images/weather-icon/sun.png'
}

const fetchWeather = async (lat, lon) => {
  weatherLoading.value = true
  weatherError.value = null
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

watch(selectedMountainId, (newId) => {
  if (newId) {
    const mountain = props.mountains.find(m => m.id === newId)
    if (mountain && mountain.latitude && mountain.longitude) {
      fetchWeather(mountain.latitude, mountain.longitude)
    }
  } else {
    weather.value = null
  }
})

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

const selectedMountainObj = computed(() => props.mountains.find(m => m.id === selectedMountainId.value))
</script>

<template>
  <div class="flex-1 w-full bg-[#F8F3E4] font-sans flex flex-col transition-all duration-300 ease-[cubic-bezier(0.4,0,0.2,1)] opacity-0 translate-y-4 animate-fadeSlideUp">
    <Head title="Weather Analytics - AltiGuide" />

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
      <div class="w-full max-w-[1100px] flex flex-col gap-10">
        
        <div class="text-center">
          <h1 class="text-[#64823E] font-bold text-[56px] drop-shadow-md mb-6" style="font-family: 'Montserrat', sans-serif;">Weather Analytics</h1>
          <p class="text-[#66533A] text-[18px] leading-relaxed max-w-3xl mx-auto" style="font-family: 'Montserrat', sans-serif;">
            Pantau prakiraan cuaca terkini untuk memastikan keamanan dan kenyamanan perjalanan pendakian Anda di berbagai gunung.
          </p>
        </div>

        <div class="w-full max-w-md mx-auto">
          <label for="mountain" class="block text-[#66533A] font-semibold mb-2">Pilih Destinasi Gunung</label>
          <div class="relative">
            <div 
              @click="isDropdownOpen = !isDropdownOpen"
              class="w-full bg-white border border-[#D7DDC2] text-[#66533A] py-3 px-4 rounded-xl flex justify-between items-center cursor-pointer hover:border-[#64823E] transition shadow-sm font-medium relative z-50"
            >
              <span>{{ selectedMountainObj ? selectedMountainObj.name : 'Pilih Gunung' }}</span>
              <svg 
                class="fill-current h-4 w-4 transition-transform duration-200" 
                :class="isDropdownOpen ? 'rotate-180' : ''" 
                xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"
              >
                <path d="M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z"/>
              </svg>
            </div>
            
            <div v-if="isDropdownOpen" class="absolute z-50 w-full mt-2 bg-white border border-[#D7DDC2] rounded-xl shadow-lg max-h-60 overflow-auto scrollbar-thin scrollbar-thumb-[#D7DDC2]">
              <div 
                @click="selectedMountainId = null; isDropdownOpen = false"
                class="px-4 py-3 cursor-pointer text-[#66533A] hover:bg-[#F8F3E4] transition font-medium"
                :class="{ 'bg-[#F8F3E4] font-semibold': !selectedMountainId }"
              >
                Pilih Gunung
              </div>
              <div 
                v-for="mountain in mountains" 
                :key="mountain.id"
                @click="selectedMountainId = mountain.id; isDropdownOpen = false"
                class="px-4 py-3 cursor-pointer text-[#66533A] hover:bg-[#F8F3E4] hover:text-[#64823E] transition font-medium border-t border-gray-50"
                :class="{ 'bg-[#F8F3E4] text-[#64823E] font-semibold': selectedMountainId === mountain.id }"
              >
                {{ mountain.name }}
              </div>
            </div>
            
            <!-- Overlay to catch clicks outside -->
            <div v-if="isDropdownOpen" @click="isDropdownOpen = false" class="fixed inset-0 z-40"></div>
          </div>
        </div>

        <div v-if="weatherLoading" class="flex justify-center py-12">
          <svg class="animate-spin h-10 w-10 text-[#64823E]" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
        </div>
        
        <div v-else-if="weatherError" class="text-center text-red-500 font-medium py-12">
          {{ weatherError }}
        </div>

        <div v-else-if="weather" class="w-full mt-8 mb-12 rounded-[40px] p-8 md:p-12 text-[#E6E6E6] relative overflow-hidden shadow-[0_20px_50px_rgba(20,30,80,0.5)]" style="background: linear-gradient(to top left, #4021CB 0%, #7176C9 25%, #122E80 100%); font-family: 'Montserrat', sans-serif;">
          
          <div class="relative z-10">
            <h2 class="text-center font-bold text-2xl mb-8 tracking-wide">Cuaca</h2>
            
            <div class="flex flex-col lg:flex-row justify-between items-center lg:items-stretch gap-8 mb-8">
              <div class="flex flex-1 flex-col sm:flex-row items-center sm:items-center justify-start gap-4 sm:gap-12 w-full">
                <div class="flex flex-col justify-center">
                  <h3 class="text-[32px] md:text-[40px] font-bold leading-[1.1] text-center sm:text-left">Gunung<br/>{{ selectedMountainObj?.name.replace('Gunung ', '') }}</h3>
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
