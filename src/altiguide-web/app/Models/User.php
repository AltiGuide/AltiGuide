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
    ];

    protected $appends = ['avatar_url'];

    public function getAvatarUrlAttribute(): ?string
    {
        if ($this->avatar) {
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
}
