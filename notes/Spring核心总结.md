# Spring Framework总览

## 核心特性

+ IoC容器（IoC Container）
+ Spring事件(Events)
+ 资源管理(Resources)
+ 国际化(i18n)
+ 校验(Validation)
+ 数据绑定(Data Binding)
+ 类型转换（Type Conversation）
+ Spring表达式(Spring Expression Language)
+ 面向切面编程(AOP)

## 数据存储

+ JDBC
+ 事务抽象(Transactions)
+ DAO 支持(Dao Support)
+ O/R Mapping
+ XML编列

## Web技术

### Web Servlet

+ Spring MVC 
+  WebSocket 
+ SockJS

### Reactive

+ Spring WebFlux 
+ WebClient 
+  WebSocket

## 技术整合

+ 远程调用（Remoting）  
+ Java 消息服务（JMS）  
+ Java 连接架构（ JCA） 
+ Java 管理扩展（JMX）  
+ Java 邮件客户端（Email） 
+ 本地任务（Tasks）
+ 本地调度（Scheduling） 
+ 缓存抽象（Caching） 
+ Spring 测试（Testing）

# 重新认识IoC

## IoC容器的职责

+ 通用职责  
+ 依赖处理 
  + 依赖查找 
  + 依赖注入 
+ 生命周期管理 
	+ 容器 
	+ 托管的资源（Java Beans 或其他资源） 
+ 配置
	+  容器 
	+  外部化配置 
	+  托管的资源（Java Beans 或其他资源）