<template>
  <el-tag :type="type || undefined" :effect="effect" :size="size" round>
    {{ text }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  text: { type: String, required: true },
  // '' / success / warning / info / danger / primary
  type: { type: String, default: '' },
  effect: { type: String, default: 'light' },
  size: { type: String, default: 'small' }
})

const type = computed(() => props.type)
</script>
