# CampusGuessSystem - AI 编码指南

## 项目概述
校园猜猜看系统 - Spring Boot 4.0 + JPA + MySQL 的 RESTful API 后端，支持用户答题、好友系统、积分排名等功能。

## 架构模式

### 分层结构
```
controller → service (interface) → impl → repository → entity
                ↓
              dto (request/response)
```
- **Controller**: 处理 HTTP 请求，路径格式 `/users/{username}/...` 或 `/资源名`
- **Service 接口 + Impl**: 业务逻辑分离，impl 类使用 `@Transactional` 注解
- **Repository**: Spring Data JPA，自定义查询使用 `@Query` JPQL

### 关键约定

**异常处理 - 使用 `BusinessException`**
```java
// 抛出业务异常时使用 HTTP 状态码 + 中文消息
throw new BusinessException(404, "用户不存在");
throw new BusinessException(400, "不能添加自己为好友");
```
全局异常处理在 [GlobalExceptionHandler.java](demo/src/main/java/com/campusguess/demo/exception/GlobalExceptionHandler.java)

**统一响应格式 - `ApiResponse<T>`**
```java
return ResponseEntity.ok(ApiResponse.success("操作成功", data));
return ResponseEntity.status(e.getCode()).body(ApiResponse.error(e.getCode(), e.getMessage()));
```

**依赖注入 - 构造器注入**
```java
@RequiredArgsConstructor  // Lombok 生成构造器
public class XxxServiceImpl {
    private final XxxRepository xxxRepository;
}
```

## 核心业务模式

### 好友关系 - 双向记录模式
好友关系在 `friendships` 表中存储双向记录（A→B 和 B→A），状态通过 `FriendshipStatus` 枚举管理：
- 添加好友时检查双向申请状态
- 接受申请时创建反向记录
- 删除好友时删除双向记录
参考实现：[FriendServiceImpl.java](demo/src/main/java/com/campusguess/demo/service/impl/FriendServiceImpl.java)

### 用户身份识别
使用路径参数 `{username}` 识别用户，而非 JWT token 解析：
```java
@GetMapping("/users/{username}/friends")
public ResponseEntity<...> getFriends(@PathVariable String username) {}
```

### 分页查询
使用 Spring Data 的 `Pageable`，前端页码从 1 开始：
```java
Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
```

## 实体关系

| 实体 | 说明 | 关联 |
|------|------|------|
| User | 用户（实现 UserDetails） | → Question(author), Record, Friendship |
| Question | 题目 | → User(author), 包含 imageKey 关联外部图床 |
| Record | 答题记录 | → User, RecordItem(1:N) |
| Friendship | 好友关系 | → User(sender), User(receiver) |

## 安全配置
- **JWT 认证**: 实际场景采用 JWT 认证（jjwt 库，24小时过期），配置在 `jwt.secret` 和 `jwt.expiration`
- **测试阶段**: `SecurityConfig` 中暂时禁用认证（`anyRequest().permitAll()`），上线前需启用
- **WebSocket**: 依赖已引入，待后续实现实时功能

## 开发命令

```bash
# 启动应用（demo 目录下）
./mvnw spring-boot:run

# 构建
./mvnw clean package
```

## 外部依赖
- **图床服务**: picui.cn API，通过 [ImageClient.java](demo/src/main/java/com/campusguess/demo/service/ImageClient.java) 调用，配置在 `application.yml` 的 `image.host.*`
- **数据库**: MySQL `campusguesssystem`，JPA 自动建表（ddl-auto: update）

## DTO 组织
```
model/dto/
├── auth/        # 登录相关
├── question/    # 题目相关
├── record/      # 答题记录
├── response/    # ApiResponse 通用响应
└── user/        # 用户、好友相关
```
