# Software Requirements Specification (SRS) - ALTIGUIDE

### [FITUR 1: AUTENTIKASI & MANAJEMEN AKUN]

#### FR-01
Sistem harus menyediakan formulir registrasi bagi Pendaki baru untuk mengisi data diri lengkap dan melakukan validasi keunikan data Email serta NIK 16 digit.  
- **Prioritas:** High  
- **Ref:** US-01  
- **Platform:** Web & Mobile

#### FR-02
Sistem harus memvalidasi kombinasi email dan password pengguna, memberikan *Bearer Token* yang disimpan di *DataStore* lokal pada Mobile, dan mengelola *session cookie* pada Web jika login berhasil.  
- **Prioritas:** High  
- **Ref:** US-02  
- **Platform:** Web & Mobile

#### FR-03
Sistem harus menyediakan alur *reset password* bagi pengguna melalui verifikasi kode OTP 6 digit yang dikirimkan ke email terdaftar dengan batas masa berlaku selama 15 menit.  
- **Prioritas:** High  
- **Ref:** US-03  
- **Platform:** Web & Mobile

#### FR-04
Sistem harus mampu menghentikan sesi aktif (logout) dengan menghapus (*revoke*) token di server, membersihkan penyimpanan *DataStore* lokal pada Mobile, serta menghancurkan data *session* pada platform Web.  
- **Prioritas:** High  
- **Ref:** US-04  
- **Platform:** Web & Mobile

#### FR-05
Sistem harus menampilkan data profil pengguna dan mengizinkan pembaruan informasi operasional, kecuali field Email dan NIK yang dikunci secara permanen (*read-only*).  
- **Prioritas:** Medium  
- **Ref:** US-05  
- **Platform:** Web & Mobile

#### FR-06
Sistem harus menyediakan fasilitas bagi pengguna yang telah terautentikasi untuk mengubah kata sandi akunnya dari dalam aplikasi setelah melalui proses validasi kata sandi lama.  
- **Prioritas:** Low  
- **Ref:** US-06  
- **Platform:** Web & Mobile

---

### [FITUR 2: EKSPLORASI GUNUNG & RUTE]

#### FR-07
Sistem harus menampilkan daftar seluruh gunung yang tersedia di platform (dapat diakses secara publik tanpa login) beserta informasi dasar, status operasional, provinsi, dan visualisasi gambar.  
- **Prioritas:** High  
- **Ref:** US-07  
- **Platform:** Web & Mobile

#### FR-08
Sistem harus menyajikan halaman detail sebuah gunung beserta semua pilihan jalur pendakian teknis dan informasi operasional logistik dari basecamp terkait.  
- **Prioritas:** High  
- **Ref:** US-08  
- **Platform:** Web & Mobile

#### FR-09
Sistem harus menampilkan daftar pos atau *checkpoint* pendakian secara terurut berdasarkan indikator indeks (`order_index`) beserta metadata elevasi, deskripsi, dan indikator ketersediaan sumber air.  
- **Prioritas:** Medium  
- **Ref:** US-09  
- **Platform:** Web & Mobile

#### FR-10
Sistem harus menampilkan informasi prakiraan cuaca real-time (suhu, kecepatan angin, kondisi langit) di titik koordinat basecamp dan puncak gunung melalui jembatan API internal backend Laravel yang terintegrasi dengan Open-Meteo.  
- **Prioritas:** Medium  
- **Ref:** US-10  
- **Platform:** Web & Mobile

---

### [FITUR 3: PEMESANAN SIMAKSI - WEB ONLY]

#### FR-11
Sistem harus memproses pembuatan reservasi kuota SIMAKSI daring dengan memvalidasi sisa kuota harian rute, tanggal mulai (tidak boleh masa lalu), dan menghitung otomatis tanggal selesai berdasarkan jenis pendakian.  
- **Prioritas:** High  
- **Ref:** US-11  
- **Platform:** Web Only

#### FR-12
Sistem harus memverifikasi data NIK seluruh anggota kelompok pendaki (maksimal 10 orang) ke database untuk mencegah adanya konflik atau tumpang tindih jadwal pendakian aktif pada rentang tanggal yang sama.  
- **Prioritas:** High  
- **Ref:** US-12  
- **Platform:** Web Only

#### FR-13
Sistem harus mengintegrasikan transaksi ke gerbang pembayaran Midtrans Snap QRIS dengan limit kedaluwarsa invoice selama 1 jam dan memperbarui status pembayaran otomatis menjadi "Lunas" via *webhook*.  
- **Prioritas:** High  
- **Ref:** US-13  
- **Platform:** Web Only

#### FR-14
Sistem harus menghasilkan dokumen resmi E-Ticket berformat PDF secara otomatis yang dilengkapi data manifest kelompok dan *QR Code Order ID* ketika status transaksi telah terkonfirmasi lunas (*settlement*).  
- **Prioritas:** Medium  
- **Ref:** US-14  
- **Platform:** Web Only

---

### [FITUR 4: RIWAYAT & E-TICKET]

#### FR-15
Sistem harus menampilkan daftar kronologis transaksi pengguna yang tersinkronisasi secara langsung (*real-time*) via API antara platform reservasi Web dengan aplikasi Mobile.  
- **Prioritas:** High  
- **Ref:** US-15  
- **Platform:** Web & Mobile

