// 库存管理：库存总览 + 库存流水 + 出入库
import request from '@/utils/request'
import { USE_MOCK, mockRequest, mockPage, like } from '@/utils/mock'
import { products, stockRecords, STOCK_STATUS, categoryName, productName, userName } from '@/mock/data'

// 库存总览
export function getStockOverview(params = {}) {
  if (USE_MOCK) {
    const list = products
      .filter((p) => like(p.name, params.name) || like(p.code, params.code))
      .map((p) => ({
        id: p.id,
        code: p.code,
        name: p.name,
        categoryName: categoryName(p.categoryId),
        stock: p.stock,
        status: STOCK_STATUS(p.stock).text,
        statusType: STOCK_STATUS(p.stock).type
      }))
    return mockRequest(mockPage(list, params))
  }
  return request({ url: '/api/stock/overview', method: 'get', params })
}

// 库存流水
export function getStockRecords(params = {}) {
  if (USE_MOCK) {
    let list = stockRecords.filter(
      (r) =>
        like(productName(r.productId), params.productName) &&
        (!params.type || r.type === Number(params.type))
    )
    const enriched = list.map((r) => ({
      ...r,
      productName: productName(r.productId),
      operator: userName(r.userId)
    }))
    return mockRequest(mockPage(enriched, params))
  }
  return request({ url: '/api/stock/records', method: 'get', params })
}

// 入库
export function stockIn(data) {
  if (USE_MOCK) {
    const p = products.find((x) => x.id === Number(data.productId))
    if (p) p.stock += Number(data.quantity)
    stockRecords.unshift({
      id: Math.max(0, ...stockRecords.map((r) => r.id)) + 1,
      productId: Number(data.productId),
      type: 1,
      quantity: Number(data.quantity),
      remark: data.remark || '采购入库',
      userId: Number(data.userId) || 1,
      createTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
    })
    return mockRequest(true)
  }
  return request({ url: '/api/stock/in', method: 'post', data })
}

// 出库
export function stockOut(data) {
  if (USE_MOCK) {
    const p = products.find((x) => x.id === Number(data.productId))
    if (!p) return mockRequest(true)
    if (p.stock < Number(data.quantity)) {
      return Promise.reject({
        response: { data: { message: '库存不足，禁止出库' } }
      })
    }
    p.stock -= Number(data.quantity)
    stockRecords.unshift({
      id: Math.max(0, ...stockRecords.map((r) => r.id)) + 1,
      productId: Number(data.productId),
      type: 2,
      quantity: Number(data.quantity),
      remark: data.remark || '销售出库',
      userId: Number(data.userId) || 1,
      createTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
    })
    return mockRequest(true)
  }
  return request({ url: '/api/stock/out', method: 'post', data })
}
