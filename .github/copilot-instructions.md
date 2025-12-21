# CampusGuessSystem - AI 编码指南

## 项目概述
校园猜猜看系统 - **Spring Boot 4.0** + **JPA** + **MySQL** 的 RESTful API 后端。  
所有 API 路径前缀：`/api`（配置于 `application.yml` 的 `server.servlet.context-path`）

## 架构模式

### 分层结构
```
controller → service (interface) → impl → repository → entity
                ↓
              dto (request/response)
```
- **Controller**: REST 端点，路径约定 `/users/{username}/资源` 或 `/资源`
- **Service**: 接口定义于 `service/`，实现于 `service/impl/`，使用 `@Transactional`
- **Repository**: Spring Data JPA，复杂查询使用 `@Query` JPQL

### 核心约定

**异常处理 - `BusinessException`**
```java
throw new BusinessException(404, "用户不存在");  // HTTP状态码 + 中文消息
throw new BusinessException(400, "不能添加自己为好友");
```
> GlobalExceptionHandler 自动捕获并转换为 `ApiResponse.error(code, message)`

**统一响应 - `ApiResponse<T>`**
```java
return ResponseEntity.ok(ApiResponse.success("操作成功", data));
return ResponseEntity.status(201).body(ApiResponse.created("创建成功", data));
```

**依赖注入 - 构造器注入 + Lombok**
```java
@Service
@RequiredArgsConstructor
@Slf4j  // 日志使用 log.info/warn/debug
public class XxxServiceImpl implements XxxService {
    private final XxxRepository xxxRepository;
}
```

**分页查询 - 前端页码从 1 开始**
```java
Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
```

## 业务模式

### 用户身份识别
当前使用**路径参数** `{username}` 识别用户（JWT 认证暂时禁用于 `SecurityConfig`）：
```java
@PostMapping("/users/{username}/questions")
public ResponseEntity<...> create(@PathVariable String username, ...) {}
```

### 好友关系 - 双向记录
`Friendship` 实体使用双向记录模式（A→B 和 B→A 各一条记录）：
- 发送申请：创建 `PENDING` 记录
- 接受申请：更新原记录为 `APPROVED` + 创建反向 `APPROVED` 记录
- 删除好友：删除双向记录
- 状态枚举：`PENDING`、`APPROVED`、`REJECTED`

### 图床集成
题目图片通过 `imageKey` 关联外部图床（picui.cn），运行时通过 `ImageClient.fetchImageByKey()` 获取完整图片信息。

## 实体关系

| 实体 | 关键字段 | 关联 |
|------|---------|------|
| User | username(唯一), points, role | 实现 UserDetails |
| Question | imageKey, correctLon/Lat | → User(author) |
| Record | earnPoints, pointBefore/After | → User, RecordItem(1:N) |
| Friendship | status, handledType | → User(sender), User(receiver) |

## 开发命令

```powershell
# 在 demo/ 目录下执行
.\mvnw spring-boot:run     # 启动应用 (端口 8080)
.\mvnw clean package       # 构建 JAR
.\mvnw test                # 运行测试
```

## 配置要点
- **数据库**: MySQL `campusguesssystem`，JPA `ddl-auto: update` 自动建表
- **JWT**: `jwt.secret` + `jwt.expiration`（24小时），认证逻辑在 `JwtTokenUtil`
- **图床**: `image.host.url` + `image.host.token`

## 新增功能检查清单
1. [ ] Entity 添加到 `model/entity/`，使用 `@PrePersist` 设置创建时间
2. [ ] Repository 继承 `JpaRepository`，复杂查询用 JPQL
3. [ ] Service 接口 + Impl 分离，Impl 加 `@Transactional`
4. [ ] DTO 按业务分组放入 `model/dto/{模块}/`
5. [ ] Controller 异常由全局处理器捕获，无需 try-catch（除特殊情况）
