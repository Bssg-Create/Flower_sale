-- =============================================
-- 花卉销售管理系统 - 数据库初始化脚本
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS flower_sales 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE flower_sales;

-- =============================================
-- 1. 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    user_type VARCHAR(20) DEFAULT 'user' COMMENT '用户类型(user/admin)',
    status VARCHAR(1) DEFAULT '1' COMMENT '状态(0禁用/1启用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_user_type (user_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 2. 角色表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    status VARCHAR(1) DEFAULT '1' COMMENT '状态(0禁用/1启用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =============================================
-- 3. 用户角色关联表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- =============================================
-- 4. 花卉分类表
-- =============================================
CREATE TABLE IF NOT EXISTS flower_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description VARCHAR(500) COMMENT '分类描述',
    icon VARCHAR(255) COMMENT '分类图标',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status VARCHAR(1) DEFAULT '1' COMMENT '状态(0禁用/1启用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='花卉分类表';

-- =============================================
-- 5. 花卉表
-- =============================================
CREATE TABLE IF NOT EXISTS flower (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '花卉名称',
    category_id BIGINT COMMENT '分类ID',
    category_name VARCHAR(100) COMMENT '分类名称',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    description TEXT COMMENT '花卉描述',
    image_url VARCHAR(500) COMMENT '图片URL',
    stock INT DEFAULT 0 COMMENT '库存数量',
    status VARCHAR(1) DEFAULT '1' COMMENT '状态(0禁用/1启用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category_id (category_id),
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='花卉表';

-- =============================================
-- 6. 订单表
-- =============================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总额',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '订单状态(pending/payed/shipped/completed/canceled)',
    pay_status VARCHAR(20) DEFAULT 'unpaid' COMMENT '支付状态(unpaid/paid/refunded)',
    shipping_address VARCHAR(500) COMMENT '收货地址',
    receiver_name VARCHAR(50) COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) COMMENT '收货人电话',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- =============================================
-- 7. 订单明细表
-- =============================================
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    flower_id BIGINT NOT NULL COMMENT '花卉ID',
    flower_name VARCHAR(100) COMMENT '花卉名称',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小计',
    INDEX idx_order_id (order_id),
    INDEX idx_flower_id (flower_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- =============================================
-- 8. DIY花束表
-- =============================================
CREATE TABLE IF NOT EXISTS diy_bouquet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(100) COMMENT '花束名称',
    package_type VARCHAR(50) COMMENT '包装类型',
    total_price DECIMAL(10,2) DEFAULT 0 COMMENT '总价',
    image_url VARCHAR(500) COMMENT '效果图URL',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态(draft/saved/submitted)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DIY花束表';

-- =============================================
-- 9. DIY花束明细表
-- =============================================
CREATE TABLE IF NOT EXISTS diy_bouquet_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    bouquet_id BIGINT NOT NULL COMMENT '花束ID',
    flower_id BIGINT NOT NULL COMMENT '花卉ID',
    flower_name VARCHAR(100) COMMENT '花卉名称',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    position VARCHAR(500) COMMENT '位置信息(JSON格式)',
    INDEX idx_bouquet_id (bouquet_id),
    INDEX idx_flower_id (flower_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DIY花束明细表';

-- =============================================
-- 10. 包装类型表
-- =============================================
CREATE TABLE IF NOT EXISTS package_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '包装名称',
    description VARCHAR(500) COMMENT '包装描述',
    image_url VARCHAR(500) COMMENT '包装效果图URL',
    price DECIMAL(10,2) DEFAULT 0 COMMENT '包装价格',
    status VARCHAR(1) DEFAULT '1' COMMENT '状态(0禁用/1启用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='包装类型表';

-- =============================================
-- 插入初始数据
-- =============================================

-- 插入角色数据
INSERT INTO sys_role (role_name, role_code, description) VALUES 
('管理员', 'admin', '系统管理员'),
('普通用户', 'user', '普通用户');

-- 插入管理员用户（密码：123456，已加密）
INSERT INTO sys_user (username, password, phone, email, user_type, status) VALUES 
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '13900000000', 'admin@flower.com', 'admin', '1');

-- 关联管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES 
(1, 1);

-- 插入花卉分类数据
INSERT INTO flower_category (name, description, icon, sort_order) VALUES 
('玫瑰', '玫瑰花是爱情的象征，多种颜色可选', '🌹', 1),
('百合', '百合花代表纯洁、高贵', '💮', 2),
('郁金香', '郁金香是荷兰国花，优雅高贵', '🌷', 3),
('向日葵', '向日葵象征阳光、希望', '🌻', 4),
('康乃馨', '康乃馨是母爱的象征', '🌺', 5),
('配叶', '用于花束搭配的绿叶植物', '🌿', 10);

-- 插入花卉数据
INSERT INTO flower (name, category_id, category_name, price, description, image_url, stock) VALUES 
('红玫瑰', 1, '玫瑰', 15.00, '精选优质红玫瑰，鲜艳欲滴，浪漫之选', '/images/red-rose.png', 100),
('白玫瑰', 1, '玫瑰', 16.00, '纯洁优雅的白玫瑰，象征纯洁的爱情', '/images/white-rose.png', 80),
('粉玫瑰', 1, '玫瑰', 15.00, '温柔甜美的粉玫瑰，适合表白', '/images/pink-rose.png', 90),
('黄玫瑰', 1, '玫瑰', 14.00, '明亮的黄玫瑰，代表友谊', '/images/yellow-rose.png', 70),
('白百合', 2, '百合', 20.00, '洁白无瑕的百合花，高贵典雅', '/images/white-lily.png', 60),
('粉百合', 2, '百合', 22.00, '粉嫩的百合花，浪漫温馨', '/images/pink-lily.png', 50),
('红郁金香', 3, '郁金香', 18.00, '热情奔放的红郁金香', '/images/red-tulip.png', 80),
('粉郁金香', 3, '郁金香', 18.00, '温柔甜美的粉郁金香', '/images/pink-tulip.png', 75),
('黄郁金香', 3, '郁金香', 17.00, '明亮灿烂的黄郁金香', '/images/yellow-tulip.png', 70),
('向日葵', 4, '向日葵', 12.00, '阳光般的向日葵，充满活力', '/images/sunflower.png', 120),
('康乃馨', 5, '康乃馨', 10.00, '温馨的康乃馨，送给最爱的人', '/images/carnation.png', 150),
('尤加利叶', 6, '配叶', 5.00, '优雅的尤加利叶，增加花束层次感', '/images/eucalyptus.png', 200),
('小雏菊', 6, '配叶', 8.00, '可爱的小雏菊，清新淡雅', '/images/daisy.png', 100);

-- 插入包装类型数据
INSERT INTO package_type (name, description, image_url, price) VALUES 
('圆形包装', '经典圆形花束包装，优雅大方', '/images/round-pack.png', 10.00),
('心形包装', '浪漫心形花束包装，适合表白', '/images/heart-pack.png', 15.00),
('长形包装', '简约长形花束包装，时尚现代', '/images/long-pack.png', 12.00),
('礼盒包装', '精美礼盒包装，送礼首选', '/images/gift-pack.png', 20.00);

-- =============================================
-- 创建存储过程和触发器（可选）
-- =============================================

-- 更新订单状态触发器
DELIMITER //
CREATE TRIGGER update_order_status AFTER UPDATE ON orders
FOR EACH ROW
BEGIN
    IF NEW.status = 'completed' THEN
        UPDATE flower f 
        JOIN order_item oi ON f.id = oi.flower_id
        SET f.stock = f.stock - oi.quantity
        WHERE oi.order_id = NEW.id;
    END IF;
END //
DELIMITER ;

-- =============================================
-- 创建视图（可选）
-- =============================================

-- 用户订单统计视图
CREATE VIEW v_user_order_stats AS
SELECT 
    u.id AS user_id,
    u.username,
    COUNT(o.id) AS order_count,
    SUM(o.total_amount) AS total_amount
FROM sys_user u
LEFT JOIN orders o ON u.id = o.user_id
GROUP BY u.id, u.username;

-- 花卉销售统计视图
CREATE VIEW v_flower_sales_stats AS
SELECT 
    f.id AS flower_id,
    f.name AS flower_name,
    SUM(oi.quantity) AS sales_count,
    SUM(oi.total_price) AS sales_amount
FROM flower f
LEFT JOIN order_item oi ON f.id = oi.flower_id
GROUP BY f.id, f.name;

-- =============================================
-- 脚本执行完成
-- =============================================
SELECT '数据库初始化完成' AS message;