<?php

namespace App\Http\Controllers;

use App\Models\Mountain;
use App\Models\Route;
use App\Models\Transaction;
use App\Models\HikingSession;
use App\Models\HikingMember;
use App\Jobs\SendETicketJob;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Carbon\Carbon;
use Inertia\Inertia;
use Midtrans\Config;
use Midtrans\Snap;
use Midtrans\Transaction as MidtransTransaction;
use Illuminate\Support\Facades\Log;

class BookingController extends Controller
{
    public function __construct()
    {
        // Konfigurasi dasar Midtrans
        Config::$serverKey = env('MIDTRANS_SERVER_KEY', 'SB-Mid-server-YOUR_KEY_HERE'); 
        Config::$isProduction = env('MIDTRANS_IS_PRODUCTION', false);
        Config::$isSanitized = true;
        Config::$is3ds = true;
    }

    /**
     * Tampilkan form booking pendakian.
     */
    public function create()
    {
        $user = Auth::user();
        if (!$user->isProfileComplete()) {
            return redirect()->route('dashboard')
                ->with('warning', 'Silakan lengkapi profil Anda terlebih dahulu sebelum melakukan booking pendakian.');
        }

        // Ambil gunung beserta rute aktifnya
        $mountains = Mountain::with(['routes' => function ($query) {
            $query->where('is_active', true)->with('routeInfo');
        }])->get();

        return Inertia::render('Booking', [
            'mountains' => $mountains
        ]);
    }

