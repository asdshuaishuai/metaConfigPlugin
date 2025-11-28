package io.github.asdshuaishuai.gradle.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

/**
 * YAML配置文件解析器
 * 
 * 负责将build.yml文件解析为BuildConfig对象
 */
class YamlParser {
    
    private val mapper: ObjectMapper = ObjectMapper(YAMLFactory()).apply {
        // 注册Kotlin模块以支持Kotlin数据类
        registerModule(KotlinModule.Builder().build())
        
        // 配置反序列化选项
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
    }
    
    /**
     * 解析YAML文件为BuildConfig对象
     * 
     * @param yamlFile YAML配置文件
     * @return 解析后的BuildConfig对象
     * @throws YamlParseException 解析失败时抛出
     */
    fun parse(yamlFile: File): BuildConfig {
        if (!yamlFile.exists()) {
            throw YamlParseException("配置文件不存在: ${yamlFile.absolutePath}")
        }
        
        if (!yamlFile.canRead()) {
            throw YamlParseException("无法读取配置文件: ${yamlFile.absolutePath}")
        }
        
        return try {
            // 首先解析为通用的Map结构
            yamlFile.inputStream().use { inputStream ->
                val yamlMap = mapper.readValue<Map<String, Any>>(inputStream)
                parseBuildConfig(yamlMap)
            }
        } catch (e: Exception) {
            throw YamlParseException("解析YAML文件失败: ${e.message}", e)
        }
    }
    
    /**
     * 解析BuildConfig对象
     */
    @Suppress("UNCHECKED_CAST")
    private fun parseBuildConfig(yamlMap: Map<String, Any>): BuildConfig {
        val projectConfig = (yamlMap["project"] as? Map<String, Any?>)?.let { project ->
            ProjectConfig(
                group = project["group"] as? String,
                version = project["version"] as? String
            )
        }
        
        val plugins = (yamlMap["plugins"] as? List<Map<String, Any>>)?.map { plugin ->
            PluginConfig(
                id = plugin["id"] as String,
                version = plugin["version"] as? String
            )
        } ?: emptyList()
        
        val repositories = (yamlMap["repositories"] as? List<String>) ?: emptyList()
        
        val dependencies = (yamlMap["dependencies"] as? Map<String, Any>)?.let { deps ->
            DependencyConfig(
                implementation = (deps["implementation"] as? List<String>) ?: emptyList(),
                api = (deps["api"] as? List<String>) ?: emptyList(),
                compileOnly = (deps["compileOnly"] as? List<String>) ?: emptyList(),
                runtimeOnly = (deps["runtimeOnly"] as? List<String>) ?: emptyList(),
                testImplementation = (deps["testImplementation"] as? List<String>) ?: emptyList(),
                testApi = (deps["testApi"] as? List<String>) ?: emptyList(),
                testCompileOnly = (deps["testCompileOnly"] as? List<String>) ?: emptyList(),
                testRuntimeOnly = (deps["testRuntimeOnly"] as? List<String>) ?: emptyList()
            )
        }
        
        val tasks = (yamlMap["tasks"] as? Map<String, Any>)?.let { tasksMap ->
            tasksMap.mapValues { (_, taskData) ->
                parseTaskConfig(taskData as Map<String, Any>)
            }
        } ?: emptyMap()
        
        return BuildConfig(
            project = projectConfig,
            plugins = plugins,
            repositories = repositories,
            dependencies = dependencies,
            tasks = tasks
        )
    }
    
