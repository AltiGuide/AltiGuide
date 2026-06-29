<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('hiking_sessions', function (Blueprint $table) {
            // Status verifikasi dokumen oleh admin
            // pending_review | terverifikasi | review | revisi
            $table->string('verification_status')->default('pending_review')->after('status');
        });
    }

    public function down(): void
    {
        Schema::table('hiking_sessions', function (Blueprint $table) {
            $table->dropColumn('verification_status');
        });
    }
};
