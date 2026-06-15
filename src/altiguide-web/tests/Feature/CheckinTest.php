<?php

namespace Tests\Feature;

use App\Models\Admin;
use App\Models\User;
use App\Models\Mountain;
use App\Models\Route;
use App\Models\RouteInfo;
use App\Models\Transaction;
use App\Models\HikingSession;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class CheckinTest extends TestCase
{
    use RefreshDatabase;

    private Admin $admin;
    private Transaction $transaction;
    private HikingSession $hikingSession;

    protected function setUp(): void
    {
        parent::setUp();

        // Create standard test data
        $user = User::factory()->create();
        
        $mountain = Mountain::create([
            'name' => 'Gunung Sumbing',
            'location' => 'Wonosobo',
            'altitude' => 3371,
            'description' => 'Test Sumbing',
            'latitude' => -7.384,
            'longitude' => 110.070
        ]);

        $route = Route::create([
            'mountain_id' => $mountain->id,
            'name' => 'Garung',
            'distance' => 7.2,
            'estimated_time' => 420,
            'difficulty' => 'hard',
            'is_active' => true,
            'latitude' => -7.385,
            'longitude' => 110.075
        ]);

        RouteInfo::create([
            'route_id' => $route->id,
            'basecamp_address' => 'Garung Wonosobo',
            'basecamp_altitude' => 1400,
            'simaksi_price' => 20000
        ]);

        $this->admin = Admin::create([
            'name' => 'Test Admin',
            'email' => 'admin@altiguide.com',
            'password' => bcrypt('password123'),
            'role' => 'superadmin'
        ]);

        $this->transaction = Transaction::create([
            'user_id' => $user->id,
            'order_id' => 'ALT-TEST-999',
            'gross_amount' => 25000,
            'status' => 'settlement',
            'payment_type' => 'qris',
            'qr_url' => 'https://app.sandbox.midtrans.com/snap/v2/vtweb/mock-token',
            'expiry_time' => now()->addHour()
        ]);

        $this->hikingSession = HikingSession::create([
            'leader_id' => $user->id,
            'route_id' => $route->id,
            'transaction_id' => $this->transaction->id,
            'group_name' => 'Garung Squad',
            'start_date' => now()->toDateString(),
            'end_date' => now()->addDay()->toDateString(),
            'hike_type' => 'camp',
            'status' => 'prepared',
            'verification_status' => 'pending_review' // Default unverified
        ]);
    }

    public function test_unverified_booking_cannot_be_scanned(): void
    {
        $response = $this->actingAs($this->admin, 'admin')
            ->getJson("/admin/checkin/scan/{$this->transaction->order_id}");

        $response->assertStatus(400);
        $response->assertJson([
            'status' => 'error',
            'message' => 'Booking belum diverifikasi.'
        ]);
    }

    public function test_unverified_booking_cannot_update_status(): void
    {
        $response = $this->actingAs($this->admin, 'admin')
            ->putJson("/admin/checkin/status/{$this->transaction->order_id}", [
                'status' => 'on_track'
            ]);

        $response->assertStatus(400);
        $response->assertJson([
            'status' => 'error',
            'message' => 'Booking belum diverifikasi.'
        ]);
    }

    public function test_verified_booking_can_be_scanned(): void
    {
        // Mark as verified
        $this->hikingSession->update(['verification_status' => 'terverifikasi']);

        $response = $this->actingAs($this->admin, 'admin')
            ->getJson("/admin/checkin/scan/{$this->transaction->order_id}");

        $response->assertStatus(200);
        $response->assertJson([
            'status' => 'success'
        ]);
    }

    public function test_verified_booking_can_update_status(): void
    {
        // Mark as verified
        $this->hikingSession->update(['verification_status' => 'terverifikasi']);

        $response = $this->actingAs($this->admin, 'admin')
            ->putJson("/admin/checkin/status/{$this->transaction->order_id}", [
                'status' => 'on_track'
            ]);

        $response->assertStatus(200);
        $response->assertJson([
            'status' => 'success',
            'message' => 'Rombongan telah Check-in di Pos Anda dan mulai mendaki gunung.'
        ]);

        $this->assertEquals('on_track', $this->hikingSession->fresh()->status);
    }
}
