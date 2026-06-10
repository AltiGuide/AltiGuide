<?php

use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Route;
use Inertia\Inertia;
use Midtrans\Config as MidtransConfig;
use Midtrans\Transaction as MidtransTransaction;

use App\Http\Controllers\Auth\LoginController;
use App\Http\Controllers\Auth\RegisterController;
use App\Http\Controllers\Auth\AdminLoginController;
use App\Http\Controllers\Auth\GoogleAuthController;

/*
|--------------------------------------------------------------------------
| Web Routes — AltiGuide
|--------------------------------------------------------------------------
*/

// ── Halaman publik ──────────────────────────────────────────────────────

Route::get('/', function () {
    return Inertia::render('Welcome');
})->name('home');

Route::get('/article', [\App\Http\Controllers\ArticleController::class, 'show'])->name('article.show');
Route::get('/mountains', [\App\Http\Controllers\MountainListController::class, 'index'])->name('mountains.index');
Route::get('/about', function () {
    return \Inertia\Inertia::render('About');
})->name('about');

Route::get('/tata-tertib', function () {
    return \Inertia\Inertia::render('TataTertib');
})->name('tata-tertib');

Route::get('/tips-keamanan', function () {
    return \Inertia\Inertia::render('TipsKeamanan');
})->name('tips-keamanan');

Route::get('/weather-analytics', [\App\Http\Controllers\WeatherAnalyticsController::class, 'index'])->name('weather-analytics');

// ── Guest routes (hanya bisa diakses kalau BELUM login) ─────────────────

Route::middleware('guest')->group(function () {
    Route::get('/login',     [LoginController::class, 'create'])->name('login');
    Route::post('/login',    [LoginController::class, 'store']);
    Route::get('/register',  [RegisterController::class, 'create'])->name('register');
    Route::post('/register', [RegisterController::class, 'store']);

    // Route Verifikasi OTP Pendaftaran
    Route::get('/register/verify',           [RegisterController::class, 'showVerifyForm'])->name('register.verify');
    Route::post('/register/verify',          [RegisterController::class, 'verifyOtp'])->name('register.verify.post');
    Route::post('/register/verify/resend',   [RegisterController::class, 'resendOtp'])->name('register.verify.resend');

    Route::post('/auth/google', [GoogleAuthController::class, 'handleGoogleLogin']);
    Route::get('/complete-profile', [GoogleAuthController::class, 'showCompleteProfileForm'])->name('complete-profile');
    Route::post('/complete-profile', [GoogleAuthController::class, 'completeProfile']);
});

// Route untuk flow Forgot Password (accessible by guests & auth users)
Route::get('/forgot-password',           [\App\Http\Controllers\Auth\PasswordResetController::class, 'showLinkRequestForm'])->name('password.request');
Route::post('/forgot-password/email',    [\App\Http\Controllers\Auth\PasswordResetController::class, 'sendResetCodeEmail'])->name('password.email');

// Route Verifikasi Kode OTP
Route::get('/forgot-password/verify',    [\App\Http\Controllers\Auth\PasswordResetController::class, 'showVerifyCodeForm'])->name('password.verify');
Route::post('/forgot-password/verify',   [\App\Http\Controllers\Auth\PasswordResetController::class, 'verifyResetCode'])->name('password.verify.post');

// Route Reset Password (setelah kode OTP divalidasi)
Route::get('/reset-password',            [\App\Http\Controllers\Auth\PasswordResetController::class, 'showResetForm'])->name('password.reset');
Route::post('/reset-password',           [\App\Http\Controllers\Auth\PasswordResetController::class, 'resetPassword'])->name('password.update');

// ── User protected routes (harus login sebagai User) ────────────────────

