| TC-ID | Judul | Precondition | Steps | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-001** | Registrasi Akun Baru Berhasil | Browser membuka halaman Register AltiGuide. | 1. Isi nama, email baru, dan password.<br>2. Klik tombol "Sign Up". | Sistem berhasil menyimpan data akun baru ke database, mengirim OTP/verifikasi, dan mengarahkan user ke halaman login. | Sistem berhasil memproses pendaftaran, memicu verifikasi OTP ke pengguna, dan setelah login berhasil dialihkan ke halaman Dashboard dengan data profil yang sebelumnya sudah diisikan. | Pass |
| **TC-002** | Registrasi Gagal (Email Sudah Terdaftar) | User mencoba mendaftar menggunakan email yang sudah ada di database. | 1. Masukkan Nama Lengkap dan No. Handphone.<br>2. Masukkan email yang sudah pernah terdaftar (`danielnapitupulu76@gmail.com`).<br>3. Klik tombol "Sign up". | Sistem menolak pendaftaran akun baru dan menampilkan pesan error validasi mengenai duplikasi email. | Sistem berhasil menolak pendaftaran dan memunculkan pesan error validasi merah tepat di bawah field email: *"Email ini sudah terdaftar di sistem kami."* | Pass |
| **TC-003** | Login Manual Berhasil (Akun Terdaftar) | User telah berhasil melakukan Sign Up pada langkah sebelumnya. | 1. Masukkan Email address terdaftar (`danielnapitupulu76@gmail.com`).<br>2. Isi password valid.<br>3. Klik "Sign In". | Autentikasi sukses, session terbentuk, dan user dialihkan masuk ke halaman Dashboard utama. | Sistem berhasil memvalidasi kredensial login dan langsung mengarahkan user ke halaman Dashboard utama AltiGuide dengan tampilan komponen yang rapi, namun kondisi data riwayat/aktivitas masih kosong karena merupakan akun baru. | Pass |
| **TC-004** | Login Gagal (Password Keliru) | Akun email sudah terdaftar di database. | 1. Masukkan Email address terdaftar (`danielnapitupulu76@gmail.com`).<br>2. Masukkan kombinasi password yang salah pada field password.<br>3. Klik tombol "Login". | Autentikasi ditolak oleh Laravel, user tetap di halaman login, dan muncul pesan error *"Credentials do not match"*. | Sistem menolak autentikasi secara instan, mempertahankan user di halaman login, serta memunculkan pesan kesalahan berwarna merah: *"Email atau password salah."* di bawah field input. | Pass |
| **TC-005** | Edit Artikel Gunung Berhasil | Login sebagai Super Admin dan berada di halaman Manajemen Konten Artikel. | 1. Pilih salah satu gunung (contoh: Gunung Andong) lalu klik tombol "Edit Artikel".<br>2. Lakukan perubahan data pada deskripsi artikel atau informasi jalur pendakian.<br>3. Klik tombol "Simpan Perubahan". | Perubahan data sukses disimpan ke dalam database PostgreSQL, sistem memperbarui informasi konten, dan memunculkan notifikasi sukses di halaman admin. | Sistem berhasil mengeksekusi perintah update data. Perubahan deskripsi artikel langsung tersimpan di database dan ketika dicek melalui tombol "Lihat Halaman Publik", konten terbaru sudah langsung ter-render dengan benar. | Pass |
| **TC-006** | Validasi Form Edit Artikel Kosong | Halaman Form Edit Artikel (Seksi Konten) sedang terbuka di dashboard admin. | 1. Kosongkan teks pada kolom input "Isi paragraf konten..." di SEKSI 1.<br>2. Klik tombol simpan/perbarui. | Sistem menolak untuk melakukan submit data kosong dan memberikan peringatan validasi kepada admin. | Form langsung memblokir proses submit data secara client-side, mempertahankan data asli di database, dan memunculkan alert tooltip browser: *"Please fill out this field."* tepat di bawah kolom paragraf yang kosong. | Pass |
| **TC-007** | Input Judul Seksi Konten di Halaman Edit Artikel Melebihi Batas Karakter | Halaman Form Edit Artikel (Manajemen Konten) sedang terbuka di dashboard admin. | 1. Pada inputan judul seksi artikel (kolom input di atas paragraf), masukkan teks yang sangat panjang hingga melebihi batas karakter database (Varchar).<br>2. Klik tombol "Simpan Perubahan" atau perbarui. | Sistem menolak proses submit karena panjang karakter melanggar batas, lalu menampilkan pesan error validasi yang jelas. | Sistem berhasil memblokir penyimpanan data panjang, namun form tidak memunculkan notifikasi/pesan gagal (silent error), sehingga admin bingung mengapa data tidak berubah. | Fail |
| **TC-008** | Pembuatan Booking SIMAKSI Sukses | User sudah melengkapi data NIK di profil dan data anggota lengkap. | 1. Masuk form Booking.<br>2. Pilih destinasi & tanggal.<br>3. Isi data rombongan.<br>4. Klik "Lanjutkan Pembayaran". | Form tervalidasi, baris transaksi baru berstatus pending tercatat di database, dan user diarahkan ke ringkasan pembayaran. | Sistem sukses memproses kelima tahapan pendaftaran SIMAKSI tanpa kendala. Database berhasil mencatat kode transaksi unik baru, kalkulasi nominal biaya tepat sebesar Rp30.000, dan payment gateway Midtrans berhasil memuat snap window QRIS secara instan. | Pass |
| **TC-009** | Validasi NIK Kosong Saat Memulai Booking | User sudah login, namun data NIK dan data pelengkap lainnya di menu Informasi Pribadi dikosongkan. | 1. Klik menu navigasi "Booking" pada navbar atas untuk memulai pendaftaran SIMAKSI. | Sistem memblokir akses ke form pendaftaran SIMAKSI dan mengarahkan paksa user untuk melengkapi data identitas diri terlebih dahulu. | Sistem berhasil mencegat alur, melakukan redirect otomatis ke halaman Informasi Pribadi, serta memunculkan flash message banner berwarna kuning di bagian atas: *"Profil Belum Lengkap! Lengkapai NIK, Umur, Alamat, dan Kontak Darurat agar dapat melakukan booking pendakian."* | Pass |
| **TC-010** | Double-Click pada Tombol Kirim Form Booking | Pengguna berada di tahap ke-4 (Review dan Kalkulasi Biaya) dengan data rombongan yang sudah tervalidasi. | 1. Klik tombol "Konfirmasi dan Bayar Sekarang" secara cepat sebanyak dua kali (double-click). | Sistem hanya memproses klik pertama, menonaktifkan (disable) tombol seketika, sehingga tidak terjadi duplikasi data transaksi di database. | Sistem berhasil menangani double-click dengan aman. Progres pendaftaran tetap berjalan normal ke tahap 5 dan tidak terjadi penggandaan baris data transaksi pada database backend. | Pass |

