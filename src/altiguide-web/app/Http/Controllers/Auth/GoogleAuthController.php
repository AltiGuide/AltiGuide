<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\Admin;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Http;
use Inertia\Inertia;

class GoogleAuthController extends Controller
{
    public function handleGoogleLogin(Request $request)
    {
        $request->validate([
            'token' => ['required', 'string']
        ]);

        $googleResponse = Http::when(
            app()->environment('local'),
            fn ($http) => $http->withoutVerifying()
        )->get('https://www.googleapis.com/oauth2/v3/userinfo', [
            'access_token' => $request->token
        ]);

        if (!$googleResponse->successful()) {
            return response()->json(['message' => 'Invalid Google Token'], 401);
        }

        $googleData = $googleResponse->json();
        $email = $googleData['email'];

        $admin = Admin::where('email', $email)->first();
        if ($admin) {
            Auth::guard('admin')->login($admin);
            $request->session()->regenerate();
            return response()->json([
                'role' => 'admin',
                'user_status' => 'old',
                'redirect' => route('admin.dashboard')
            ]);
        }

        $user = User::where('email', $email)
            ->orWhere('google_id', $googleData['sub'] ?? null)
            ->first();

        if (!$user) {
            $user = User::create([
                'name' => $googleData['name'] ?? 'Google User',
                'email' => $email,
                'google_id' => $googleData['sub'] ?? null,
                'avatar' => $googleData['picture'] ?? null,
                'email_verified_at' => now(),
            ]);
        } else {
            $updatedData = [];
            if (empty($user->google_id) && isset($googleData['sub'])) {
                $updatedData['google_id'] = $googleData['sub'];
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

        Auth::login($user);
        $request->session()->regenerate();

        return response()->json([
            'role' => 'user',
            'user_status' => 'old',
            'redirect' => '/dashboard'
        ]);
    }

    public function showCompleteProfileForm()
    {
        return Inertia::render('Auth/CompleteProfile');
    }

    public function completeProfile(Request $request)
    {
        $request->validate([
            'name' => ['required', 'string'],
            'email' => ['required', 'string', 'email'],
            'nik' => ['required', 'string', 'max:16'],
            'phone_number' => ['required', 'string', 'max:15'],
            'emergency_contact' => ['required', 'string', 'max:15'],
            'address' => ['required', 'string'],
            'age' => ['required', 'integer']
        ]);

        $user = User::updateOrCreate(
            ['email' => $request->email],
            [
                'name' => $request->name,
                'nik' => $request->nik,
                'phone_number' => $request->phone_number,
                'emergency_contact' => $request->emergency_contact,
                'address' => $request->address,
                'age' => $request->age,
                'email_verified_at' => now()
            ]
        );

        Auth::login($user);
        $request->session()->regenerate();

        return response()->json([
            'success' => true,
            'redirect' => '/dashboard'
        ]);
    }
}
