<template>
  <div class="page" v-loading="loading">
    <PageHeader title="新建销售订单">
      <template #extra>
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="block">
      <template #header><span>客户信息</span></template>
      <el-form label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="选择客户" required>
              <el-select v-model="form.customerId" placeholder="请选择客户" filterable style="width: 100%">
                <el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售人员">
              <!-- 销售员：锁死为本人，不可改 -->
              <el-select v-if="isSales" v-model="form.userId" placeholder="请选择销售人员" disabled style="width: 100%">
                <el-option :key="selfId" :label="selfName" :value="selfId" />
              </el-select>
              <!-- 管理员：可自由选择销售人员 -->
              <el-select v-else v-model="form.userId" placeholder="请选择销售人员" filterable style="width: 100%">
                <el-option v-for="s in salesUsers" :key="s.id" :label="s.realName" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="never" class="block">
      <template #header>
        <div class="card-head">
          <span>商品明细</span>
          <el-button type="primary" size="small" :icon="Plus" @click="addRow">添加商品</el-button>
        </div>
      </template>
      <el-table :data="form.items" border>
        <el-table-column label="商品" min-width="240">
          <template #default="{ row }">
            <el-select v-model="row.productId" placeholder="选择商品" filterable style="width: 100%" @change="onProductChange(row)">
              <el-option v-for="p in products" :key="p.id" :label="`${p.name}（${formatMoney(p.price)}）`" :value="p.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="150" align="right">
          <template #default="{ row }"><span class="num">{{ formatMoney(row.price) }}</span></template>
        </el-table-column>
        <el-table-column label="数量" width="160">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" :max="9999" @change="recalc" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="150" align="right">
          <template #default="{ row }"><span class="num">{{ formatMoney(row.price * row.quantity) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ $index }">
            <el-button link type="danger" :icon="Delete" @click="removeRow($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="block">
      <el-form label-width="90px">
        <el-form-item label="订单备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <div class="submit-bar">
        <div class="total">
          订单总金额：<span class="num amount">{{ formatMoney(totalAmount) }}</span>
        </div>
        <div>
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submit">提交订单</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { formatMoney } from '@/utils/format'
import { getCustomerList } from '@/api/customer'
import { getProductList } from '@/api/product'
import { createOrder } from '@/api/order'
import { getEmployeeList } from '@/api/employee'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
// 销售员（roleId=2）创建订单时，销售人员锁死为本人
const isSales = computed(() => Number(userStore.userInfo?.roleId) === 2)
const selfId = computed(() => Number(userStore.userInfo?.id))
const selfName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || '本人')
const loading = ref(false)
const submitting = ref(false)
const customers = ref([])
const products = ref([])
const salesUsers = ref([])

const form = reactive({
  customerId: '',
  userId: '',
  remark: '',
  items: []
})
const totalAmount = computed(() =>
  form.items.reduce((s, it) => s + (it.price || 0) * (it.quantity || 0), 0)
)

function addRow() {
  form.items.push({ productId: '', productName: '', price: 0, quantity: 1 })
}
function removeRow(index) {
  form.items.splice(index, 1)
}
function onProductChange(row) {
  const p = products.value.find((x) => x.id === row.productId)
  if (p) {
    row.price = p.price
    row.productName = p.name
  }
  recalc()
}
function recalc() {}

async function submit() {
  if (!form.customerId) return ElMessage.warning('请选择客户')
  if (form.items.length === 0) return ElMessage.warning('请至少添加一个商品')
  if (form.items.some((it) => !it.productId)) return ElMessage.warning('存在未选择商品的明细')
  submitting.value = true
  try {
    await createOrder({
      customerId: form.customerId,
      userId: form.userId,
      remark: form.remark,
      items: form.items.map((it) => ({ productId: it.productId, quantity: it.quantity }))
    })
    ElMessage.success('订单创建成功')
    router.push('/sales/order')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}
const goBack = () => router.push('/sales/order')

onMounted(async () => {
  loading.value = true
  try {
    const c = await getCustomerList({ page: 1, size: 200, status: 1 })
    customers.value = c.data.records
    const p = await getProductList({ page: 1, size: 300, status: 1 })
    products.value = p.data.records
    // 销售员下拉：roleId=2 的销售员
    const s = await getEmployeeList({ page: 1, size: 200, roleId: 2 })
    salesUsers.value = s.data.records
    // 销售员登录时，销售人员锁死为本人
    if (isSales.value) {
      form.userId = selfId.value
    }
    addRow()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}
.amount {
  font-size: 22px;
  font-weight: 700;
  color: var(--brand-primary);
}
.submit-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  .total {
    font-size: 15px;
    color: var(--text-secondary);
  }
}
</style>
