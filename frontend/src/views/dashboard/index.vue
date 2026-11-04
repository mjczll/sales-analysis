<template>
  <div class="dashboard page">
    <PageHeader title="数据概览" description="欢迎回来，这里是企业销售业务数据概览" />

    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="block">
      <el-col :xs="24" :sm="12" :lg="6" v-for="c in statCards" :key="c.label">
        <StatCard :label="c.label" :value="c.value" :icon="c.icon" :color="c.color" :prefix="c.prefix" />
      </el-col>
    </el-row>

    <!-- 销售趋势 + 订单状态分布 -->
    <el-row :gutter="20" class="block">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-head">
              <span>销售趋势</span>
              <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
                <el-radio-button :value="7">近7天</el-radio-button>
                <el-radio-button :value="30">近30天</el-radio-button>
                <el-radio-button :value="90">近90天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <BaseChart :option="trendOption" height="320px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="chart-card">
          <template #header><span>订单状态分布</span></template>
          <BaseChart :option="statusOption" height="320px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 商品排行 + 员工业绩 -->
    <el-row :gutter="20" class="block">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-head">
              <span>商品销售排行</span>
              <el-radio-group v-model="rankTop" size="small" @change="loadProductRank">
                <el-radio-button :value="5">Top5</el-radio-button>
                <el-radio-button :value="10">Top10</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <BaseChart :option="productRankOption" height="340px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header><span>员工业绩排行</span></template>
          <div class="employee-list">
            <div v-for="(e, i) in employeeRank" :key="e.name" class="employee-item">
              <div class="employee-rank" :class="'rank-' + (i + 1)">{{ i + 1 }}</div>
              <div class="employee-info">
                <div class="employee-top">
                  <span class="employee-name">{{ e.name }}</span>
                  <span class="num employee-amount">{{ formatMoney(e.totalAmount) }}</span>
                </div>
                <el-progress
                  :percentage="maxEmp ? Math.round((e.totalAmount / maxEmp) * 100) : 0"
                  :stroke-width="8"
                  :show-text="false"
                  color="#2563EB"
                />
                <div class="employee-sub">订单 {{ e.orderCount }} 笔</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近订单 -->
    <el-card shadow="never" class="chart-card block">
      <template #header><span>最近销售订单</span></template>
      <el-table :data="recentOrders" stripe>
        <el-table-column prop="orderNo" label="订单编号" width="200" />
        <el-table-column prop="customerName" label="客户名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="salesman" label="销售人员" width="100" />
        <el-table-column label="订单金额" width="140" align="right">
          <template #default="{ row }"><span class="num">{{ formatMoney(row.totalAmount) }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <StatusTag :text="orderStatusText(row.status)" :type="orderStatusTag(row.status)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="120">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import BaseChart from '@/components/BaseChart.vue'
import StatusTag from '@/components/StatusTag.vue'
import {
  getOverviewData,
  getSalesTrend,
  getOrderStatusDistribution,
  getProductRank,
  getEmployeeRank,
  getRecentOrders
} from '@/api/dashboard'
import { formatMoney, orderStatusText, orderStatusTag, formatDateTime } from '@/utils/format'

const overview = reactive({ totalSales: 0, orderCount: 0, productCount: 0, customerCount: 0 })
const statCards = ref([])
const trendDays = ref(30)
const trendOption = ref({})
const statusOption = ref({})
const rankTop = ref(10)
const productRankOption = ref({})
const employeeRank = ref([])
const maxEmp = ref(0)
const recentOrders = ref([])

const STATUS_COLORS = ['#F59E0B', '#60A5FA', '#3B82F6', '#16A34A', '#EF4444']

async function loadOverview() {
  const { data } = await getOverviewData()
  Object.assign(overview, data)
  statCards.value = [
    { label: '销售总额', value: formatMoney(overview.totalSales), icon: 'Money', color: '#2563EB' },
    { label: '订单总数', value: overview.orderCount, icon: 'List', color: '#16A34A' },
    { label: '商品数量', value: overview.productCount, icon: 'Goods', color: '#F59E0B' },
    { label: '客户数量', value: overview.customerCount, icon: 'User', color: '#3B82F6' }
  ]
}

async function loadTrend() {
  const { data } = await getSalesTrend({ days: trendDays.value })
  trendOption.value = {
    grid: { left: 68, right: 24, top: 30, bottom: 30 },
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#E2E8F0',
      textStyle: { color: '#1E293B' }
    },
    xAxis: {
      type: 'category',
      data: data.map((d) => d.date.slice(5)),
      axisLine: { lineStyle: { color: '#E2E8F0' } },
      axisLabel: { color: '#94A3B8' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#EEF2F7' } },
      axisLabel: { color: '#94A3B8' }
    },
    series: [
      {
        type: 'line',
        smooth: true,
        data: data.map((d) => d.amount),
        showSymbol: false,
        lineStyle: { color: '#2563EB', width: 2.5 },
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

async function loadStatus() {
  const { data } = await getOrderStatusDistribution()
  statusOption.value = {
    tooltip: { trigger: 'item', backgroundColor: '#fff', borderColor: '#E2E8F0', textStyle: { color: '#1E293B' } },
    legend: { bottom: 0, textStyle: { color: '#64748B' } },
    color: STATUS_COLORS,
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        label: { show: false },
        data: data.map((d) => ({ name: d.name, value: d.value }))
      }
    ]
  }
}

async function loadProductRank() {
  const { data } = await getProductRank(rankTop.value)
  const names = data.map((d) => d.name).reverse()
  const values = data.map((d) => d.quantity).reverse()
  productRankOption.value = {
    grid: { left: 140, right: 40, top: 20, bottom: 20 },
    tooltip: {
      trigger: 'axis', backgroundColor: '#fff', borderColor: '#E2E8F0', textStyle: { color: '#1E293B' }
    },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: '#EEF2F7' } }, axisLabel: { color: '#94A3B8' } },
    yAxis: {
      type: 'category', data: names, axisLine: { lineStyle: { color: '#E2E8F0' } },
      axisLabel: { color: '#475569' }
    },
    series: [
      {
        type: 'bar',
        data: values,
        barWidth: 14,
        itemStyle: { color: '#2563EB', borderRadius: [0, 7, 7, 0] }
      }
    ]
  }
}

async function loadEmployee() {
  const { data } = await getEmployeeRank()
  employeeRank.value = data
  maxEmp.value = Math.max(...data.map((e) => e.totalAmount), 0)
}

async function loadRecent() {
  const { data } = await getRecentOrders(6)
  recentOrders.value = data
}

onMounted(() => {
  loadOverview()
  loadTrend()
  loadStatus()
  loadProductRank()
  loadEmployee()
  loadRecent()
})
</script>

<style scoped lang="scss">
.chart-card {
  margin-bottom: 0;
}
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.employee-list {
  padding: 4px 6px;
}
.employee-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);
  &:last-child {
    border-bottom: none;
  }
}
.employee-rank {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #eef2f7;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
  margin-right: 14px;
  flex-shrink: 0;
  &.rank-1 { background: #fef3c7; color: #b45309; }
  &.rank-2 { background: #e2e8f0; color: #475569; }
  &.rank-3 { background: #fde9d3; color: #c2740b; }
}
.employee-info {
  flex: 1;
}
.employee-top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 6px;
}
.employee-name {
  font-weight: 600;
  color: var(--text-primary);
}
.employee-amount {
  font-weight: 700;
  color: var(--brand-primary);
}
.employee-sub {
  font-size: 12px;
  color: var(--text-weak);
  margin-top: 4px;
}
</style>
