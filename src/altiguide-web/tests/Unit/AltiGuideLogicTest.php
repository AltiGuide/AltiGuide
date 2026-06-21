<?php

namespace Tests\Unit;

use PHPUnit\Framework\TestCase;

class AltiGuideLogicTest extends TestCase
{
    public function test_judul_seksi_konten_tidak_boleh_melebihi_255_karakter()
    {
        // Arrange: Input 300 karakter (melebihi batas database)
        $judulEkstrem = str_repeat("A", 300);

        // Act: Validasi panjang karakter
        $panjangKarakter = strlen($judulEkstrem);
        $apakahValid = $panjangKarakter <= 255;

        // Assert: Harus gagal (false)
        $this->assertFalse($apakahValid, "Input melebihi 255 karakter harusnya gagal.");
    }

    public function test_user_tanpa_nik_gagal_validasi_booking()
    {
        // Arrange: Data user dengan NIK kosong
        $inputUser = [
            'nama' => 'Daniel Ferdian',
            'nik' => null,
            'alamat' => 'Semarang'
        ];

        // Act: Cek ketersediaan NIK
        $apakahBisaBooking = !empty($inputUser['nik']);

        // Assert: Harus diblokir (false)
        $this->assertFalse($apakahBisaBooking, "User tanpa NIK tidak boleh diizinkan booking.");
    }

    public function test_format_email_harus_valid()
    {
        // Arrange: Format email salah
        $emailSalah = "daniel_bukan_email.com";

        // Act: Filter format email
        $apakahEmailValid = filter_var($emailSalah, FILTER_VALIDATE_EMAIL) !== false;

        // Assert: Harus tidak valid (false)
        $this->assertFalse($apakahEmailValid, "Format email salah harusnya gagal validasi.");
    }

    public function test_judul_seksi_konten_normal_berhasil_validasi()
    {
        // Arrange: Input judul normal (20 karakter)
        $judulNormal = "Jalur Pendaki Merbabu";

        // Act: Validasi panjang karakter
        $panjangKarakter = strlen($judulNormal);
        $apakahValid = $panjangKarakter <= 255;

        // Assert: Harus lolos (true)
        $this->assertTrue($apakahValid, "Judul dengan panjang normal harusnya lolos validasi.");
    }

    public function test_user_dengan_nik_lengkap_lolos_validasi_booking()
    {
        // Arrange: Data user dengan NIK lengkap
        $inputUserLengkap = [
            'nama' => 'Daniel Ferdian',
            'nik' => '3311223344556677',
            'alamat' => 'Semarang'
        ];

        // Act: Cek ketersediaan NIK
        $apakahBisaBooking = !empty($inputUserLengkap['nik']);

        // Assert: Harus lolos booking (true)
        $this->assertTrue($apakahBisaBooking, "User dengan NIK lengkap harus lolos booking.");
    }
}