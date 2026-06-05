<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\Mountain;
use App\Models\Route;
use App\Models\RouteInfo;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;
use Mockery;

class WebBookingTest extends TestCase
{
    use RefreshDatabase;

    public function test_guest_cannot_access_booking_form(): void
    {
        $response = $this->get('/booking');
        $response->assertRedirect('/login');
    }

    public function test_logged_in_user_with_incomplete_profile_is_redirected_to_dashboard(): void
    {
        $user = User::factory()->create([
            'nik' => null // Incomplete profile
        ]);

        $response = $this->actingAs($user)->get('/booking');
        $response->assertRedirect(route('dashboard'));
        $response->assertSessionHas('warning');
    }

    public function test_logged_in_user_with_complete_profile_can_access_booking_form(): void
    {
        $user = User::factory()->create();

        // Create a mountain and a route
        $mountain = Mountain::create([
            'name' => 'Gunung Merbabu',
            'location' => 'Boyolali',
            'altitude' => 3142,
            'description' => 'Test Merbabu',
            'latitude' => -7.4,
            'longitude' => 110.4
        ]);

        $route = Route::create([
            'mountain_id' => $mountain->id,
            'name' => 'Selo',
            'distance' => 5.7,
            'estimated_time' => 360,
            'difficulty' => 'moderate',
            'is_active' => true,
            'latitude' => -7.45,
            'longitude' => 110.45
        ]);

        RouteInfo::create([
            'route_id' => $route->id,
            'basecamp_address' => 'Selo Boyolali',
            'basecamp_altitude' => 1500,
            'simaksi_price' => 15000
        ]);

        $response = $this->actingAs($user)->get('/booking');
        $response->assertStatus(200);
        
        // Assert that the mountain and routes are passed to Inertia
        $response->assertInertia(fn ($page) => $page
            ->component('Booking')
            ->has('mountains')
        );
    }

    public function test_guest_cannot_checkout(): void
    {
        $response = $this->postJson('/booking/checkout', []);
        $response->assertStatus(401);
    }

    public function test_user_with_incomplete_profile_cannot_checkout(): void
    {
        $user = User::factory()->create([
            'nik' => null
        ]);

        $response = $this->actingAs($user)->postJson('/booking/checkout', []);
        $response->assertStatus(403);
        $response->assertJson([
            'status' => 'error',
            'message' => 'Silakan lengkapi profil Anda terlebih dahulu sebelum melakukan booking pendakian.'
        ]);
    }

    public function test_checkout_validation_errors(): void
    {
        $user = User::factory()->create();

        $response = $this->actingAs($user)->postJson('/booking/checkout', []);
        $response->assertStatus(422);
        $response->assertJsonValidationErrors(['route_id', 'start_date', 'hike_type', 'group_name', 'members']);
    }

    public function test_successful_checkout(): void
    {
        // Mock static Snap call before loading Midtrans\Snap
        $snapMock = Mockery::mock('alias:Midtrans\Snap');
        $snapMock->shouldReceive('createTransaction')
            ->once()
            ->andReturn((object)[
                'redirect_url' => 'https://app.sandbox.midtrans.com/snap/v2/vtweb/12345',
                'token' => 'mock-snap-token-12345'
            ]);

        $user = User::factory()->create();

        $mountain = Mountain::create([
            'name' => 'Gunung Merbabu',
            'location' => 'Boyolali',
            'altitude' => 3142,
            'description' => 'Test Merbabu',
            'latitude' => -7.4,
            'longitude' => 110.4
        ]);

        $route = Route::create([
            'mountain_id' => $mountain->id,
            'name' => 'Selo',
            'distance' => 5.7,
            'estimated_time' => 360,
            'difficulty' => 'moderate',
            'is_active' => true,
            'latitude' => -7.45,
            'longitude' => 110.45,
            'daily_quota' => 100
        ]);

        RouteInfo::create([
            'route_id' => $route->id,
            'basecamp_address' => 'Selo Boyolali',
            'basecamp_altitude' => 1500,
            'simaksi_price' => 15000
        ]);

        $memberUser = User::factory()->create();

        $payload = [
            'route_id' => $route->id,
            'start_date' => now()->addDays(2)->format('Y-m-d'),
            'hike_type' => 'camp',
            'group_name' => 'Sobat Mendaki',
            'members' => [
                [
                    'user_id' => $memberUser->id,
                    'identity_number' => $memberUser->nik,
                    'full_name' => $memberUser->name,
                    'phone_number' => $memberUser->phone_number,
                    'emergency_contact' => $memberUser->emergency_contact,
                ]
            ]
        ];

        $response = $this->actingAs($user)->postJson('/booking/checkout', $payload);

        $response->assertStatus(201);
        $response->assertJson([
            'status' => 'success',
            'message' => 'Pesanan berhasil dibuat. Lakukan pembayaran via QRIS.',
            'data' => [
                'payment_url' => 'https://app.sandbox.midtrans.com/snap/v2/vtweb/12345',
                'snap_token' => 'mock-snap-token-12345'
            ]
        ]);

        $this->assertDatabaseHas('transactions', [
            'user_id' => $user->id,
            'gross_amount' => 20000, // 15000 + 5000 fee
            'status' => 'pending'
        ]);

        $this->assertDatabaseHas('hiking_sessions', [
            'leader_id' => $user->id,
            'route_id' => $route->id,
            'group_name' => 'Sobat Mendaki',
            'hike_type' => 'camp',
            'status' => 'prepared'
        ]);

        $this->assertDatabaseHas('hiking_members', [
            'user_id' => $memberUser->id,
            'identity_number' => $memberUser->nik,
            'full_name' => $memberUser->name
        ]);
    }

