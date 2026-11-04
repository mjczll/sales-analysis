<template>
  <el-breadcrumb separator="/" class="app-breadcrumb">
    <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-for="(item, index) in items" :key="index">
      {{ item }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const items = computed(() => {
  const title = route.meta?.title
  return title ? [title] : []
})
</script>

<style scoped>
.app-breadcrumb {
  font-size: 13px;
}
.app-breadcrumb :deep(.el-breadcrumb__inner) {
  color: var(--text-secondary);
  font-weight: 500;
}
.app-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--text-primary);
}
</style>
