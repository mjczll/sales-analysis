<template>
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<script setup>
// 根组件：仅承载路由视图与页面切换淡入效果
</script>

<style>
/* 页面切换：轻微淡入（150–250ms），参考设计计划 6.1 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