    public function test_validate_nik_requires_auth(): void
    {
        $response = $this->postJson('/booking/validate-nik', []);
        $response->assertStatus(401);
    }

    public function test_validate_nik_returns_validation_errors(): void
    {
        $user = User::factory()->create();

        $response = $this->actingAs($user)->postJson('/booking/validate-nik', []);
        $response->assertStatus(422);
        $response->assertJsonValidationErrors(['nik', 'start_date', 'hike_type']);
    }

    public function test_validate_nik_success(): void
    {
        $user = User::factory()->create();
        $memberUser = User::factory()->create();

        $payload = [
            'nik' => $memberUser->nik,
            'start_date' => now()->addDays(2)->format('Y-m-d'),
            'hike_type' => 'camp'
        ];

        $response = $this->actingAs($user)->postJson('/booking/validate-nik', $payload);

        $response->assertStatus(200);
        $response->assertJson([
            'status' => 'success',
            'message' => 'NIK Valid dan siap ditambahkan ke rombongan.',
            'data' => [
                'user_id' => $memberUser->id,
                'nik' => $memberUser->nik,
                'full_name' => $memberUser->name
            ]
        ]);
    }

    public function test_user_can_repay_pending_transaction(): void
    {
        $user = User::factory()->create();

        $mountain = Mountain::create([
            'name' => 'Gunung Merbabu',
            'location' => 'Boyolali',
            'altitude' => 3142,
            'description' => 'Test Merbabu',
            'latitude' => -7.4,
            'longitude' => 110.4
        ]);

        $route = Route::create([
            'mountain_id' => $mountain->id,
            'name' => 'Selo',
            'distance' => 5.7,
            'estimated_time' => 360,
            'difficulty' => 'moderate',
            'is_active' => true,
            'latitude' => -7.45,
            'longitude' => 110.45
        ]);

        RouteInfo::create([
            'route_id' => $route->id,
            'basecamp_address' => 'Selo Boyolali',
            'basecamp_altitude' => 1500,
            'simaksi_price' => 15000
        ]);

        $transaction = \App\Models\Transaction::create([
            'user_id' => $user->id,
            'order_id' => 'ALT-TEST-123',
            'gross_amount' => 20000,
            'status' => 'pending',
            'payment_type' => 'qris',
            'qr_url' => 'https://app.sandbox.midtrans.com/snap/v2/vtweb/mock-token',
            'expiry_time' => now()->addHour()
        ]);

        $session = \App\Models\HikingSession::create([
            'leader_id' => $user->id,
            'route_id' => $route->id,
            'transaction_id' => $transaction->id,
            'group_name' => 'Test Group',
            'start_date' => now()->addDays(2),
            'end_date' => now()->addDays(3),
            'hike_type' => 'camp',
            'status' => 'prepared'
        ]);

        $response = $this->actingAs($user)->get('/booking/pay/ALT-TEST-123');
        $response->assertStatus(200);
        $response->assertInertia(fn ($page) => $page
            ->component('Booking')
            ->has('preloadedPayment')
        );
    }

