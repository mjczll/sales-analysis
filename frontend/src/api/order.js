// 销售订单管理
import request from '@/utils/request'
import { USE_MOCK, mockRequest, mockPage, like } from '@/utils/mock'
import { orders, orderItems, products, customers, users, customerName, userName } from '@/mock/data'

// 列表：返回订单，并附上客户名/下单人名（对齐文档：仅存 ID，展示层关联）
export function getOrderList(params = {}) {
  if (USE_MOCK) {
    let list = orders.filter(
      (o) =>
        like(o.orderNo, params.orderNo) &&
        (params.customerId === undefined || params.customerId === null || params.customerId === '' || o.customerId === Number(params.customerId)) &&
        (params.status === undefined ||
          params.status === null ||
          params.status === '' ||
          o.status === Number(params.status))
    )
    if (params.start && params.end) {
      list = list.filter(
        (o) => o.createTime.slice(0, 10) >= params.start && o.createTime.slice(0, 10) <= params.end
      )
    }
    // 客户名 / 下单人搜索：按 ID 反查后再过滤
    if (params.customerName) {
      const cidSet = new Set(
        customers.filter((c) => like(c.name, params.customerName)).map((c) => c.id)
      )
      list = list.filter((o) => cidSet.has(o.customerId))
    }
    if (params.salesmanName) {
      const uidSet = new Set(
        users.filter((u) => like(u.realName, params.salesmanName)).map((u) => u.id)
      )
      list = list.filter((o) => uidSet.has(o.userId))
    }
    const enriched = list.map((o) => ({
      ...o,
      customerName: customerName(o.customerId),
      salesman: userName(o.userId)
    }))
    return mockRequest(mockPage(enriched, params))
  }
  return request({ url: '/api/order/list', method: 'get', params })
}

export function getOrderDetail(id) {
  if (USE_MOCK) {
    const o = orders.find((x) => x.id === Number(id))
    if (!o) return mockRequest(null)
    const items = orderItems
      .filter((it) => it.orderId === o.id)
      .map((it) => ({
        id: it.id,
        orderId: it.orderId,
        productId: it.productId,
        productName: it.productName,
        price: it.price,
        quantity: it.quantity,
        amount: it.amount
      }))
    return mockRequest({
      ...o,
      customerName: customerName(o.customerId),
      salesman: userName(o.userId),
      items
    })
  }
  return request({ url: `/api/order/${id}`, method: 'get' })
}

// 新建订单：服务端计算总金额，前端仅传明细
export function createOrder(data) {
  if (USE_MOCK) {
    const items = (data.items || []).map((it) => {
      const p = products.find((x) => x.id === Number(it.productId))
      const price = p ? p.price : Number(it.price)
      const quantity = Number(it.quantity)
      return {
        productId: it.productId,
        productName: p ? p.name : it.productName,
        price,
        quantity,
        amount: Math.round(price * quantity * 100) / 100
      }
    })
    const totalAmount = Math.round(
      items.reduce((s, it) => s + it.amount, 0) * 100
    ) / 100
    const id = Math.max(0, ...orders.map((o) => o.id)) + 1
    const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
    const order = {
      id,
      orderNo: `SO${now.slice(0, 10).replace(/-/g, '')}${String(id).padStart(3, '0')}`,
      customerId: Number(data.customerId),
      userId: Number(data.userId) || 1,
      totalAmount,
      remark: data.remark || '',
      status: 0,
      createTime: now,
      updateTime: now
    }
    orders.unshift(order)
    // 写入明细表
    let nextItemId = Math.max(0, ...orderItems.map((x) => x.id)) + 1
    items.forEach((it) => {
      orderItems.push({
        id: nextItemId++,
        orderId: id,
        productId: it.productId,
        productName: it.productName,
        price: it.price,
        quantity: it.quantity,
        amount: it.amount
      })
    })
    return mockRequest({ id, totalAmount })
  }
  return request({ url: '/api/order', method: 'post', data })
}

export function updateOrderStatus(id, status) {
  if (USE_MOCK) {
    const o = orders.find((x) => x.id === id)
    if (o) o.status = Number(status)
    return mockRequest(true)
  }
  return request({ url: `/api/order/${id}/status`, method: 'put', params: { status } })
}

export function deleteOrder(id) {
  if (USE_MOCK) {
    const idx = orders.findIndex((o) => o.id === id)
    if (idx > -1) orders.splice(idx, 1)
    return mockRequest(true)
  }
  return request({ url: `/api/order/${id}`, method: 'delete' })
}
