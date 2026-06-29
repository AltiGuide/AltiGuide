<?php

namespace App\Http\Controllers;

use App\Models\Mountain;
use Inertia\Inertia;

class WeatherAnalyticsController extends Controller
{
    public function index()
    {
        $mountains = Mountain::select('id', 'name', 'latitude', 'longitude')
            ->orderBy('name', 'asc')
            ->get();

        return Inertia::render('WeatherAnalytics', [
            'mountains' => $mountains
        ]);
    }
}
