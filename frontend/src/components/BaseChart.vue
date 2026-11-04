<template>
  <div ref="chartRef" class="base-chart" :style="{ height: height }"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  option: { type: Object, required: true },
  height: { type: String, default: '320px' }
})

const chartRef = ref(null)
let chart = null

function render() {
  if (!chart) return
  chart.setOption(props.option, true)
}

function resize() {
  chart && chart.resize()
}

onMounted(() => {
  chart = echarts.init(chartRef.value)
  render()
  window.addEventListener('resize', resize)
})

watch(
  () => props.option,
  () => nextTick(render),
  { deep: true }
)

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart && chart.dispose()
  chart = null
})
</script>

<style scoped>
.base-chart {
  width: 100%;
}
</style>
