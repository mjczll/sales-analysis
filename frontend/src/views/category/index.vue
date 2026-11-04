<template>
  <div class="page">
    <PageHeader title="商品分类管理" description="管理商品分类信息" />

    <div class="filter-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="分类名称">
          <el-input v-model="query.name" placeholder="请输入分类名称" clearable style="width: 220px" @keyup.enter="load" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load">查询</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right; margin-right: 0">
          <el-button type="primary" :icon="Plus" @click="openCreate">新增分类</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="分类名称" min-width="160" />
        <el-table-column prop="description" label="分类描述" min-width="240" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="120">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="460px" align-center>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入分类描述" />
        </el-form-item>
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
import { getCategoryList, createCategory, updateCategory, deleteCategory } from '@/api/category'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ name: '', page: 1, size: 10 })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, name: '', description: '' })
const rules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

async function load() {
  loading.value = true
  try {
    const { data } = await getCategoryList(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}
function reset() {
  query.name = ''
  query.page = 1
  load()
}
function openCreate() {
  dialogTitle.value = '新增分类'
  Object.assign(form, { id: null, name: '', description: '' })
  dialogVisible.value = true
}
function openEdit(row) {
  dialogTitle.value = '编辑分类'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}
async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) await updateCategory(form)
    else await createCategory(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}
async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除分类「${row.name}」吗？`, '提示', { type: 'warning' })
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    // 后端业务错误（如「该分类下还有商品」）展示具体提示
    if (e?.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    }
    // 取消 / 关闭确认框（'cancel' / 'close'）静默处理
  }
}

onMounted(load)
</script>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
