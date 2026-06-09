<?php

namespace App\Http\Controllers;

use App\Models\Mountain;
use Inertia\Inertia;

class WeatherController extends Controller
{
    public function index()
    {
        $mountains = Mountain::with(['routes.routeInfo'])
            ->get()
            ->map(function ($mountain) {
                return [
                    'slug'      => $mountain->slug,
                    'name'      => $mountain->name,
                    'latitude'  => $mountain->latitude,
                    'longitude' => $mountain->longitude,
                    'routes'    => $mountain->routes->map(function ($route) {
                        $shortName = str_replace(
                            $route->mountain->name ?? '', '', $route->name
                        );
                        $shortName = trim(str_replace(' via ', 'Jalur ', $shortName));
                        if (str_contains($route->name, ' via ')) {
                            $parts = explode(' via ', $route->name);
                            $shortName = 'Jalur ' . ($parts[1] ?? $route->name);
                        } else {
                            $shortName = $route->name;
                        }

                        return [
                            'id'         => $route->id,
                            'name'       => $route->name,
                            'short_name' => $shortName,
                            'slug'       => $route->slug,
                            'latitude'   => $route->latitude,
                            'longitude'  => $route->longitude,
                        ];
                    }),
                ];
            });

        return Inertia::render('Weather/Index', [
            'mountains' => $mountains,
        ]);
    }
}