# [BUG] Gagal Menyimpan Judul Seksi yang Terlalu Panjang Tanpa Peringatan Validasi (Silent Error)

---

## Deskripsi / Ringkasan
Form pengeditan artikel terlihat seperti berhasil terproses tanpa ada *code crash* (HTTP 500) saat diinput judul seksi yang melebihi batas karakter. Namun, sistem melakukan *silent handling*: perubahan tidak tersimpan ke database, tampilan publik tetap menggunakan judul lama, dan tidak ada notifikasi gagal atau peringatan validasi apa pun yang muncul di layar admin.

| Atribut | Informasi |
| :--- | :--- |
| **Severity** | **Medium** *(Fitur aman dari kerusakan DB, namun merusak User Experience admin)* |
| **Aplikasi** | AltiGuide v1.0-dev |
| **Backend** | Laravel Framework |
| **Database** | PostgreSQL |

---

## Langkah-langkah Reproduksi (Steps to Reproduce)
1. Login ke dalam dashboard admin **AltiGuide**.
2. Buka menu **Manajemen Konten Artikel**, lalu klik tombol **"Edit Artikel"** pada salah satu gunung (misal: *Gunung Andong*).
3. Pada kolom input teks judul di bagian **SEKSI 1**, masukkan teks berupa karakter panjang yang melebihi batas kapasitas field database *(contoh string ekstrem panjang)*.
4. Klik tombol **"Simpan Perubahan"** / perbarui.
5. Periksa halaman web admin dan halaman publik artikel tersebut.

---

## Hasil yang Diharapkan (Expected Result)
Sistem menolak *request* update secara aman melalui framework, mempertahankan admin di halaman form edit, serta memunculkan **pesan error validasi yang jelas** (misal: `"The section title may not be greater than X characters"`), agar admin tahu bahwa inputannya melebihi batas.

## Hasil Aktual (Actual Result)
Sistem memproses halaman tanpa error (tidak ada HTTP 500), namun data baru **gagal masuk ke database** (tetap menggunakan data lama) dan **tidak ada *alert/flash message* validasi** yang muncul di interface admin.

---

## Lingkungan Pengujian (Environment)
* **OS:** Windows 11
* **Browser:** Microsoft Edge

## Lampiran (Screenshot)

![Bukti Bug Form Admin](../src/altiguide-web/public/images/Bug%20P9/edit.png)
![Bukti Bug Form Admin](../src/altiguide-web/public/images/Bug%20P9/hasil.png)