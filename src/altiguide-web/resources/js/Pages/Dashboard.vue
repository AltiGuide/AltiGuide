<script setup>
import { Head, Link } from '@inertiajs/vue3'
</script>

<template>
    <Head title="Dashboard - AltiGuide" />

    <div class="min-h-screen bg-gray-100 flex flex-col items-center justify-center">
        <div class="bg-white p-8 rounded-xl shadow-md text-center max-w-md w-full">
            <h1 class="text-2xl font-bold text-[#374426] mb-4">Welcome to AltiGuide!</h1>
            <p class="text-gray-600 mb-6">
                You have successfully logged in. Your personal dashboard is currently under construction.
            </p>
            <div class="flex flex-col gap-3">
                <Link 
                    href="/" 
                    class="inline-block px-6 py-2 bg-[#64823E] text-white rounded-lg hover:bg-[#4d662e] transition"
                >
                    Back to Home
                </Link>

                <Link 
                    href="/logout" 
                    method="post" 
                    as="button"
                    class="inline-block px-6 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition w-full"
                >
                    Logout
                </Link>
            </div>
        </div>
    </div>
</template>
