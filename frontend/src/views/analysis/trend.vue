<template>
  <div class="page">
    <PageHeader title="销售趋势" description="按时间维度分析销售业绩" />

    <el-row :gutter="20" class="block">
      <el-col :xs="24" :sm="8">
        <StatCard label="销售总额" :value="formatMoney(summary.totalSales)" icon="Money" color="#2563EB" />
      </el-col>
      <el-col :xs="24" :sm="8">
        <StatCard label="订单数量" :value="summary.orderCount" icon="List" color="#16A34A" />
      </el-col>
      <el-col :xs="24" :sm="8">
        <StatCard label="平均订单金额" :value="formatMoney(summary.avgOrderAmount)" icon="Coin" color="#F59E0B" />
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <span>销售趋势</span>
          <div class="range">
            <el-radio-group v-model="range" size="small" @change="load">
              <el-radio-button :value="7">近7天</el-radio-button>
              <el-radio-button :value="30">近30天</el-radio-button>
              <el-radio-button :value="90">近90天</el-radio-button>
            </el-radio-group>
            <el-date-picker
              v-model="customRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              range-separator="至"
              start-placeholder="开始"
              end-placeholder="结束"
              size="small"
              style="width: 240px; margin-left: 10px"
              @change="onCustom"
            />
          </div>
        </div>
      </template>
      <BaseChart :option="option" height="380px" v-loading="loading" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import BaseChart from '@/components/BaseChart.vue'
import { formatMoney } from '@/utils/format'
import { getAnalysisSummary, getSalesTrend } from '@/api/dashboard'

const loading = ref(false)
const range = ref(30)
const customRange = ref([])
const summary = reactive({ totalSales: 0, orderCount: 0, avgOrderAmount: 0 })
const option = ref({})

async function load() {
  loading.value = true
  try {
    const sum = await getAnalysisSummary({ days: range.value })
    Object.assign(summary, sum.data)
    const trend = await getSalesTrend({ days: range.value })
    buildOption(trend.data)
  } finally {
    loading.value = false
  }
}
async function onCustom(val) {
  if (!val || val.length !== 2) return
  loading.value = true
  try {
    const trend = await getSalesTrend({ start: val[0], end: val[1] })
    buildOption(trend.data)
    const sum = await getAnalysisSummary({ start: val[0], end: val[1] })
    Object.assign(summary, sum.data)
  } finally {
    loading.value = false
  }
}
function buildOption(data) {
  option.value = {
    grid: { left: 68, right: 24, top: 30, bottom: 40 },
    tooltip: {
      trigger: 'axis', backgroundColor: '#fff', borderColor: '#E2E8F0', textStyle: { color: '#1E293B' }
    },
    xAxis: {
      type: 'category', data: data.map((d) => d.date.slice(5)),
      axisLine: { lineStyle: { color: '#E2E8F0' } }, axisLabel: { color: '#94A3B8' }
    },
    yAxis: {
      type: 'value', name: '金额(¥)', nameTextStyle: { color: '#94A3B8' },
      splitLine: { lineStyle: { color: '#EEF2F7' } }, axisLabel: { color: '#94A3B8' }
    },
    series: [
      {
        type: 'line', smooth: true, data: data.map((d) => d.amount),
        showSymbol: false, lineStyle: { color: '#2563EB', width: 2.5 },
        areaStyle: {
          color: {
            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(37,99,235,0.25)' },
              { offset: 1, color: 'rgba(37,99,235,0.02)' }
            ]
          }
        }
      }
    ]
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
.range {
  display: flex;
  align-items: center;
}
</style>
