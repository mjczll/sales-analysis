<template>
  <el-container class="app-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="app-aside">
      <AppSidebar :collapse="isCollapse" />
    </el-aside>
    <el-container>
      <el-header class="app-header-wrap">
        <AppHeader :collapse="isCollapse" @toggle="isCollapse = !isCollapse" />
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import AppSidebar from './components/AppSidebar.vue'
import AppHeader from './components/AppHeader.vue'

const isCollapse = ref(false)
</script>

<style scoped lang="scss">
.app-layout {
  height: 100vh;

  .app-aside {
    transition: width 0.25s ease;
    overflow: hidden;
  }

  .app-header-wrap {
    padding: 0;
    height: var(--header-height);
  }

  .app-main {
    background: var(--bg-page);
    padding: 0;
    overflow-y: auto;
  }
}
</style>
