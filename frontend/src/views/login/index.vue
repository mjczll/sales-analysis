<template>
  <div class="login-page">
    <!-- 左侧品牌展示 -->
    <div class="login-banner">
      <div class="banner-inner">
        <div class="banner-logo">
          <img class="banner-logo-img" :src="logoUrl" alt="销售业绩分析系统" />
          <span>销售业绩分析系统</span>
        </div>
        <h1 class="banner-title">企业销售数据<br />一站式管理平台</h1>
        <p class="banner-sub">以数据驱动决策，让每一笔销售业绩清晰可见</p>
        <div class="banner-graphic">
          <div class="bar" v-for="(h, i) in bars" :key="i" :style="{ height: h + '%' }"></div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-form-wrap">
      <el-card class="login-card" shadow="never">
        <h2 class="login-heading">欢迎回来</h2>
        <p class="login-tip">请输入账号信息登录系统</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              :prefix-icon="Lock"
              size="large"
            />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form>

        <div class="login-footer">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
        <div class="login-demo">
          <div>演示账号（密码均为 123456）：</div>
          <div>admin / 123456（超级管理员）</div>
          <div>sales / 123456（销售员）</div>
          <div>stock / 123456（库存管理员）</div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'
import { useUserStore } from '@/stores/user'
import logoUrl from '@/assets/logo.svg'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const bars = [40, 65, 52, 80, 70, 95, 60, 88]

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    const { token, user } = res.data
    userStore.setToken(token)
    userStore.setUserInfo(user)
    userStore.setPermissions(['*'])
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;

  .login-banner {
    flex: 1;
    background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;

    .banner-inner {
      max-width: 460px;
      width: 100%;
    }

    .banner-logo {
      display: flex;
      align-items: center;
      font-size: 18px;
      font-weight: 700;
      margin-bottom: 48px;

      .banner-logo-img {
        width: 40px;
        height: 40px;
        border-radius: 12px;
        background: #fff;
        padding: 5px;
        box-sizing: border-box;
        margin-right: 12px;
        flex-shrink: 0;
      }
    }

    .banner-title {
      font-size: 34px;
      font-weight: 700;
      line-height: 1.3;
      margin: 0 0 16px;
    }

    .banner-sub {
      font-size: 15px;
      opacity: 0.85;
      margin: 0 0 56px;
    }

    .banner-graphic {
      display: flex;
      align-items: flex-end;
      gap: 14px;
      height: 160px;

      .bar {
        flex: 1;
        background: rgba(255, 255, 255, 0.22);
        border-radius: 6px 6px 0 0;
        transition: height 0.6s ease;
      }
    }
  }

  .login-form-wrap {
    width: 480px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff;

    .login-card {
      width: 360px;
      border: none;
      box-shadow: none;
    }

    .login-heading {
      font-size: 24px;
      font-weight: 700;
      color: var(--text-primary);
      margin: 0 0 6px;
    }
    .login-tip {
      font-size: 14px;
      color: var(--text-secondary);
      margin: 0 0 28px;
    }

    .login-btn {
      width: 100%;
      margin-top: 8px;
    }

    .login-footer {
      margin-top: 20px;
      font-size: 14px;
      color: var(--text-secondary);
      text-align: center;

      a {
        color: var(--brand-primary);
        text-decoration: none;
        font-weight: 500;
      }
    }

    .login-demo {
      margin-top: 14px;
      padding: 10px 12px;
      font-size: 12px;
      line-height: 1.8;
      color: var(--text-weak);
      text-align: left;
      background: var(--fill-light, #f5f7fa);
      border-radius: 8px;
    }
  }
}

@media (max-width: 768px) {
  .login-page .login-banner {
    display: none;
  }
  .login-page .login-form-wrap {
    width: 100%;
  }
}
</style>
