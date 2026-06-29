<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Http;
use Illuminate\Validation\Rules\Password;

class AuthController extends Controller
{
    /**
     * Registrasi user baru dan kirim OTP via email.
     */
    public function register(Request $request)
    {
        $validated = $request->validate([
            'name'         => ['required', 'string', 'max:100'],
            'email'        => ['required', 'string', 'email', 'max:150'],
            'phone_number' => ['required', 'string', 'max:15'],
            'password'     => ['required', Password::min(8)],
        ]);

        $existingUser = User::where('email', $validated['email'])->first();
        if ($existingUser) {
            if (!is_null($existingUser->email_verified_at)) {
                return response()->json([
                    'message' => 'Email ini sudah terdaftar di sistem kami.'
                ], 422);
            }

            // Update user yang belum terverifikasi dengan data baru
            $existingUser->update([
                'name'         => $validated['name'],
                'phone_number' => $validated['phone_number'],
                'password'     => bcrypt($validated['password']),
            ]);
            $user = $existingUser;
        } else {
            $user = User::create([
                'name'              => $validated['name'],
                'email'             => $validated['email'],
                'phone_number'      => $validated['phone_number'],
                'password'          => bcrypt($validated['password']),
                'age'               => null,
                'address'           => null,
                'emergency_contact' => null,
                'nik'               => null,
                'email_verified_at' => null,
            ]);
        }

        // Generate OTP 6 digit
        $code = str_pad((string) random_int(100000, 999999), 6, '0', STR_PAD_LEFT);

        \Illuminate\Support\Facades\DB::transaction(function () use ($validated, $code) {
            \Illuminate\Support\Facades\DB::table('email_verification_codes')->where('email', $validated['email'])->delete();
            \Illuminate\Support\Facades\DB::table('email_verification_codes')->insert([
                'email'       => $validated['email'],
                'code'        => $code,
                'expires_at'  => \Carbon\Carbon::now()->addMinutes(15),
                'created_at'  => \Carbon\Carbon::now(),
            ]);
        });

        // Kirim OTP via Gmail
        \Illuminate\Support\Facades\Mail::to($user->email)->send(new \App\Mail\RegisterOtp($code, $user->name));

        return response()->json([
            'message' => 'Registrasi berhasil! Kode OTP telah dikirim ke email Anda.',
            'email'   => $user->email
        ], 201);
    }

    /**
     * Verifikasi OTP registrasi.
     */
    public function verifyOtp(Request $request)
    {
        $request->validate([
            'email' => ['required', 'string', 'email'],
            'code'  => ['required', 'string', 'size:6'],
        ]);

        $otpRecord = \Illuminate\Support\Facades\DB::table('email_verification_codes')
            ->where('email', $request->email)
            ->where('code', $request->code)
            ->first();

        if (!$otpRecord) {
            return response()->json(['message' => 'Kode OTP tidak valid.'], 422);
        }

        if (\Carbon\Carbon::now()->greaterThan($otpRecord->expires_at)) {
            return response()->json(['message' => 'Kode OTP telah kedaluwarsa.'], 422);
        }

        // Aktifkan akun user
        $user = User::where('email', $request->email)->firstOrFail();
        $user->email_verified_at = \Carbon\Carbon::now();
        $user->save();

        // Hapus kode OTP
        \Illuminate\Support\Facades\DB::table('email_verification_codes')->where('email', $request->email)->delete();

        // Login dan return token
        $token = $user->createToken('mobile-app')->plainTextToken;

        return response()->json([
            'message' => 'Akun Anda berhasil terverifikasi!',
            'user'    => $user,
            'token'   => $token,
        ]);
    }

