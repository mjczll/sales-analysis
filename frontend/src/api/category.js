// 商品分类管理
import request from '@/utils/request'
import { USE_MOCK, mockRequest, mockPage, like } from '@/utils/mock'
import { categories, products } from '@/mock/data'

export function getCategoryList(params = {}) {
  if (USE_MOCK) {
    const list = categories.filter((c) => like(c.name, params.name))
    return mockRequest(mockPage(list, params))
  }
  return request({ url: '/api/category/list', method: 'get', params })
}

export function getAllCategories() {
  if (USE_MOCK) return mockRequest(categories)
  return request({ url: '/api/category/all', method: 'get' })
}

export function createCategory(data) {
  if (USE_MOCK) {
    const id = Math.max(0, ...categories.map((c) => c.id)) + 1
    categories.push({
      id,
      name: data.name,
      description: data.description || '',
      createTime: new Date().toISOString().slice(0, 10)
    })
    return mockRequest(true)
  }
  return request({ url: '/api/category', method: 'post', data })
}

export function updateCategory(data) {
  if (USE_MOCK) {
    const c = categories.find((x) => x.id === data.id)
    if (c) Object.assign(c, data)
    return mockRequest(true)
  }
  return request({ url: '/api/category', method: 'put', data })
}

export function deleteCategory(id) {
  if (USE_MOCK) {
    // 与后端一致：分类下还有商品时禁止删除
    const count = products.filter((p) => p.categoryId === id).length
    if (count > 0) {
      return Promise.reject({
        response: { data: { message: `该分类下还有 ${count} 个商品，请先移除或调整这些商品的分类后再删除` } }
      })
    }
    const idx = categories.findIndex((x) => x.id === id)
    if (idx > -1) categories.splice(idx, 1)
    return mockRequest(true)
  }
  return request({ url: `/api/category/${id}`, method: 'delete' })
}
