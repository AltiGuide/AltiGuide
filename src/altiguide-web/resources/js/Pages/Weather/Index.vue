<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { Head, Link } from '@inertiajs/vue3'

const props = defineProps({
  mountains: {
    type: Array,
    required: true,
  }
})

const selectedMountainSlug = ref('')
const selectedRouteId = ref('')

const currentMountain = computed(() => {
  return props.mountains.find(m => m.slug === selectedMountainSlug.value)
})

const availableRoutes = computed(() => {
  return currentMountain.value ? currentMountain.value.routes : []
})

const currentRoute = computed(() => {
  if (!availableRoutes.value.length) return null
  return availableRoutes.value.find(r => r.id === selectedRouteId.value)
})

watch(selectedMountainSlug, (newVal) => {
  if (availableRoutes.value.length > 0) {
    selectedRouteId.value = availableRoutes.value[0].id
  } else {
    selectedRouteId.value = ''
  }
})

watch(currentRoute, (route) => {
  if (route && route.latitude && route.longitude) {
    fetchWeather(route.latitude, route.longitude)
  } else {
    weather.value = null
  }
})

onMounted(() => {
  if (props.mountains.length > 0) {
    selectedMountainSlug.value = props.mountains[0].slug
  }
})

// ── Weather ──────────────────────────────────────────────────────────────────
const weather = ref(null)
const weatherLoading = ref(false)
const weatherError = ref(null)

const WMO_CODES = {
  0:  { label: 'Cerah', icon: '☀️' },
  1:  { label: 'Hampir Cerah', icon: '🌤️' },
  2:  { label: 'Berawan Sebagian', icon: '⛅' },
  3:  { label: 'Berawan Penuh', icon: '☁️' },
  45: { label: 'Berkabut', icon: '🌫️' },
  48: { label: 'Kabut Beku', icon: '🌫️' },
  51: { label: 'Gerimis Ringan', icon: '🌦️' },
  53: { label: 'Gerimis', icon: '🌦️' },
  55: { label: 'Gerimis Lebat', icon: '🌧️' },
  61: { label: 'Hujan Ringan', icon: '🌧️' },
  63: { label: 'Hujan Sedang', icon: '🌧️' },
  65: { label: 'Hujan Lebat', icon: '🌧️' },
  71: { label: 'Salju Ringan', icon: '❄️' },
  80: { label: 'Hujan Lokal', icon: '🌦️' },
  95: { label: 'Badai Petir', icon: '⛈️' },
}

