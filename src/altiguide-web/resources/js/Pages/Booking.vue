<script setup>
import { Head, Link } from '@inertiajs/vue3'
</script>

<template>
    <Head title="Booking Simaksi - AltiGuide" />

    <div class="min-h-screen bg-[#F4F1E6] flex flex-col font-sans">
        <!-- Navbar -->
        <nav class="w-full flex justify-between items-center px-4 md:px-8 xl:px-12 py-4 text-[#3b4b3b] font-semibold bg-[#374426]/10 backdrop-blur-md border-b border-[#D6CCAF] shadow-sm relative z-50">
            <div class="flex items-center gap-2 xl:gap-3">
                <img src="/images/logo_2.png" alt="AltiGuide Logo" class="w-8 h-8 md:w-10 md:h-10 object-contain" />
                <span class="text-xl md:text-2xl xl:text-3xl font-bold text-[#374426] tracking-tight">AltiGuide</span>
            </div>

            <div class="flex items-center gap-4 md:gap-6 xl:gap-9 text-sm md:text-base">
                <Link href="/" class="hover:text-black transition">Home</Link>
                <Link href="/dashboard" class="hover:text-black transition">Dashboard</Link>
                <Link 
                    href="/logout" 
                    method="post" 
                    as="button" 
                    class="border border-red-600 text-red-600 px-4 py-1.5 rounded-lg hover:bg-red-600 hover:text-white transition duration-200"
                >
                    Logout
                </Link>
            </div>
        </nav>

        <!-- Main content -->
        <div class="flex-1 flex items-center justify-center py-12 px-4">
            <div class="max-w-md w-full bg-white p-8 rounded-2xl border border-[#D6CCAF] shadow-xl text-center">
                <div class="w-20 h-20 bg-green-50 rounded-full flex items-center justify-center mx-auto mb-6 border border-green-200">
                    <span class="text-4xl">🏔️</span>
                </div>
                
                <h1 class="text-2xl font-bold text-[#374426] mb-3">Booking Simaksi AltiGuide</h1>
                <p class="text-gray-600 mb-8 leading-relaxed">
                    Selamat! Akun Anda telah lengkap dan siap untuk melakukan booking. Halaman booking versi web sedang dalam proses integrasi. Silakan lanjutkan pemesanan melalui aplikasi mobile AltiGuide Anda.
                </p>

                <div class="flex flex-col gap-3">
                    <Link 
                        href="/dashboard" 
                        class="inline-block px-6 py-3 bg-[#374426] text-white rounded-full font-semibold hover:bg-[#2c361e] transition-colors shadow-md"
                    >
                        Kembali ke Dashboard
                    </Link>

                    <Link 
                        href="/" 
                        class="inline-block px-6 py-3 border border-[#D6CCAF] text-gray-600 rounded-full font-semibold hover:bg-gray-50 transition-colors"
                    >
                        Kembali ke Beranda
                    </Link>
                </div>
            </div>
        </div>
    </div>
</template>
