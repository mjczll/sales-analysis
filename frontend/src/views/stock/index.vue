<template>
  <div class="page">
    <PageHeader title="库存管理" description="查看库存与记录出入库流水" />

    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <!-- 库存总览 -->
        <el-tab-pane label="库存总览" name="overview">
          <div class="filter-bar" style="margin-bottom: 16px">
            <el-form :inline="true" :model="ovQuery" @submit.prevent>
              <el-form-item label="商品名称">
                <el-input v-model="ovQuery.name" placeholder="名称/编号" clearable style="width: 200px" @keyup.enter="loadOverview" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :icon="Search" @click="loadOverview">查询</el-button>
                <el-button :icon="RefreshLeft" @click="ovQuery.name='';loadOverview()">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="overview" v-loading="ovLoading" stripe>
            <el-table-column prop="code" label="商品编号" width="110" />
            <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="categoryName" label="商品分类" width="120" />
            <el-table-column label="当前库存" width="120" align="right">
              <template #default="{ row }">
                <span class="num" :class="{ 'warn': row.statusType === 'warning', 'danger': row.statusType === 'danger' }">{{ row.stock }}</span>
              </template>
            </el-table-column>
            <el-table-column label="库存状态" width="100" align="center">
              <template #default="{ row }"><StatusTag :text="row.status" :type="row.statusType" /></template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="success" @click="openStock(row, 1)">入库</el-button>
                <el-button link type="primary" @click="openStock(row, 2)">出库</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination v-model:current-page="ovQuery.page" v-model:page-size="ovQuery.size" :total="ovTotal" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" background @current-change="loadOverview" @size-change="loadOverview" />
          </div>
        </el-tab-pane>

        <!-- 库存流水 -->
        <el-tab-pane label="库存流水" name="records">
          <div class="filter-bar" style="margin-bottom: 16px">
            <el-form :inline="true" :model="rcQuery" @submit.prevent>
              <el-form-item label="商品名称">
                <el-input v-model="rcQuery.productName" placeholder="商品名称" clearable style="width: 200px" @keyup.enter="loadRecords" />
              </el-form-item>
              <el-form-item label="变动类型">
                <el-select v-model="rcQuery.type" placeholder="全部" clearable style="width: 120px">
                  <el-option label="入库" :value="1" />
                  <el-option label="出库" :value="2" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :icon="Search" @click="loadRecords">查询</el-button>
                <el-button :icon="RefreshLeft" @click="rcQuery.productName='';rcQuery.type='';loadRecords()">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="records" v-loading="rcLoading" stripe>
            <el-table-column prop="productName" label="商品名称" min-width="200" show-overflow-tooltip />
            <el-table-column label="变动类型" width="100" align="center">
              <template #default="{ row }">
                <StatusTag :text="row.type === 1 ? '入库' : '出库'" :type="row.type === 1 ? 'success' : 'warning'" />
              </template>
            </el-table-column>
            <el-table-column label="变动数量" width="120" align="right">
              <template #default="{ row }"><span class="num" :class="row.type === 1 ? 'in' : 'out'">{{ row.type === 1 ? '+' : '-' }}{{ row.quantity }}</span></template>
            </el-table-column>
            <el-table-column prop="operator" label="操作人" width="100" />
            <el-table-column prop="remark" label="备注" min-width="140" />
            <el-table-column prop="createTime" label="变动时间" min-width="120">
              <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination v-model:current-page="rcQuery.page" v-model:page-size="rcQuery.size" :total="rcTotal" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" background @current-change="loadRecords" @size-change="loadRecords" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 出入库弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="440px" align-center>
      <el-form label-width="90px">
        <el-form-item label="商品">
          <el-select v-model="stockForm.productId" placeholder="选择商品" filterable style="width: 100%" :disabled="!!stockForm.fixedProduct">
            <el-option v-for="p in productOptions" :key="p.id" :label="`${p.name}（库存 ${p.stock}）`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="变动数量">
          <el-input-number v-model="stockForm.quantity" :min="1" :max="99999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockForm.remark" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitStock">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useUserStore } from '@/stores/user'
import { getStockOverview, getStockRecords, stockIn, stockOut } from '@/api/stock'
import { getProductList } from '@/api/product'
import { formatDateTime } from '@/utils/format'

const userStore = useUserStore()
const activeTab = ref('overview')

const ovLoading = ref(false)
const overview = ref([])
const ovTotal = ref(0)
const ovQuery = reactive({ name: '', page: 1, size: 10 })

const rcLoading = ref(false)
const records = ref([])
const rcTotal = ref(0)
const rcQuery = reactive({ productName: '', type: '', page: 1, size: 10 })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const productOptions = ref([])
const stockForm = reactive({ productId: '', quantity: 1, remark: '', type: 1, fixedProduct: false })

async function loadOverview() {
  ovLoading.value = true
  try {
    const { data } = await getStockOverview(ovQuery)
    overview.value = data.records
    ovTotal.value = data.total
  } finally {
    ovLoading.value = false
  }
}
async function loadRecords() {
  rcLoading.value = true
  try {
    const { data } = await getStockRecords(rcQuery)
    records.value = data.records
    rcTotal.value = data.total
  } finally {
    rcLoading.value = false
  }
}
function openStock(row, type) {
  dialogTitle.value = type === 1 ? '商品入库' : '商品出库'
  stockForm.type = type
  stockForm.quantity = 1
  stockForm.remark = type === 1 ? '采购入库' : '销售出库'
  if (row) {
    stockForm.productId = row.id
    stockForm.fixedProduct = true
  } else {
    stockForm.fixedProduct = false
  }
  dialogVisible.value = true
}
async function submitStock() {
  if (!stockForm.productId) return ElMessage.warning('请选择商品')
  if (!stockForm.quantity || stockForm.quantity < 1) return ElMessage.warning('请输入变动数量')
  submitting.value = true
  const payload = {
    productId: stockForm.productId,
    quantity: stockForm.quantity,
    remark: stockForm.remark,
    userId: userStore.userInfo?.id || 1
  }
  try {
    if (stockForm.type === 1) await stockIn(payload)
    else await stockOut(payload)
    ElMessage.success(stockForm.type === 1 ? '入库成功' : '出库成功')
    dialogVisible.value = false
    loadOverview()
    loadRecords()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const { data } = await getProductList({ page: 1, size: 300, status: 1 })
  productOptions.value = data.records
  loadOverview()
  loadRecords()
})
</script>

<style scoped lang="scss">
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.warn { color: var(--color-warning); font-weight: 600; }
.danger { color: var(--color-danger); font-weight: 700; }
.in { color: var(--color-success); font-weight: 600; }
.out { color: var(--color-warning); font-weight: 600; }
</style>
