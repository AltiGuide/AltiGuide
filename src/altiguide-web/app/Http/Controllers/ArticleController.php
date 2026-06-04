<?php

namespace App\Http\Controllers;

use App\Models\Mountain;
use Inertia\Inertia;

class ArticleController extends Controller
{
    public function show()
    {
        // Mountain cover photos — served from public/images directly
        $mountainImageMap = [
            'gunung-sumbing' => '/images/gunung_sumbing_4.png',
            'gunung-sindoro' => '/images/gunung_sindoro_8.png',
            'gunung-prau'    => '/images/gunung_prau_7.png',
            'gunung-merbabu' => '/images/gunung_merbabu_6.png',
            'gunung-lawu'    => '/images/gunung_lawu_2.png',
            'gunung-andong'  => '/images/gunung_andong_1.png',
            'gunung-ungaran' => '/images/gunung_ungaran_5.png',
            'gunung-slamet'  => '/images/gunung_slamet_3.png',
        ];

        $mountains = Mountain::with(['routes.routeInfo', 'routes.waypoints'])
            ->get()
            ->map(function ($mountain) use ($mountainImageMap) {
                $mountainImage = $mountainImageMap[$mountain->slug]
                    ?? ($mountain->image ? '/images/routes/' . basename($mountain->image) : null);

                return [
                    'slug'      => $mountain->slug,
                    'name'      => $mountain->name,
                    'image'     => $mountainImage,
                    'altitude'  => $mountain->altitude,
                    'latitude'  => $mountain->latitude,
                    'longitude' => $mountain->longitude,
                    'routes'    => $mountain->routes->map(function ($route) {
                        $shortName = str_replace(
                            $route->mountain->name ?? '', '', $route->name
                        );
                        // e.g. "Gunung Merbabu via Suwanting" => "via Suwanting" => "Jalur Suwanting"
                        $shortName = trim(str_replace(' via ', 'Jalur ', $shortName));
                        if (str_contains($route->name, ' via ')) {
                            $parts = explode(' via ', $route->name);
                            $shortName = 'Jalur ' . ($parts[1] ?? $route->name);
                        } else {
                            $shortName = $route->name;
                        }

                        $info = $route->routeInfo;

                        // Build image paths using public/images/routes/ (no symlink dependency)
                        $routeImage = null;
                        if ($route->image) {
                            $filename = basename($route->image);
                            $routeImage = '/images/routes/' . $filename;
                        }

                        $mapImage = null;
                        if ($route->map_image) {
                            $filename = basename($route->map_image);
                            $mapImage = '/images/waypoints/' . $filename;
                        }

                        return [
                            'id'             => $route->id,
                            'name'           => $route->name,
                            'short_name'     => $shortName,
                            'slug'           => $route->slug,
                            'image'          => $routeImage,
                            'map_image'      => $mapImage,
                            'distance'       => $route->distance,
                            'estimated_time' => $route->estimated_time,
                            'difficulty'     => $route->difficulty,
                            'latitude'       => $route->latitude,
                            'longitude'      => $route->longitude,
                            'route_info'     => $info ? [
                                'basecamp_address'       => $info->basecamp_address,
                                'basecamp_altitude'      => $info->basecamp_altitude,
                                'simaksi_price'          => $info->simaksi_price,
                                'ojek_price'             => $info->ojek_price,
                                'ojek_description'       => $info->ojek_description,
                                'facilities_description' => $info->facilities_description,
                                'logistics_description'  => $info->logistics_description,
                            ] : null,
                            'waypoints' => $route->waypoints->map(function ($wp) {
                                return [
                                    'name'                   => $wp->name,
                                    'altitude'               => $wp->altitude,
                                    'order_index'            => $wp->order_index,
                                    'estimated_time_minutes' => $wp->estimated_time_minutes,
                                    'distance_from_prev'     => $wp->distance_from_prev,
                                    'description'            => $wp->description,
                                    'has_water_source'       => (bool) $wp->has_water_source,
                                ];
                            }),
                        ];
                    }),
                    'content' => $mountain->content,
                ];
            });

        return Inertia::render('Article/Show', [
            'mountains' => $mountains,
        ]);
    }
}