    /**
     * Kirim ulang OTP registrasi.
     */
    public function resendOtp(Request $request)
    {
        $request->validate([
            'email' => ['required', 'string', 'email']
        ]);

        $user = User::where('email', $request->email)->first();
        if (!$user) {
            return response()->json(['message' => 'Email tidak terdaftar.'], 404);
        }

        if ($user->email_verified_at) {
            return response()->json(['message' => 'Akun Anda sudah terverifikasi.'], 400);
        }

        $code = str_pad((string) random_int(100000, 999999), 6, '0', STR_PAD_LEFT);

        \Illuminate\Support\Facades\DB::transaction(function () use ($user, $code) {
            \Illuminate\Support\Facades\DB::table('email_verification_codes')->where('email', $user->email)->delete();
            \Illuminate\Support\Facades\DB::table('email_verification_codes')->insert([
                'email'       => $user->email,
                'code'        => $code,
                'expires_at'  => \Carbon\Carbon::now()->addMinutes(15),
                'created_at'  => \Carbon\Carbon::now(),
            ]);
        });

        \Illuminate\Support\Facades\Mail::to($user->email)->send(new \App\Mail\RegisterOtp($code, $user->name));

        return response()->json([
            'message' => 'Kode OTP baru telah dikirim ke email Anda.',
        ]);
    }

    /**
     * Login dan return Bearer token.
     */
    public function login(Request $request)
    {
        $request->validate([
            'email'    => ['required', 'string', 'email'],
            'password' => ['required', 'string'],
        ]);

        $user = User::where('email', $request->email)->first();

        if (! $user || ! Hash::check($request->password, $user->password)) {
            return response()->json([
                'message' => 'Email atau password salah.',
            ], 401);
        }

        $token = $user->createToken('mobile-app')->plainTextToken;

        return response()->json([
            'message' => 'Login berhasil!',
            'user'    => $user,
            'token'   => $token,
        ]);
    }

    /**
     * Logout — revoke token yang sedang dipakai.
     */
    public function logout(Request $request)
    {
        // Hapus token yang sedang digunakan saja
        $request->user()->currentAccessToken()->delete();

        return response()->json([
            'message' => 'Logout berhasil!',
        ]);
    }

    /**
     * Ambil data profil user yang sedang login.
     */
    public function user(Request $request)
    {
        return response()->json($request->user());
    }

    /**
     * Update profil user (dari mobile app).
     * Semua field bersifat optional (partial update).
     */
    public function updateProfile(Request $request)
    {
        $validated = $request->validate([
            'name'              => ['sometimes', 'string', 'max:100'],
            'phone_number'      => ['sometimes', 'string', 'max:15'],
            'age'               => ['sometimes', 'integer', 'min:1', 'max:120'],
            'address'           => ['sometimes', 'string'],
            'emergency_contact' => ['sometimes', 'string', 'max:15'],
        ]);

        $request->user()->update($validated);

        return response()->json([
            'message' => 'Profil berhasil diperbarui.',
            'user'    => $request->user()->fresh(),
        ]);
    }

    /**
     * Login via Google ID token.
     */
    public function loginWithGoogle(Request $request)
    {
        $request->validate([
            'id_token' => ['required', 'string']
        ]);

        $googleResponse = Http::get('https://oauth2.googleapis.com/tokeninfo', [
            'id_token' => $request->id_token
        ]);

        if (!$googleResponse->successful()) {
            return response()->json(['message' => 'Google ID Token tidak valid.'], 401);
        }

        $googleData = $googleResponse->json();
        $email = $googleData['email'] ?? null;
        $googleId = $googleData['sub'] ?? null;

        if (!$email || !$googleId) {
            return response()->json(['message' => 'Gagal mendapatkan data email atau sub dari Google.'], 400);
        }

        $user = User::where('email', $email)
            ->orWhere('google_id', $googleId)
            ->first();

        if (!$user) {
            $user = User::create([
                'name'              => $googleData['name'] ?? 'Google User',
                'email'             => $email,
                'google_id'         => $googleId,
                'avatar'            => $googleData['picture'] ?? null,
                'email_verified_at' => now(),
            ]);
        } else {
            $updatedData = [];
            if (empty($user->google_id)) {
                $updatedData['google_id'] = $googleId;
            }
            if (empty($user->avatar) && isset($googleData['picture'])) {
                $updatedData['avatar'] = $googleData['picture'];
            }
            if (empty($user->email_verified_at)) {
                $updatedData['email_verified_at'] = now();
            }
            if (!empty($updatedData)) {
                $user->update($updatedData);
            }
        }

        $token = $user->createToken('mobile-app')->plainTextToken;

        return response()->json([
            'message' => 'Login berhasil!',
            'user'    => $user,
            'token'   => $token,
        ]);
    }
}
