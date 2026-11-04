// 客户管理
import request from '@/utils/request'
import { USE_MOCK, mockRequest, mockPage, like } from '@/utils/mock'
import { customers } from '@/mock/data'

export function getCustomerList(params = {}) {
  if (USE_MOCK) {
    const list = customers.filter(
      (c) =>
        like(c.name, params.name) &&
        like(c.contactPerson, params.contactPerson) &&
        (!params.level || c.level === params.level) &&
        (params.status === undefined ||
          params.status === null ||
          params.status === '' ||
          c.status === Number(params.status))
    )
    return mockRequest(mockPage(list, params))
  }
  return request({ url: '/api/customer/list', method: 'get', params })
}

export function getCustomerDetail(id) {
  if (USE_MOCK) return mockRequest(customers.find((c) => c.id === id) || null)
  return request({ url: `/api/customer/${id}`, method: 'get' })
}

export function createCustomer(data) {
  if (USE_MOCK) {
    const id = Math.max(0, ...customers.map((c) => c.id)) + 1
    customers.push({
      id,
      ...data,
      status: data.status === undefined ? 1 : Number(data.status),
      createTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
    })
    return mockRequest(true)
  }
  return request({ url: '/api/customer', method: 'post', data })
}

export function updateCustomer(data) {
  if (USE_MOCK) {
    const c = customers.find((x) => x.id === data.id)
    if (c) Object.assign(c, data)
    return mockRequest(true)
  }
  return request({ url: '/api/customer', method: 'put', data })
}

export function deleteCustomer(id) {
  if (USE_MOCK) {
    const idx = customers.findIndex((c) => c.id === id)
    if (idx > -1) customers.splice(idx, 1)
    return mockRequest(true)
  }
  return request({ url: `/api/customer/${id}`, method: 'delete' })
}
