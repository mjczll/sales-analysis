<template>
  <div class="page">
    <PageHeader title="员工管理" description="管理系统用户与角色分配" />

    <div class="filter-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="用户名/姓名/手机号" clearable style="width: 200px" @keyup.enter="load" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.roleId" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load">查询</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="员工" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="employee-cell">
              <el-avatar :size="32" :src="resolveAvatarUrl(row.avatar) || undefined">
                {{ (row.realName || row.username || '用').charAt(0) }}
              </el-avatar>
              <div class="employee-name">
                <div class="name">{{ row.realName }}</div>
                <div class="username">{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="row.roleId === 1 ? 'warning' : row.roleId === 3 ? 'success' : 'primary'" effect="light" size="small">
              {{ roleName(row.roleId) || '—' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <StatusTag :text="row.status === 1 ? '启用' : '停用'" :type="row.status === 1 ? 'success' : 'info'" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="120">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" align-center>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名">
          <el-input :model-value="form.username" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input :model-value="form.realName" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input :model-value="form.phone" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input :model-value="form.email" disabled />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
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
import { useRouter } from 'vue-router'
import { Search, RefreshLeft, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { getEmployeeList, updateEmployee, deleteEmployee } from '@/api/employee'
import { getAllRoles } from '@/api/role'
import { resolveAvatarUrl, formatDateTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const roles = ref([])
const query = reactive({ keyword: '', roleId: '', status: '', page: 1, size: 10 })

// 根据 roleId 取角色名（从后端角色列表查找）
function roleName(id) {
  const r = roles.value.find((x) => x.id === Number(id))
  return r ? r.roleName : ''
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, roleId: 2, status: 1,
  username: '', realName: '', phone: '', email: ''
})
const rules = {
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    const { data } = await getEmployeeList(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}
function reset() {
  query.keyword = ''
  query.roleId = ''
  query.status = ''
  query.page = 1
  load()
}
function openEdit(row) {
  dialogTitle.value = '编辑员工'
  Object.assign(form, {
    id: row.id, roleId: row.roleId, status: row.status,
    username: row.username || '', realName: row.realName || '',
    phone: row.phone || '', email: row.email || ''
  })
  dialogVisible.value = true
}
async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await updateEmployee(form)
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
  // 判断是否删除自己
  const isSelf = row.id === userStore.userInfo.id
  try {
    if (isSelf) {
      // 删除自己：需输入密码二次确认
      const { value } = await ElMessageBox.prompt(
        '删除自己的账号将立即退出登录，且无法撤销。请输入登录密码确认：',
        '删除自己的账号',
        {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'warning',
          inputType: 'password',
          inputPlaceholder: '请输入登录密码',
          inputValidator: (v) => (v ? true : '请输入密码'),
          distinguishCancelAndClose: true
        }
      )
      await deleteEmployee(row.id, value)
      ElMessage.success('账号已删除，正在退出登录')
      userStore.logout()
      router.push('/login')
    } else {
      await ElMessageBox.confirm(`确认删除员工「${row.realName}」吗？`, '提示', { type: 'warning' })
      await deleteEmployee(row.id)
      ElMessage.success('删除成功')
      load()
    }
  } catch (e) {
    // 密码错误等业务错误（axios 返回 message）
    if (e?.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    }
    // 取消 / 关闭对话框（'cancel' / 'close'）静默处理
  }
}

onMounted(async () => {
  // 加载角色列表（下拉/名称展示）
  try {
    const { data } = await getAllRoles()
    roles.value = data
  } catch (e) {}
  load()
})
</script>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.employee-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.employee-name .name {
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
}
.employee-name .username {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
