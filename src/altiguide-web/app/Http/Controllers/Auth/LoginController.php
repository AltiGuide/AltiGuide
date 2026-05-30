<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Inertia\Inertia;

class LoginController extends Controller
{
    /**
     * Tampilkan halaman login user (pendaki).
     */
    public function create()
    {
        return Inertia::render('Auth/Login');
    }

    /**
     * Proses login user.
     */
    public function store(Request $request)
    {
        $credentials = $request->validate([
            'email'    => ['required', 'string', 'email'],
            'password' => ['required', 'string'],
        ]);

        if (! Auth::attempt($credentials, $request->boolean('remember'))) {
            return back()->withErrors([
                'email' => 'Email atau password salah.',
            ])->onlyInput('email');
        }

        $user = Auth::user();
        if ($user && is_null($user->email_verified_at)) {
            Auth::logout();
            $request->session()->invalidate();
            $request->session()->regenerateToken();

            return redirect()->route('register.verify', ['email' => $user->email])
                             ->with('error', 'Akun Anda belum terverifikasi. Silakan lakukan verifikasi OTP terlebih dahulu.');
        }

        $request->session()->regenerate();

        return redirect()->intended('/dashboard');
    }

    /**
     * Logout user.
     */
    public function destroy(Request $request)
    {
        Auth::guard('web')->logout();

        $request->session()->invalidate();
        $request->session()->regenerateToken();

        return redirect('/');
    }
}
