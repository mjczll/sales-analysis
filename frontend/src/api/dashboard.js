// 数据概览 / 销售分析（首页 + 分析页共用）
import request from '@/utils/request'
import { USE_MOCK, mockRequest } from '@/utils/mock'
import {
  getOverview as genOverview,
  getSalesTrend as genSalesTrend,
  getOrderStatusDistribution as genOrderStatus,
  getProductRank as genProductRank,
  getEmployeeRank as genEmployeeRank,
  getRecentOrders as genRecentOrders,
  getAnalysisSummary as genAnalysisSummary
} from '@/mock/data'

// 顶部四项统计
export function getOverviewData() {
  if (USE_MOCK) return mockRequest(genOverview())
  return request({ url: '/api/dashboard/overview', method: 'get' })
}

// 销售趋势（近 days 天；也支持 start/end 自定义）
export function getSalesTrend(params = {}) {
  if (USE_MOCK) {
    let days = params.days
    if (!days && params.start && params.end) {
      days = Math.max(
        1,
        Math.round((new Date(params.end) - new Date(params.start)) / 86400000) + 1
      )
    }
    return mockRequest(genSalesTrend(days || 30))
  }
  return request({ url: '/api/dashboard/sales-trend', method: 'get', params })
}

// 订单状态分布
export function getOrderStatusDistribution() {
  if (USE_MOCK) return mockRequest(genOrderStatus())
  return request({ url: '/api/dashboard/order-status', method: 'get' })
}

// 商品销售排行（Top N）
export function getProductRank(topN = 10) {
  if (USE_MOCK) return mockRequest(genProductRank(topN))
  return request({ url: '/api/dashboard/product-rank', method: 'get', params: { topN } })
}

// 员工业绩排行
export function getEmployeeRank() {
  if (USE_MOCK) return mockRequest(genEmployeeRank())
  return request({ url: '/api/dashboard/employee-rank', method: 'get' })
}

// 最近订单
export function getRecentOrders(limit = 6) {
  if (USE_MOCK) return mockRequest(genRecentOrders(limit))
  return request({ url: '/api/dashboard/recent-orders', method: 'get', params: { limit } })
}

// 分析页汇总（近 days 天：总额/订单数/均额 + 趋势；也支持 start/end 自定义区间）
export function getAnalysisSummary(params = {}) {
  if (USE_MOCK) {
    const days = params.days || 30
    return mockRequest(genAnalysisSummary(days))
  }
  return request({ url: '/api/dashboard/analysis', method: 'get', params })
}
