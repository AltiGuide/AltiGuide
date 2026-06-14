# Team Contract & Project Progress - AltiGuide

Dokumen ini merupakan kesepakatan resmi anggota tim untuk memastikan kelancaran kolaborasi serta dokumentasi progress berkala selama praktikum Rekayasa Perangkat Lunak (RPL).

## 🚀 Status Progress Fitur Aplikasi (Update Modul P7)
Berikut adalah daftar fitur utama (*Features*) yang telah berhasil diimplementasikan dan di-merge ke dalam branch `dev`:

### 1. Fitur Utama Tahap 1 (Must-have #1 - Modul P6)
* **[Selesai] Authentication System**: Sistem Login dan Register fungsional menggunakan arsitektur Laravel + Inertia.js untuk menjamin keamanan akses pendaki.
* **[Selesai] Landing Page / Dashboard**: Antarmuka awal aplikasi sebagai gerbang informasi utama platform AltiGuide.

### 2. Fitur Utama Tahap 2 (Must-have #2 - Modul P7)
* **[Selesai] Mountain & Article Detail**: Halaman eksplorasi informasi artikel detail gunung (deskripsi, foto, dan integrasi rute) serta seksi rekomendasi destinasi pendakian lainnya (*Other Destinations*).
* **[Selesai] Dynamic Route Routing & Database Integration**: Sistem pencarian data berbasis `slug` dan penampilan data dinamis dari database tanpa *hardcoded values*.

### 3. Fitur Utama Tahap 3 (Must-have #3 - Modul P8)
* **[Selesai] Online Booking SIMAKSI System**: Sistem registrasi pendakian daring yang terintegrasi penuh, memandu pengguna melalui alur pemilihan destinasi, inisiasi grup/manifes, hingga validasi anggota secara otomatis.
* **[Selesai] Dynamic Cost Calculation & Review**: Modul kalkulasi rincian biaya tiket masukan dan biaya layanan pendakian secara *real-time* dan dinamis yang ditarik langsung dari database pada tahap tinjauan akhir.

---

## 1. Peran Anggota Tim (Team Roles)
Setiap anggota bertanggung jawab atas tugas utama berikut:

* **Fauzil Azhim**: **Lead Developer & Backend**
  * Bertanggung jawab atas desain arsitektur sistem dan struktur basis data (SQL).
  * Mengelola logika bisnis di sisi server dan integrasi API.
  * Melakukan peninjauan kode (*code review*) pada setiap Pull Request untuk menjaga kualitas kode di repositori.

* **Adrian Alviano Susatyo**: **Quality Assurance (QA) & Frontend**
  * Bertanggung jawab melakukan pengujian fungsionalitas (Black-box testing) pada setiap fitur yang selesai dibuat.
  * Mendokumentasikan bug atau error ke dalam Issue Tracker dan memantau proses perbaikannya.
  * Mengembangkan komponen antarmuka pengguna (Frontend) berdasarkan desain yang telah disepakati.

* **Daniel Ferdian Napitupulu**: **Documentation & Technical Writer**
  * Bertanggung jawab menyusun laporan resmi praktikum Bab I hingga Bab akhir.
  * Mengelola dokumentasi teknis dalam repositori seperti file README, Wiki, dan dokumentasi endpoint API.
  * Memastikan seluruh artefak proyek (diagram UML, ERD) terdokumentasi dengan rapi sesuai standar RPL.

* **Diva Valencia Christianarta**: **UI/UX Designer**
  * Bertanggung jawab atas riset pengalaman pengguna dan pembuatan desain *high-fidelity* (Mockup) serta Prototype.
  * Mengimplementasikan desain antarmuka menjadi kode frontend yang responsif dan user-friendly.
  * Memastikan konsistensi visual (warna, tipografi, dan aset gambar) di seluruh bagian aplikasi.

## 2. Jadwal Pertemuan (Meeting Schedule)
Pertemuan rutin dilakukan untuk sinkronisasi progress:
* **Hari**: Setiap hari Jumat
* **Waktu**:  19.00 WIB - Selesai
* **Platform**:  Google Meet / Discord / Zoom/ Offline
* **Agenda**: Update progress mingguan, pembagian task baru, dan penyelesaian kendala teknis.

## 3. Saluran Komunikasi (Communication Channels)
* **Komunikasi Harian**: Whatsapp Group untuk koordinasi cepat dan pengumuman penting.
* **Diskusi Teknis**: Discord, Zoom, atau Google Meet untuk diskusi mendalam terkait pengembangan dan permasalahan teknis.
* **Manajemen Tugas**: Spreadsheet Google Sheets untuk pembagian tugas, tracking progress, dan deadline, Opsional Menggunakan Scrum /Trello untuk manajemen proyek yang lebih terstruktur.

## 4. Aturan Respons (Communication Policy)
Untuk menjaga efisiensi kerja, seluruh anggota tim menyepakati:
* **Jam Aktif**: 09:00 - 17:00 WIB
* **Waktu Respons**: Maksimal **2 jam** setelah pesan dikirimkan pada jam aktif.
* **Ketersediaan**: Jika anggota akan tidak aktif (berhalangan) lebih dari 12 jam, wajib menginfokan tim terlebih dahulu.

## 5. Standar Pesan Commit (Commit Message Standards)
Gunakan format **imperative** dalam Bahasa Inggris atau Bahasa Indonesia untuk setiap perubahan di repositori:
* **Format**: `[Tag]: [Short Description]`
* **Contoh Tag**: `feat` (fitur baru), `fix` (perbaikan bug), `docs` (dokumentasi), `style` (formatting).
* **Contoh Pesan**:
    * `feat: add login validation logic`
    * `fix: resolve null pointer exception in database connection`
    * `docs: update team contract file`

## 6. Mekanisme Eskalasi (Escalation Mechanism)
Jika terjadi masalah yang tidak dapat diselesaikan secara internal:
1.  **Diskusi Internal**: Masalah dibahas terlebih dahulu dalam pertemuan tim secara terbuka.
2.  **Deadlock/Konflik**: Jika terjadi jalan buntu dalam pengambilan keputusan atau masalah komitmen anggota, ketua tim akan mengambil keputusan akhir.
3.  **Bantuan Eksternal**: Jika masalah tetap tidak teratasi atau mengganggu keberlangsungan proyek, tim akan segera menghubungi **Asisten Praktikum** sebagai mediator/pemberi solusi.

---
*Kontrak ini disepakati oleh seluruh anggota tim pada tanggal [2 April 2026].*
