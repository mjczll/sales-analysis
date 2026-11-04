// 商品管理
import request from '@/utils/request'
import { USE_MOCK, mockRequest, mockPage, like } from '@/utils/mock'
import { products, categories, categoryName } from '@/mock/data'

export function getProductList(params = {}) {
  if (USE_MOCK) {
    let list = products.filter(
      (p) =>
        like(p.name, params.name) &&
        like(p.code, params.code) &&
        (!params.categoryId || p.categoryId === Number(params.categoryId)) &&
        (params.status === undefined ||
          params.status === null ||
          params.status === '' ||
          p.status === Number(params.status))
    )
    // 展示层附上分类名（对齐文档：仅存 category_id）
    const enriched = list.map((p) => ({ ...p, categoryName: categoryName(p.categoryId) }))
    return mockRequest(mockPage(enriched, params))
  }
  return request({ url: '/api/product/list', method: 'get', params })
}

export function getProductDetail(id) {
  if (USE_MOCK) {
    const p = products.find((x) => x.id === Number(id))
    return mockRequest(p ? { ...p, categoryName: categoryName(p.categoryId) } : null)
  }
  return request({ url: `/api/product/${id}`, method: 'get' })
}

export function createProduct(data) {
  if (USE_MOCK) {
    const id = Math.max(0, ...products.map((p) => p.id)) + 1
    const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
    products.push({
      id,
      code: data.code,
      name: data.name,
      categoryId: Number(data.categoryId),
      spec: data.spec || '',
      unit: data.unit || '件',
      price: Number(data.price),
      costPrice: Number(data.costPrice || 0),
      stock: Number(data.stock || 0),
      status: data.status === undefined ? 1 : Number(data.status),
      remark: data.remark || '',
      createTime: now,
      updateTime: now
    })
    return mockRequest(true)
  }
  return request({ url: '/api/product', method: 'post', data })
}

export function updateProduct(data) {
  if (USE_MOCK) {
    const p = products.find((x) => x.id === data.id)
    if (p) {
      Object.assign(p, {
        ...data,
        categoryId: Number(data.categoryId),
        price: Number(data.price),
        costPrice: Number(data.costPrice || 0),
        stock: Number(data.stock || 0),
        status: Number(data.status),
        updateTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
      })
    }
    return mockRequest(true)
  }
  return request({ url: '/api/product', method: 'put', data })
}

export function deleteProduct(id) {
  if (USE_MOCK) {
    const idx = products.findIndex((p) => p.id === id)
    if (idx > -1) products.splice(idx, 1)
    return mockRequest(true)
  }
  return request({ url: `/api/product/${id}`, method: 'delete' })
}

export function updateProductStatus(id, status) {
  if (USE_MOCK) {
    const p = products.find((x) => x.id === id)
    if (p) p.status = Number(status)
    return mockRequest(true)
  }
  return request({ url: `/api/product/${id}/status`, method: 'put', params: { status } })
}

// 商品分类下拉
export function getProductCategories() {
  if (USE_MOCK) return mockRequest(categories)
  return request({ url: '/api/product/category/list', method: 'get' })
}
