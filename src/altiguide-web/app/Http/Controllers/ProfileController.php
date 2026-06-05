<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Storage;
use Illuminate\Validation\Rules\Password;

class ProfileController extends Controller
{
    /**
     * Update user profile details (including avatar).
     */
    public function update(Request $request)
    {
        $user = Auth::user();

        $validated = $request->validate([
            'name'              => ['required', 'string', 'max:100'],
            'phone_number'      => ['required', 'string', 'max:15'],
            'age'               => ['required', 'integer', 'min:1', 'max:120'],
            'address'           => ['required', 'string'],
            'emergency_contact' => ['required', 'string', 'max:15'],
            'nik'               => ['required', 'string', 'size:16', 'unique:users,nik,' . $user->id],
            'avatar'            => ['nullable', 'image', 'mimes:jpeg,jpg,png,webp', 'max:2048'],
        ], [
            'nik.unique' => 'NIK ini sudah terdaftar di sistem kami.',
            'nik.size'   => 'NIK harus tepat 16 angka.',
            'avatar.image' => 'File harus berupa gambar.',
            'avatar.max'   => 'Ukuran foto maksimal 2MB.',
        ]);

        // Handle avatar upload
        if ($request->hasFile('avatar')) {
            // Hapus avatar lama jika ada
            if ($user->avatar && Storage::disk('public')->exists($user->avatar)) {
                Storage::disk('public')->delete($user->avatar);
            }
            $validated['avatar'] = $request->file('avatar')->store('avatars', 'public');
        } else {
            unset($validated['avatar']);
        }

        $user->update($validated);

        return redirect()->back()->with('success', 'Profil Anda berhasil diperbarui.');
    }

    /**
     * Change user password.
     */
    public function changePassword(Request $request)
    {
        $user = Auth::user();

        $request->validate([
            'current_password'      => ['required', 'string'],
            'password'              => ['required', 'string', 'min:8', 'confirmed', Password::defaults()],
            'password_confirmation' => ['required', 'string'],
        ], [
            'password.confirmed' => 'Konfirmasi password baru tidak cocok.',
            'password.min'       => 'Password baru minimal 8 karakter.',
        ]);

        // Verifikasi password lama
        if (!Hash::check($request->current_password, $user->password)) {
            return redirect()->back()->withErrors([
                'current_password' => 'Password lama yang Anda masukkan salah.',
            ])->withInput();
        }

        $user->update([
            'password' => Hash::make($request->password),
        ]);

        return redirect()->back()->with('success', 'Password Anda berhasil diubah.');
    }
}
