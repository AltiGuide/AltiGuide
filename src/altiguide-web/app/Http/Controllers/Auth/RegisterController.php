<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Mail\RegisterOtp;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Mail;
use Illuminate\Validation\Rules\Password;
use Inertia\Inertia;
use Carbon\Carbon;

class RegisterController extends Controller
{
    /**
     * Tampilkan halaman form registrasi.
     */
    public function create()
    {
        return Inertia::render('Auth/Register');
    }

    /**
     * Proses pendaftaran user pertama kali.
     */
    public function store(Request $request)
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
                throw \Illuminate\Validation\ValidationException::withMessages([
                    'email' => 'Email ini sudah terdaftar di sistem kami.'
                ]);
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

        // Generate OTP
        $code = str_pad((string) random_int(100000, 999999), 6, '0', STR_PAD_LEFT);

        DB::transaction(function () use ($validated, $code) {
            DB::table('email_verification_codes')->where('email', $validated['email'])->delete();
            DB::table('email_verification_codes')->insert([
                'email'       => $validated['email'],
                'code'        => $code,
                'expires_at'  => Carbon::now()->addMinutes(15),
                'created_at'  => Carbon::now(),
            ]);
        });

        // Kirim OTP via Gmail
        Mail::to($user->email)->send(new RegisterOtp($code, $user->name));

        return redirect()->route('register.verify', ['email' => $user->email])
                         ->with('success', 'Kode verifikasi OTP telah dikirimkan ke email Anda.');
    }

    /**
     * Tampilkan halaman verifikasi OTP.
     */
    public function showVerifyForm(Request $request)
    {
        return Inertia::render('Auth/VerifyRegisterOtp', [
            'email' => $request->query('email')
        ]);
    }

    /**
     * Proses verifikasi OTP.
     */
    public function verifyOtp(Request $request)
    {
        $request->validate([
            'email' => ['required', 'string', 'email'],
            'code'  => ['required', 'string', 'size:6'],
        ]);

        $otpRecord = DB::table('email_verification_codes')
            ->where('email', $request->email)
            ->where('code', $request->code)
            ->first();

        if (!$otpRecord) {
            return back()->withErrors(['code' => 'Kode OTP tidak valid.']);
        }

        if (Carbon::now()->greaterThan($otpRecord->expires_at)) {
            return back()->withErrors(['code' => 'Kode OTP telah kedaluwarsa.']);
        }

        // Aktifkan akun user
        $user = User::where('email', $request->email)->firstOrFail();
        $user->email_verified_at = Carbon::now();
        $user->save();

        // Hapus kode OTP
        DB::table('email_verification_codes')->where('email', $request->email)->delete();

        // Log in user
        Auth::login($user);
        $request->session()->regenerate();

        return redirect()->route('dashboard')->with('success', 'Akun Anda berhasil terverifikasi!');
    }

    /**
     * Kirim ulang kode OTP.
     */
    public function resendOtp(Request $request)
    {
        $request->validate([
            'email' => ['required', 'string', 'email']
        ]);

        $user = User::where('email', $request->email)->first();
        if (!$user) {
            return back()->withErrors(['email' => 'Email tidak terdaftar.']);
        }

        if ($user->email_verified_at) {
            return redirect()->route('login')->with('success', 'Akun Anda sudah terverifikasi, silakan login.');
        }

        // Generate OTP baru
        $code = str_pad((string) random_int(100000, 999999), 6, '0', STR_PAD_LEFT);

        DB::transaction(function () use ($user, $code) {
            DB::table('email_verification_codes')->where('email', $user->email)->delete();
            DB::table('email_verification_codes')->insert([
                'email'       => $user->email,
                'code'        => $code,
                'expires_at'  => Carbon::now()->addMinutes(15),
                'created_at'  => Carbon::now(),
            ]);
        });

        // Kirim email
        Mail::to($user->email)->send(new RegisterOtp($code, $user->name));

        return back()->with('success', 'Kode verifikasi OTP baru telah dikirim ke email Anda.');
    }
}

