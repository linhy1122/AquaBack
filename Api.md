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
  - [五、数据模型](#五数据模型)
    - [5.1 统一响应（ApiResponse）](#51-统一响应apiresponse)
    - [5.2 用户实体（User）](#52-用户实体user)
    - [5.3 登录请求（LoginRequest）](#53-登录请求loginrequest)
    - [5.4 注册请求（RegisterRequest）](#54-注册请求registerrequest)
    - [5.5 创建用户请求（CreateUserRequest）](#55-创建用户请求createuserrequest)
    - [5.6 脱敏用户信息（UserInfo）](#56-脱敏用户信息userinfo)
  - [六、错误码说明](#六错误码说明)
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

## 五、数据模型

### 5.1 统一响应（ApiResponse）

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

### 5.2 用户实体（User）

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

### 5.3 登录请求（LoginRequest）

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|----------|
| `username` | String | 是 | 3-50 个字符 |
| `password` | String | 是 | 至少 6 位 |
| `captcha` | String | 否 | 验证码文本 |

### 5.4 注册请求（RegisterRequest）

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|----------|
| `username` | String | 是 | 3-50 个字符 |
| `password` | String | 是 | 至少 6 位 |
| `email` | String | 是 | 邮箱格式 |
| `captcha` | String | 否 | 验证码文本 |

### 5.5 创建用户请求（CreateUserRequest）

| 字段 | 类型 | 必填 | 默认值 | 校验规则 |
|------|------|------|--------|----------|
| `username` | String | 是 | — | 3-50 字符，仅字母/数字/下划线 |
| `password` | String | 是 | — | 6-100 字符 |
| `email` | String | 是 | — | 邮箱格式 |
| `role` | String | 否 | `USER` | `USER` / `MANAGER` / `ADMIN` |
| `enabled` | Boolean | 否 | `true` | — |

### 5.6 脱敏用户信息（UserInfo）

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

---

## 六、错误码说明

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