Route::middleware('auth')->group(function () {
    Route::post('/logout', [LoginController::class, 'destroy'])->name('logout');

    Route::get('/dashboard', function (\Illuminate\Http\Request $request) {
        $user = Auth::user();

        // Otomatis ubah status pembayaran di database jika menerima redirect sukses dari Midtrans (sangat berguna untuk demo lokal)
        if ($request->has('order_id') && $request->has('transaction_status')) {
            $orderId = $request->input('order_id');
            $status = $request->input('transaction_status');
            
            if ($status === 'settlement' || $status === 'capture') {
                $transaction = \App\Models\Transaction::where('order_id', $orderId)
                    ->where('user_id', $user->id)
                    ->first();
                    
                if ($transaction && $transaction->status === 'pending') {
                    $transaction->status = 'settlement';
                    $transaction->save();
                    
                    // Kirim E-Ticket secara sinkron/antrean
                    dispatch(new \App\Jobs\SendETicketJob($transaction));
                }
            }
        }

        // Ambil semua transaksi user beserta relasi hiking session, route, dan mountain
        $transactions = $user->transactions()
            ->with(['hikingSession.route.mountain', 'hikingSession.members', 'hikingSession.leader'])
            ->orderByDesc('created_at')
            ->get();

        // Cek status pending ke Midtrans secara real-time untuk sinkronisasi database lokal
        $hasPending = $transactions->contains(fn($tx) => $tx->status === 'pending');
        if ($hasPending) {
            try {
                MidtransConfig::$serverKey = env('MIDTRANS_SERVER_KEY', 'SB-Mid-server-YOUR_KEY_HERE');
                MidtransConfig::$isProduction = env('MIDTRANS_IS_PRODUCTION', false);

                foreach ($transactions as $tx) {
                    if ($tx->status === 'pending') {
                        try {
                            $statusResponse = MidtransTransaction::status($tx->order_id);
                            $midtransStatus = $statusResponse->transaction_status ?? null;

                            if ($midtransStatus === 'settlement' || $midtransStatus === 'capture') {
                                $tx->status = 'settlement';
                                $tx->save();
                                dispatch(new \App\Jobs\SendETicketJob($tx));
                            } elseif (in_array($midtransStatus, ['expire', 'cancel', 'deny'])) {
                                $tx->status = 'expire';
                                $tx->save();
                            }
                        } catch (\Exception $e) {
                            // Abaikan error individual per order_id agar tidak mengganggu loading dashboard
                        }
                    }
                }
            } catch (\Exception $e) {
                // Abaikan error setup
            }
        }

        $mappedTransactions = $transactions->map(function ($tx) {
            $session = $tx->hikingSession;
            return [
                'id'           => $tx->id,
                'order_id'     => $tx->order_id,
                'status'       => $tx->status,
                'gross_amount' => $tx->gross_amount,
                'expiry_time'  => $tx->expiry_time,
                'mountain_name'=> $session?->route?->mountain?->name,
                'route_name'   => $session?->route?->name,
                'start_date'   => $session?->start_date,
                'end_date'     => $session?->end_date,
                'member_count' => $session?->members?->count() ?? 0,
                'group_name'   => $session?->group_name,
                'hike_type'    => $session?->hike_type,
                'leader'       => $session?->leader ? [
                    'name'  => $session->leader->name,
                    'email' => $session->leader->email,
                    'phone' => $session->leader->phone_number,
                    'nik'   => $session->leader->nik,
                ] : null,
                'members'      => $session?->members->map(function ($m) {
                    return [
                        'full_name'       => $m->full_name,
                        'identity_number' => $m->identity_number,
                        'phone_number'    => $m->phone_number,
                        'emergency_contact' => $m->emergency_contact,
                    ];
                }) ?? [],
            ];
        });

        return Inertia::render('Dashboard', [
            'bookings' => $mappedTransactions,
        ]);
    })->name('dashboard');

    // Profile update (with avatar)
    Route::post('/profile/update', [\App\Http\Controllers\ProfileController::class, 'update'])->name('profile.update');

    // Password change
    Route::post('/profile/password', [\App\Http\Controllers\ProfileController::class, 'changePassword'])->name('profile.password');

    // Booking routes
    Route::get('/booking', [\App\Http\Controllers\BookingController::class, 'create'])->name('booking');
    Route::post('/booking/checkout', [\App\Http\Controllers\BookingController::class, 'store'])->name('booking.checkout');
    Route::post('/booking/calculate', [\App\Http\Controllers\BookingController::class, 'calculatePrice'])->name('booking.calculate');
    Route::get('/booking/status/{order_id}', [\App\Http\Controllers\BookingController::class, 'checkStatus'])->name('booking.status');
    Route::get('/booking/pay/{order_id}', [\App\Http\Controllers\BookingController::class, 'repay'])->name('booking.repay');
    Route::post('/booking/validate-nik', [\App\Http\Controllers\Api\MemberValidationController::class, 'validateNik'])->name('booking.validate-nik');
});

