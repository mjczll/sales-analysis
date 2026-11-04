-- ============================================================
-- 企业级销售业绩分析系统 —— 数据库表结构
-- 数据库：MySQL 8.0 · 库名：sales · 字符集：utf8mb4 · 引擎：InnoDB
-- 严格对齐前端 src/mock/data.js 的 8 张表（唯一标准）
--   sys_role    : 角色表
--   sys_user    : 用户表
--   category    : 商品分类表
--   product     : 商品表
--   customer    : 客户表
--   sales_order : 销售订单表
--   order_item  : 订单明细表
--   stock_record: 库存流水表
-- 设计要点：逻辑外键（只存 ID，不建 FOREIGN KEY），应用层保证一致性
-- ============================================================

CREATE DATABASE IF NOT EXISTS sales DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE sales;

-- ------------------------------------------------------------
-- 1. 角色表 sys_role
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_name   VARCHAR(50)  NOT NULL                COMMENT '角色名称',
    role_code   VARCHAR(50)  NOT NULL                COMMENT '角色编码（如 ADMIN）',
    description VARCHAR(200) DEFAULT NULL            COMMENT '描述',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- ------------------------------------------------------------
-- 2. 用户表 sys_user
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL                COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL                COMMENT '密码（MD5 加密）',
    real_name   VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
    phone       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    email       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    avatar      VARCHAR(255) DEFAULT NULL            COMMENT '头像地址',
    status      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1 启用 / 0 禁用',
    role_id     BIGINT       DEFAULT NULL            COMMENT '角色 ID（逻辑外键 → sys_role.id）',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ------------------------------------------------------------
-- 3. 商品分类表 category
-- ------------------------------------------------------------
DROP TABLE IF EXISTS category;
CREATE TABLE category (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(50)  NOT NULL                COMMENT '分类名称',
    description VARCHAR(200) DEFAULT NULL            COMMENT '描述',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类表';

-- ------------------------------------------------------------
-- 4. 商品表 product
-- ------------------------------------------------------------
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    code        VARCHAR(50)   NOT NULL                COMMENT '商品编码',
    name        VARCHAR(100)  NOT NULL                COMMENT '商品名称',
    category_id BIGINT        DEFAULT NULL            COMMENT '分类 ID（逻辑外键 → category.id）',
    spec        VARCHAR(100)  DEFAULT NULL            COMMENT '规格型号',
    unit        VARCHAR(20)   DEFAULT '件'            COMMENT '单位',
    price       DECIMAL(12,2) NOT NULL DEFAULT 0      COMMENT '销售单价',
    cost_price  DECIMAL(12,2) NOT NULL DEFAULT 0      COMMENT '成本价',
    stock       INT           NOT NULL DEFAULT 0      COMMENT '当前库存',
    status      TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：1 上架 / 0 下架',
    remark      VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品表';

-- ------------------------------------------------------------
-- 5. 客户表 customer
-- ------------------------------------------------------------
DROP TABLE IF EXISTS customer;
CREATE TABLE customer (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name           VARCHAR(100) NOT NULL                COMMENT '客户名称',
    contact_person VARCHAR(50)  DEFAULT NULL            COMMENT '联系人',
    phone          VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
    email          VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    address        VARCHAR(255) DEFAULT NULL            COMMENT '地址',
    level          VARCHAR(20)  DEFAULT '普通'          COMMENT '客户等级',
    source         VARCHAR(50)  DEFAULT NULL            COMMENT '客户来源',
    remark         VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    status         TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1 正常 / 0 停用',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='客户表';

-- ------------------------------------------------------------
-- 6. 销售订单表 sales_order
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sales_order;
CREATE TABLE sales_order (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no     VARCHAR(50)   NOT NULL                COMMENT '订单编号（SO + 时间戳 + 随机数）',
    customer_id  BIGINT        NOT NULL                COMMENT '客户 ID（逻辑外键 → customer.id）',
    user_id      BIGINT        DEFAULT NULL            COMMENT '下单人 / 销售员 ID（逻辑外键 → sys_user.id）',
    total_amount DECIMAL(14,2) NOT NULL DEFAULT 0      COMMENT '订单总金额（冗余，下单时汇总）',
    remark       VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
    status       TINYINT       NOT NULL DEFAULT 0      COMMENT '状态：0 待审核 / 1 已审核 / 2 已发货 / 3 已完成 / 4 已取消',
    create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    update_time  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_customer (customer_id),
    KEY idx_status (status),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='销售订单表';

-- ------------------------------------------------------------
-- 7. 订单明细表 order_item
-- ------------------------------------------------------------
DROP TABLE IF EXISTS order_item;
CREATE TABLE order_item (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id     BIGINT        NOT NULL                COMMENT '订单 ID（逻辑外键 → sales_order.id）',
    product_id   BIGINT        NOT NULL                COMMENT '商品 ID（逻辑外键 → product.id）',
    product_name VARCHAR(100)  NOT NULL                COMMENT '商品名称（快照，防改名影响历史订单）',
    price        DECIMAL(12,2) NOT NULL DEFAULT 0      COMMENT '成交单价（快照）',
    quantity     INT           NOT NULL DEFAULT 1      COMMENT '数量',
    amount       DECIMAL(14,2) NOT NULL DEFAULT 0      COMMENT '小计金额 = price × quantity',
    PRIMARY KEY (id),
    KEY idx_order (order_id),
    KEY idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单明细表';

-- ------------------------------------------------------------
-- 8. 库存流水表 stock_record
-- ------------------------------------------------------------
DROP TABLE IF EXISTS stock_record;
CREATE TABLE stock_record (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    product_id  BIGINT       NOT NULL                COMMENT '商品 ID（逻辑外键 → product.id）',
    type        TINYINT      NOT NULL                COMMENT '类型：1 入库 / 2 出库',
    quantity    INT          NOT NULL                COMMENT '变动数量（正数）',
    remark      VARCHAR(255) DEFAULT NULL            COMMENT '备注（如「销售出库：SOxxx」）',
    user_id     BIGINT       DEFAULT NULL            COMMENT '操作人 ID（逻辑外键 → sys_user.id）',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_product (product_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='库存流水表';
