<template>
  <div class="app-sidebar" :class="{ 'is-collapse': collapse }">
    <div class="sidebar-logo">
      <img class="logo-img" :src="logoUrl" alt="销售业绩分析系统" />
      <span v-show="!collapse" class="logo-text">销售业绩分析系统</span>
    </div>

    <el-menu
      :default-active="activeMenu"
      :collapse="collapse"
      :collapse-transition="false"
      router
      class="sidebar-menu"
    >
      <template v-for="group in menus" :key="group.title">
        <el-sub-menu v-if="group.children" :index="group.title">
          <template #title>
            <el-icon><component :is="iconMap[group.icon]" /></el-icon>
            <span>{{ group.title }}</span>
          </template>
          <el-menu-item
            v-for="child in group.children"
            :key="child.path"
            :index="child.path"
          >
            <el-icon><component :is="iconMap[child.icon]" /></el-icon>
            <template #title>{{ child.title }}</template>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item v-else :index="group.path">
          <el-icon><component :is="iconMap[group.icon]" /></el-icon>
          <template #title>{{ group.title }}</template>
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ROLES } from '@/router'
import logoUrl from '@/assets/logo.svg'
import {
  Odometer,
  Files,
  Goods,
  User as UserIcon,
  List,
  Box,
  TrendCharts,
  Histogram,
  Medal,
  UserFilled,
  Lock
} from '@element-plus/icons-vue'

defineProps({
  collapse: { type: Boolean, default: false }
})

const route = useRoute()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)

// 当前用户角色
const roleId = computed(() => Number(userStore.userInfo?.roleId) || null)

const iconMap = {
  Odometer,
  Files,
  Goods,
  User: UserIcon,
  List,
  Box,
  TrendCharts,
  Histogram,
  Medal,
  UserFilled,
  Lock
}

// 菜单结构（贴合前端设计计划 3.3），roles 为空表示所有角色可见
const allMenus = [
  { title: '数据概览', path: '/dashboard', icon: 'Odometer', roles: [ROLES.ADMIN, ROLES.SALES, ROLES.STOCK] },
  {
    title: '业务管理',
    icon: 'Files',
    children: [
      { title: '商品分类', path: '/category', icon: 'Files', roles: [ROLES.ADMIN] },
      { title: '商品管理', path: '/product', icon: 'Goods', roles: [ROLES.ADMIN] },
      { title: '客户管理', path: '/customer', icon: 'User', roles: [ROLES.ADMIN] },
      { title: '员工管理', path: '/employee', icon: 'UserFilled', roles: [ROLES.ADMIN] },
      { title: '销售订单', path: '/sales/order', icon: 'List', roles: [ROLES.ADMIN, ROLES.SALES] },
      { title: '库存管理', path: '/stock', icon: 'Box', roles: [ROLES.ADMIN, ROLES.STOCK] }
    ]
  },
  {
    title: '数据分析',
    icon: 'TrendCharts',
    children: [
      { title: '销售趋势', path: '/analysis/trend', icon: 'TrendCharts', roles: [ROLES.ADMIN] },
      { title: '商品排行', path: '/analysis/product-rank', icon: 'Histogram', roles: [ROLES.ADMIN] },
      { title: '员工业绩', path: '/analysis/employee', icon: 'Medal', roles: [ROLES.ADMIN] }
    ]
  },
  {
    title: '个人中心',
    icon: 'UserFilled',
    children: [
      { title: '个人资料', path: '/profile', icon: 'UserFilled', roles: [ROLES.ADMIN, ROLES.SALES, ROLES.STOCK] },
      { title: '修改密码', path: '/profile/password', icon: 'Lock', roles: [ROLES.ADMIN, ROLES.SALES, ROLES.STOCK] }
    ]
  }
]

// 判断某项是否对当前角色可见
function visible(item) {
  if (!item.roles || item.roles.length === 0) return true
  return !!roleId.value && item.roles.includes(roleId.value)
}

// 过滤后的菜单
const menus = computed(() => {
  return allMenus
    .map((group) => {
      if (!group.children) return group
      const children = group.children.filter(visible)
      return { ...group, children }
    })
    .filter((group) => {
      if (group.children) return group.children.length > 0
      return visible(group)
    })
})
</script>

<style scoped lang="scss">
.app-sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-right: 1px solid var(--border-color);
  transition: width 0.25s ease;

  .sidebar-logo {
    height: var(--header-height);
    display: flex;
    align-items: center;
    padding: 0 18px;
    border-bottom: 1px solid var(--border-color);

    .logo-img {
      width: 32px;
      height: 32px;
      border-radius: 8px;
      flex-shrink: 0;
      object-fit: contain;
    }

    .logo-text {
      margin-left: 12px;
      font-size: 16px;
      font-weight: 700;
      color: var(--text-primary);
      white-space: nowrap;
    }
  }

  .sidebar-menu {
    border-right: none;
    flex: 1;
    overflow-y: auto;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      height: 48px;
      line-height: 48px;
      color: var(--text-secondary);

      .el-icon {
        color: var(--text-secondary);
      }

      &:hover {
        background: var(--brand-primary-light);
        color: var(--brand-primary);

        .el-icon {
          color: var(--brand-primary);
        }
      }
    }

    :deep(.el-menu-item.is-active) {
      background: var(--brand-primary-light);
      color: var(--brand-primary);
      font-weight: 600;
      border-right: 3px solid var(--brand-primary);

      .el-icon {
        color: var(--brand-primary);
      }
    }
  }
}

.app-sidebar.is-collapse {
  .sidebar-logo {
    justify-content: center;
    padding: 0;
  }
}
</style>
