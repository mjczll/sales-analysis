<template>
  <el-card class="stat-card" shadow="never">
    <div class="stat-card-body">
      <div class="stat-card-text">
        <div class="stat-card-label">{{ label }}</div>
        <div class="stat-card-value num">{{ displayValue }}</div>
        <div v-if="trend" class="stat-card-trend" :class="trend.positive ? 'up' : 'down'">
          <el-icon><component :is="trend.positive ? 'CaretTop' : 'CaretBottom'" /></el-icon>
          <span>{{ trend.text }}</span>
        </div>
      </div>
      <div class="stat-card-icon" :style="{ background: tintColor }">
        <el-icon :size="22" :style="{ color: color }"><component :is="icon" /></el-icon>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], default: '' },
  prefix: { type: String, default: '' },
  icon: { type: String, default: 'Money' },
  color: { type: String, default: '#2563EB' },
  trend: { type: Object, default: null } // { text, positive }
})

const displayValue = computed(() =>
  props.prefix ? `${props.prefix}${props.value}` : props.value
)
const tintColor = computed(() => props.color + '14') // 10% 透明度
</script>

<style scoped lang="scss">
.stat-card {
  border-radius: var(--radius-card);

  .stat-card-body {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .stat-card-label {
    font-size: 13px;
    color: var(--text-secondary);
  }

  .stat-card-value {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary);
    margin: 8px 0 4px;
    line-height: 1.1;
  }

  .stat-card-trend {
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 2px;

    &.up {
      color: var(--color-success);
    }
    &.down {
      color: var(--color-danger);
    }
  }

  .stat-card-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
}
</style>
