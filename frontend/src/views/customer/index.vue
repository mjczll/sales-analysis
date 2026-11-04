<template>
  <div class="page">
    <PageHeader title="客户管理" description="管理客户信息与分级" />

    <div class="filter-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="客户名称">
          <el-input v-model="query.name" placeholder="名称/联系人" clearable style="width: 200px" @keyup.enter="load" />
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select v-model="query.level" placeholder="全部" clearable style="width: 120px">
            <el-option label="普通" value="普通" />
            <el-option label="重要" value="重要" />
            <el-option label="核心" value="核心" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load">查询</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right; margin-right: 0">
          <el-button type="primary" :icon="Plus" @click="openCreate">新增客户</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="name" label="客户名称" min-width="220" show-overflow-tooltip />
        <el-table-column prop="contactPerson" label="联系人" width="110" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="level" label="等级" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.level === '核心' ? 'warning' : row.level === '重要' ? 'primary' : 'info'" effect="light" size="small">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="100" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <StatusTag :text="row.status === 1 ? '启用' : '停用'" :type="row.status === 1 ? 'success' : 'info'" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="openDetail(row)">详情</el-button>
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

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="客户详细资料" size="420px">
      <el-descriptions :column="1" border v-if="detailData">
        <el-descriptions-item label="客户名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detailData.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailData.email }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ detailData.address }}</el-descriptions-item>
        <el-descriptions-item label="客户等级">{{ detailData.level }}</el-descriptions-item>
        <el-descriptions-item label="客户来源">{{ detailData.source }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detailData.remark }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailData.status === 1 ? '启用' : '停用' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detailData.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" align-center>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="客户等级">
              <el-select v-model="form.level" style="width: 100%">
                <el-option label="普通" value="普通" />
                <el-option label="重要" value="重要" />
                <el-option label="核心" value="核心" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户来源">
              <el-select v-model="form.source" style="width: 100%">
                <el-option label="线上" value="线上" />
                <el-option label="线下" value="线下" />
                <el-option label="转介绍" value="转介绍" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
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
import { Search, RefreshLeft, Plus, Edit, Delete, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { getCustomerList, createCustomer, updateCustomer, deleteCustomer } from '@/api/customer'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ name: '', level: '', page: 1, size: 10 })

const detailVisible = ref(false)
const detailData = ref(null)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, name: '', contactPerson: '', phone: '', email: '', address: '',
  level: '普通', source: '线上', remark: '', status: 1
})
const rules = {
  name: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '联系电话格式不正确', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const { data } = await getCustomerList(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}
function reset() {
  query.name = ''
  query.level = ''
  query.page = 1
  load()
}
function openDetail(row) {
  detailData.value = row
  detailVisible.value = true
}
function openCreate() {
  dialogTitle.value = '新增客户'
  Object.assign(form, {
    id: null, name: '', contactPerson: '', phone: '', email: '', address: '',
    level: '普通', source: '线上', remark: '', status: 1
  })
  dialogVisible.value = true
}
function openEdit(row) {
  dialogTitle.value = '编辑客户'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}
async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) await updateCustomer(form)
    else await createCustomer(form)
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
    await ElMessageBox.confirm(`确认删除客户「${row.name}」吗？`, '提示', { type: 'warning' })
    await deleteCustomer(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    if (e?.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    }
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
