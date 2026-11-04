// 用户 / 鉴权 / 个人中心
import request from '@/utils/request'
import { USE_MOCK, mockRequest, mockError } from '@/utils/mock'
import { users } from '@/mock/data'

// 登录：返回 { token, user }
export function login(form) {
  if (USE_MOCK) {
    const u = users.find(
      (x) => x.username === form.username && x.password === form.password
    )
    if (!u) return mockError('用户名或密码错误')
    const { password, ...user } = u
    return mockRequest({ token: 'mock-token-' + u.id, user })
  }
  return request({ url: '/api/auth/login', method: 'post', data: form })
}

// 注册
export function register(form) {
  if (USE_MOCK) {
    if (users.some((x) => x.username === form.username)) {
      return mockError('该用户名已存在')
    }
    const id = Math.max(...users.map((u) => u.id)) + 1
    const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
    const user = {
      id,
      username: form.username,
      realName: form.realName || form.username,
      phone: form.phone || '',
      email: form.email || '',
      avatar: '',
      roleId: 2,
      status: 1,
      createTime: now,
      updateTime: now
    }
    users.push({ ...user, password: form.password })
    return mockRequest({ token: 'mock-token-' + id, user })
  }
  return request({ url: '/api/auth/register', method: 'post', data: form })
}

// 获取用户信息
export function getInfo(username) {
  if (USE_MOCK) {
    const u = users.find((x) => x.username === username) || users[0]
    const { password, ...user } = u
    return mockRequest(user)
  }
  return request({ url: `/api/user/info/${username}`, method: 'get' })
}

// 获取权限 / 菜单
export function getUserPermissions(username) {
  if (USE_MOCK) {
    return mockRequest({ permissions: ['*'], menus: [] })
  }
  return request({ url: `/api/user/permissions/${username}`, method: 'get' })
}

// 修改个人资料
export function updateProfile(data) {
  if (USE_MOCK) {
    const u = users.find((x) => x.id === data.id)
    if (u) Object.assign(u, data)
    return mockRequest(true)
  }
  return request({ url: '/api/user/profile', method: 'put', data })
}

// 上传头像：后端保存文件并返回相对地址（如 /uploads/avatar/xxx.png）
export function uploadAvatar(file) {
  if (USE_MOCK) {
    // 模拟后端保存文件后返回相对地址，与真实接口返回结构一致
    const ext = (file.name.split('.').pop() || 'png').toLowerCase()
    const url = `/uploads/avatar/${Date.now()}_${Math.floor(Math.random() * 10000)}.${ext}`
    return mockRequest({ url })
  }
  const formData = new FormData()
  formData.append('file', file)
  return request({ url: '/api/user/avatar', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}

// 修改密码
export function changePassword(data) {
  if (USE_MOCK) {
    const u = users.find((x) => x.id === data.id)
    if (u && data.oldPassword && u.password !== data.oldPassword) {
      return mockError('原密码不正确')
    }
    if (u) u.password = data.newPassword
    return mockRequest(true)
  }
  return request({ url: '/api/user/password', method: 'put', data })
}
