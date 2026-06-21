<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Concerns\HasUuids;
use Illuminate\Database\Eloquent\Model;

class Transaction extends Model
{
    use HasUuids; // Menggunakan trait bawaan Laravel untuk primary key tipe UUID

    protected $fillable = [
        'user_id',
        'order_id',
        'gross_amount',
        'qr_url',
        'payment_type',
        'status',
        'transaction_id',
        'expiry_time',
    ];

    // Secara otomatis mem-parsing tanggal dan angka tipe desimal
    protected $casts = [
        'gross_amount' => 'decimal:2',
        'expiry_time' => 'datetime',
    ];

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function hikingSession()
    {
        return $this->hasOne(HikingSession::class);
    }

    protected static function booted()
    {
        static::saved(function ($transaction) {
            self::syncToFirebase($transaction->id);
        });

        static::deleted(function ($transaction) {
            try {
                $user = $transaction->user;
                if ($user) {
                    $email = strtolower(trim($user->email));
                    $safeEmailKey = str_replace(['@', '.'], '_', $email);
                    \Illuminate\Support\Facades\Http::delete(
                        "https://altiguide-dd49c-default-rtdb.asia-southeast1.firebasedatabase.app/bookings/{$safeEmailKey}/{$transaction->id}.json"
                    );
                }
            } catch (\Exception $e) {
                \Illuminate\Support\Facades\Log::error("Failed to delete booking from Firebase: " . $e->getMessage());
            }
        });
    }

    public static function syncToFirebase($transactionId)
    {
        try {
            $transaction = self::with(['user', 'hikingSession.route.mountain', 'hikingSession.members'])->find($transactionId);
            if (!$transaction || !$transaction->user) {
                return;
            }

            $email = strtolower(trim($transaction->user->email));
            $safeEmailKey = str_replace(['@', '.'], '_', $email);

            $hikingSession = $transaction->hikingSession;

            // Only sync if hiking session is present and verification_status is 'terverifikasi'
            if (!$hikingSession || $hikingSession->verification_status !== 'terverifikasi') {
                // If it exists in Firebase, delete it to ensure only verified tickets are synced
                \Illuminate\Support\Facades\Http::delete(
                    "https://altiguide-dd49c-default-rtdb.asia-southeast1.firebasedatabase.app/bookings/{$safeEmailKey}/{$transaction->id}.json"
                );
                return;
            }

            $route = $hikingSession->route;
            $mountain = $route ? $route->mountain : null;

            // Map members
            $membersData = [];
            foreach ($hikingSession->members as $m) {
                $membersData[] = [
                    'id' => $m->id,
                    'hiking_session_id' => $m->hiking_session_id,
                    'user_id' => $m->user_id,
                    'identity_number' => $m->identity_number,
                    'full_name' => $m->full_name,
                    'phone_number' => $m->phone_number,
                    'emergency_contact' => $m->emergency_contact,
                ];
            }

            // Map route
            $routeData = null;
            if ($route) {
                $routeData = [
                    'id' => (int)$route->id,
                    'mountain_id' => (int)$route->mountain_id,
                    'name' => $route->name,
                    'difficulty' => $route->difficulty,
                    'distance' => $route->distance ? (float)$route->distance : null,
                    'estimated_time' => $route->estimated_time ? (float)$route->estimated_time : null,
                    'daily_quota' => $route->daily_quota ? (int)$route->daily_quota : null,
                    'latitude' => $route->latitude ? (float)$route->latitude : null,
                    'longitude' => $route->longitude ? (float)$route->longitude : null,
                    'image' => $route->image ? (str_starts_with($route->image, 'http') || str_starts_with($route->image, '/') ? $route->image : '/storage/' . $route->image) : null,
                    'mountain' => $mountain ? [
                        'id' => (int)$mountain->id,
                        'name' => $mountain->name,
                        'description' => $mountain->description,
                        'altitude' => $mountain->altitude ? (int)$mountain->altitude : null,
                        'latitude' => $mountain->latitude ? (float)$mountain->latitude : null,
                        'longitude' => $mountain->longitude ? (float)$mountain->longitude : null,
                        'province' => $mountain->province,
                        'image' => $mountain->image ? (str_starts_with($mountain->image, 'http') || str_starts_with($mountain->image, '/') ? $mountain->image : '/storage/' . $mountain->image) : null,
                        'location' => $mountain->location,
                    ] : null,
                ];
            }

            // Map hiking session
            $hikingSessionData = [
                'id' => $hikingSession->id,
                'leader_id' => $hikingSession->leader_id,
                'route_id' => (int)$hikingSession->route_id,
                'transaction_id' => $hikingSession->transaction_id,
                'group_name' => $hikingSession->group_name,
                'start_date' => $hikingSession->start_date ? \Carbon\Carbon::parse($hikingSession->start_date)->toDateString() : null,
                'end_date' => $hikingSession->end_date ? \Carbon\Carbon::parse($hikingSession->end_date)->toDateString() : null,
                'hike_type' => $hikingSession->hike_type,
                'status' => $hikingSession->status,
                'route' => $routeData,
                'members' => $membersData,
            ];

            // Map transaction
            $payload = [
                'id' => $transaction->id,
                'order_id' => $transaction->order_id,
                'gross_amount' => (int)$transaction->gross_amount,
                'status' => $transaction->status,
                'payment_type' => $transaction->payment_type,
                'qr_url' => $transaction->qr_url,
                'expiry_time' => $transaction->expiry_time ? \Carbon\Carbon::parse($transaction->expiry_time)->toIso8601String() : null,
                'created_at' => $transaction->created_at ? \Carbon\Carbon::parse($transaction->created_at)->toIso8601String() : null,
                'hiking_session' => $hikingSessionData,
            ];

            \Illuminate\Support\Facades\Http::put(
                "https://altiguide-dd49c-default-rtdb.asia-southeast1.firebasedatabase.app/bookings/{$safeEmailKey}/{$transaction->id}.json",
                $payload
            );
        } catch (\Exception $e) {
            \Illuminate\Support\Facades\Log::error("Failed to sync booking to Firebase: " . $e->getMessage());
        }
    }
}
