// 角色管理
import request from '@/utils/request'

// 获取所有角色（下拉/展示用）
export function getAllRoles() {
  return request({ url: '/api/role/all', method: 'get' })
}

// 分页列表
export function getRoleList(params = {}) {
  return request({ url: '/api/role/list', method: 'get', params })
}
