// ============================================================
// Mock 数据集 —— 企业级销售业绩分析系统
// 字段严格对齐《数据库表结构设计文档》(库名 sales)：
//   sys_role    : id/role_name/role_code/description/create_time
//   sys_user    : id/username/password/real_name/phone/email/avatar/status/role_id/create_time/update_time
//   customer    : id/name/contact_person/phone/email/address/level/source/remark/status/create_time
//   category    : id/name/description/create_time
//   product     : id/code/name/category_id/spec/unit/price/cost_price/stock/status/remark/create_time/update_time
//   sales_order : id/order_no/customer_id/user_id/total_amount/remark/status/create_time/update_time
//   order_item  : id/order_id/product_id/product_name/price/quantity/amount
//   stock_record: id/product_id/type/quantity/remark/user_id/create_time
// 逻辑外键：只存 ID，展示层通过辅助查询关联名称。
// ============================================================

// ---------- 确定性随机（保证刷新数据稳定） ----------
function mulberry32(seed) {
  return function () {
    seed |= 0
    seed = (seed + 0x6d2b79f5) | 0
    let t = Math.imul(seed ^ (seed >>> 15), 1 | seed)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}
const rand = mulberry32(20261103)
const pick = (arr) => arr[Math.floor(rand() * arr.length)]
const between = (min, max) => Math.floor(rand() * (max - min + 1)) + min
const round2 = (n) => Math.round(n * 100) / 100

function fmtDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}
function fmtDateTime(d) {
  return `${fmtDate(d)} ${String(d.getHours()).padStart(2, '0')}:${String(
    d.getMinutes()
  ).padStart(2, '0')}`
}

// ---------- 基础字典 ----------
const CATEGORY_NAMES = [
  '手机数码', '电脑办公', '家用电器', '服饰鞋包', '食品生鲜',
  '美妆个护', '母婴用品', '运动户外', '图书文娱', '家居家装'
]
const CATEGORY_DESC = {
  手机数码: '手机、平板、耳机及数码配件',
  电脑办公: '笔记本、台式机及办公外设',
  家用电器: '大家电与小家电',
  服饰鞋包: '男女服装、鞋履与箱包',
  食品生鲜: '零食、饮料与生鲜',
  美妆个护: '护肤、彩妆与个人护理',
  母婴用品: '奶粉、尿裤与婴童用品',
  运动户外: '运动服饰与户外装备',
  图书文娱: '图书、文具与影音',
  家居家装: '家具、家纺与装修材料'
}
const SPEC_POOL = ['标准版', '豪华版', '青春版', '旗舰版', '基础款', 'Pro', 'Plus', 'Mini']
const UNIT_POOL = ['台', '件', '盒', '套', '箱', '包', '瓶']
// 客户等级：中文语义等级（对齐文档 level 默认「普通」）
const CUSTOMER_LEVELS = ['普通', '重要', '核心']
const CUSTOMER_SOURCES = ['线上', '线下', '转介绍']
// 各分类下的商品名称池（真实感命名，避免机械拼接）
const PRODUCT_NAMES = {
  手机数码: [
    'iPhone 15 Pro Max', '华为 Mate 60 Pro', '小米 14 Ultra',
    'OPPO Find X7', 'vivo X100 Pro', '荣耀 Magic6 Pro',
    'AirPods Pro 2 无线耳机', '索尼 WH-1000XM5 降噪耳机',
    'iPad Air 11 英寸', '小米平板 6S Pro', '三星 Galaxy S24'
  ],
  电脑办公: [
    '联想拯救者 Y9000P 游戏本', 'MacBook Pro 14 英寸',
    'ThinkPad X1 Carbon 商务本', '华硕 ROG 幻16',
    '戴尔 XPS 15 笔记本', '惠普战66 商务本',
    '罗技 MX Master 3S 鼠标', '机械师 K600 键盘',
    '明基 PD2705U 显示器', '西部数据 2TB 移动硬盘'
  ],
  家用电器: [
    '海尔 535L 对开门冰箱', '美的 1.5 匹变频空调',
    '格力云锦立式空调', '西门子 12 套洗碗机',
    '小天鹅滚筒洗衣机', '美的微波炉', '九阳破壁机',
    '戴森 V12 无线吸尘器', '苏泊尔电饭煲', '飞利浦空气炸锅'
  ],
  服饰鞋包: [
    '优衣库轻薄羽绒服', '耐克 Air Force 1 运动鞋',
    '阿迪达斯 Ultraboost 跑鞋', '李宁运动卫衣',
    '海澜之家商务衬衫', '波司登羽绒服',
    '新秀丽旅行拉杆箱', 'Coach 单肩手提包', 'Zara 牛仔外套'
  ],
  食品生鲜: [
    '五常大米 10kg 装', '金龙鱼花生油 5L',
    '三只松鼠坚果礼盒', '农夫山泉饮用水 24 瓶装',
    '伊利金典纯牛奶', '蒙牛特仑苏', '澳洲进口牛排',
    '阳澄湖大闸蟹礼盒', '良品铺子零食大礼包', '士力架巧克力'
  ],
  美妆个护: [
    '兰蔻小黑瓶精华', '雅诗兰黛小棕瓶眼霜',
    'SK-II 神仙水', '资生堂红腰子精华',
    '欧莱雅男士洁面', '完美日记口红套装',
    '海飞丝去屑洗发水', '飞利浦电动剃须刀', '花西子散粉'
  ],
  母婴用品: [
    '美素佳儿婴儿奶粉 3 段', '爱他美奶粉 2 段',
    '帮宝适纸尿裤', '好孩子婴儿推车',
    '贝亲婴儿奶瓶', '乐高积木玩具',
    '惠氏启赋奶粉', '英氏婴儿辅食米粉', '迪士尼儿童保温杯'
  ],
  运动户外: [
    '迪卡侬户外登山包', '北面冲锋衣',
    '始祖鸟软壳外套', '探路者帐篷',
    '斯伯丁篮球', '尤尼克斯羽毛球拍',
    'Keep 智能跑步机', '迪卡侬瑜伽垫', '杰士邦运动水壶'
  ],
  图书文娱: [
    '三体全集（典藏版）', '百年孤独', '活着',
    '哈利波特全集', '小王子', '万历十五年',
    '晨光中性笔 12 支装', '得力笔记本套装', '小米电纸书阅读器'
  ],
  家居家装: [
    '宜家毕利书架', '无印良品床品四件套',
    '全友实木餐桌', '顾家家居布艺沙发',
    '欧普照明吸顶灯', '飞利浦台灯',
    '乐扣乐扣保鲜盒套装', '双立人刀具套装', '网易严选乳胶枕'
  ]
}

