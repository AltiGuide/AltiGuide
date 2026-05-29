<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        // 1. Modifikasi tabel users
        Schema::table('users', function (Blueprint $table) {
            $table->timestamp('email_verified_at')->nullable();
            $table->smallInteger('age')->nullable()->change();
            $table->text('address')->nullable()->change();
            $table->string('emergency_contact', 15)->nullable()->change();
            $table->string('nik', 16)->nullable()->change();
        });

        // 2. Buat tabel kode verifikasi email (OTP)
        Schema::create('email_verification_codes', function (Blueprint $table) {
            $table->id();
            $table->string('email')->index();
            $table->string('code', 6);
            $table->timestamp('expires_at');
            $table->timestamp('created_at')->useCurrent();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('email_verification_codes');

        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn('email_verified_at');
            $table->smallInteger('age')->change();
            $table->text('address')->change();
            $table->string('emergency_contact', 15)->change();
            $table->string('nik', 16)->change();
        });
    }
};
