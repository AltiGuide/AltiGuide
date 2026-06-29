<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Mountain;
use App\Models\Route;
use App\Models\RouteInfo;
use Illuminate\Http\Request;

class ContentController extends Controller
{
    /**
     * Update konten artikel gunung (field description + content JSON).
     */
    public function updateMountainContent(Request $request, Mountain $mountain)
    {
        $request->validate([
            'description'             => 'nullable|string|max:1000',
            'content'                 => 'nullable|array',
            'content.*.title'         => 'nullable|string|max:255',
            'content.*.text'          => 'required|string',
        ]);

        $mountain->update([
            'description' => $request->description,
            'content'     => $request->content ?? [],
        ]);

        return back()->with('success', "Konten artikel {$mountain->name} berhasil disimpan.");
    }

    /**
     * Upsert RouteInfo untuk sebuah jalur.
     */
    public function updateRouteInfo(Request $request, Route $route)
    {
        $request->validate([
            'basecamp_address'       => 'nullable|string|max:500',
            'basecamp_altitude'      => 'nullable|integer|min:0',
            'simaksi_price'          => 'nullable|integer|min:0',
            'ojek_price'             => 'nullable|integer|min:0',
            'ojek_description'       => 'nullable|string|max:500',
            'facilities_description' => 'nullable|string|max:2000',
            'logistics_description'  => 'nullable|string|max:2000',
        ]);

        RouteInfo::updateOrCreate(
            ['route_id' => $route->id],
            $request->only([
                'basecamp_address',
                'basecamp_altitude',
                'simaksi_price',
                'ojek_price',
                'ojek_description',
                'facilities_description',
                'logistics_description',
            ])
        );

        return back()->with('success', "Info jalur {$route->name} berhasil disimpan.");
    }
}
