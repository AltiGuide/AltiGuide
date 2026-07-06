# Changelog
## [1.0.0] - 2026-06-29

### Added
- Fitur e-Ticketing dan integrasi otomatis dengan Midtrans Payment Gateway.
- Portal Web Admin menggunakan Laravel 13, Vue 3, dan Inertia.js untuk manajemen data gunung dan verifikasi.
- Aplikasi Mobile Android menggunakan Kotlin, Jetpack Compose, dan arsitektur MVVM.
- Modul navigasi presisi (GPS Tracking) luring dengan integrasi berkas koordinat `.gpx` dan OSM Droid.
- Penyimpanan lokal menggunakan Room DB untuk mekanisme *caching* data luring di perangkat mobile.
- Sinkronisasi data web dan mobile secara real-time via Firebase Realtime Database.
- Autentikasi pengguna menggunakan Google OAuth.

### Fixed
- Perbaikan masalah *silent error* pada validasi input judul seksi konten admin yang melebihi batas 255 karakter.
- Perbaikan resolusi dependensi komponen framework akibat konflik versi minor Symfony pada lingkungan lokal.