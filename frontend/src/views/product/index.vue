<template>
  <div class="page">
    <PageHeader title="商品管理" description="管理商品信息与库存" />

    <div class="filter-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="商品名称">
          <el-input v-model="query.name" placeholder="名称/编号" clearable style="width: 180px" @keyup.enter="load" />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 160px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="在售" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load">查询</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right; margin-right: 0">
          <el-button type="primary" :icon="Plus" @click="openCreate">新增商品</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="code" label="商品编号" width="110" />
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="spec" label="规格" width="90" />
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column label="销售价格" width="120" align="right">
          <template #default="{ row }"><span class="num">{{ formatMoney(row.price) }}</span></template>
        </el-table-column>
        <el-table-column label="成本价格" width="120" align="right">
          <template #default="{ row }"><span class="num text-secondary">{{ formatMoney(row.costPrice) }}</span></template>
        </el-table-column>
        <el-table-column label="当前库存" width="110" align="right">
          <template #default="{ row }">
            <span class="num" :class="{ 'low-stock': row.stock < 50, 'out-stock': row.stock <= 0 }">{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <StatusTag :text="row.status === 1 ? '在售' : '下架'" :type="row.status === 1 ? 'success' : 'info'" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >{{ row.status === 1 ? '下架' : '上架' }}</el-button>
            <el-button link type="danger" :icon="Delete" @click="remove(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" align-center>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="商品编号" prop="code">
              <el-input v-model="form.code" placeholder="如 P1001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择" style="width: 100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位">
              <el-input v-model="form.unit" placeholder="如 件" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格">
              <el-input v-model="form.spec" placeholder="如 标准版" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成本价格">
              <el-input-number v-model="form.costPrice" :min="0" :precision="2" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="初始库存">
              <el-input-number v-model="form.stock" :min="0" :step="10" :disabled="!!form.id" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">在售</el-radio>
                <el-radio :value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="商品备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, RefreshLeft, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { formatMoney } from '@/utils/format'
import {
  getProductList,
  getProductCategories,
  createProduct,
  updateProduct,
  deleteProduct,
  updateProductStatus
} from '@/api/product'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const categories = ref([])
const query = reactive({ name: '', categoryId: '', status: '', page: 1, size: 10 })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, code: '', name: '', categoryId: '', spec: '', unit: '件',
  price: 0, costPrice: 0, stock: 0, status: 1, remark: ''
})
const rules = {
  code: [{ required: true, message: '请输入商品编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入销售价格', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const { data } = await getProductList(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}
function reset() {
  query.name = ''
  query.categoryId = ''
  query.status = ''
  query.page = 1
  load()
}
function openCreate() {
  dialogTitle.value = '新增商品'
  Object.assign(form, {
    id: null, code: '', name: '', categoryId: '', spec: '', unit: '件',
    price: 0, costPrice: 0, stock: 0, status: 1, remark: ''
  })
  dialogVisible.value = true
}
function openEdit(row) {
  dialogTitle.value = '编辑商品'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}
async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) await updateProduct(form)
    else await createProduct(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}
async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  try {
    await updateProductStatus(row.id, next)
    ElMessage.success(next === 1 ? '已上架' : '已下架')
    load()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}
async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除商品「${row.name}」吗？`, '提示', { type: 'warning' })
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    if (e?.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    }
  }
}

onMounted(async () => {
  const { data } = await getProductCategories()
  categories.value = data
  load()
})
</script>

<style scoped lang="scss">
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.low-stock { color: var(--color-warning); font-weight: 600; }
.out-stock { color: var(--color-danger); font-weight: 600; }
</style>