// ---------- 角色表 sys_role ----------
export const roles = [
  { id: 1, roleName: '超级管理员', roleCode: 'ADMIN', description: '系统最高权限', createTime: fmtDateTime(new Date(2026, 0, 1, 9, 0)) },
  { id: 2, roleName: '销售员', roleCode: 'SALES', description: '负责销售订单', createTime: fmtDateTime(new Date(2026, 0, 1, 9, 0)) },
  { id: 3, roleName: '库存管理员', roleCode: 'STOCK', description: '负责库存与出入库', createTime: fmtDateTime(new Date(2026, 0, 1, 9, 0)) }
]

// ---------- 用户表 sys_user ----------
// roleId 关联 roles.id
export const users = [
  {
    id: 1,
    username: 'admin',
    password: '123456',
    realName: '系统管理员',
    phone: '13800000001',
    email: 'admin@sales.com',
    avatar: 'https://api.dicebear.com/7.x/initials/svg?seed=admin&backgroundColor=2563eb',
    status: 1,
    roleId: 1,
    createTime: fmtDateTime(new Date(2026, 0, 1, 9, 0)),
    updateTime: fmtDateTime(new Date(2026, 0, 1, 9, 0))
  },
  {
    id: 2,
    username: 'sales',
    password: '123456',
    realName: '张伟',
    phone: '13800000002',
    email: 'sales@sales.com',
    avatar: 'https://api.dicebear.com/7.x/initials/svg?seed=%E5%BC%A0%E4%BC%9F&backgroundColor=16a34a',
    status: 1,
    roleId: 2,
    createTime: fmtDateTime(new Date(2026, 0, 2, 9, 30)),
    updateTime: fmtDateTime(new Date(2026, 0, 2, 9, 30))
  },
  {
    id: 3,
    username: 'stock',
    password: '123456',
    realName: '王芳',
    phone: '13800000003',
    email: 'stock@sales.com',
    avatar: 'https://api.dicebear.com/7.x/initials/svg?seed=%E7%8E%8B%E8%8A%B3&backgroundColor=f59e0b',
    status: 1,
    roleId: 3,
    createTime: fmtDateTime(new Date(2026, 0, 3, 9, 30)),
    updateTime: fmtDateTime(new Date(2026, 0, 3, 9, 30))
  }
]
// 销售员列表（用于订单 user_id 关联，取角色为 SALES 的用户）
export const salesUsers = users.filter((u) => u.roleId === 2)

// ---------- 分类 category ----------
export const categories = CATEGORY_NAMES.map((name, i) => ({
  id: i + 1,
  name,
  description: CATEGORY_DESC[name],
  createTime: fmtDate(new Date(2026, 0, 1 + i))
}))