    /**
     * 解析任务配置
     */
    @Suppress("UNCHECKED_CAST")
    private fun parseTaskConfig(taskData: Map<String, Any>): TaskConfig {
        val type = taskData["type"] as? String ?: throw YamlParseException("任务配置缺少type字段")
        
        return when (type) {
            "Copy" -> CopyTaskConfig(
                type = type,
                from = taskData["from"] as? String,
                into = taskData["into"] as? String,
                include = taskData["include"] as? List<String>,
                exclude = taskData["exclude"] as? List<String>,
                description = taskData["description"] as? String,
                group = taskData["group"] as? String,
                dependsOn = taskData["dependsOn"] as? List<String>
            )
            
            "Exec" -> ExecTaskConfig(
                type = type,
                commandLine = taskData["commandLine"] as? List<String>,
                workingDir = taskData["workingDir"] as? String,
                environment = taskData["environment"] as? Map<String, String>,
                ignoreExitValue = taskData["ignoreExitValue"] as? Boolean,
                description = taskData["description"] as? String,
                group = taskData["group"] as? String,
                dependsOn = taskData["dependsOn"] as? List<String>
            )
            
            "JavaExec" -> JavaExecTaskConfig(
                type = type,
                mainClass = taskData["mainClass"] as? String,
                classpath = taskData["classpath"] as? String,
                args = taskData["args"] as? List<String>,
                jvmArgs = taskData["jvmArgs"] as? List<String>,
                workingDir = taskData["workingDir"] as? String,
                environment = taskData["environment"] as? Map<String, String>,
                ignoreExitValue = taskData["ignoreExitValue"] as? Boolean,
                description = taskData["description"] as? String,
                group = taskData["group"] as? String,
                dependsOn = taskData["dependsOn"] as? List<String>
            )
            
            "Delete" -> DeleteTaskConfig(
                type = type,
                delete = taskData["delete"] as? List<String>,
                description = taskData["description"] as? String,
                group = taskData["group"] as? String,
                dependsOn = taskData["dependsOn"] as? List<String>
            )
            
            "Sync" -> SyncTaskConfig(
                type = type,
                from = taskData["from"] as? String,
                into = taskData["into"] as? String,
                include = taskData["include"] as? List<String>,
                exclude = taskData["exclude"] as? List<String>,
                description = taskData["description"] as? String,
                group = taskData["group"] as? String,
                dependsOn = taskData["dependsOn"] as? List<String>
            )
            
            else -> GenericTaskConfig(
                type = type,
                properties = taskData - setOf("type", "description", "group", "dependsOn"),
                description = taskData["description"] as? String,
                group = taskData["group"] as? String,
                dependsOn = taskData["dependsOn"] as? List<String>
            )
        }
    }
    
    /**
     * 验证配置文件的格式和内容
     * 
     * @param config 待验证的配置对象
     * @throws YamlValidationException 验证失败时抛出
     */
    fun validate(config: BuildConfig) {
        val errors = mutableListOf<String>()
        
        // 验证插件配置
        config.plugins.forEachIndexed { index, plugin ->
            if (plugin.id.isBlank()) {
                errors.add("插件[${index}]的ID不能为空")
            }
        }
        
        // 验证依赖配置中的依赖格式
        config.dependencies?.let { deps ->
            deps.implementation.forEachIndexed { index, dep ->
                if (!isValidDependencyNotation(dep)) {
                    errors.add("implementation依赖[${index}]格式无效: $dep")
                }
            }
            
            deps.testImplementation.forEachIndexed { index, dep ->
                if (!isValidDependencyNotation(dep)) {
                    errors.add("testImplementation依赖[${index}]格式无效: $dep")
                }
            }
            
            // 验证其他依赖配置...
            deps.api.forEachIndexed { index, dep ->
                if (!isValidDependencyNotation(dep)) {
                    errors.add("api依赖[${index}]格式无效: $dep")
                }
            }
        }
        
        // 验证任务配置
        config.tasks.forEach { (taskName, taskConfig) ->
            when (taskConfig) {
                is JavaExecTaskConfig -> {
                    if (taskConfig.mainClass.isNullOrBlank()) {
                        errors.add("任务[$taskName]的JavaExec类型需要指定mainClass")
                    }
                }
                is ExecTaskConfig -> {
                    if (taskConfig.commandLine.isNullOrEmpty()) {
                        errors.add("任务[$taskName]的Exec类型需要指定commandLine")
                    }
                }
                is CopyTaskConfig -> {
                    if (taskConfig.from.isNullOrBlank() && taskConfig.into.isNullOrBlank()) {
                        errors.add("任务[$taskName]的${taskConfig.type}类型需要指定from或into")
                    }
                }
                is SyncTaskConfig -> {
                    if (taskConfig.from.isNullOrBlank() && taskConfig.into.isNullOrBlank()) {
                        errors.add("任务[$taskName]的${taskConfig.type}类型需要指定from或into")
                    }
                }
                is DeleteTaskConfig -> {
                    if (taskConfig.delete.isNullOrEmpty()) {
                        errors.add("任务[$taskName]的Delete类型需要指定delete")
                    }
                }
                else -> {
                    // GenericTaskConfig不需要特殊验证
                }
            }
        }
        
        if (errors.isNotEmpty()) {
            throw YamlValidationException("配置验证失败:\n${errors.joinToString("\n") { "  - $it" }}")
        }
    }
    
    /**
     * 检查依赖表示法是否有效
     * 
     * @param dependency 依赖字符串，格式应为 "group:artifact:version" 或 "group:artifact"
     * @return 是否有效
     */
    private fun isValidDependencyNotation(dependency: String): Boolean {
        if (dependency.isBlank()) return false
        
        val parts = dependency.split(":")
        return when (parts.size) {
            2 -> parts.all { it.isNotBlank() } // group:artifact
            3 -> parts.all { it.isNotBlank() } // group:artifact:version
            else -> false
        }
    }
}

/**
 * YAML解析异常
 */
class YamlParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * YAML验证异常
 */
class YamlValidationException(message: String) : Exception(message)