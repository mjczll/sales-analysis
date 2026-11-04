<template>
  <div class="page">
    <PageHeader title="修改密码" description="定期修改密码以保障账号安全" />

    <el-card shadow="never" class="block" style="max-width: 520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirm">
          <el-input v-model="form.confirm" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">提交修改</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/user'

const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirm: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== form.newPassword) callback(new Error('两次输入的新密码不一致'))
  else callback()
}
const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, message: '新密码至少 6 位', trigger: 'blur' }],
  confirm: [{ required: true, validator: validateConfirm, trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await changePassword({
      id: userStore.userInfo?.id,
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })
    ElMessage.success('密码修改成功')
    reset()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '修改失败')
  } finally {
    submitting.value = false
  }
}
function reset() {
  formRef.value?.resetFields()
}
</script>
