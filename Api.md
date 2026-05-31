# AquaBackEnd API 接口文档

> 基于 Spring Boot 2.7 + MyBatis-Plus + Spring Security + JWT
>
> 基础路径: `http://localhost:8899/Aqua`

---

## 目录

- [AquaBackEnd API 接口文档](#aquabackend-api-接口文档)
  - [目录](#目录)
  - [一、通用说明](#一通用说明)
    - [请求头要求](#请求头要求)
    - [统一响应格式](#统一响应格式)
  - [二、公开接口（无需 Token）](#二公开接口无需-token)
    - [1. 获取验证码](#1-获取验证码)
    - [2. 用户登录](#2-用户登录)
    - [3. 用户注册](#3-用户注册)
    - [4. 添加用户](#4-添加用户)
  - [三、认证接口（需 Token）](#三认证接口需-token)
    - [5. 获取当前用户信息](#5-获取当前用户信息)
    - [6. 退出登录](#6-退出登录)
  - [四、管理接口（需 ADMIN 角色 + Token）](#四管理接口需-admin-角色--token)
    - [7. 管理员权限测试](#7-管理员权限测试)
    - [8. 用户列表](#8-用户列表)
    - [9. 用户详情](#9-用户详情)
    - [10. 更新用户](#10-更新用户)
    - [11. 删除用户](#11-删除用户)
  - [五、数据概览（Dashboard）](#五数据概览dashboard)
    - [12. 获取数据概览](#12-获取数据概览)
  - [六、放养记录管理（Stocking）](#六放养记录管理stocking)
    - [13. 分页查询放养记录](#13-分页查询放养记录)
    - [14. 新增放养记录](#14-新增放养记录)
    - [15. 编辑放养记录](#15-编辑放养记录)
    - [16. 删除放养记录](#16-删除放养记录)
    - [16b. 按塘口查询放养记录](#16b-按塘口查询放养记录)
  - [七、饲料库存管理（Feed）](#七饲料库存管理feed)
    - [17. 查询饲料库存](#17-查询饲料库存)
    - [18. 饲料入库](#18-饲料入库)
    - [19. 饲料出库](#19-饲料出库)
    - [20. 出入库流水明细](#20-出入库流水明细)
  - [八、塘口管理（Pond）](#八塘口管理pond)
    - [21. 分页查询塘口列表](#21-分页查询塘口列表)
    - [22. 新增塘口](#22-新增塘口)
    - [23. 编辑塘口](#23-编辑塘口)
    - [24. 删除塘口](#24-删除塘口)
    - [25. 获取塘口详情](#25-获取塘口详情)
  - [九、统计数据（Statistic）](#九统计数据statistic)
    - [26. 塘口统计数据](#26-塘口统计数据)
    - [27. 放养统计数据](#27-放养统计数据)
  - [十、数据模型](#十数据模型)
    - [10.1 统一响应（ApiResponse）](#101-统一响应apiresponse)
    - [10.2 用户实体（User）](#102-用户实体user)
    - [10.3 登录请求（LoginRequest）](#103-登录请求loginrequest)
    - [10.4 注册请求（RegisterRequest）](#104-注册请求registerrequest)
    - [10.5 创建用户请求（CreateUserRequest）](#105-创建用户请求createuserrequest)
    - [10.6 脱敏用户信息（UserInfo）](#106-脱敏用户信息userinfo)
    - [10.7 放养记录请求（StockingRecordDTO）](#107-放养记录请求stockingrecorddto)
    - [10.8 饲料入库请求（FeedInStockDTO）](#108-饲料入库请求feedinstockdto)
    - [10.9 饲料出库请求（FeedOutStockDTO）](#109-饲料出库请求feedoutstockdto)
    - [10.10 放养记录实体（FarmingBatch）](#1010-放养记录实体farmingbatch)
    - [10.11 塘口请求（PondDTO）](#1011-塘口请求ponddto)
    - [10.12 塘口实体（Pond）](#1012-塘口实体pond)
    - [10.13 品种实体（Breed）](#1013-品种实体breed)
    - [10.14 塘口统计响应（PondStatisticVO）](#1014-塘口统计响应pondstatisticvo)
    - [10.15 放养统计响应（StockingStatisticVO）](#1015-放养统计响应stockingstatisticvo)
  - [十一、错误码说明](#十一错误码说明)
    - [常见业务错误消息](#常见业务错误消息)
  - [附录：完整接口速查表](#附录完整接口速查表)

---

## 一、通用说明

### 请求头要求

| Header | 说明 | 必填 |
|--------|------|------|
| `Content-Type` | `application/json` | 是（POST/PUT 请求） |
| `Authorization` | `Bearer <token>` | 认证/管理接口必填 |

### 统一响应格式

所有接口返回统一的 JSON 结构：

```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "key1": "value1",
    "key2": "value2"
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `success` | boolean | 请求是否成功 |
| `message` | string | 提示信息 |
| `data` | object | 业务数据（Map 结构） |

---

## 二、公开接口（无需 Token）

### 1. 获取验证码

> 返回 Base64 图片验证码，存储在 Session 中，5 分钟有效

```
GET /api/auth/captcha
```

**响应参数：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `captchaImage` | string | Base64 编码的 JPEG 图片（`data:image/jpeg;base64,...`） |
| `captchaKey` | string | 当前 Session ID（需在后续请求中携带相同 Cookie） |

**响应示例：**

```json
{
  "success": true,
  "message": "验证码生成成功",
  "data": {
    "captchaImage": "data:image/jpeg;base64,/9j/4AAQ...",
    "captchaKey": "B1A2C3D4-E5F6-7890-ABCD-EF1234567890"
  }
}
```

---

### 2. 用户登录

> 使用用户名、密码和验证码登录，成功后返回 JWT Token

```
POST /api/auth/login
Content-Type: application/json
```

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 校验规则 | 示例 |
|------|------|------|----------|------|
| `username` | string | 是 | 3-50 个字符 | `admin` |
| `password` | string | 是 | 至少 6 位 | `admin123` |
| `captcha` | string | 否 | 调用 `/captcha` 获取 | `A1B2` |

**响应参数（data 内）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `token` | string | JWT Token（后续请求需在 Authorization 头中携带） |
| `username` | string | 用户名 |
| `role` | string | 用户角色（ADMIN / USER / MANAGER） |
| `userId` | number | 用户 ID |

**响应示例：**

```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxNTIzOTAwMCwiZXhwIjoxNzE1MzI1NDAwfQ...",
    "username": "admin",
    "role": "ADMIN",
    "userId": 1
  }
}
```

**错误响应：**

```json
// 验证码错误
{ "success": false, "message": "验证码错误", "data": {} }

// 用户名或密码错误
{ "success": false, "message": "用户名或密码错误", "data": {} }

// 账户已被禁用
{ "success": false, "message": "账户已被禁用", "data": {} }

// 账户已被锁定
{ "success": false, "message": "账户已被锁定", "data": {} }

// 账户已过期
{ "success": false, "message": "账户已过期", "data": {} }
```

---

### 3. 用户注册

> 注册新用户账号，默认角色为 USER，注册成功后自动登录并返回 JWT Token

```
POST /api/auth/register
Content-Type: application/json
```

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 校验规则 | 示例 |
|------|------|------|----------|------|
| `username` | string | 是 | 3-50 个字符 | `newuser` |
| `password` | string | 是 | 至少 6 位 | `password123` |
| `email` | string | 是 | 邮箱格式 | `user@example.com` |
| `captcha` | string | 否 | 调用 `/captcha` 获取 | `A1B2` |

**响应参数（data 内）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `token` | string | JWT Token（自动登录） |
| `username` | string | 用户名 |
| `userId` | number | 用户 ID |

**响应示例：**

```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "newuser",
    "userId": 3
  }
}
```

---

### 4. 添加用户

> 公开接口，无需认证。创建新用户，可指定用户名、密码、邮箱、角色和启用状态

```
POST /api/admin/users
Content-Type: application/json
```

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 校验规则 | 默认值 | 示例 |
|------|------|------|----------|--------|------|
| `username` | string | 是 | 3-50 字符，仅字母/数字/下划线 | — | `testuser` |
| `password` | string | 是 | 6-100 字符 | — | `password123` |
| `email` | string | 是 | 邮箱格式 | — | `test@example.com` |
| `role` | string | 否 | `USER` / `MANAGER` / `ADMIN` | `USER` | `USER` |
| `enabled` | boolean | 否 | — | `true` | `true` |

**响应参数（data 内）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `userId` | number | 新建用户 ID |
| `username` | string | 用户名 |
| `email` | string | 邮箱 |
| `role` | string | 用户角色 |
| `enabled` | boolean | 是否启用 |

**响应示例：**

```json
{
  "success": true,
  "message": "用户创建成功",
  "data": {
    "userId": 2,
    "username": "testuser",
    "email": "test@example.com",
    "role": "USER",
    "enabled": true
  }
}
```

**错误响应：**

```json
{ "success": false, "message": "用户名已存在", "data": {} }
```

---

## 三、认证接口（需 Token）

> 以下接口需要在请求头中携带有效的 JWT Token：
> ```
> Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
> ```

### 5. 获取当前用户信息

> 获取当前已登录用户的详细信息

```
GET /api/auth/me
Authorization: Bearer <token>
```

**响应参数（data 内）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `username` | string | 用户名 |
| `email` | string | 邮箱 |
| `role` | string | 用户角色 |
| `userId` | number | 用户 ID |

**响应示例：**

```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "username": "admin",
    "email": "admin@aqua.com",
    "role": "ADMIN",
    "userId": 1
  }
}
```

---

### 6. 退出登录

> 销毁当前 Session（客户端需同时丢弃 Token）

```
POST /api/auth/logout
Authorization: Bearer <token>
```

**响应示例：**

```json
{
  "success": true,
  "message": "退出登录成功",
  "data": {}
}
```

---

## 四、管理接口（需 ADMIN 角色 + Token）

> 以下接口需要调用者具有 `ADMIN` 角色，并在请求头中携带 JWT Token

### 7. 管理员权限测试

> 用于测试当前 Token 是否具有 ADMIN 角色权限

```
GET /api/auth/admin/test
Authorization: Bearer <admin_token>
```

**响应示例：**

```json
{
  "success": true,
  "message": "您有管理员权限",
  "data": {}
}
```

---

### 8. 用户列表

> 获取所有用户的列表（已脱敏，不包含密码等敏感信息）

```
GET /api/admin/users
Authorization: Bearer <admin_token>
```

**响应参数（data 内）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `users` | array | 用户对象数组（详见 UserInfo） |
| `total` | number | 用户总数 |

**UserInfo 对象结构：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | number | 用户 ID |
| `username` | string | 用户名 |
| `email` | string | 邮箱 |
| `role` | string | 用户角色 |
| `enabled` | boolean | 是否启用 |
| `accountLocked` | boolean | 是否锁定 |
| `createTime` | string | 创建时间（ISO 格式） |
| `updateTime` | string | 更新时间（ISO 格式） |

**响应示例：**

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "users": [
      {
        "id": 1,
        "username": "admin",
        "email": "admin@aqua.com",
        "role": "ADMIN",
        "enabled": true,
        "accountLocked": false,
        "createTime": "2025-01-01T00:00:00",
        "updateTime": "2025-01-01T00:00:00"
      }
    ],
    "total": 1
  }
}
```

---

### 9. 用户详情

> 根据用户 ID 获取单个用户的详细信息

```
GET /api/admin/users/{id}
Authorization: Bearer <admin_token>
```

**路径参数：**

| 参数 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | 是 | 用户 ID | `1` |

**响应示例：**

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@aqua.com",
      "role": "ADMIN",
      "enabled": true,
      "accountLocked": false,
      "createTime": "2025-01-01T00:00:00",
      "updateTime": "2025-01-01T00:00:00"
    }
  }
}
```

---

### 10. 更新用户

> 管理员更新指定用户的用户名、邮箱、角色、启用状态或密码

```
PUT /api/admin/users/{id}
Content-Type: application/json
Authorization: Bearer <admin_token>
```

**路径参数：**

| 参数 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | 是 | 要更新的用户 ID | `1` |

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| `username` | string | 是 | 3-50 字符 | 新用户名（不可与其他用户冲突） |
| `password` | string | 否 | 6-100 字符 | 新密码（不传或为空则不修改密码） |
| `email` | string | 是 | 邮箱格式 | 新邮箱 |
| `role` | string | 否 | `USER` / `MANAGER` / `ADMIN` | 新角色 |
| `enabled` | boolean | 否 | — | 是否启用 |

**响应示例：**

```json
{
  "success": true,
  "message": "用户更新成功",
  "data": {
    "user": {
      "id": 1,
      "username": "admin_new",
      "email": "newemail@aqua.com",
      "role": "ADMIN",
      "enabled": true,
      "accountLocked": false,
      "createTime": "2025-01-01T00:00:00",
      "updateTime": "2025-06-01T00:00:00"
    }
  }
}
```

---

### 11. 删除用户

> 逻辑删除指定用户（将 `deleted` 标记置为 1，并非物理删除）

```
DELETE /api/admin/users/{id}
Authorization: Bearer <admin_token>
```

**路径参数：**

| 参数 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | 是 | 要删除的用户 ID | `2` |

**响应示例：**

```json
{
  "success": true,
  "message": "用户删除成功",
  "data": {}
}
```

---

## 五、数据概览（Dashboard）

> 公开接口，无需 Token。提供首页顶部 4 个卡片统计数据

### 12. 获取数据概览

> 返回存储总量、较上月增长率、饲料总库存、可用天数、本月消耗饲料

```
GET /api/dashboard/summary
```

**响应参数（data 内）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `totalStock` | number | 存储总量（放养总数量，尾/只） |
| `stockGrowthRate` | number | 较上月增长率（%） |
| `feedStockKg` | number | 饲料总库存（kg） |
| `feedAvailableDays` | number | 饲料可用天数（天） |
| `monthlyFeedConsumed` | number | 本月消耗饲料（kg） |

**响应示例：**

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "totalStock": 50000,
    "stockGrowthRate": 12.5,
    "feedStockKg": 2500.00,
    "feedAvailableDays": 15,
    "monthlyFeedConsumed": 5000.00
  }
}
```

---

## 六、放养记录管理（Stocking）

> 公开接口，无需 Token。管理养殖放养记录

### 13. 分页查询放养记录

> 支持塘口名称模糊搜索 + 品种名称精准筛选

```
GET /api/stocking/list?page=1&size=10&pondName=&species=
```

**请求参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | 否 | `1` | 页码 |
| `size` | number | 否 | `10` | 每页条数 |
| `pondName` | string | 否 | — | 塘口名称（模糊搜索） |
| `species` | string | 否 | — | 品种名称（精准筛选） |

**响应参数（data 内）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `records` | array | 放养记录列表 |
| `total` | number | 总记录数 |
| `page` | number | 当前页码 |
| `size` | number | 每页条数 |

**records 元素结构：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `batchId` | number | 批次 ID |
| `pondId` | number | 塘口 ID |
| `pondName` | string | 塘口名称 |
| `breedId` | number | 品种 ID |
| `species` | string | 品种名称 |
| `stockCount` | number | 放养数量 |
| `currentNum` | number | 当前存活数量 |
| `avgSpec` | number | 平均规格（g/尾） |
| `survivalRate` | number | 存活率（%） |
| `stockDate` | string | 放养日期 |
| `status` | string | 状态 |

**响应示例：**

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "batchId": 1,
        "pondId": 1,
        "pondName": "1号塘",
        "species": "南美白对虾",
        "stockCount": 10000,
        "stockDate": "2025-06-01",
        "status": "active"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

---

### 14. 新增放养记录

```
POST /api/stocking/add
Content-Type: application/json
```

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 校验规则 | 示例 |
|------|------|------|----------|------|
| `pondId` | number | 是 | 不能为空 | `1` |
| `breedId` | number | 否 | — | `1` |
| `species` | string | 是 | 不能为空 | `南美白对虾` |
| `stockCount` | number | 是 | 必须大于 0 | `10000` |
| `currentNum` | number | 否 | — | `10000` |
| `avgSpec` | number | 否 | — | `15.0` |
| `survivalRate` | number | 否 | 0-100 | `85.0` |
| `stockDate` | string | 是 | 日期格式 | `2025-06-01` |
| `status` | string | 否 | — | `active` |

**响应示例：**

```json
{
  "success": true,
  "message": "添加成功",
  "data": {
    "batchId": 1
  }
}
```

---

### 15. 编辑放养记录

```
PUT /api/stocking/update
Content-Type: application/json
```

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 校验规则 | 示例 |
|------|------|------|----------|------|
| `batchId` | number | 是 | 不能为空（编辑时必填） | `1` |
| `pondId` | number | 是 | 不能为空 | `1` |
| `breedId` | number | 否 | — | `1` |
| `species` | string | 是 | 不能为空 | `南美白对虾` |
| `stockCount` | number | 是 | 必须大于 0 | `15000` |
| `currentNum` | number | 否 | — | `12000` |
| `avgSpec` | number | 否 | — | `18.0` |
| `survivalRate` | number | 否 | 0-100 | `80.0` |
| `stockDate` | string | 是 | 日期格式 | `2025-06-15` |
| `status` | string | 否 | — | `active` |

**响应示例：**

```json
{
  "success": true,
  "message": "更新成功",
  "data": {
    "batchId": 1
  }
}
```

---

### 16. 删除放养记录

> 逻辑删除（将 status 置为 deleted）

```
DELETE /api/stocking/delete/{id}
```

**路径参数：**

| 参数 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | 是 | 批次 ID | `1` |

**响应示例：**

```json
{
  "success": true,
  "message": "删除成功",
  "data": {}
}
```

---

### 16b. 按塘口查询放养记录

> 按塘口 ID 分页查询，支持品种名称筛选

```
GET /api/stocking/listByPond?page=1&size=10&pondId=1&species=
```

**请求参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | 否 | `1` | 页码 |
| `size` | number | 否 | `10` | 每页条数 |
| `pondId` | number | 否 | — | 塘口 ID |
| `species` | string | 否 | — | 品种名称（精准筛选） |

**响应参数（data 内）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `records` | array | 放养记录列表 |
| `total` | number | 总记录数 |
| `page` | number | 当前页码 |
| `size` | number | 每页条数 |

**records 元素结构** 同 13. 分页查询放养记录。

**响应示例：**

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "batchId": 1,
        "pondId": 1,
        "pondName": "1号塘",
        "species": "南美白对虾",
        "stockCount": 10000,
        "stockDate": "2025-06-01",
        "status": "active"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

---

## 七、饲料库存管理（Feed）

> 公开接口，无需 Token。管理饲料入库、出库与库存查询

### 17. 查询饲料库存

> 返回饲料库存明细列表（含实时库存状态和预计可用天数）

```
GET /api/feed/inventory?page=1&size=10&name=
```

**请求参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | 否 | `1` | 页码 |
| `size` | number | 否 | `10` | 每页条数 |
| `name` | string | 否 | — | 饲料名称（模糊搜索） |

**响应参数（data 内）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `records` | array | 库存列表（含实时状态） |
| `total` | number | 总记录数 |
| `page` | number | 当前页码 |
| `size` | number | 每页条数 |

**records 元素结构：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `materialId` | number | 物料 ID |
| `name` | string | 饲料名称 |
| `category` | string | 类别 |
| `unit` | string | 单位 |
| `unitPrice` | number | 单价（元/kg） |
| `currentStock` | number | 当前库存（kg） |
| `stockStatus` | string | 库存状态（`充足` / `偏低`） |
| `availableDays` | number | 预计可用天数 |

> 库存状态实时计算（不持久化）：`currentStock >= 100` → `充足`，否则 → `偏低`

**响应示例：**

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "materialId": 1,
        "name": "对虾配合饲料",
        "category": "饲料",
        "unit": "kg",
        "unitPrice": 150.00,
        "currentStock": 500.00,
        "stockStatus": "充足",
        "availableDays": 50
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

---

### 18. 饲料入库

> 事务操作：增加库存 + 写入入库流水

```
POST /api/feed/inStock
Content-Type: application/json
```

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 校验规则 | 示例 |
|------|------|------|----------|------|
| `materialId` | number | 否 | 与 materialName 二选一 | `1` |
| `materialName` | string | 否 | 与 materialId 二选一 | `对虾配合饲料` |
| `quantity` | number | 是 | 必须大于 0 | `500` |
| `unitPrice` | number | 否 | — | `150.00` |
| `operator` | string | 否 | — | `张三` |
| `remark` | string | 否 | — | `新批次饲料到货` |

> **物料匹配规则**：优先按 `materialId` 查找；不存在时若有 `materialName` 则自动创建新物料；仅提供 `materialName` 时按名称查找或新建。

**响应示例：**

```json
{
  "success": true,
  "message": "入库成功",
  "data": {
    "materialId": 1,
    "materialName": "对虾配合饲料",
    "currentStock": 1000.00
  }
}
```

---

### 19. 饲料出库

> 事务操作：扣减库存 + 写入出库流水；校验库存不足时拒绝出库

```
POST /api/feed/outStock
Content-Type: application/json
```

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 校验规则 | 示例 |
|------|------|------|----------|------|
| `materialId` | number | 是 | 不能为空 | `1` |
| `quantity` | number | 是 | 必须大于 0 | `200` |
| `batchId` | number | 否 | — | `1` |
| `operator` | string | 否 | — | `张三` |
| `remark` | string | 否 | — | `1号塘投喂` |

**响应示例：**

```json
{
  "success": true,
  "message": "出库成功",
  "data": {
    "materialId": 1,
    "materialName": "对虾配合饲料",
    "currentStock": 300.00
  }
}
```

**错误响应（库存不足）：**

```json
{
  "success": false,
  "message": "库存不足！当前库存: 100.00 kg, 出库数量: 200.00 kg",
  "data": {}
}
```

---

### 20. 出入库流水明细

```
GET /api/feed/records?page=1&size=10&materialId=
```

**请求参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | 否 | `1` | 页码 |
| `size` | number | 否 | `10` | 每页条数 |
| `materialId` | number | 否 | — | 物料 ID（筛选） |

**响应参数（data 内）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `records` | array | 流水记录列表 |
| `total` | number | 总记录数 |
| `page` | number | 当前页码 |
| `size` | number | 每页条数 |

**records 元素结构：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `recordId` | number | 记录 ID |
| `materialId` | number | 物料 ID |
| `batchId` | number | 关联批次 ID |
| `type` | string | 类型（`in`=入库 / `out`=出库） |
| `quantity` | number | 数量（kg） |
| `totalCost` | number | 总成本（元） |
| `recordDate` | string | 记录时间 |

**响应示例：**

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "recordId": 1,
        "materialId": 1,
        "batchId": null,
        "type": "in",
        "quantity": 500.00,
        "totalCost": 75000.00,
        "recordDate": "2025-06-10T10:30:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

---

## 八、塘口管理（Pond）

> 公开接口，无需 Token。管理塘口基础信息

### 21. 分页查询塘口列表

> 支持塘口名称模糊搜索 + 状态筛选

```
GET /api/pond/list?page=1&size=10&name=&status=
```

**请求参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | number | 否 | `1` | 页码 |
| `size` | number | 否 | `10` | 每页条数 |
| `name` | string | 否 | — | 塘口名称（模糊搜索） |
| `status` | string | 否 | — | 状态（1-使用中, 2-空闲, 3-维修） |

**响应参数（data 内）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `records` | array | 塘口列表 |
| `total` | number | 总记录数 |
| `page` | number | 当前页码 |
| `size` | number | 每页条数 |

**records 元素结构：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `pondId` | number | 塘口 ID |
| `code` | string | 塘口编号 |
| `name` | string | 塘口名称 |
| `area` | number | 面积（亩） |
| `depth` | number | 水深（米） |
| `waterSource` | string | 水源类型（1-地下水, 2-地表水, 3-海水） |
| `location` | string | 位置/地址 |
| `status` | string | 状态（1-使用中, 2-空闲, 3-维修） |
| `manager` | string | 负责人 |
| `createdAt` | string | 创建时间 |

**响应示例：**

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "pondId": 1,
        "code": "P001",
        "name": "1号南美白对虾塘",
        "area": 5.0,
        "depth": 1.8,
        "waterSource": "1",
        "location": "A区3号",
        "status": "1",
        "manager": "张三",
        "createdAt": "2025-06-01T10:00:00"
      }
    ],
    "total": 3,
    "page": 1,
    "size": 10
  }
}
```

---

### 22. 新增塘口

```
POST /api/pond/add
Content-Type: application/json
```

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 校验规则 | 示例 |
|------|------|------|----------|------|
| `code` | string | 是 | 不能为空 | `P004` |
| `name` | string | 是 | 不能为空 | `4号罗非鱼塘` |
| `area` | number | 是 | 不能为空 | `6.0` |
| `depth` | number | 否 | — | `1.5` |
| `waterSource` | string | 否 | — | `1` |
| `location` | string | 否 | — | `D区1号` |
| `status` | string | 否 | — | `2` |
| `manager` | string | 否 | — | `王五` |

**响应示例：**

```json
{
  "success": true,
  "message": "添加成功",
  "data": {
    "pondId": 4
  }
}
```

---

### 23. 编辑塘口

```
PUT /api/pond/update
Content-Type: application/json
```

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 校验规则 | 示例 |
|------|------|------|----------|------|
| `pondId` | number | 是 | 不能为空（编辑时必填） | `4` |
| `code` | string | 是 | 不能为空 | `P004` |
| `name` | string | 是 | 不能为空 | `4号罗非鱼塘` |
| `area` | number | 是 | 不能为空 | `6.5` |
| `depth` | number | 否 | — | `1.8` |
| `waterSource` | string | 否 | — | `2` |
| `location` | string | 否 | — | `D区2号` |
| `status` | string | 否 | — | `1` |
| `manager` | string | 否 | — | `赵六` |

**响应示例：**

```json
{
  "success": true,
  "message": "更新成功",
  "data": {
    "pondId": 4
  }
}
```

---

### 24. 删除塘口

> 软删除（将 `deleted` 标记置为 1）

```
DELETE /api/pond/delete/{id}
```

**路径参数：**

| 参数 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | 是 | 塘口 ID | `4` |

**响应示例：**

```json
{
  "success": true,
  "message": "删除成功",
  "data": {}
}
```

---

### 25. 获取塘口详情

```
GET /api/pond/{id}
```

**路径参数：**

| 参数 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `id` | number | 是 | 塘口 ID | `1` |

**响应示例：**

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "pond": {
      "pondId": 1,
      "code": "P001",
      "name": "1号南美白对虾塘",
      "area": 5.0,
      "depth": 1.8,
      "waterSource": "1",
      "location": "A区3号",
      "status": "1",
      "manager": "张三",
      "createdAt": "2025-06-01T10:00:00",
      "deleted": 0
    }
  }
}
```

---

## 九、统计数据（Statistic）

> 公开接口，无需 Token。提供塘口和放养两个维度的统计数据

### 26. 塘口统计数据

> 返回塘口总数、使用中数、空闲数、总养殖规模

```
GET /api/statistic/pond
```

**响应参数（data 内）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `totalCount` | number | 塘口总数 |
| `inUseCount` | number | 使用中塘口数 |
| `idleCount` | number | 空闲塘口数 |
| `totalArea` | number | 总养殖规模（亩） |

**响应示例：**

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "totalCount": 10,
    "inUseCount": 6,
    "idleCount": 3,
    "totalArea": 55.5
  }
}
```

---

### 27. 放养统计数据

> 返回总放养量、当前存活量、平均存活率、总放养重量

```
GET /api/statistic/stocking
```

**响应参数（data 内）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `totalStockingCount` | number | 总放养量（尾/只） |
| `totalCurrentNum` | number | 当前存活总量（尾/只） |
| `avgSurvivalRate` | number | 平均存活率（%） |
| `totalWeight` | number | 总放养重量（kg） |

**计算逻辑：**
- 总放养重量 = `SUM(stock_count * avg_spec / 1000)`
- 平均存活率 = `AVG(survival_rate)`，保留一位小数

**响应示例：**

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "totalStockingCount": 85000,
    "totalCurrentNum": 72000,
    "avgSurvivalRate": 84.7,
    "totalWeight": 1275.50
  }
}
```

---

## 十、数据模型

### 10.1 统一响应（ApiResponse）

```json
{
  "success": true,
  "message": "提示信息",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `success` | boolean | 请求是否成功 |
| `message` | string | 服务器返回的提示信息 |
| `data` | object | 业务数据的 Map，key-value 结构 |

### 10.2 用户实体（User）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 用户 ID（自动递增） |
| `username` | String | 用户名 |
| `password` | String | 密码（BCrypt 加密存储，不对外暴露） |
| `email` | String | 邮箱 |
| `role` | String | 角色：`ADMIN` / `USER` / `MANAGER` |
| `enabled` | Boolean | 是否启用 |
| `accountLocked` | Boolean | 是否锁定 |
| `accountExpired` | Boolean | 是否过期 |
| `credentialsExpired` | Boolean | 凭据是否过期 |
| `createTime` | LocalDateTime | 创建时间 |
| `updateTime` | LocalDateTime | 更新时间 |
| `deleted` | Integer | 逻辑删除标记（0=正常，1=删除） |

### 10.3 登录请求（LoginRequest）

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|----------|
| `username` | String | 是 | 3-50 个字符 |
| `password` | String | 是 | 至少 6 位 |
| `captcha` | String | 否 | 验证码文本 |

### 10.4 注册请求（RegisterRequest）

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|----------|
| `username` | String | 是 | 3-50 个字符 |
| `password` | String | 是 | 至少 6 位 |
| `email` | String | 是 | 邮箱格式 |
| `captcha` | String | 否 | 验证码文本 |

### 10.5 创建用户请求（CreateUserRequest）

| 字段 | 类型 | 必填 | 默认值 | 校验规则 |
|------|------|------|--------|----------|
| `username` | String | 是 | — | 3-50 字符，仅字母/数字/下划线 |
| `password` | String | 是 | — | 6-100 字符 |
| `email` | String | 是 | — | 邮箱格式 |
| `role` | String | 否 | `USER` | `USER` / `MANAGER` / `ADMIN` |
| `enabled` | Boolean | 否 | `true` | — |

### 10.6 脱敏用户信息（UserInfo）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 用户 ID |
| `username` | String | 用户名 |
| `email` | String | 邮箱 |
| `role` | String | 用户角色 |
| `enabled` | Boolean | 是否启用 |
| `accountLocked` | Boolean | 是否锁定 |
| `createTime` | LocalDateTime | 创建时间 |
| `updateTime` | LocalDateTime | 更新时间 |

> 注意：UserInfo 不包含 `password`、`accountExpired`、`credentialsExpired`、`deleted` 等敏感或内部字段。

### 10.7 放养记录请求（StockingRecordDTO）

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| `batchId` | Integer | 否 | — | 批次 ID（编辑时必填） |
| `pondId` | Integer | 是 | `@NotNull` | 塘口 ID |
| `breedId` | Integer | 否 | — | 品种 ID |
| `species` | String | 是 | `@NotNull` | 品种名称 |
| `stockCount` | Integer | 是 | `@Min(1)` | 放养数量 |
| `currentNum` | Integer | 否 | — | 当前存活数量（默认等于放养数量） |
| `avgSpec` | Double | 否 | — | 平均规格（g/尾） |
| `survivalRate` | Double | 否 | `@DecimalMin(0)` `@DecimalMax(100)` | 存活率（%）0-100 |
| `stockDate` | LocalDate | 是 | `@NotNull` | 放养日期 |
| `status` | String | 否 | — | 状态（默认 `active`） |

### 10.8 饲料入库请求（FeedInStockDTO）

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| `materialId` | Integer | 否 | 与 materialName 二选一 | 物料 ID（优先使用） |
| `materialName` | String | 否 | 与 materialId 二选一 | 饲料名称（不存在时自动创建物料） |
| `quantity` | Double | 是 | `@Min(0)` | 入库数量（kg） |
| `unitPrice` | Double | 否 | — | 单价（元/kg） |
| `operator` | String | 否 | — | 操作人 |
| `remark` | String | 否 | — | 备注 |

> 物料匹配规则：优先按 `materialId` 查找物料；`materialId` 不存在但有 `materialName` 时自动创建新物料（类别=饲料，单位=kg）；仅提供 `materialName` 时按名称查找，不存在则新建。

### 10.9 饲料出库请求（FeedOutStockDTO）

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| `materialId` | Integer | 是 | `@NotNull` | 物料 ID |
| `quantity` | Double | 是 | `@Min(0)` | 出库数量（kg） |
| `batchId` | Integer | 否 | — | 关联养殖批次 ID |
| `operator` | String | 否 | — | 操作人 |
| `remark` | String | 否 | — | 备注 |

### 10.10 放养记录实体（FarmingBatch）

> 映射数据库表 `stocking_record`，主键列 `id`（Java 字段名 `batchId`）

| 字段 | 类型 | 数据库列 | 说明 |
|------|------|----------|------|
| `batchId` | Integer | `id`（PK，自增） | 批次 ID（编辑时必填） |
| `pondId` | Integer | `pond_id` | 塘口 ID |
| `breedId` | Integer | `breed_id` | 品种 ID |
| `species` | String | `species` | 品种名称 |
| `stockCount` | Integer | `stock_count` | 放养数量 |
| `currentNum` | Integer | `current_num` | 当前存活数量 |
| `avgSpec` | Double | `avg_spec` | 平均规格（g/尾） |
| `survivalRate` | Double | `survival_rate` | 存活率（%）0-100 |
| `stockDate` | LocalDate | `stock_date` | 放养日期 |
| `status` | String | `status` | 状态：`active`-正常, `deleted`-删除 |

---

### 10.11 塘口请求（PondDTO）

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| `pondId` | Integer | 否 | — | 塘口 ID（编辑时必填） |
| `code` | String | 是 | `@NotBlank` | 塘口编号 |
| `name` | String | 是 | `@NotBlank` | 塘口名称 |
| `area` | Double | 是 | `@NotNull` | 面积（亩） |
| `depth` | Double | 否 | — | 水深（米） |
| `waterSource` | String | 否 | — | 水源类型：1-地下水, 2-地表水, 3-海水 |
| `location` | String | 否 | — | 位置/地址 |
| `status` | String | 否 | — | 状态：1-使用中, 2-空闲, 3-维修 |
| `manager` | String | 否 | — | 负责人 |

### 10.12 塘口实体（Pond）

| 字段 | 类型 | 说明 |
|------|------|------|
| `pondId` | Integer | 塘口 ID（自动递增） |
| `code` | String | 塘口编号 |
| `name` | String | 塘口名称 |
| `area` | Double | 面积（亩） |
| `depth` | Double | 水深（米） |
| `waterSource` | String | 水源类型：1-地下水, 2-地表水, 3-海水 |
| `location` | String | 位置/地址 |
| `status` | String | 状态：1-使用中, 2-空闲, 3-维修 |
| `manager` | String | 负责人 |
| `createdAt` | LocalDateTime | 创建时间 |
| `deleted` | Integer | 软删除标记：0-正常, 1-删除 |

### 10.13 品种实体（Breed）

| 字段 | 类型 | 说明 |
|------|------|------|
| `breedId` | Integer | 品种 ID（自动递增） |
| `name` | String | 品种名称 |
| `category` | String | 品种类别 |
| `defaultSpec` | Double | 默认平均规格（g/尾） |
| `remark` | String | 备注 |
| `deleted` | Integer | 软删除标记：0-正常, 1-删除 |

### 10.14 塘口统计响应（PondStatisticVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| `totalCount` | Long | 塘口总数 |
| `inUseCount` | Long | 使用中塘口数 |
| `idleCount` | Long | 空闲塘口数 |
| `totalArea` | Double | 总养殖规模（亩） |

### 10.15 放养统计响应（StockingStatisticVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| `totalStockingCount` | Long | 总放养量 |
| `totalCurrentNum` | Long | 当前存活总量 |
| `avgSurvivalRate` | Double | 平均存活率（%） |
| `totalWeight` | Double | 总放养重量（kg）= SUM(stock_count * avg_spec / 1000) |

---

## 十一、错误码说明

| HTTP 状态码 | 说明 | 典型场景 |
|-------------|------|----------|
| `200` | 请求成功 | 正常响应 |
| `400` | 请求参数错误 | 参数校验失败、用户名已存在、验证码错误 |
| `401` | 未认证 | 未提供 Token 或 Token 无效/过期 |
| `403` | 权限不足 | 角色不是 ADMIN、账户被禁用/锁定/过期 |
| `500` | 服务器内部错误 | 系统异常 |

### 常见业务错误消息

| 错误消息 | 触发条件 |
|----------|----------|
| `验证码错误` | 验证码不匹配或已过期 |
| `用户名或密码错误` | 用户名不存在或密码错误 |
| `账户已被禁用` | 用户 enabled = false |
| `账户已被锁定` | 用户 accountLocked = true |
| `账户已过期` | 用户 accountExpired = true |
| `用户名已存在` | 注册或创建时用户名已被占用 |
| `用户不存在` | 按 ID 查询不到用户 |
| `用户不存在或已被删除` | 删除操作时用户不存在 |
| `未登录` | 未提供有效 Token 访问 /me 接口 |
| `权限不足：拒绝访问` | 非 ADMIN 访问管理接口 |

---

## 附录：完整接口速查表

| # | 方法 | 路径 | 认证 | 角色 | 说明 |
|---|------|------|------|------|------|
| 1 | `GET` | `/api/auth/captcha` | ❌ | — | 获取验证码图片 |
| 2 | `POST` | `/api/auth/login` | ❌ | — | 用户登录 |
| 3 | `POST` | `/api/auth/register` | ❌ | — | 用户注册 |
| 4 | `POST` | `/api/admin/users` | ❌ | — | 添加用户 |
| 5 | `GET` | `/api/auth/me` | ✅ | 任意 | 获取当前用户信息 |
| 6 | `POST` | `/api/auth/logout` | ✅ | 任意 | 退出登录 |
| 7 | `GET` | `/api/auth/admin/test` | ✅ | ADMIN | 权限测试 |
| 8 | `GET` | `/api/admin/users` | ✅ | ADMIN | 用户列表 |
| 9 | `GET` | `/api/admin/users/{id}` | ✅ | ADMIN | 用户详情 |
| 10 | `PUT` | `/api/admin/users/{id}` | ✅ | ADMIN | 更新用户 |
| 11 | `DELETE` | `/api/admin/users/{id}` | ✅ | ADMIN | 删除用户 |
| 12 | `GET` | `/api/dashboard/summary` | ❌ | — | 获取数据概览 |
| 13 | `GET` | `/api/stocking/list` | ❌ | — | 分页查询放养记录 |
| 14 | `POST` | `/api/stocking/add` | ❌ | — | 新增放养记录 |
| 15 | `PUT` | `/api/stocking/update` | ❌ | — | 编辑放养记录 |
| 16 | `DELETE` | `/api/stocking/delete/{id}` | ❌ | — | 删除放养记录 |
| 17 | `GET` | `/api/stocking/listByPond` | ❌ | — | 按塘口分页查询放养记录 |
| 18 | `GET` | `/api/feed/inventory` | ❌ | — | 查询饲料库存 |
| 19 | `POST` | `/api/feed/inStock` | ❌ | — | 饲料入库 |
| 20 | `POST` | `/api/feed/outStock` | ❌ | — | 饲料出库 |
| 21 | `GET` | `/api/feed/records` | ❌ | — | 出入库流水明细 |
| 22 | `GET` | `/api/pond/list` | ❌ | — | 分页查询塘口列表 |
| 23 | `POST` | `/api/pond/add` | ❌ | — | 新增塘口 |
| 24 | `PUT` | `/api/pond/update` | ❌ | — | 编辑塘口 |
| 25 | `DELETE` | `/api/pond/delete/{id}` | ❌ | — | 删除塘口 |
| 26 | `GET` | `/api/pond/{id}` | ❌ | — | 获取塘口详情 |
| 27 | `GET` | `/api/statistic/pond` | ❌ | — | 塘口统计数据 |
| 28 | `GET` | `/api/statistic/stocking` | ❌ | — | 放养统计数据 |