#### FR-16
Sistem harus menyajikan halaman detail transaksi yang menampilkan status pembayaran, data rincian biaya, struktur kelompok, serta membatasi hak akses halaman hanya untuk pemilik transaksi tersebut.  
- **Prioritas:** Medium  
- **Ref:** US-16  
- **Platform:** Web & Mobile

#### FR-17
Sistem pada platform Mobile harus mampu mengunduh file biner E-Ticket PDF ke penyimpanan lokal perangkat untuk transaksi berstatus lunas dan langsung membukanya menggunakan aplikasi *PDF Viewer* internal ponsel.  
- **Prioritas:** Medium  
- **Ref:** US-17  
- **Platform:** Mobile Only

---

### [FITUR 5: NAVIGASI OFFLINE - MOBILE ONLY]

#### FR-18
Sistem harus menyajikan pelacakan jadwal agenda sesi pendakian pengguna mendatang dan memisahkan status pendaftaran berdasarkan fase operasional lapangan (*Siap*, *Dalam Pendakian*, *Selesai*).  
- **Prioritas:** High  
- **Ref:** US-18  
- **Platform:** Mobile Only

#### FR-19
Sistem harus secara otomatis melakukan *caching* (penyimpanan lokal) seluruh struktur data rute, info basecamp, dan koordinat *waypoints* ke dalam database **Room DB** lokal ponsel sesaat setelah user membuka detail rute secara daring.  
- **Prioritas:** High  
- **Ref:** US-19  
- **Platform:** Mobile Only

#### FR-20
Sistem harus mampu menampilkan komponen daftar pos, jalur koordinat, dan informasi logistik navigasi secara utuh dari Room DB lokal meskipun perangkat berada di area tanpa sinyal internet (*airplane mode*).  
- **Prioritas:** Medium  
- **Ref:** US-20  
- **Platform:** Mobile Only

---

### [FITUR 6: ADMIN & MANAJEMEN SISTEM - WEB ONLY]

#### FR-21
Sistem harus mengisolasi gerbang login pengelola pada *endpoint* khusus (`/admin/login`) menggunakan sistem proteksi *guard session* yang terpisah dari akun Pendaki umum.  
- **Prioritas:** Low  
- **Ref:** US-21  
- **Platform:** Web Only

#### FR-22
Sistem harus menyediakan modul pengelolaan parameter gunung (CRUD) bagi Superadmin dan memproteksi data gunung agar tidak dapat dihapus jika masih terikat dengan rute pendakian yang aktif.  
- **Prioritas:** Low  
- **Ref:** US-22  
- **Platform:** Web Only (Superadmin)

#### FR-23
Sistem harus memfasilitasi antarmuka pengisian parameter rute tingkat kesulitan, kuota harian, serta struktur informasi operasional basecamp (kontak CP, harga tiket, info ojek).  
- **Prioritas:** Medium  
- **Ref:** US-23  
- **Platform:** Web Only (Superadmin)

#### FR-24
Sistem harus mengizinkan pengubahan susunan letak *checkpoint* rute (CRUD Waypoint) berdasarkan urutan sekuensial numerik indeks (`order_index`) untuk menjamin keselarasan visual peta luring di perangkat mobile.  
- **Prioritas:** Low  
- **Ref:** US-24  
- **Platform:** Web Only (Superadmin)

#### FR-25
Sistem harus menyediakan modul bagi Admin Basecamp untuk mencari transaksi berdasarkan *Order ID*, memvalidasi kesesuaian tanggal kunjungan, serta memverifikasi status kehadiran pendaki di gerbang masuk (*check-in*).  
- **Prioritas:** High  
- **Ref:** US-25  
- **Platform:** Web Only (Admin Basecamp)

---

## 4. Kebutuhan Non-Fungsional (Non-Functional Requirements)

#### NFR-01: Performance
Waktu respons (*response time*) dari API backend Laravel untuk memuat data teks fasilitas basecamp, riwayat transaksi, dan komponen cuaca tidak boleh melebihi **3 detik** saat diakses menggunakan koneksi internet seluler standar (3G/4G) maupun Wi-Fi.

#### NFR-02: Security
Seluruh jalur komunikasi pertukaran data antara klien (Web/Mobile) dengan server API wajib dilindungi menggunakan enkripsi transport data protokol **HTTPS**. Penyimpanan *Bearer Token* di sisi klien mobile harus diamankan menggunakan enkripsi internal ponsel (*Android DataStore Preferences*).

#### NFR-03: Scalability
Sistem infrastruktur server backend harus mampu menangani beban operasional hingga **100 permintaan serentak (concurrent requests)** secara stabil dengan tingkat keberhasilan pemrosesan data minimal **99.9%** (tingkat kegagalan atau error rate maksimal **0.1%**).

#### NFR-04: Usability
Proses pencadangan data (*offline caching*) komponen rute ke dalam Room DB lokal ponsel pendaki harus berjalan di jalur latar belakang (*background thread/coroutine*) untuk mencegah terjadinya pembekuan antarmuka (*UI freezing*) pada aplikasi mobile.

---

## 5. Catatan & Asumsi
- Fitur pelacakan koordinat luring di gunung sepenuhnya mengandalkan sensor internal perangkat hardware **GPS (Global Positioning System)** pendaki.
- Sistem membutuhkan koneksi internet untuk sinkronisasi data kuota, riwayat transaksi, dan pembaruan cuaca sebelum mode luring di lapangan digunakan.
- Keberhasilan proses pemesanan dan otomasi status transaksi bergantung penuh pada *uptime* server sistem penyedia layanan *payment gateway* pihak ketiga (Midtrans).