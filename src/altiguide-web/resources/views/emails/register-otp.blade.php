<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <style>
        body { font-family: 'Segoe UI', Arial, sans-serif; background: #f4f7fa; margin: 0; padding: 20px; }
        .container { max-width: 480px; margin: 0 auto; background: #fff; border-radius: 12px; padding: 40px 32px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
        .logo { text-align: center; font-size: 24px; font-weight: 700; color: #374426; margin-bottom: 24px; }
        .code-box { background: #f4fdf4; border: 2px dashed #64823E; border-radius: 8px; text-align: center; padding: 20px; margin: 24px 0; }
        .code { font-size: 36px; font-weight: 800; letter-spacing: 8px; color: #374426; }
        .text { color: #374151; font-size: 15px; line-height: 1.6; }
        .footer { text-align: center; color: #9ca3af; font-size: 12px; margin-top: 32px; }
        .warning { color: #dc2626; font-size: 13px; margin-top: 16px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="logo">🏔️ AltiGuide</div>

        <p class="text">Halo <strong>{{ $userName }}</strong>,</p>

        <p class="text">Terima kasih telah mendaftar di AltiGuide! Gunakan kode verifikasi (OTP) berikut untuk mengaktifkan akun Anda:</p>

        <div class="code-box">
            <div class="code">{{ $code }}</div>
        </div>

        <p class="text">Kode verifikasi ini hanya berlaku selama <strong>15 menit</strong>. Mohon tidak membagikan kode ini kepada siapapun demi keamanan akun Anda.</p>

        <p class="warning">⚠️ Jika Anda merasa tidak melakukan pendaftaran ini, silakan abaikan email ini.</p>

        <div class="footer">
            &copy; {{ date('Y') }} AltiGuide — Teman Pendakian Kamu
        </div>
    </div>
</body>
</html>
