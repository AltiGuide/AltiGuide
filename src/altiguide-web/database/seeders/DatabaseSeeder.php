<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class DatabaseSeeder extends Seeder
{
    /**
     * Seed the application's database.
     */
    public function run(): void
    {
        if (!User::where('email', 'test@example.com')->exists()) {
            User::factory()->create([
                'name' => 'Test User',
                'email' => 'test@example.com',
            ]);
        }

        $this->call([
            UngaranSeeder::class,
            MerbabuSeeder::class,
            SlametSeeder::class,
            LawuSeeder::class,
            SindoroSeeder::class,
            SumbingSeeder::class,
            AndongSeeder::class,
            PrauSeeder::class,
        ]);

        // Aturan solo hiking/jumlah minimal anggota untuk masing-masing gunung
        \App\Models\Mountain::where('name', 'Gunung Merbabu')->update(['min_members' => 3]);
        \App\Models\Mountain::where('name', 'Gunung Lawu')->update(['min_members' => 2]);
        \App\Models\Mountain::where('name', 'Gunung Sindoro')->update(['min_members' => 2]);
        \App\Models\Mountain::where('name', 'Gunung Sumbing')->update(['min_members' => 2]);
        \App\Models\Mountain::where('name', 'Gunung Slamet')->update(['min_members' => 2]);
        \App\Models\Mountain::where('name', 'Gunung Ungaran')->update(['min_members' => 2]);
        \App\Models\Mountain::where('name', 'Gunung Andong')->update(['min_members' => 1]);
        \App\Models\Mountain::where('name', 'Gunung Prau')->update(['min_members' => 1]);
    }
}
