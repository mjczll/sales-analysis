<template>
  <div class="page">
    <PageHeader title="员工业绩" description="分析销售人员业绩表现" />

    <el-card shadow="never" class="block">
      <template #header><span>员工业绩销售额排行</span></template>
      <BaseChart :option="option" height="360px" v-loading="loading" />
    </el-card>

    <el-card shadow="never">
      <template #header><span>员工业绩明细</span></template>
      <el-table :data="list" stripe>
        <el-table-column label="排名" width="80" align="center">
          <template #default="{ $index }">
            <span class="rank-badge" :class="'r-' + ($index + 1)">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="员工姓名" min-width="140" />
        <el-table-column label="销售订单数" width="140" align="right">
          <template #default="{ row }"><span class="num">{{ row.orderCount }}</span></template>
        </el-table-column>
        <el-table-column label="销售总额" width="180" align="right">
          <template #default="{ row }"><span class="num amount">{{ formatMoney(row.totalAmount) }}</span></template>
        </el-table-column>
        <el-table-column label="业绩占比" min-width="200">
          <template #default="{ row }">
            <el-progress
              :percentage="max ? Math.round((row.totalAmount / max) * 100) : 0"
              :stroke-width="12"
              :show-text="false"
              color="#2563EB"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import BaseChart from '@/components/BaseChart.vue'
import { formatMoney } from '@/utils/format'
import { getEmployeeRank } from '@/api/dashboard'

const loading = ref(false)
const list = ref([])
const max = ref(0)
const option = ref({})

async function load() {
  loading.value = true
  try {
    const { data } = await getEmployeeRank()
    list.value = data
    max.value = Math.max(...data.map((e) => e.totalAmount), 0)
    option.value = {
      grid: { left: 68, right: 24, top: 30, bottom: 30 },
      tooltip: { trigger: 'axis', axisLabel: {}, backgroundColor: '#fff', borderColor: '#E2E8F0', textStyle: { color: '#1E293B' } },
      xAxis: {
        type: 'category', data: data.map((d) => d.name),
        axisLine: { lineStyle: { color: '#E2E8F0' } }, axisLabel: { color: '#475569' }
      },
      yAxis: {
        type: 'value', name: '金额(¥)', nameTextStyle: { color: '#94A3B8' },
        splitLine: { lineStyle: { color: '#EEF2F7' } }, axisLabel: { color: '#94A3B8' }
      },
      series: [
        {
          type: 'bar', data: data.map((d) => d.totalAmount), barWidth: 28,
          itemStyle: { color: '#2563EB', borderRadius: [8, 8, 0, 0] }
        }
      ]
    }
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped lang="scss">
.amount { color: var(--brand-primary); font-weight: 700; }
.rank-badge {
  display: inline-flex;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #eef2f7;
  color: var(--text-secondary);
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
  &.r-1 { background: #fef3c7; color: #b45309; }
  &.r-2 { background: #e2e8f0; color: #475569; }
  &.r-3 { background: #fde9d3; color: #c2740b; }
}
</style>
