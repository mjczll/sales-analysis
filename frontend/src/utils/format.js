// 通用格式化工具
export function formatMoney(n, symbol = '¥') {
  if (n === null || n === undefined || n === '' || isNaN(n)) return '-'
  return (
    symbol +
    Number(n).toLocaleString('zh-CN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })
  )
}

export function formatNumber(n) {
  if (n === null || n === undefined || n === '' || isNaN(n)) return '-'
  return Number(n).toLocaleString('zh-CN')
}

// 只显示日期（去掉时分秒）。兼容：
// - '2026-01-01T00:00:00'（后端 LocalDateTime 序列化）
// - '2026-01-01 00:00:00'（数据库/其它来源）
// - 已是 '2026-01-01' 则原样返回
export function formatDateTime(s) {
  if (!s) return '-'
  const str = String(s)
  // 截取前 10 位（yyyy-MM-dd），无论后面是 T 还是空格分隔
  const m = str.match(/^(\d{4}-\d{2}-\d{2})/)
  return m ? m[1] : str
}

// 订单状态：0待审核 1已审核 2已发货 3已完成 4已取消
export const ORDER_STATUS = [
  { text: '待审核', tag: 'warning' },
  { text: '已审核', tag: 'info' },
  { text: '已发货', tag: '' },
  { text: '已完成', tag: 'success' },
  { text: '已取消', tag: 'danger' }
]

export function orderStatusText(status) {
  return ORDER_STATUS[status]?.text ?? '未知'
}
export function orderStatusTag(status) {
  return ORDER_STATUS[status]?.tag ?? 'info'
}

// 解析头像地址：
// - 完整 URL（http/https/data/blob）直接返回
// - 相对地址（如 /upload/2026/11/05/xxx.png）：直接返回原路径，
//   由 vite 代理 /upload → 后端透传（见 vite.config.js），或生产环境由后端静态资源映射提供
export function resolveAvatarUrl(avatar) {
  if (!avatar) return ''
  if (/^(https?:|data:|blob:)/.test(avatar)) return avatar
  // 相对路径：直接返回，交给 vite 代理 / 后端静态映射处理
  return avatar
}
