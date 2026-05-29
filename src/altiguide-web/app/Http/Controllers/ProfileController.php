<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class ProfileController extends Controller
{
    /**
     * Update user profile details.
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
        ], [
            'nik.unique' => 'NIK ini sudah terdaftar di sistem kami.',
            'nik.size'   => 'NIK harus tepat 16 angka.',
        ]);

        $user->update($validated);

        return redirect()->back()->with('success', 'Profil Anda berhasil diperbarui.');
    }
}
