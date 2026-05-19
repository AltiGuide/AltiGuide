# Product Backlog: AltiGuide

Dokumen ini mencakup seluruh daftar kebutuhan fitur sistem AltiGuide yang diprioritaskan menggunakan metode MoSCoW (*Must-have, Should-have, Could-have*) serta disesuaikan dengan kebutuhan platform Web CMS dan Mobile App.

---

## 📊 Tabel Product Backlog

| ID | Deskripsi User Story | Peran (Role) | Prioritas (MoSCoW) | Platform |
| :--- | :--- | :--- | :--- | :--- |
| **US-01** | Melakukan registrasi akun baru dengan data diri lengkap dan validasi NIK. | Pendaki (Hiker) | **Must-have** | Web & Mobile |
| **US-02** | Masuk ke sistem (Login) menggunakan email dan password untuk akses data personal. | Pendaki (Hiker) | **Must-have** | Web & Mobile |
| **US-03** | Mereset password yang lupa secara mandiri melalui verifikasi kode OTP email. | Pendaki (Hiker) | **Must-have** | Web & Mobile |
| **US-04** | Melakukan keluar dari sistem (Logout) untuk mengamankan akses akun. | Pendaki (Hiker) | **Must-have** | Web & Mobile |
| **US-05** | Melihat dan memperbarui data informasi profil dan kontak darurat. | Pendaki (Hiker) | **Should-have** | Web & Mobile |
| **US-06** | Mengubah kata sandi akun aktif secara berkala dari dalam sistem keamanan aplikasi. | Pendaki (Hiker) | **Could-have** | Web & Mobile |
| **US-07** | Melihat daftar katalog gunung yang tersedia beserta status operasionalnya. | Pendaki (Hiker) | **Must-have** | Web & Mobile |
| **US-08** | Melihat informasi detail gunung, pilihan jalur pendakian, dan logistik basecamp. | Pendaki (Hiker) | **Must-have** | Web & Mobile |
| **US-09** | Melihat susunan urutan pos/checkpoint pendakian beserta ketersediaan sumber air. | Pendaki (Hiker) | **Should-have** | Web & Mobile |
| **US-10** | Memantau prediksi cuaca real-time di puncak gunung dan area basecamp secara berkala. | Pendaki (Hiker) | **Should-have** | Web & Mobile |
| **US-11** | Mengajukan permohonan booking izin pendakian (SIMAKSI) secara daring. | Pendaki (Hiker) | **Must-have** | Web Only |
| **US-12** | Melakukan validasi lintasan NIK seluruh anggota kelompok pendaki untuk menghindari konflik jadwal. | Pendaki (Hiker) | **Must-have** | Web Only |
| **US-13** | Melakukan transaksi pembayaran biaya SIMAKSI menggunakan QRIS otomatis via Midtrans. | Pendaki (Hiker) | **Must-have** | Web Only |
| **US-14** | Menerima berkas dokumen E-Ticket resmi berformat PDF setelah pembayaran terkonfirmasi lunas. | Pendaki (Hiker) | **Should-have** | Web Only |
| **US-15** | Memantau daftar riwayat transaksi pemesanan SIMAKSI secara berkala. | Pendaki (Hiker) | **Must-have** | Web & Mobile |
| **US-16** | Melihat detail rincian data transaksi, biaya, dan struktur anggota kelompok pendakian. | Pendaki (Hiker) | **Should-have** | Web & Mobile |
| **US-17** | Mengunduh file biner dokumen E-Ticket PDF langsung ke dalam penyimpanan lokal ponsel. | Pendaki (Hiker) | **Should-have** | Mobile Only |
| **US-18** | Memantau daftar jadwal agenda sesi pendakian aktif yang akan dijalani. | Pendaki (Hiker) | **Must-have** | Mobile Only |
| **US-19** | Menyimpan data rute pendakian ke database lokal ponsel (caching) untuk persiapan luring. | Pendaki (Hiker) | **Must-have** | Mobile Only |
| **US-20** | Menggunakan panduan informasi jalur dan pos pendakian secara offline saat di gunung. | Pendaki (Hiker) | **Should-have** | Mobile Only |
| **US-21** | Masuk ke panel sistem pengelola (Login Admin) menggunakan kredensial dan guard terpisah. | Pengelola Sistem | **Could-have** | Web Only |
| **US-22** | Mengelola, mengubah, dan menghapus data master gunung di dalam sistem. | Superadmin | **Could-have** | Web Only |
| **US-23** | Mengelola informasi operasional rute pendakian dan data kelayakan fasilitas basecamp. | Superadmin | **Should-have** | Web Only |
| **US-24** | Menyusun struktur tata letak koordinat pos jalur berdasarkan indeks urutan sekuensial. | Superadmin | **Could-have** | Web Only |
| **US-25** | Melakukan pemindaian (scanning) QR Code e-ticket pendaki untuk verifikasi check-in lapangan. | Basecamp Staff | **Must-have** | Web Only |