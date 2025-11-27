# Gradle插件开发模式指南

## 元配置架构模式
- **插件入口点**: 实现 `Plugin<Settings>` 而非 `Plugin<Project>`，确保在构建生命周期最早期介入
- **钩子注册**: 使用 `settings.gradle.afterProject` 钩子，在 `build.gradle.kts` 评估前执行配置注入
- **配置分离**: 声明式配置(build.yml)与实现逻辑(build.gradle.kts)完全分离

## 配置处理模式
- **容错设计**: 配置失败不阻塞构建，仅记录错误日志
- **条件应用**: 检查现有属性值，避免覆盖已设置的group/version
- **自动插件应用**: 当检测到依赖配置时自动应用Java插件

## YAML解析模式
- **宽松解析**: 配置Jackson忽略未知属性，允许空字符串
- **分层验证**: 先解析后验证，提供详细的错误定位
- **依赖格式验证**: 支持 `group:artifact` 和 `group:artifact:version` 两种格式

## 仓库配置模式
- **智能识别**: 将字符串映射到对应的仓库类型(mavencentral→mavenCentral)
- **自定义支持**: 非预定义仓库名称直接作为Maven仓库URL
- **异常隔离**: 单个仓库配置失败不影响其他仓库应用

## 服务层模式
- **单一职责**: ConfigService专注配置读取，ProjectConfigurator专注应用配置
- **日志聚合**: 通过构造函数注入Logger，统一日志输出格式
- **配置摘要**: 提供人类可读的配置概览信息