// ---------- 商品 product ----------
export const products = []
for (let i = 1; i <= 64; i++) {
  const cat = categories[i % categories.length]
  const namePool = PRODUCT_NAMES[cat.name]
  const name = namePool[(i - 1) % namePool.length]
  const spec = pick(SPEC_POOL)
  const unit = pick(UNIT_POOL)
  const price = round2(between(29, 4999) + rand())
  const costPrice = round2(price * (0.5 + rand() * 0.3))
  const stock = between(0, 480)
  products.push({
    id: i,
    code: `P${String(1000 + i)}`,
    name,
    categoryId: cat.id,
    spec,
    unit,
    price,
    costPrice,
    stock,
    status: rand() > 0.12 ? 1 : 0, // 1 上架 / 0 下架
    remark: '',
    createTime: fmtDateTime(new Date(2026, 0, 1 + (i % 28), between(8, 19), between(0, 59))),
    updateTime: fmtDateTime(new Date(2026, 0, 1 + (i % 28), between(8, 19), between(0, 59)))
  })
}

// ---------- 客户 customer ----------
const COMPANY_SUFFIX = ['科技有限公司', '贸易有限公司', '商贸行', '集团有限公司', '电子有限公司']
export const customers = []
for (let i = 1; i <= 42; i++) {
  const name = `${pick(['华', '盛', '宏', '瑞', '卓越', '鑫', '联', '恒'])}${pick(
    ['星', '源', '达', '通', '创', '辉', '宇', '信']
  )}${pick(COMPANY_SUFFIX)}`
  customers.push({
    id: i,
    name,
    contactPerson: pick(['赵经理', '钱经理', '孙主管', '周主任', '吴先生', '郑女士', '冯总']),
    phone: `1${pick(['3', '5', '7', '8'])}${String(between(100000000, 999999999))}`,
    email: `contact${i}@example.com`,
    address: '',
    level: pick(CUSTOMER_LEVELS),
    source: pick(CUSTOMER_SOURCES),
    remark: '',
    status: rand() > 0.1 ? 1 : 0,
    createTime: fmtDateTime(new Date(2026, 0, 1 + (i % 26), between(8, 18), 0))
  })
}

// ---------- 订单 sales_order + 订单明细 order_item ----------
const ORDER_STATUS = [0, 1, 2, 3, 4] // 0待审核 1已审核 2已发货 3已完成 4已取消
export const orders = []
export const orderItems = []
let orderSeq = 1
let itemSeq = 1
for (let i = 1; i <= 168; i++) {
  const customer = pick(customers)
  const salesman = pick(salesUsers) // 关联 sys_user.id
  const itemCount = between(1, 4)
  let total = 0
  for (let j = 0; j < itemCount; j++) {
    const p = pick(products)
    const quantity = between(1, 12)
    const amount = round2(p.price * quantity)
    total += amount
    orderItems.push({
      id: itemSeq++,
      orderId: i,
      productId: p.id,
      productName: p.name, // 快照，文档要求保留
      price: p.price, // 快照
      quantity,
      amount
    })
  }
  // 近 90 天内分布，越近越密集
  const daysAgo = Math.floor(Math.pow(rand(), 1.6) * 90)
  const d = new Date()
  d.setDate(d.getDate() - daysAgo)
  const status = daysAgo > 60 ? pick([3, 4, 1]) : pick(ORDER_STATUS)
  orders.push({
    id: i,
    orderNo: `SO${fmtDate(d).replace(/-/g, '')}${String(orderSeq++).padStart(3, '0')}`,
    customerId: customer.id,
    userId: salesman.id,
    totalAmount: round2(total),
    remark: '',
    status,
    createTime: fmtDateTime(d),
    updateTime: fmtDateTime(d)
  })
}
orders.sort((a, b) => (a.createTime < b.createTime ? 1 : -1))
orderItems.sort((a, b) => a.id - b.id)

// ---------- 库存流水 stock_record ----------
export const stockRecords = []
let srId = 1
for (let i = 0; i < 120; i++) {
  const p = pick(products)
  const type = rand() > 0.4 ? 1 : 2 // 1入库 2出库
  const quantity = between(10, 200)
  const op = pick(salesUsers)
  const d = new Date()
  d.setDate(d.getDate() - between(0, 60))
  stockRecords.push({
    id: srId++,
    productId: p.id,
    type,
    quantity,
    remark: type === 1 ? '采购入库' : '销售出库',
    userId: op.id,
    createTime: fmtDateTime(d)
  })
}
stockRecords.sort((a, b) => (a.createTime < b.createTime ? 1 : -1))

