package io.github.asdshuaishuai.gradle.config

/**
 * build.yml 文件的根配置结构
 */
data class BuildConfig(
    val project: ProjectConfig? = null,
    val plugins: List<PluginConfig> = emptyList(),
    val repositories: List<String> = emptyList(),
    val dependencies: DependencyConfig? = null,
    val tasks: Map<String, TaskConfig> = emptyMap()
)

/**
 * 项目基本信息配置
 */
data class ProjectConfig(
    val group: String? = null,
    val version: String? = null
)

/**
 * 插件配置
 */
data class PluginConfig(
    val id: String,
    val version: String? = null
)

/**
 * 依赖配置
 */
data class DependencyConfig(
    val implementation: List<String> = emptyList(),
    val api: List<String> = emptyList(),
    val compileOnly: List<String> = emptyList(),
    val runtimeOnly: List<String> = emptyList(),
    val testImplementation: List<String> = emptyList(),
    val testApi: List<String> = emptyList(),
    val testCompileOnly: List<String> = emptyList(),
    val testRuntimeOnly: List<String> = emptyList()
)

/**
 * 任务配置基类
 */
sealed class TaskConfig {
    abstract val type: String
    abstract val description: String?
    abstract val group: String?
    abstract val dependsOn: List<String>?
}

/**
 * Copy任务配置
 */
data class CopyTaskConfig(
    override val type: String = "Copy",
    val from: String? = null,
    val into: String? = null,
    val include: List<String>? = null,
    val exclude: List<String>? = null,
    override val description: String? = null,
    override val group: String? = null,
    override val dependsOn: List<String>? = null
) : TaskConfig()

/**
 * Exec任务配置
 */
data class ExecTaskConfig(
    override val type: String = "Exec",
    val commandLine: List<String>? = null,
    val workingDir: String? = null,
    val environment: Map<String, String>? = null,
    val ignoreExitValue: Boolean? = null,
    override val description: String? = null,
    override val group: String? = null,
    override val dependsOn: List<String>? = null
) : TaskConfig()

/**
 * JavaExec任务配置
 */
data class JavaExecTaskConfig(
    override val type: String = "JavaExec",
    val mainClass: String? = null,
    val classpath: String? = null,
    val args: List<String>? = null,
    val jvmArgs: List<String>? = null,
    val workingDir: String? = null,
    val environment: Map<String, String>? = null,
    val ignoreExitValue: Boolean? = null,
    override val description: String? = null,
    override val group: String? = null,
    override val dependsOn: List<String>? = null
) : TaskConfig()

/**
 * Delete任务配置
 */
data class DeleteTaskConfig(
    override val type: String = "Delete",
    val delete: List<String>? = null,
    override val description: String? = null,
    override val group: String? = null,
    override val dependsOn: List<String>? = null
) : TaskConfig()

/**
 * Sync任务配置
 */
data class SyncTaskConfig(
    override val type: String = "Sync",
    val from: String? = null,
    val into: String? = null,
    val include: List<String>? = null,
    val exclude: List<String>? = null,
    override val description: String? = null,
    override val group: String? = null,
    override val dependsOn: List<String>? = null
) : TaskConfig()

/**
 * 通用任务配置（用于其他类型的任务）
 */
data class GenericTaskConfig(
    override val type: String,
    val properties: Map<String, Any>? = null,
    override val description: String? = null,
    override val group: String? = null,
    override val dependsOn: List<String>? = null
) : TaskConfig()
