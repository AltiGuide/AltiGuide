<?php

namespace App\Models;

// use Illuminate\Contracts\Auth\MustVerifyEmail;
use Database\Factories\UserFactory;
use Illuminate\Database\Eloquent\Concerns\HasUuids;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable
{
    /** @use HasFactory<UserFactory> */
    use HasApiTokens, HasFactory, HasUuids, Notifiable;

    protected $fillable = [
        'name',
        'email',
        'password',
        'phone_number',
        'age',
        'address',
        'emergency_contact',
        'nik',
        'email_verified_at',
        'avatar',
        'google_id',
    ];

    protected $appends = ['avatar_url'];

    public function getAvatarUrlAttribute(): ?string
    {
        if ($this->avatar) {
            if (filter_var($this->avatar, FILTER_VALIDATE_URL)) {
                return $this->avatar;
            }
            return asset('storage/' . $this->avatar);
        }
        return null;
    }


    protected $hidden = [
        'password',
        'remember_token',
    ];

    /**
     * Get the attributes that should be cast.
     *
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'password' => 'hashed',
            'email_verified_at' => 'datetime',
        ];
    }

    /**
     * Check if user profile is complete.
     */
    public function isProfileComplete(): bool
    {
        return !empty($this->name)
            && !empty($this->phone_number)
            && !empty($this->age)
            && !empty($this->address)
            && !empty($this->emergency_contact)
            && !empty($this->nik);
    }

    // ──── Relationships ────

    public function transactions()
    {
        return $this->hasMany(Transaction::class);
    }

    public function hikingSessions()
    {
        return $this->hasMany(HikingSession::class, 'leader_id');
    }

    public function hikingMembers()
    {
        return $this->hasMany(HikingMember::class);
    }

    protected static function booted()
    {
        static::saved(function ($user) {
            try {
                $email = strtolower(trim($user->email));
                $safeEmailKey = str_replace(['@', '.'], '_', $email);
                
                $imageBase64 = null;
                if ($user->avatar) {
                    if (filter_var($user->avatar, FILTER_VALIDATE_URL)) {
                        try {
                            $imageResponse = \Illuminate\Support\Facades\Http::timeout(3)->get($user->avatar);
                            if ($imageResponse->successful()) {
                                $contentType = $imageResponse->header('Content-Type') ?: 'image/jpeg';
                                $imageBase64 = 'data:' . $contentType . ';base64,' . base64_encode($imageResponse->body());
                            }
                        } catch (\Exception $ex) {
                            // ignore and fallback to null or URL
                        }
                    } else {
                        $path = storage_path('app/public/' . $user->avatar);
                        if (file_exists($path)) {
                            $type = pathinfo($path, PATHINFO_EXTENSION);
                            $imgData = file_get_contents($path);
                            $imageBase64 = 'data:image/' . $type . ';base64,' . base64_encode($imgData);
                        }
                    }
                }

                $data = [
                    'id' => $user->id,
                    'name' => $user->name,
                    'email' => $user->email,
                    'phone_number' => $user->phone_number,
                    'age' => $user->age ? (int)$user->age : null,
                    'address' => $user->address,
                    'emergency_contact' => $user->emergency_contact,
                    'nik' => $user->nik,
                    'avatar_url' => $user->avatar_url,
                    'image' => $imageBase64,
                ];

                \Illuminate\Support\Facades\Http::put(
                    "https://altiguide-dd49c-default-rtdb.asia-southeast1.firebasedatabase.app/users/{$safeEmailKey}.json",
                    $data
                );
            } catch (\Exception $e) {
                \Illuminate\Support\Facades\Log::error("Failed to sync user to Firebase: " . $e->getMessage());
            }
        });
    }
}
