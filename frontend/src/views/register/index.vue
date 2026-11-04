<template>
  <div class="register-page">
    <el-card class="register-card" shadow="never">
      <div class="register-logo">
        <img class="logo-img" :src="logoUrl" alt="销售业绩分析系统" />
        <span>销售业绩分析系统</span>
      </div>
      <h2 class="register-title">创建账号</h2>
      <p class="register-tip">注册后即可使用销售业绩分析平台</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" :prefix-icon="Postcard" size="large" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" :prefix-icon="Iphone" size="large" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" :prefix-icon="Message" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" :prefix-icon="Lock" size="large" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm">
          <el-input v-model="form.confirm" type="password" show-password placeholder="请再次输入密码" :prefix-icon="Lock" size="large" />
        </el-form-item>
        <el-button type="primary" size="large" class="register-btn" :loading="loading" @click="handleRegister">
          注 册
        </el-button>
      </el-form>

      <div class="register-footer">
        已有账号？<router-link to="/login">返回登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Postcard, Iphone, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'
import { useUserStore } from '@/stores/user'
import logoUrl from '@/assets/logo.svg'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  password: '',
  confirm: ''
})
const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  confirm: [{ required: true, validator: validateConfirm, trigger: 'blur' }]
}

const handleRegister = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await register({ ...form })
    const { token, user } = res.data
    userStore.setToken(token)
    userStore.setUserInfo(user)
    ElMessage.success('注册成功')
    router.push('/dashboard')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
  padding: 24px;

  .register-card {
    width: 420px;
    border-radius: var(--radius-card);
    padding: 12px 24px;
  }

  .register-logo {
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 8px;

    .logo-img {
      width: 32px;
      height: 32px;
      border-radius: 8px;
      margin-right: 10px;
    }
  }

  .register-title {
    text-align: center;
    font-size: 22px;
    font-weight: 700;
    margin: 4px 0 4px;
  }
  .register-tip {
    text-align: center;
    font-size: 13px;
    color: var(--text-secondary);
    margin: 0 0 20px;
  }
  .register-btn {
    width: 100%;
  }
  .register-footer {
    text-align: center;
    margin-top: 16px;
    font-size: 14px;
    color: var(--text-secondary);

    a {
      color: var(--brand-primary);
      text-decoration: none;
    }
  }
}
</style>
