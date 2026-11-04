// ============================================================
// Mock 数据基建
// 设计计划允许：后端未完成时使用明确标记的临时 Mock 数据，
// 且必须方便后续切换为真实 API。
//
// 切换方式：将项目根目录 .env 中 VITE_USE_MOCK 设为 false，
// 或将下方 USE_MOCK 改为 false，所有 api 模块即自动改走 request。
// ============================================================

// 默认开启 Mock；仅当显式设置为 'false' 时关闭
export const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false'

// 统一响应包装：与后端约定结构 { code, message, data } 一致
export function mockRequest(data, { delay = 280 } = {}) {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({ code: 200, message: 'success', data })
    }, delay)
  })
}

// 统一错误包装：结构对齐响应拦截器期望的 error.response.data.message
export function mockError(message, code = 500) {
  return Promise.reject({ response: { data: { code, message } } })
}

// 列表分页包装：与后端约定 { records, total, page, size } 一致
export function mockPage(list, { page = 1, size = 10 } = {}) {
  const total = list.length
  const start = (page - 1) * size
  const records = list.slice(start, start + size)
  return { records, total, page, size }
}

// 简单数组筛选辅助
export function like(value, keyword) {
  if (!keyword) return true
  return String(value ?? '').toLowerCase().includes(String(keyword).toLowerCase())
}
