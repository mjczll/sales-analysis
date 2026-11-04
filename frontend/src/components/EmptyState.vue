<template>
  <div class="empty-state">
    <el-icon :size="40" class="empty-icon"><Box /></el-icon>
    <p class="empty-text">{{ text }}</p>
  </div>
</template>

<script setup>
import { Box } from '@element-plus/icons-vue'

defineProps({
  text: { type: String, default: '暂无数据' }
})
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
  color: var(--text-weak);

  .empty-icon {
    margin-bottom: 8px;
  }
  .empty-text {
    font-size: 13px;
    margin: 0;
  }
}
</style>
