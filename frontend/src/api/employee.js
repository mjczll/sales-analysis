// 员工管理（用户表 sys_user）
import request from '@/utils/request'
import { USE_MOCK, mockRequest, mockPage, like } from '@/utils/mock'
import { users } from '@/mock/data'

export function getEmployeeList(params = {}) {
  if (USE_MOCK) {
    const list = users.filter(
      (u) =>
        like(u.username, params.keyword) &&
        like(u.realName, params.keyword) &&
        like(u.phone, params.keyword) &&
        (params.roleId === undefined ||
          params.roleId === null ||
          params.roleId === '' ||
          u.roleId === Number(params.roleId)) &&
        (params.status === undefined ||
          params.status === null ||
          params.status === '' ||
          u.status === Number(params.status))
    )
    return mockRequest(mockPage(list, params))
  }
  return request({ url: '/api/employee/list', method: 'get', params })
}

export function updateEmployee(data) {
  if (USE_MOCK) {
    const u = users.find((x) => x.id === data.id)
    if (u) Object.assign(u, data)
    return mockRequest(true)
  }
  return request({ url: '/api/employee', method: 'put', data })
}

export function deleteEmployee(id, password) {
  if (USE_MOCK) {
    const idx = users.findIndex((u) => u.id === id)
    if (idx > -1) users.splice(idx, 1)
    return mockRequest(true)
  }
  return request({ url: `/api/employee/${id}`, method: 'delete', params: password ? { password } : {} })
}
