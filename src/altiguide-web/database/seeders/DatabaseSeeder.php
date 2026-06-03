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
    }
}
