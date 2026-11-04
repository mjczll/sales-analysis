<template>
  <div class="page" v-loading="loading">
    <PageHeader title="订单详情">
      <template #extra>
        <el-button :icon="ArrowLeft" @click="goBack">返回列表</el-button>
      </template>
    </PageHeader>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="never" class="block">
          <template #header><span>订单信息</span></template>
          <el-descriptions :column="3" border v-if="order">
            <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="客户名称">{{ order.customerName }}</el-descriptions-item>
            <el-descriptions-item label="销售人员">{{ order.salesman }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <StatusTag :text="orderStatusText(order.status)" :type="orderStatusTag(order.status)" />
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(order.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="订单金额">
              <span class="num amount">{{ formatMoney(order.totalAmount) }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header><span>订单商品明细</span></template>
      <el-table :data="order?.items || []" stripe>
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="productName" label="商品名称" min-width="200" />
        <el-table-column label="单价" width="140" align="right">
          <template #default="{ row }"><span class="num">{{ formatMoney(row.price) }}</span></template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" align="center" />
        <el-table-column label="小计" width="140" align="right">
          <template #default="{ row }"><span class="num">{{ formatMoney(row.amount) }}</span></template>
        </el-table-column>
      </el-table>
      <div class="total-row">
        <span>订单总金额：</span>
        <span class="num amount">{{ formatMoney(order?.totalAmount) }}</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { formatMoney, orderStatusText, orderStatusTag, formatDateTime } from '@/utils/format'
import { getOrderDetail } from '@/api/order'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const { data } = await getOrderDetail(route.params.id)
    order.value = data
  } finally {
    loading.value = false
  }
}
const goBack = () => router.push('/sales/order')

onMounted(load)
</script>

<style scoped lang="scss">
.amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--brand-primary);
}
.total-row {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 8px;
  margin-top: 16px;
  font-size: 14px;
  color: var(--text-secondary);
}
</style>