function getWmoLabel(code) {
  return WMO_CODES[code]?.label ?? 'Tidak Diketahui'
}
function getWmoIcon(code) {
  let label = WMO_CODES[code]?.label || 'unknown';
  let filename = label.toLowerCase().replace(/ /g, '-') + '.png';
  if (label === 'Tidak Diketahui') filename = 'unknown.png';
  return '/images/weather-icon/' + filename;
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
      'weathercode',
      'windspeed_10m',
      'relativehumidity_2m',
      'precipitation',
      'visibility',
      'surface_pressure',
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
</script>

<template>
  <div class="min-h-screen bg-[#F8F3E4] font-sans flex flex-col">
    <Head title="Weather Analytics - AltiGuide" />

    <!-- Navbar -->
    <nav class="w-full flex justify-between items-center px-4 md:px-8 xl:px-12 py-4 border-b border-[#D7DDC2]/50 shadow-sm bg-[#374426]/20">
      <div class="flex items-center gap-2 xl:gap-3">
        <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 md:w-10 md:h-10 object-contain" />
        <span class="text-xl md:text-2xl xl:text-3xl font-bold text-[#374426] tracking-tight">AltiGuide</span>
      </div>

      <div class="flex items-center gap-4 md:gap-6 xl:gap-9 text-sm md:text-base font-semibold text-[#374426]">
        <a href="/" class="hover:text-black transition">Home</a>
        <a href="/article" class="hover:text-black transition">Article</a>
        <a href="/booking" class="hover:text-black transition">Booking</a>
        <a href="/login" class="border border-[#374426] px-4 md:px-6 py-2 rounded-lg hover:bg-[#374426] hover:text-white transition duration-200 whitespace-nowrap">
          Login
        </a>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="flex-1 w-full max-w-[1340px] mx-auto px-6 py-12">
      <div class="mb-12">
        <h1 class="text-4xl md:text-5xl font-extrabold text-[#374426] mb-4 tracking-tight" style="font-family: 'Montserrat', sans-serif;">Weather Analytics</h1>
        <p class="text-lg text-[#64823E] max-w-2xl leading-relaxed font-medium">
          Pantau kondisi cuaca secara real-time di berbagai gunung untuk mempersiapkan pendakian Anda dengan lebih baik.
        </p>
      </div>

      <!-- Selector Section -->
      <div class="bg-white/60 backdrop-blur-md rounded-2xl p-6 shadow-sm border border-[#D7DDC2]/50 mb-8 flex flex-col md:flex-row gap-6">
        <div class="flex-1 flex flex-col gap-2">
          <label class="text-[#374426] font-semibold text-sm">Pilih Gunung</label>
          <select v-model="selectedMountainSlug" class="w-full bg-[#F8F3E4] border border-[#D7DDC2] rounded-xl px-4 py-3 text-[#374426] font-medium outline-none focus:border-[#64823E] transition">
            <option v-for="m in mountains" :key="m.slug" :value="m.slug">{{ m.name }}</option>
          </select>
        </div>
        <div class="flex-1 flex flex-col gap-2" v-if="availableRoutes.length > 0">
          <label class="text-[#374426] font-semibold text-sm">Pilih Jalur</label>
          <select v-model="selectedRouteId" class="w-full bg-[#F8F3E4] border border-[#D7DDC2] rounded-xl px-4 py-3 text-[#374426] font-medium outline-none focus:border-[#64823E] transition">
            <option v-for="r in availableRoutes" :key="r.id" :value="r.id">{{ r.name }}</option>
          </select>
        </div>
      </div>

      <!-- Weather Section -->
      <div class="route-weather-section rounded-3xl overflow-hidden bg-transparent p-0">
        <div v-if="weatherLoading" class="route-weather-card flex items-center justify-center" style="min-height: 180px;">
          <div class="text-center text-white/80" style="font-family: 'Poppins', sans-serif;">
            <div class="inline-block animate-spin w-8 h-8 border-4 border-white border-t-transparent rounded-full mb-3"></div>
            <p class="text-lg animate-pulse">Memuat cuaca...</p>
          </div>
        </div>

        <div v-else-if="weatherError" class="route-weather-card flex items-center justify-center" style="min-height: 180px;">
          <div class="text-center text-white/70 px-8" style="font-family: 'Poppins', sans-serif;">
            <p class="text-lg mb-2">⚠️ Gagal memuat data cuaca</p>
            <p class="text-sm">{{ weatherError }}</p>
          </div>
        </div>

        <div v-else-if="weather" class="route-weather-card">
          <div class="route-weather-card__header">
            <span class="route-weather-card__badge">Cuaca Real-Time</span>
          </div>

          <div class="route-weather-card__body">
            <!-- Current -->
            <div class="route-weather-current">
              <div class="route-weather-current__mountain">
                <h3>Gunung</h3>
                <h2>{{ currentMountain?.name.replace('Gunung ', '') }}</h2>
              </div>
              <div class="route-weather-current__icon">
                <img :src="getWmoIcon(weather.current?.weathercode)" alt="Weather Icon" class="w-24 h-24 object-contain drop-shadow-md" />
              </div>
              <div class="route-weather-current__temp">
                <span class="route-weather-current__degrees">{{ Math.round(weather.current?.temperature_2m ?? 0) }}</span>
                <span class="route-weather-current__unit">°C</span>
              </div>
              <div class="route-weather-current__condition">{{ getWmoLabel(weather.current?.weathercode) }}</div>
            </div>

            <!-- Forecast -->
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
                  <tr v-for="(date, i) in weather.daily?.time?.slice(1, 6)" :key="date">
                    <td>{{ HARI[new Date(date).getDay()] }}</td>
                    <td>{{ Math.round(weather.daily.temperature_2m_min[i + 1]) }}–{{ Math.round(weather.daily.temperature_2m_max[i + 1]) }}°C</td>
                    <td>
                      <div class="flex items-center gap-2">
                        <img :src="getWmoIcon(weather.daily.weathercode[i + 1])" class="w-8 h-8 object-contain drop-shadow-sm" />
                        <span>{{ getWmoLabel(weather.daily.weathercode[i + 1]) }}</span>
                      </div>
                    </td>
                    <td>{{ Math.round(weather.daily.windspeed_10m_max[i + 1]) }} km/j</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Footer info -->
          <div class="route-weather-card__footer">
            <div class="route-weather-info-item">
              <span class="route-weather-info-item__label">🌡 Kelembaban</span>
              <span class="route-weather-info-item__value">{{ weather.current?.relativehumidity_2m ?? '-' }}%</span>
            </div>
            <div class="route-weather-info-item">
              <span class="route-weather-info-item__label">🌧 Curah Hujan</span>
              <span class="route-weather-info-item__value">{{ weather.current?.precipitation ?? '0' }} mm</span>
            </div>
            <div class="route-weather-info-item">
              <span class="route-weather-info-item__label">💨 Angin</span>
              <span class="route-weather-info-item__value">{{ Math.round(weather.current?.windspeed_10m ?? 0) }} km/j</span>
            </div>
            <div class="route-weather-info-item">
              <span class="route-weather-info-item__label">💧 Tekanan</span>
              <span class="route-weather-info-item__value">{{ Math.round(weather.current?.surface_pressure ?? 0) }} hPa</span>
            </div>
            <div class="route-weather-info-item">
              <span class="route-weather-info-item__label">🌅 Sunrise</span>
              <span class="route-weather-info-item__value">{{ formatHour(weather.daily?.sunrise?.[0]) }}</span>
            </div>
            <div class="route-weather-info-item">
              <span class="route-weather-info-item__label">🌇 Sunset</span>
              <span class="route-weather-info-item__value">{{ formatHour(weather.daily?.sunset?.[0]) }}</span>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <footer class="w-full bg-[#FFFFFF] px-8 md:px-16 xl:px-24 py-10 flex flex-col mt-auto">
      <div class="flex flex-col lg:flex-row justify-between items-start gap-12 mb-8">
        <div class="flex flex-col gap-[96px]">
          <a href="/" class="text-[24px] font-medium text-[#374426] underline underline-offset-8">
            AltiGuide.com
          </a>
          <p class="text-[14px] text-[#64823E] max-w-md">
            AltiGuide is a platform dedicated to providing climbers with detailed, reliable, and up-to-date information on mountain routes.
          </p>
        </div>

        <div class="flex flex-wrap gap-12 md:gap-24">
          <div class="flex flex-col gap-4">
            <h4 class="text-[16px] font-semibold text-[#374426]">Company</h4>
            <div class="flex flex-col gap-3 text-[14px]">
              <a href="/about" class="hover:text-[#374426] transition-colors">About Us</a>
              <a href="#" class="hover:text-[#374426] transition-colors">Career</a>
              <a href="#" class="hover:text-[#374426] transition-colors">Contact</a>
            </div>
          </div>
          <div class="flex flex-col gap-4">
            <h4 class="text-[16px] font-semibold text-[#374426]">Explore</h4>
            <div class="flex flex-col gap-3 text-[14px]">
              <a href="/article" class="hover:text-[#374426] transition-colors">Article</a>
              <a href="/weather" class="hover:text-[#374426] transition-colors">Weather Analytics</a>
              <a href="/booking" class="hover:text-[#374426] transition-colors">Booking Simaksi</a>
            </div>
          </div>
        </div>
      </div>
      
      <div class="w-full h-px bg-[#D6CCAF] mb-8"></div>
      
      <div class="flex flex-col md:flex-row justify-between items-center gap-4 text-[#64823E] text-[14px]">
        <p>© 2024 AltiGuide. All rights reserved.</p>
        <div class="flex gap-6">
          <a href="#" class="hover:text-[#374426] transition-colors">Terms of Service</a>
          <a href="#" class="hover:text-[#374426] transition-colors">Privacy Policy</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
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
  background: rgba(255, 255, 255, 0.1);
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
  font-weight: 500;
  font-size: 16px;
  color: #E6E6E6;
  opacity: 0.8;
}

.route-weather-current__mountain h2 {
  font-weight: 800;
  font-size: 32px;
  color: #E6E6E6;
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
  font-weight: 800;
  font-size: 56px;
  line-height: 1;
}

.route-weather-current__unit {
  font-weight: 600;
  font-size: 20px;
  margin-top: 8px;
}

.route-weather-current__condition {
  font-size: 14px;
  color: #E6E6E6;
  opacity: 0.8;
}

.route-weather-forecast {
  flex: 1;
}

.route-weather-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  color: #E6E6E6;
}

.route-weather-table thead th {
  text-align: left;
  font-weight: 600;
  padding: 8px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  color: #E6E6E6;
  opacity: 0.8;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.route-weather-table tbody td {
  padding: 8px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: #E6E6E6;
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
  font-size: 12px;
  color: #E6E6E6;
  opacity: 0.8;
}

.route-weather-info-item__value {
  font-weight: 700;
  font-size: 16px;
  color: #E6E6E6;
}

@media (max-width: 768px) {
  .route-weather-card__body {
    flex-direction: column;
  }
}
</style>
