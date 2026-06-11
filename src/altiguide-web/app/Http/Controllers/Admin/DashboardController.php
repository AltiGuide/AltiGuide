<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Article;
use App\Models\HikingSession;
use App\Models\Mountain;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Inertia\Inertia;

class DashboardController extends Controller
{
    public function index()
    {
        $today = Carbon::today();

        // ── Stats ─────────────────────────────────────────────────────────
        $totalRombongan = HikingSession::whereHas('transaction', fn ($q) =>
            $q->whereIn('status', ['settlement', 'pending'])
        )->count();

        // Pendaki yang sedang di gunung (seluruh rombongan pada hari ini dengan status settlement/Lunas dan belum check-out)
        $pendakiDiGunung = HikingSession::whereDate('start_date', '<=', $today)
            ->whereDate('end_date', '>=', $today)
            ->where('status', '!=', 'finished')
            ->whereHas('transaction', fn ($q) => $q->where('status', 'settlement'))
            ->withCount('members')
            ->get()
            ->sum('members_count');

        // Check-in hari ini (start_date = hari ini, sudah bayar)
        $checkInHariIni = HikingSession::whereDate('start_date', $today)
            ->whereHas('transaction', fn ($q) => $q->where('status', 'settlement'))
            ->count();

        // Check-out hari ini (status finished, updated_at = hari ini)
        $checkOutHariIni = HikingSession::whereDate('updated_at', $today)
            ->where('status', 'finished')
            ->count();

        $stats = [
            'totalRombongan'  => $totalRombongan,
            'pendakiDiGunung' => $pendakiDiGunung,
            'checkInHariIni'  => $checkInHariIni,
            'checkOutHariIni' => $checkOutHariIni,
        ];

        // ── Bookings ──────────────────────────────────────────────────────
        $bookings = HikingSession::with([
            'transaction',
            'route.mountain',
            'route.routeInfo',
            'leader',
            'members',
        ])
            ->orderByDesc('created_at')
            ->get()
            ->map(function ($session) {
                $tx    = $session->transaction;
                $route = $session->route;

                return [
                    'id'                  => $session->id,
                    'order_id'            => $tx?->order_id ?? '-',
                    'group_name'          => $session->group_name,
                    'mountain'            => $route?->mountain?->name ?? '-',
                    'route'               => $route?->name ?? '-',
                    'start_date'          => $session->start_date ? $session->start_date->toDateString() : null,
                    'end_date'            => $session->end_date   ? $session->end_date->toDateString()   : null,
                    'hike_type'           => $session->hike_type,
                    'status'              => $session->status,
                    'updated_at'          => $session->updated_at ? $session->updated_at->toDateString() : null,
                    'member_count'        => $session->members->count(),
                    'payment_status'      => $tx?->status ?? 'pending',
                    'gross_amount'        => $tx ? (float) $tx->gross_amount : 0,
                    'daily_quota'         => $route?->daily_quota ?? 70,
                    'verification_status' => $session->verification_status ?? 'pending_review',
                    'members'             => $session->members->map(fn ($m) => [
                        'full_name'         => $m->full_name,
                        'identity_number'   => $m->identity_number,
                        'phone_number'      => $m->phone_number,
                        'emergency_contact' => $m->emergency_contact,
                    ])->values(),
                    'leader' => $session->leader ? [
                        'name'  => $session->leader->name,
                        'email' => $session->leader->email,
                        'phone' => $session->leader->phone_number,
                    ] : null,
                ];
            });

        // ── Mountains with content (for Articles panel) ──────────────────
        $mountainsWithContent = Mountain::with(['routes.routeInfo'])
            ->orderBy('name')
            ->get()
            ->map(fn ($m) => [
                'id'          => $m->id,
                'name'        => $m->name,
                'slug'        => $m->slug,
                'image'       => $m->image ? (str_starts_with($m->image, 'http') || str_starts_with($m->image, '/') ? $m->image : '/storage/' . $m->image) : null,
                'altitude'    => $m->altitude,
                'location'    => $m->location,
                'description' => $m->description,
                'content'     => $m->content ?? [],   // array [{title?, text}]
                'routes'      => $m->routes->map(fn ($r) => [
                    'id'   => $r->id,
                    'name' => $r->name,
                    'route_info' => $r->routeInfo ? [
                        'basecamp_address'       => $r->routeInfo->basecamp_address,
                        'basecamp_altitude'      => $r->routeInfo->basecamp_altitude,
                        'simaksi_price'          => $r->routeInfo->simaksi_price,
                        'ojek_price'             => $r->routeInfo->ojek_price,
                        'ojek_description'       => $r->routeInfo->ojek_description,
                        'facilities_description' => $r->routeInfo->facilities_description,
                        'logistics_description'  => $r->routeInfo->logistics_description,
                    ] : null,
                ])->values(),
            ]);

        // ── Mountains (for weather panel) ─────────────────────────────────
        $mountains = Mountain::select('id', 'name', 'latitude', 'longitude')
            ->orderBy('name')
            ->get();

        return Inertia::render('Admin/Dashboard', [
            'bookings'             => $bookings,
            'stats'                => $stats,
            'mountainsWithContent' => $mountainsWithContent,
            'mountains'            => $mountains,
            'adminName'            => auth('admin')->user()?->name ?? 'Admin',
        ]);
    }

    /**
     * Update status verifikasi dokumen sebuah booking.
     */
    public function updateVerification(Request $request, $id)
    {
        $request->validate([
            'verification_status' => 'required|in:pending_review,terverifikasi,review,revisi',
        ]);

        $session = HikingSession::findOrFail($id);
        $session->update(['verification_status' => $request->verification_status]);

        return back()->with('success', 'Status verifikasi diperbarui.');
    }
}
