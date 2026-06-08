<?php

namespace App\Http\Controllers;

use App\Models\Mountain;
use Inertia\Inertia;

class MountainListController extends Controller
{
    public function index()
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

        $mountains = Mountain::all()->map(function ($mountain) use ($mountainImageMap) {
            $mountainImage = $mountainImageMap[$mountain->slug]
                ?? ($mountain->image ? '/images/routes/' . basename($mountain->image) : null);

            return [
                'slug'      => $mountain->slug,
                'name'      => $mountain->name,
                'image'     => $mountainImage,
                'altitude'  => $mountain->altitude,
                'latitude'  => $mountain->latitude,
                'longitude' => $mountain->longitude,
                'content'   => $mountain->content,
            ];
        });

        return Inertia::render('Mountain/Index', [
            'mountains' => $mountains,
        ]);
    }
}
