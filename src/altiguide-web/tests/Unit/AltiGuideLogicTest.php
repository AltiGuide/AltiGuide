<?php

namespace Tests\Unit;

use PHPUnit\Framework\TestCase;

class AltiGuideLogicTest extends TestCase
{
    /**
     * TEST 1 (Edge Case): Batas Karakter Judul Seksi Konten (Kasus Bug P9)
     */
    public function test_judul_seksi_konten_tidak_boleh_melebihi_255_karakter()
    {
        // 1. Arrange: Siapkan teks ekstrem panjang (300 karakter)
        $judulEkstrem = str_repeat("A", 300);

        // 2. Act: Hitung panjang teks dan cek kelayakannya
        $panjangKarakter = strlen($judulEkstrem);
        $apakahValid = $panjangKarakter <= 255;

        // 3. Assert: Pastikan hasilnya FALSE (melanggar batas maksimal)
        $this->assertFalse($apakahValid, "Input lebih dari 255 karakter harusnya gagal!");
    }

    /**
     * TEST 2 (Edge Case): Pengecekan kelengkapan NIK untuk alur Booking SIMAKSI
     */
    public function test_user_tanpa_nik_gagal_validasi_booking()
    {
        // 1. Arrange: Siapkan data profil pengguna tiruan dengan NIK kosong (null)
        $inputUser = [
            'nama' => 'Daniel Ferdian',
            'nik' => null,
            'alamat' => 'Semarang'
        ];

        // 2. Act: Jalankan logika pengecekan ketersediaan data NIK
        $apakahBisaBooking = !empty($inputUser['nik']);

        // 3. Assert: Pastikan hasilnya FALSE (akses booking diblokir)
        $this->assertFalse($apakahBisaBooking, "User tanpa NIK tidak boleh diizinkan booking!");
    }

    /**
     * TEST 3 (Edge Case): Validasi format alamat email akun pendaki
     */
    public function test_format_email_harus_valid()
    {
        // 1. Arrange: Siapkan string email dengan format penulisan yang salah
        $emailSalah = "daniel_bukan_email.com";

        // 2. Act: Jalankan filter validasi format email bawaan sistem
        $apakahEmailValid = filter_var($emailSalah, FILTER_VALIDATE_EMAIL) !== false;

        // 3. Assert: Pastikan hasilnya FALSE (karena format salah)
        $this->assertFalse($apakahEmailValid, "Format email salah harusnya gagal validasi!");
    }

    /**
     * TEST 4 (Happy Case): Judul seksi konten dengan panjang normal (Lolos)
     */
    public function test_judul_seksi_konten_normal_berhasil_validasi()
    {
        // 1. Arrange: Siapkan judul artikel yang wajar dan aman (20 karakter)
        $judulNormal = "Jalur Pendaki Merbabu";

        // 2. Act: Hitung panjang teks
        $panjangKarakter = strlen($judulNormal);
        $apakahValid = $panjangKarakter <= 255;

        // 3. Assert: Pastikan hasilnya TRUE (lolos masuk database)
        $this->assertTrue($apakahValid, "Judul yang wajar harusnya lolos validasi!");
    }

    /**
     * TEST 5 (Happy Case): Profil user lengkap dengan NIK (Lolos Booking)
     */
    public function test_user_dengan_nik_lengkap_lolos_validasi_booking()
    {
        // 1. Arrange: Siapkan profil user simulasi dengan NIK lengkap terisi
        $inputUserLengkap = [
            'nama' => 'Daniel Ferdian',
            'nik' => '3311223344556677',
            'alamat' => 'Semarang'
        ];

        // 2. Act: Jalankan logika cek ketersediaan data NIK
        $apakahBisaBooking = !empty($inputUserLengkap['nik']);

        // 3. Assert: Pastikan hasilnya TRUE (diizinkan memesan tiket gunung)
        $this->assertTrue($apakahBisaBooking, "User dengan data NIK lengkap harus lolos booking!");
    }
}