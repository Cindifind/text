# Text - Spring Boot 服务注册与监控框架

一个基于 Spring Boot 的轻量级服务注册与监控框架，通过注解的方式实现服务的自动注册和系统资源监控。

## 项目概述

本项目提供了一套简单易用的服务注册机制，通过 `@Client` 和 `@RegisterClient` 注解，可以自动将服务信息注册到指定的注册中心，并定期发送心跳包含系统监控信息。

## 主要特性

- 🚀 **注解驱动**: 通过简单的注解即可实现服务注册
- 📊 **系统监控**: 自动收集 CPU 和内存使用率信息
- 💓 **心跳检测**: 定期向注册中心发送心跳包
- 🔧 **自动配置**: 基于 Spring Boot 自动配置机制
- 🌐 **HTTP 通信**: 使用 HTTP 协议进行服务通信

## 技术栈

- **Java**: 17
- **Spring Boot**: 3.5.5
- **HTTP 客户端**: Unirest Java 3.14.5
- **JSON 处理**: org.json 20231013
- **构建工具**: Maven

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── org/example/text/
│   │       ├── autoconfigure/          # 自动配置类
│   │       │   └── ClientAnnotationAutoConfiguration.java
│   │       ├── client/                 # 客户端注解和实体
│   │       │   ├── Client.java         # 客户端注解
│   │       │   ├── RegisterClient.java # 注册客户端注解
│   │       │   └── State.java          # 状态实体类
│   │       ├── config/                 # 配置类
│   │       │   └── ServerClineConfig.java
│   │       ├── service/                # 服务类
│   │       │   └── ClientAnnotationService.java
│   │       └── util/                   # 工具类
│   │           ├── StageTimer.java     # 定时器工具
│   │           └── SystemMonitor.java  # 系统监控工具
│   └── resources/
│       ├── application.properties      # 应用配置
│       └── META-INF/
│           └── spring.factories        # Spring 自动配置
└── test/
    └── java/
```

## 快速开始

### 1. 添加依赖

在你的 `pom.xml` 中添加本项目依赖：

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>text</artifactId>
    <version>1.0.1</version>
</dependency>
```

### 2. 配置文件

在 `application.properties` 中添加配置：

```properties
# 应用名称
spring.application.name=your-app-name

# 服务注册中心配置
server.cline.port=2345
server.cline.server=localhost
server.cline.pubIp=127.0.0.1
```

### 3. 使用注解

#### 方式一：类级别注解

```java
@Component
@Client(address="/api/user", name="用户服务")
public class UserService {
    // 服务实现
}
```

#### 方式二：方法级别注解

```java
@RestController
public class UserController {
    
    @Client(address="/api/user/list", name="用户列表")
    @GetMapping("/api/user/list")
    public List<User> getUserList() {
        // 方法实现
    }
}
```

#### 方式三：注册客户端注解

```java
@SpringBootApplication
@RegisterClient
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 核心组件说明

### @Client 注解

用于标记需要注册的服务或接口：

- `address()`: 服务地址路径
- `name()`: 服务名称描述

### @RegisterClient 注解

用于启用服务注册功能，通常标记在主类上。

### ClientAnnotationService

核心服务类，负责：
- 扫描带有 `@Client` 注解的类和方法
- 收集服务信息并注册到注册中心
- 定期发送心跳包含系统监控数据

### SystemMonitor

系统监控工具类，提供：
- CPU 使用率监控
- 内存使用率监控
- 格式化的监控数据输出

## 配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `server.cline.port` | 注册中心端口 | 2345 |
| `server.cline.server` | 注册中心地址 | localhost |
| `server.cline.pubIp` | 公网IP地址 | 127.0.0.1 |

## 工作原理

1. **启动扫描**: 应用启动时，`ClientAnnotationService` 自动扫描所有带有 `@Client` 和 `@RegisterClient` 注解的类和方法

2. **信息收集**: 收集服务的地址、名称等信息，构建服务状态对象

3. **注册服务**: 将服务信息发送到配置的注册中心

4. **心跳监控**: 每30秒向注册中心发送心跳包，包含最新的CPU和内存使用率信息

## 构建和运行

### 构建项目

```bash
./mvnw clean package
```

### 运行项目

```bash
./mvnw spring-boot:run
```

或者运行打包后的 JAR 文件：

```bash
java -jar target/text-1.0.1.jar
```

## 注意事项

- 确保注册中心服务正常运行并监听配置的端口
- 心跳间隔固定为30秒，暂不支持配置
- 系统监控数据依赖于 JVM 的 `OperatingSystemMXBean`

## 版本信息

- **当前版本**: 1.0.1
- **Java 版本**: 17+
- **Spring Boot 版本**: 3.5.5

## 许可证

本项目采用 Apache License 2.0 许可证。

## 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！

---

*如有问题或建议，请通过 GitHub Issues 联系我们。*
