<script setup>
import { usePage } from '@inertiajs/vue3'

const page = usePage()
</script>

<template>
  <Transition name="page" mode="out-in" appear>
    <div :key="page.url" class="min-h-screen w-full flex flex-col relative bg-[#F4F1E6]">
      <slot />
    </div>
  </Transition>
</template>