// ── Admin routes ────────────────────────────────────────────────────────

Route::prefix('admin')->group(function () {

    // Guest admin (belum login sebagai admin)
    Route::middleware('guest:admin')->group(function () {
        Route::get('/login',  [AdminLoginController::class, 'create'])->name('admin.login');
        Route::post('/login', [AdminLoginController::class, 'store']);
    });

    // Protected admin (harus login sebagai admin + middleware is_admin)
    Route::middleware(['auth:admin', 'is_admin'])->group(function () {
        Route::post('/logout', [AdminLoginController::class, 'destroy'])->name('admin.logout');

        Route::get('/dashboard', function () {
            return Inertia::render('Admin/Dashboard');
        })->name('admin.dashboard');

        // CMS Manajemen Gunung (Mountain CRUD)
        Route::resource('mountains', \App\Http\Controllers\Admin\MountainController::class)->names([
            'index'   => 'admin.mountains.index',
            'create'  => 'admin.mountains.create',
            'store'   => 'admin.mountains.store',
            'show'    => 'admin.mountains.show',
            'edit'    => 'admin.mountains.edit',
            'update'  => 'admin.mountains.update',
            'destroy' => 'admin.mountains.destroy',
        ]);

        // CMS Manajemen Rute (Rute merupakan child dari Gunung)
        Route::prefix('mountains/{mountain}')->group(function () {
            Route::get('/routes/create', [\App\Http\Controllers\Admin\RouteController::class, 'create'])->name('admin.routes.create');
            Route::post('/routes', [\App\Http\Controllers\Admin\RouteController::class, 'store'])->name('admin.routes.store');
            Route::get('/routes/{route}/edit', [\App\Http\Controllers\Admin\RouteController::class, 'edit'])->name('admin.routes.edit');
            Route::put('/routes/{route}', [\App\Http\Controllers\Admin\RouteController::class, 'update'])->name('admin.routes.update');
            Route::delete('/routes/{route}', [\App\Http\Controllers\Admin\RouteController::class, 'destroy'])->name('admin.routes.destroy');
        });

        // CMS Route Info & Route Waypoints (Child dari Rute)
        Route::prefix('routes/{route}')->group(function () {
            // Informasi Rute (1:1 Relation dengan Rute)
            Route::get('/info/edit', [\App\Http\Controllers\Admin\RouteInfoController::class, 'edit'])->name('admin.routes.info.edit');
            Route::put('/info', [\App\Http\Controllers\Admin\RouteInfoController::class, 'update'])->name('admin.routes.info.update');
            
            // Titik Singgah / Pos / Waypoints (1:M Relation dengan Rute)
            Route::get('/waypoints', [\App\Http\Controllers\Admin\RouteWaypointController::class, 'index'])->name('admin.routes.waypoints.index');
            Route::get('/waypoints/create', [\App\Http\Controllers\Admin\RouteWaypointController::class, 'create'])->name('admin.routes.waypoints.create');
            Route::post('/waypoints', [\App\Http\Controllers\Admin\RouteWaypointController::class, 'store'])->name('admin.routes.waypoints.store');
            Route::get('/waypoints/{waypoint}/edit', [\App\Http\Controllers\Admin\RouteWaypointController::class, 'edit'])->name('admin.routes.waypoints.edit');
            Route::put('/waypoints/{waypoint}', [\App\Http\Controllers\Admin\RouteWaypointController::class, 'update'])->name('admin.routes.waypoints.update');
            Route::delete('/waypoints/{waypoint}', [\App\Http\Controllers\Admin\RouteWaypointController::class, 'destroy'])->name('admin.routes.waypoints.destroy');
        });
    });
});