// ============================================================
// 关联查询辅助（展示层按 ID 取名称）
// ============================================================
export function categoryName(id) {
  const c = categories.find((x) => x.id === Number(id))
  return c ? c.name : ''
}
export function customerName(id) {
  const c = customers.find((x) => x.id === Number(id))
  return c ? c.name : ''
}
export function productName(id) {
  const p = products.find((x) => x.id === Number(id))
  return p ? p.name : ''
}
export function userName(id) {
  const u = users.find((x) => x.id === Number(id))
  return u ? u.realName : ''
}
export function roleName(id) {
  const r = roles.find((x) => x.id === Number(id))
  return r ? r.roleName : ''
}

// ============================================================
// 派生统计（供首页 / 分析页使用）
// ============================================================

// ============================================================
// 订单口径统一约定：
//   - 业绩订单（计入销售额/趋势/排行）：status ∈ {1已审核, 2已发货, 3已完成}
//   - 有效订单（计入订单总数）：status ≠ 4（含待审核，排除已取消）
// 状态映射：0待审核 1已审核 2已发货 3已完成 4已取消
// ============================================================
const SALE_STATUS = [1, 2, 3]
const isSale = (o) => SALE_STATUS.includes(o.status)
const isValid = (o) => o.status !== 4

// 业绩订单（已审核/已发货/已完成）
const saleOrders = orders.filter(isSale)
// 有效订单（非取消）
const validOrders = orders.filter(isValid)

export function getOverview() {
  const totalSales = round2(
    saleOrders.reduce((s, o) => s + o.totalAmount, 0)
  )
  return {
    totalSales,
    orderCount: validOrders.length,
    productCount: products.filter((p) => p.status === 1).length,
    customerCount: customers.filter((c) => c.status === 1).length
  }
}

// 近 N 天销售趋势（按日聚合，仅业绩订单）
export function getSalesTrend(days = 30) {
  const map = {}
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    map[fmtDate(d)] = { amount: 0, orderCount: 0 }
  }
  saleOrders.forEach((o) => {
    const key = o.createTime.slice(0, 10)
    if (map[key]) {
      map[key].amount += o.totalAmount
      map[key].orderCount += 1
    }
  })
  return Object.entries(map).map(([date, v]) => ({
    date,
    amount: round2(v.amount),
    orderCount: v.orderCount
  }))
}

export function getOrderStatusDistribution() {
  const labels = ['待审核', '已审核', '已发货', '已完成', '已取消']
  return ORDER_STATUS.map((s, i) => ({
    name: labels[i],
    value: orders.filter((o) => o.status === s).length
  }))
}

export function getProductRank(topN = 10) {
  const map = {}
  saleOrders.forEach((o) => {
    const its = orderItems.filter((it) => it.orderId === o.id)
    its.forEach((it) => {
      if (!map[it.productName]) map[it.productName] = { quantity: 0, amount: 0 }
      map[it.productName].quantity += it.quantity
      map[it.productName].amount += it.amount
    })
  })
  return Object.entries(map)
    .map(([name, v]) => ({ name, quantity: v.quantity, amount: round2(v.amount) }))
    .sort((a, b) => b.quantity - a.quantity)
    .slice(0, topN)
}

export function getEmployeeRank() {
  const map = {}
  saleOrders.forEach((o) => {
    const uname = userName(o.userId)
    if (!map[uname]) map[uname] = { orderCount: 0, totalAmount: 0 }
    map[uname].orderCount += 1
    map[uname].totalAmount += o.totalAmount
  })
  return Object.entries(map)
    .map(([name, v]) => ({
      name,
      orderCount: v.orderCount,
      totalAmount: round2(v.totalAmount)
    }))
    .sort((a, b) => b.totalAmount - a.totalAmount)
}

export function getRecentOrders(limit = 6) {
  return orders.slice(0, limit).map((o) => ({
    id: o.id,
    orderNo: o.orderNo,
    customerId: o.customerId,
    customerName: customerName(o.customerId),
    userId: o.userId,
    salesman: userName(o.userId),
    totalAmount: o.totalAmount,
    status: o.status,
    createTime: o.createTime
  }))
}

// 分析页汇总（近 N 天）
export function getAnalysisSummary(days = 30) {
  const trend = getSalesTrend(days)
  const totalSales = round2(trend.reduce((s, t) => s + t.amount, 0))
  const orderCount = trend.reduce((s, t) => s + t.orderCount, 0)
  const avgOrderAmount = orderCount ? round2(totalSales / orderCount) : 0
  return { totalSales, orderCount, avgOrderAmount, trend }
}

// 订单状态中文映射（全局复用）
export const ORDER_STATUS_TEXT = ['待审核', '已审核', '已发货', '已完成', '已取消']
export const ORDER_STATUS_TAG = ['warning', 'info', '', 'success', 'danger']
export const STOCK_STATUS = (stock) =>
  stock <= 0 ? { text: '缺货', type: 'danger' } : stock < 50 ? { text: '预警', type: 'warning' } : { text: '正常', type: 'success' }
