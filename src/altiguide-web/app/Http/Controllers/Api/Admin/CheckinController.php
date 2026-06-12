<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Models\Transaction;
use App\Models\HikingSession;
use Carbon\Carbon;
use Illuminate\Http\Request;

class CheckinController extends Controller
{
    /**
     * Memsindai (Scan) Tiket / QR Code berdasarkan Order ID
     * Menampilkan status pesanan, daftar member, dll.
     */
    public function scan(Request $request, $orderId)
    {
        // Bersihkan orderId dari awalan '#' jika ada
        $orderId = ltrim($orderId, '#');

        // Ambil data admin yang sedang request/scan (bisa dari Sanctum atau session admin)
        $admin = $request->user('admin') ?? $request->user();

        // Cari transaksi beserta relasinya
        $transaction = Transaction::with([
            'hikingSession.route.mountain',
            'hikingSession.members',
            'user' // Ketua yang memesan
        ])->where('order_id', $orderId)->first();

        if (!$transaction) {
            return response()->json([
                'status'  => 'error',
                'message' => 'Tiket tidak ditemukan di sistem. Harap cek kembali Order ID.'
            ], 404);
        }

        // --- VALIDASI WEWENANG POS PENJAGAAN ADMIN ---
        // Jika admin adalah basecamp_staff, pastikan dia hanya bisa scan tiket rute-nya sendiri
        if ($admin && $admin->role === 'basecamp_staff' && $admin->route_id) {
            $tiketRouteId = $transaction->hikingSession->route_id;
            if ($admin->route_id !== $tiketRouteId) {
                return response()->json([
                    'status'  => 'error',
                    'message' => 'Akses Ditolak: Tiket ini bukan untuk jalur pos penjagaan Anda. (Wewenang Rute Tidak Sesuai)'
                ], 403);
            }
        }

        if ($transaction->status !== 'settlement') {
            return response()->json([
                'status'  => 'error',
                'message' => 'PERINGATAN: Tiket ini berstatus [' . strtoupper($transaction->status) . ']. Belum Lunas atau Kadaluwarsa!',
                'data'    => $transaction
            ], 400); // 400 Bad Request karena tiketnya bermasalah
        }

        // Tiket wajib sudah terverifikasi dokumennya oleh admin
        if ($transaction->hikingSession && $transaction->hikingSession->verification_status !== 'terverifikasi') {
            return response()->json([
                'status'  => 'error',
                'message' => 'Booking belum diverifikasi.'
            ], 400);
        }

        // --- VALIDASI TANGGAL CHECK-IN HARUS SESUAI TANGGAL PENDAKIAN ---
        $hikingSession = $transaction->hikingSession;
        if ($hikingSession) {
            $today = Carbon::today();

            if ($hikingSession->status === 'prepared') {
                $startDate = Carbon::parse($hikingSession->start_date);
                if (!$today->equalTo($startDate)) {
                    $formattedStart = $startDate->translatedFormat('l, d F Y');
                    return response()->json([
                        'status'  => 'error',
                        'message' => "Tanggal check-in tidak sesuai. Tiket ini untuk tanggal {$formattedStart}. Hari ini: {$today->translatedFormat('l, d F Y')}."
                    ], 400);
                }
            } elseif ($hikingSession->status === 'on_track') {
                $endDate = Carbon::parse($hikingSession->end_date);
                if (!$today->equalTo($endDate)) {
                    $formattedEnd = $endDate->translatedFormat('l, d F Y');
                    return response()->json([
                        'status'  => 'error',
                        'message' => "Tanggal check-out tidak sesuai. Jadwal turun tiket ini adalah tanggal {$formattedEnd}. Hari ini: {$today->translatedFormat('l, d F Y')}."
                    ], 400);
                }
            }
        }

        return response()->json([
            'status'  => 'success',
            'message' => 'Data tiket Valid dan Lunas. Tersertifikasi dari Pos Anda.',
            'data'    => $transaction
        ]);
    }

    /**
     * Mengupdate status perjalanan rombongan
     * prepared -> on_track (Mulai Naik) -> finished (Udah Turun/Selesai)
     */
    public function updateStatus(Request $request, $orderId)
    {
        // Bersihkan orderId dari awalan '#' jika ada
        $orderId = ltrim($orderId, '#');

        $request->validate([
            'status' => 'required|in:prepared,on_track,finished'
        ]);

        $admin = $request->user('admin') ?? $request->user();
        $transaction = Transaction::where('order_id', $orderId)->first();

        if (!$transaction || $transaction->status !== 'settlement') {
            return response()->json([
                'status'  => 'error',
                'message' => 'Tiket tidak valid atau belum lunas.'
            ], 400);
        }

        $hikingSession = HikingSession::where('transaction_id', $transaction->id)->first();
        
        if (!$hikingSession) {
            return response()->json([
                'status'  => 'error',
                'message' => 'Detail pendakian tidak ditemukan.'
            ], 404);
        }

        // --- VALIDASI WEWENANG POS PENJAGAAN ADMIN ---
        if ($admin && $admin->role === 'basecamp_staff' && $admin->route_id) {
            if ($admin->route_id !== $hikingSession->route_id) {
                return response()->json([
                    'status'  => 'error',
                    'message' => 'Akses Ditolak: Anda tidak memiliki wewenang untuk jalur pendakian ini.'
                ], 403);
            }
        }

        $currentStatus = $hikingSession->status;
        $newStatus = $request->status;

        // --- VALIDASI TANGGAL CHECK-IN HARUS SESUAI TANGGAL PENDAKIAN ---
        if ($newStatus === 'on_track') {
            $today = Carbon::today();
            $startDate = Carbon::parse($hikingSession->start_date);

            if (!$today->equalTo($startDate)) {
                $formattedStart = $startDate->translatedFormat('l, d F Y');
                return response()->json([
                    'status'  => 'error',
                    'message' => "Tanggal check-in tidak sesuai. Tiket ini untuk tanggal {$formattedStart}. Hari ini: {$today->translatedFormat('l, d F Y')}."
                ], 400);
            }
        } elseif ($newStatus === 'finished') {
            $today = Carbon::today();
            $endDate = Carbon::parse($hikingSession->end_date);

            if (!$today->equalTo($endDate)) {
                $formattedEnd = $endDate->translatedFormat('l, d F Y');
                return response()->json([
                    'status'  => 'error',
                    'message' => "Tanggal check-out tidak sesuai. Jadwal turun tiket ini adalah tanggal {$formattedEnd}. Hari ini: {$today->translatedFormat('l, d F Y')}."
                ], 400);
            }
        }

        $hikingSession->status = $newStatus;
        $hikingSession->save();

        // Pesan Sukses yang dinamis sesuai status
        $msg = "Status rombongan berhasil di-update: {$newStatus}";
        if ($newStatus === 'on_track') {
            $msg = "Rombongan telah Check-in di Pos Anda dan mulai mendaki gunung.";
        } elseif ($newStatus === 'finished') {
            $msg = "Rombongan telah Check-out di Pos Anda dan kembali dengan selamat.";
        }

        return response()->json([
            'status'  => 'success',
            'message' => $msg,
            'data'    => $hikingSession
        ]);
    }
}
