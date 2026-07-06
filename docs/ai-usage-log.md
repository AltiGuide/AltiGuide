# AI-Usage Log & Reflection

### AI Tools Used
- **Gemini AI**: Digunakan oleh tim QA dan Dokumentasi untuk analisis kode, penyusunan skenario pengujian, dan penyusunan draf laporan praktikum.
- **Antigravity**: Digunakan oleh tim Front-End (FE) dan Back-End (BE) sebagai asisten *code generation*, penulisan komponen UI mobile, dan akselerasi pembuatan *endpoint* API.

### What worked well? (Apa yang berhasil saat menggunakan AI)
- **Gemini AI**: Sangat akseleratif dalam mentransformasikan kebutuhan sistem menjadi dokumen Software Requirements Specification (SRS) yang terstruktur, memetakan fungsionalitas ke dalam User Stories dan Use Case Diagram secara komprehensif, serta mempermudah penulisan skenario automated unit testing untuk mengunci kebenaran logika bisnis.
- **Antigravity**: Berhasil mempercepat pembuatan struktur komponen layout UI mobile secara modular dan mempercepat penulisan boilerplate query basis data pada repositori back-end.

### What didn't work well? (Apa yang tidak berhasil saat menggunakan AI)
- **AI Terkadang memberikan saran struktur dokumen atau sintaks framework versi lama (seperti Laravel terdahulu), sehingga tim harus menyesuaikan manual agar cocok dengan standar Laravel 13 yang digunakan.
- AI Sering kali menghasilkan kode komponen UI yang terlalu generik dan mengalami *clashing* (tabrakan layout) dengan system status bar pada perangkat mobile, serta beberapa logika *state management* yang di-generate tidak sinkron dengan Firebase Realtime DB sehingga harus di-refactor ulang secara manual oleh tim developer.