<template>
  <div class="page">
    <PageHeader title="个人资料" description="查看与修改个人基本信息" />

    <el-row :gutter="20" class="profile-row">
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="block profile-card">
          <div class="avatar-wrap">
            <el-avatar
              :size="72"
              class="avatar avatar-clickable"
              :src="avatarUrl || undefined"
              @click="triggerUpload"
            >
              {{ initial }}
            </el-avatar>
            <div class="profile-name">{{ form.realName || form.username }}</div>
            <StatusTag v-if="roleText" :text="roleText" :type="form.roleId === 1 ? 'warning' : 'primary'" />
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="onFileChange"
            />
          </div>
          <el-descriptions :column="1" border class="profile-meta">
            <el-descriptions-item label="用户名">{{ form.username }}</el-descriptions-item>
            <el-descriptions-item label="账号状态">{{ form.status === 0 ? '停用' : '正常' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="block profile-edit-card">
          <template #header>
            <div class="edit-header">
              <span>编辑资料</span>
              <el-button v-if="!editing" type="primary" plain size="small" @click="startEdit">修改</el-button>
            </div>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" style="max-width: 480px">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" :disabled="!editing" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" :disabled="!editing" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" :disabled="!editing" />
            </el-form-item>
            <el-form-item v-if="editing">
              <el-button type="primary" :loading="submitting" @click="submit">保存修改</el-button>
              <el-button @click="cancelEdit">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useUserStore } from '@/stores/user'
import { updateProfile, uploadAvatar } from '@/api/user'
import { getAllRoles } from '@/api/role'
import { resolveAvatarUrl } from '@/utils/format'

const userStore = useUserStore()
const formRef = ref(null)
const fileInput = ref(null)
const submitting = ref(false)
const editing = ref(false)
const form = reactive({ id: null, username: '', realName: '', phone: '', email: '', avatar: '', roleId: null, status: 1 })
// 编辑前的快照，用于取消时回滚
const snapshot = ref(null)

const initial = computed(() => (form.realName || form.username || '用').charAt(0))
const avatarUrl = computed(() => resolveAvatarUrl(form.avatar))
const roles = ref([])
const roleText = computed(() => {
  const r = roles.value.find((x) => x.id === Number(form.roleId))
  return r ? r.roleName : ''
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

// 触发本地文件选择
function triggerUpload() {
  fileInput.value && fileInput.value.click()
}

// 选择图片后上传，后端返回相对地址存入 form.avatar（对齐 avatar VARCHAR(255) 存地址的语义）
async function onFileChange(e) {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  if (!/^image\//.test(file.type)) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片不能超过 2MB')
    return
  }
  try {
    const res = await uploadAvatar(file)
    form.avatar = res.data.url
    // 同步到 userStore，确保头像立即生效、刷新后仍保留
    userStore.setUserInfo({ ...userStore.userInfo, avatar: res.data.url })
    ElMessage.success('头像上传成功')
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '头像上传失败')
  } finally {
    // 清空 value，便于连续选择同一文件
    e.target.value = ''
  }
}

function startEdit() {
  snapshot.value = { ...form }
  editing.value = true
}

function cancelEdit() {
  if (snapshot.value) {
    Object.assign(form, snapshot.value)
  }
  snapshot.value = null
  editing.value = false
  formRef.value?.clearValidate()
}

async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await updateProfile({
      id: form.id,
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      avatar: form.avatar
    })
    userStore.setUserInfo({
      ...userStore.userInfo,
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      avatar: form.avatar
    })
    ElMessage.success('资料已更新')
    snapshot.value = null
    editing.value = false
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  Object.assign(form, userStore.userInfo || {})
  try {
    const { data } = await getAllRoles()
    roles.value = data
  } catch (e) {}
})
</script>

<style scoped lang="scss">
.profile-row {
  align-items: stretch;
}
.profile-row :deep(.el-col) {
  display: flex;
}
.profile-card,
.profile-edit-card {
  display: flex;
  flex-direction: column;
  width: 100%;
}
.profile-card {
  text-align: center;
}
.edit-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.avatar-wrap {
  padding: 12px 0 20px;
}
.avatar {
  background: var(--brand-primary);
  color: #fff;
  font-size: 28px;
  font-weight: 700;
}
.avatar-clickable {
  cursor: pointer;
  transition: opacity 0.2s ease;
}
.avatar-clickable:hover {
  opacity: 0.82;
}
.profile-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 12px 0 10px;
}
.profile-meta {
  margin-top: 12px;
  text-align: left;
}
</style>