    /**
     * Proses pembuatan pesanan SIMAKSI (Checkout) dari web.
     */
    public function store(Request $request)
    {
        $user = $request->user();
        if (!$user->isProfileComplete()) {
            return response()->json([
                'status'  => 'error',
                'message' => 'Silakan lengkapi profil Anda terlebih dahulu sebelum melakukan booking pendakian.'
            ], 403);
        }

        $validated = $request->validate([
            'route_id'                     => 'required|exists:routes,id',
            'start_date'                   => 'required|date|after_or_equal:today',
            'hike_type'                    => 'required|in:tektok,camp',
            'group_name'                   => 'required|string|max:100',
            'members'                      => 'required|array|min:1|max:10',
            'members.*.user_id'            => 'required|exists:users,id',
            'members.*.identity_number'    => 'required|string|size:16',
            'members.*.full_name'          => 'required|string|max:100',
            'members.*.phone_number'       => 'required|string|max:15',
            'members.*.emergency_contact'  => 'required|string|max:15',
        ]);

        $route = Route::with(['routeInfo', 'mountain'])->findOrFail($request->route_id);

        $totalMembers = count($request->members);

        // Verifikasi aturan jumlah anggota (Solo Hiking vs Kelompok)
        $minMembers = $route->mountain->min_members ?? 1;
        if ($totalMembers < $minMembers) {
            return response()->json([
                'status'  => 'error',
                'message' => "Gunung {$route->mountain->name} tidak memperbolehkan solo hiking. Jumlah anggota minimal untuk pendakian ini adalah {$minMembers} orang (termasuk ketua).",
            ], 422);
        }

        $startDate = Carbon::parse($request->start_date)->startOfDay();
        
        $endDate = ($request->hike_type === 'camp') 
                   ? $startDate->copy()->addDay()->endOfDay() 
                   : $startDate->copy()->endOfDay();

        // Pengecekan Kuota
        $bookedCount = HikingMember::whereHas('hikingSession', function($query) use ($route, $startDate) {
            $query->where('route_id', $route->id)
                  ->whereDate('start_date', $startDate)
                  ->whereHas('transaction', function($qTx) {
                      $qTx->whereIn('status', ['pending', 'settlement']);
                  });
        })->count();

        $remainingQuota = $route->daily_quota - $bookedCount;

        if ($remainingQuota < $totalMembers) {
            return response()->json([
                'status'  => 'error',
                'message' => 'Maaf, sisa kuota untuk rute pada tanggal tersebut tidak mencukupi.',
            ], 422);
        }

        // Kalkulasi Biaya
        $simaksiPrice = $route->routeInfo->simaksi_price ?? 0;
        $applicationFee = 5000;
        $grossAmount = ($simaksiPrice * $totalMembers) + ($applicationFee * $totalMembers);

        DB::beginTransaction();

        try {
            $orderId = 'ALT-' . date('Ymd') . '-' . strtoupper(Str::random(6));

            // Siapkan Parameter Midtrans
            $midtransParams = [
                'transaction_details' => [
                    'order_id'     => $orderId,
                    'gross_amount' => $grossAmount,
                ],
                'customer_details' => [
                    'first_name' => $request->user()->name,
                    'email'      => $request->user()->email,
                    'phone'      => $request->user()->phone_number,
                ],
                'item_details' => [
                    [
                        'id'       => 'TIKET',
                        'price'    => $simaksiPrice,
                        'quantity' => $totalMembers,
                        'name'     => 'Simaksi ' . $route->mountain->name,
                    ],
                    [
                        'id'       => 'FEE',
                        'price'    => $applicationFee,
                        'quantity' => $totalMembers,
                        'name'     => 'Biaya Layanan AltiGuide',
                    ]
                ],
                'enabled_payments' => ['gopay', 'shopeepay', 'other_qris'], 
                'callbacks' => [
                    'finish' => route('dashboard'),
                ],
            ];

            // Panggil API Midtrans untuk dapatkan Snap Payment URL
            $snapTransaction = Snap::createTransaction($midtransParams);
            $paymentUrl = $snapTransaction->redirect_url;
            $snapToken = $snapTransaction->token;

            $transaction = Transaction::create([
                'user_id'      => $request->user()->id,
                'order_id'     => $orderId,
                'gross_amount' => $grossAmount,
                'status'       => 'pending',
                'payment_type' => 'qris',
                'qr_url'       => $paymentUrl,
                'expiry_time'  => Carbon::now()->addHour()
            ]);

            $hikingSession = HikingSession::create([
                'leader_id'      => $request->user()->id,
                'route_id'       => $route->id,
                'transaction_id' => $transaction->id,
                'group_name'     => $request->group_name,
                'start_date'     => $startDate,
                'end_date'       => $endDate,
                'hike_type'      => $request->hike_type,
                'status'         => 'prepared'
            ]);

            foreach ($request->members as $memberData) {
                HikingMember::create([
                    'hiking_session_id' => $hikingSession->id,
                    'user_id'           => $memberData['user_id'],
                    'identity_number'   => $memberData['identity_number'],
                    'full_name'         => $memberData['full_name'],
                    'phone_number'      => $memberData['phone_number'],
                    'emergency_contact' => $memberData['emergency_contact']
                ]);
            }

            DB::commit();

            return response()->json([
                'status'  => 'success',
                'message' => 'Pesanan berhasil dibuat. Lakukan pembayaran via QRIS.',
                'data'    => [
                    'order_id'     => $transaction->order_id,
                    'gross_amount' => $transaction->gross_amount,
                    'payment_url'  => $paymentUrl,
                    'snap_token'   => $snapToken,
                    'expiry_time'  => $transaction->expiry_time,
                ]
            ], 201);

        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json([
                'status'  => 'error',
                'message' => 'Terjadi kesalahan sistem saat menghubungi Midtrans.',
                'error'   => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Kalkulasi preview biaya booking tanpa membuat transaksi.
     */
    public function calculatePrice(Request $request)
    {
        $request->validate([
            'route_id'     => 'required|exists:routes,id',
            'member_count' => 'required|integer|min:1|max:10',
        ]);

        $route = Route::with(['routeInfo', 'mountain'])->findOrFail($request->route_id);
        $simaksiPrice  = $route->routeInfo->simaksi_price ?? 0;
        $applicationFee = 5000;
        $memberCount    = (int) $request->member_count;

        return response()->json([
            'simaksi_price'    => $simaksiPrice,
            'application_fee'  => $applicationFee,
            'member_count'     => $memberCount,
            'subtotal_simaksi' => $simaksiPrice * $memberCount,
            'subtotal_fee'     => $applicationFee * $memberCount,
            'total'            => ($simaksiPrice + $applicationFee) * $memberCount,
        ]);
    }

    /**
     * Cek status pembayaran untuk polling dari frontend.
     */
    public function checkStatus($orderId)
    {
        $transaction = Transaction::where('order_id', $orderId)
            ->where('user_id', Auth::id())
            ->first();

        if (!$transaction) {
            return response()->json([
                'status'  => 'error',
                'message' => 'Transaksi tidak ditemukan.',
            ], 404);
        }

        // Sinkronisasi status pending secara real-time dengan Midtrans API
        if ($transaction->status === 'pending') {
            try {
                Config::$serverKey = env('MIDTRANS_SERVER_KEY', 'SB-Mid-server-YOUR_KEY_HERE');
                Config::$isProduction = env('MIDTRANS_IS_PRODUCTION', false);

                $statusResponse = MidtransTransaction::status($orderId);
                $midtransStatus = $statusResponse->transaction_status ?? null;

                if ($midtransStatus === 'settlement' || $midtransStatus === 'capture') {
                    $transaction->status = 'settlement';
                    $transaction->save();
                    dispatch(new \App\Jobs\SendETicketJob($transaction));
                } elseif (in_array($midtransStatus, ['expire', 'cancel', 'deny'])) {
                    $transaction->status = 'expire';
                    $transaction->save();
                }
            } catch (\Exception $e) {
                Log::warning("Gagal memperbarui status Midtrans untuk order_id: {$orderId}. Error: " . $e->getMessage());
            }
        }

        return response()->json([
            'status'      => $transaction->status,
            'order_id'    => $transaction->order_id,
            'expiry_time' => $transaction->expiry_time,
        ]);
    }

    /**
     * Tampilkan halaman pembayaran ulang untuk transaksi pending.
     */
    public function repay($orderId)
    {
        $transaction = Transaction::where('order_id', $orderId)
            ->where('user_id', Auth::id())
            ->where('status', 'pending')
            ->firstOrFail();

        $session = HikingSession::where('transaction_id', $transaction->id)
            ->with(['route.mountain', 'members'])
            ->firstOrFail();

        $mountains = Mountain::with(['routes' => function ($query) {
            $query->where('is_active', true)->with('routeInfo');
        }])->get();

        $urlParts = explode('/', $transaction->qr_url);
        $snapToken = end($urlParts);

        return Inertia::render('Booking', [
            'mountains' => $mountains,
            'preloadedPayment' => [
                'orderId'          => $transaction->order_id,
                'snapToken'         => $snapToken,
                'paymentUrl'        => $transaction->qr_url,
                'grossAmount'       => (float) $transaction->gross_amount,
                'expiryTime'        => $transaction->expiry_time ? $transaction->expiry_time->toIso8601String() : null,
                'selectedMountain'  => $session->route->mountain,
                'selectedRoute'     => $session->route,
                'groupName'         => $session->group_name,
                'startDate'         => $session->start_date ? \Carbon\Carbon::parse($session->start_date)->toDateString() : null,
                'endDate'           => $session->end_date ? \Carbon\Carbon::parse($session->end_date)->toDateString() : null,
                'memberCount'       => $session->members->count(),
                'members'           => $session->members->map(function($m) {
                    return [
                        'user_id' => $m->user_id,
                        'identity_number' => $m->identity_number,
                        'full_name' => $m->full_name,
                        'phone_number' => $m->phone_number,
                        'emergency_contact' => $m->emergency_contact,
                    ];
                })->toArray(),
            ]
        ]);
    }
}
