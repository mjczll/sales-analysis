<template>
  <div class="page">
    <PageHeader title="销售订单" description="创建与跟踪销售订单" />

    <div class="filter-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="订单编号">
          <el-input v-model="query.orderNo" placeholder="订单编号" clearable style="width: 170px" @keyup.enter="load" />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="query.customerName" placeholder="客户名称" clearable style="width: 150px" @keyup.enter="load" />
        </el-form-item>
        <el-form-item label="销售人员">
          <el-input v-model="query.salesmanName" placeholder="销售人员" clearable style="width: 120px" @keyup.enter="load" />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="(s, i) in statusOptions" :key="i" :label="s.text" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load">查询</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right; margin-right: 0">
          <el-button type="primary" :icon="Plus" @click="goCreate">新建订单</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单编号" width="190" />
        <el-table-column prop="customerName" label="客户名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="salesman" label="销售人员" width="100" />
        <el-table-column label="订单金额" width="140" align="right">
          <template #default="{ row }"><span class="num">{{ formatMoney(row.totalAmount) }}</span></template>
        </el-table-column>
        <el-table-column label="订单状态" width="100" align="center">
          <template #default="{ row }">
            <StatusTag :text="orderStatusText(row.status)" :type="orderStatusTag(row.status)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="120">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="op-btns">
              <el-button link type="primary" :icon="View" @click="goDetail(row)">详情</el-button>
              <el-button v-if="isAdmin" link type="primary" :icon="Edit" @click="openStatus(row)">改状态</el-button>
              <el-button v-if="isAdmin" link type="danger" :icon="Delete" @click="remove(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="load"
          @size-change="load"
        />
      </div>
    </el-card>

    <el-dialog v-model="statusVisible" title="修改订单状态" width="420px" align-center>
      <el-form label-width="90px">
        <el-form-item label="当前状态">
          <StatusTag :text="orderStatusText(currentRow?.status)" :type="orderStatusTag(currentRow?.status)" />
        </el-form-item>
        <el-form-item label="目标状态">
          <el-select v-model="targetStatus" style="width: 100%">
            <el-option v-for="(s, i) in statusOptions" :key="i" :label="s.text" :value="i" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitStatus">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, RefreshLeft, Plus, Edit, Delete, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { formatMoney, ORDER_STATUS, orderStatusText, orderStatusTag, formatDateTime } from '@/utils/format'
import { getOrderList, updateOrderStatus, deleteOrder } from '@/api/order'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => Number(userStore.userInfo?.roleId) === 1)
const statusOptions = ORDER_STATUS
const loading = ref(false)
const list = ref([])
const total = ref(0)
const dateRange = ref([])
const query = reactive({ orderNo: '', customerName: '', salesmanName: '', status: '', start: '', end: '', page: 1, size: 10 })

const statusVisible = ref(false)
const currentRow = ref(null)
const targetStatus = ref(0)
const submitting = ref(false)

async function load() {
  loading.value = true
  query.start = dateRange.value?.[0] || ''
  query.end = dateRange.value?.[1] || ''
  try {
    const { data } = await getOrderList(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}
function reset() {
  Object.assign(query, { orderNo: '', customerName: '', salesmanName: '', status: '', start: '', end: '' })
  dateRange.value = []
  query.page = 1
  load()
}
const goCreate = () => router.push('/sales/order/create')
const goDetail = (row) => router.push(`/sales/order/detail/${row.id}`)
function openStatus(row) {
  currentRow.value = row
  targetStatus.value = row.status
  statusVisible.value = true
}
async function submitStatus() {
  submitting.value = true
  try {
    await updateOrderStatus(currentRow.value.id, targetStatus.value)
    ElMessage.success('状态已更新')
    statusVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}
async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除订单「${row.orderNo}」吗？`, '提示', { type: 'warning' })
    await deleteOrder(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {}
}

onMounted(load)
</script>

<style scoped>
.op-btns {
  white-space: nowrap;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
