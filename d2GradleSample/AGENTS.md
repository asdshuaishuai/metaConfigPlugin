# D2GradleSample 项目智能体指南

## 核心设计模式
- **元配置插件**: 通过 `build.yml` 文件管理项目配置，而非直接在 `build.gradle.kts` 中声明
- **混合配置模式**: `build.yml` 负责声明，`build.gradle.kts` 负责实现逻辑
- **插件实现**: 实现 `Plugin<Settings>` 接口，在构建生命周期最早期介入

## 关键技术细节
- **JVM工具链**: Java 21（非默认的Java 17）
- **插件ID**: `com.d2rabbit.meta-config`
- **配置文件**: 项目根目录的 `build.yml`（非标准位置）
- **依赖管理**: 自动应用Java插件以确保依赖配置存在

## 非标准配置
- **仓库处理**: 支持自定义Maven仓库URL作为仓库名称
- **配置合并**: 元配置与build.gradle.kts配置遵循Gradle标准行为（依赖累加，属性覆盖）
- **错误处理**: 配置失败不阻塞构建，仅记录错误

## 构建特性
- **发布配置**: 自动发布到本地Maven仓库
- **重复文件处理**: 使用 `DuplicatesStrategy.EXCLUDE` 策略
- **YAML解析**: 使用Jackson YAML模块，配置宽松解析模式

## 代码约定
- **日志级别**: 使用lifecycle级别输出关键配置信息
- **验证规则**: 依赖格式支持 `group:artifact` 和 `group:artifact:version` 两种格式
- **包结构**: 分离配置(config)和服务(service)层