    public function test_user_cannot_checkout_with_less_than_min_members(): void
    {
        $user = User::factory()->create();

        $mountain = Mountain::create([
            'name' => 'Gunung Merbabu',
            'location' => 'Boyolali',
            'altitude' => 3142,
            'description' => 'Test Merbabu',
            'latitude' => -7.4,
            'longitude' => 110.4,
            'min_members' => 3
        ]);

        $route = Route::create([
            'mountain_id' => $mountain->id,
            'name' => 'Selo',
            'distance' => 5.7,
            'estimated_time' => 360,
            'difficulty' => 'moderate',
            'is_active' => true,
            'latitude' => -7.45,
            'longitude' => 110.45,
            'daily_quota' => 100
        ]);

        RouteInfo::create([
            'route_id' => $route->id,
            'basecamp_address' => 'Selo Boyolali',
            'basecamp_altitude' => 1500,
            'simaksi_price' => 15000
        ]);

        $payload = [
            'route_id' => $route->id,
            'start_date' => now()->addDays(2)->format('Y-m-d'),
            'hike_type' => 'camp',
            'group_name' => 'Sobat Mendaki',
            'members' => [
                [
                    'user_id' => $user->id,
                    'identity_number' => $user->nik,
                    'full_name' => $user->name,
                    'phone_number' => $user->phone_number,
                    'emergency_contact' => $user->emergency_contact,
                ]
            ]
        ];

        $response = $this->actingAs($user)->postJson('/booking/checkout', $payload);

        $response->assertStatus(422);
        $response->assertJson([
            'status' => 'error',
            'message' => 'Gunung Gunung Merbabu tidak memperbolehkan solo hiking. Jumlah anggota minimal untuk pendakian ini adalah 3 orang (termasuk ketua).'
        ]);
    }

    public function test_dashboard_returns_mapped_bookings_with_details(): void
    {
        $user = User::factory()->create();

        $mountain = Mountain::create([
            'name' => 'Gunung Merbabu',
            'location' => 'Boyolali',
            'altitude' => 3142,
            'description' => 'Test Merbabu',
            'latitude' => -7.4,
            'longitude' => 110.4
        ]);

        $route = Route::create([
            'mountain_id' => $mountain->id,
            'name' => 'Selo',
            'distance' => 5.7,
            'estimated_time' => 360,
            'difficulty' => 'moderate',
            'is_active' => true,
            'latitude' => -7.45,
            'longitude' => 110.45
        ]);

        $transaction = \App\Models\Transaction::create([
            'user_id' => $user->id,
            'order_id' => 'ALT-DASH-123',
            'gross_amount' => 20000,
            'status' => 'settlement',
            'payment_type' => 'qris',
            'qr_url' => 'https://app.sandbox.midtrans.com/snap/v2/vtweb/mock-token',
            'expiry_time' => now()->addHour()
        ]);

        $session = \App\Models\HikingSession::create([
            'leader_id' => $user->id,
            'route_id' => $route->id,
            'transaction_id' => $transaction->id,
            'group_name' => 'Test Group',
            'start_date' => now()->addDays(2),
            'end_date' => now()->addDays(3),
            'hike_type' => 'camp',
            'status' => 'prepared'
        ]);

        $member = \App\Models\HikingMember::create([
            'hiking_session_id' => $session->id,
            'user_id' => $user->id,
            'identity_number' => '1234567890123456',
            'full_name' => 'Test Member',
            'phone_number' => '08123456789',
            'emergency_contact' => '08123456780'
        ]);

        $response = $this->actingAs($user)->get('/dashboard');
        $response->assertStatus(200);
        
        $bookings = $response->original->getData()['page']['props']['bookings'];
        $this->assertCount(1, $bookings);
        $this->assertEquals('ALT-DASH-123', $bookings[0]['order_id']);
        $this->assertEquals('Gunung Merbabu', $bookings[0]['mountain_name']);
        $this->assertEquals('camp', $bookings[0]['hike_type']);
        $this->assertEquals($user->name, $bookings[0]['leader']['name']);
        $this->assertEquals('Test Member', $bookings[0]['members'][0]['full_name']);
    }

    protected function tearDown(): void
    {
        Mockery::close();
        parent::tearDown();
    }
}
