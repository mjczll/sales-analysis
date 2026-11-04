<template>
  <div class="page">
    <PageHeader title="商品排行" description="分析商品销售数量与金额" />

    <el-card shadow="never" class="block">
      <template #header>
        <div class="card-head">
          <span>商品销售数量排行</span>
          <el-radio-group v-model="topN" size="small" @change="load">
            <el-radio-button :value="5">Top5</el-radio-button>
            <el-radio-button :value="10">Top10</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <BaseChart :option="option" height="380px" v-loading="loading" />
    </el-card>

    <el-card shadow="never">
      <template #header><span>商品销售明细</span></template>
      <el-table :data="rankList" stripe>
        <el-table-column label="排名" width="80" align="center">
          <template #default="{ $index }">
            <span class="rank-badge" :class="'r-' + ($index + 1)">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="220" show-overflow-tooltip />
        <el-table-column label="销售数量" width="140" align="right">
          <template #default="{ row }"><span class="num">{{ row.quantity }}</span></template>
        </el-table-column>
        <el-table-column label="销售金额" width="160" align="right">
          <template #default="{ row }"><span class="num">{{ formatMoney(row.amount) }}</span></template>
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
import { getProductRank } from '@/api/dashboard'

const loading = ref(false)
const topN = ref(10)
const rankList = ref([])
const option = ref({})

async function load() {
  loading.value = true
  try {
    const { data } = await getProductRank(topN.value)
    rankList.value = data
    const names = data.map((d) => d.name).reverse()
    const values = data.map((d) => d.quantity).reverse()
    option.value = {
      grid: { left: 160, right: 50, top: 20, bottom: 20 },
      tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#E2E8F0', textStyle: { color: '#1E293B' } },
      xAxis: { type: 'value', splitLine: { lineStyle: { color: '#EEF2F7' } }, axisLabel: { color: '#94A3B8' } },
      yAxis: { type: 'category', data: names, axisLine: { lineStyle: { color: '#E2E8F0' } }, axisLabel: { color: '#475569' } },
      series: [{ type: 'bar', data: values, barWidth: 16, itemStyle: { color: '#2563EB', borderRadius: [0, 8, 8, 0] } }]
    }
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped lang="scss">
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}
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
