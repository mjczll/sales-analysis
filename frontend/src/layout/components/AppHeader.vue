<template>
  <div class="app-header">
    <div class="header-left">
      <el-icon class="fold-btn" @click="$emit('toggle')">
        <component :is="collapse ? 'Expand' : 'Fold'" />
      </el-icon>
      <Breadcrumb />
    </div>

    <div class="header-right">
      <el-dropdown trigger="click">
        <span class="user-info">
          <el-avatar :size="34" class="user-avatar" :src="avatarUrl || undefined">
            {{ initial }}
          </el-avatar>
          <span class="user-name">{{ displayName }}</span>
          <el-icon><CaretBottom /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="goProfile">
              <el-icon><UserFilled /></el-icon>个人资料
            </el-dropdown-item>
            <el-dropdown-item @click="goPassword">
              <el-icon><Lock /></el-icon>修改密码
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { CaretBottom, UserFilled, Lock, SwitchButton } from '@element-plus/icons-vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import { useUserStore } from '@/stores/user'
import { resolveAvatarUrl } from '@/utils/format'
import { ElMessageBox } from 'element-plus'

defineProps({
  collapse: { type: Boolean, default: false }
})
defineEmits(['toggle'])

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo || {})
const displayName = computed(
  () => userInfo.value.realName || userInfo.value.username || '用户'
)
const initial = computed(() => (displayName.value || '用').charAt(0))
const avatarUrl = computed(() => resolveAvatarUrl(userInfo.value.avatar))

const goProfile = () => router.push('/profile')
const goPassword = () => router.push('/profile/password')

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确认退出登录吗？', '提示', { type: 'warning' })
    userStore.logout()
    router.push('/login')
  } catch (e) {
    /* 取消 */
  }
}
</script>

<style scoped lang="scss">
.app-header {
  height: var(--header-height);
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;

  .header-left {
    display: flex;
    align-items: center;
    height: 100%;

    .fold-btn {
      font-size: 20px;
      cursor: pointer;
      color: var(--text-secondary);
      margin-right: 14px;
      padding: 6px;
      border-radius: 6px;

      &:hover {
        background: var(--bg-page);
        color: var(--brand-primary);
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      outline: none;

      .user-avatar {
        background: var(--brand-primary);
        color: #fff;
        font-weight: 600;
      }

      .user-name {
        margin: 0 6px 0 10px;
        font-size: 14px;
        color: var(--text-primary);
      }

      .el-icon {
        color: var(--text-secondary);
      }
    }
  }
}
</style>
