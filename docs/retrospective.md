# Team Retrospective - AltiGuide Project

### What went well? (Apa yang berjalan baik)
- Implementasi arsitektur modular client-server terintegrasi dengan baik menggunakan tech stack modern (Laravel, Kotlin, Vue 3).
- Fitur utama pelacakan offline dengan berkas `.gpx` berhasil menjawab problem utama kebutuhan keselamatan pendaki secara riil.
- Pembagian tugas kelompok berjalan cukup terstruktur dan komunikasi tim dinamis saat menyelesaikan bug kritis.

### What didn't go well? (Apa yang tidak berjalan baik)
- Terjadi hambatan manajemen waktu di fase akhir karena adanya konflik versi dependensi (Symfony package) yang sempat menghentikan proses pengujian unit di lokal.
- Ketergantungan alur kerja tim pada proses Pull Request (PR) yang kadang tertunda karena kesibukan anggota kelompok lain.

### What can we improve? (Apa yang bisa diperbaiki ke depan)
- Menerapkan otomasi pengujian atau penguncian dependensi pihak ketiga (`composer.lock` / `package-lock.json`) lebih awal untuk menghindari error kecocokan versi di perangkat yang berbeda.
- Menetapkan jadwal *review* PR harian yang disepakati bersama agar kode tidak menumpuk di branch terpisah.

### Shout-outs (Apresiasi untuk anggota tim)
- Apresiasi besar untuk seluruh anggota tim kelompok atas dedikasi dan kerja kerasnya, mulai dari tim UI/UX yang merancang antarmuka modern, tim QA dan Developer yang lembur menyelesaikan integrasi API serta memastikan kestabilan kode, hingga tim dokumentasi dalam menyusun serta merevisi berkas laporan.