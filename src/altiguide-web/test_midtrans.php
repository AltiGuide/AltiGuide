<?php
require __DIR__.'/vendor/autoload.php';

// Boot Laravel
$app = require_once __DIR__.'/bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

try {
    \Midtrans\Config::$serverKey = env('MIDTRANS_SERVER_KEY');
    \Midtrans\Config::$isProduction = env('MIDTRANS_IS_PRODUCTION', false);
    \Midtrans\Config::$isSanitized = true;
    \Midtrans\Config::$is3ds = true;

    echo "Using Server Key: " . \Midtrans\Config::$serverKey . "\n";
    echo "Is Production: " . (\Midtrans\Config::$isProduction ? 'true' : 'false') . "\n";

    $params = [
        'transaction_details' => [
            'order_id' => 'TEST-' . time(),
            'gross_amount' => 10000,
        ],
        'customer_details' => [
            'first_name' => 'Test',
            'email' => 'test@example.com',
        ]
    ];

    $snapTransaction = \Midtrans\Snap::createTransaction($params);
    echo "Snap Token: " . $snapTransaction->token . "\n";
    echo "Redirect URL: " . $snapTransaction->redirect_url . "\n";
} catch (\Exception $e) {
    echo "EXCEPTION CAUGHT: " . $e->getMessage() . "\n";
}
