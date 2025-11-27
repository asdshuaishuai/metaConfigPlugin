package com.d2rabbit.gradle.config

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File

/**
 * 配置服务
 * 
 * 提供统一的配置读取、解析和管理功能
 */
class ConfigService(private val logger: Logger) {
    
    private val yamlParser = YamlParser()
    private var lastModifiedTime: Long = 0
    
    /**
     * 加载并解析build.yml配置文件
     * 
     * @param project Gradle项目对象
     * @return 解析后的BuildConfig对象，如果文件不存在则返回空配置
     */
    fun loadConfig(project: Project): BuildConfig {
        val buildYamlFile = project.projectDir.resolve("build.yml")
        
        return if (buildYamlFile.exists()) {
            try {
                // 检查文件是否有变化
                val currentModifiedTime = buildYamlFile.lastModified()
                if (hasFileChanged(buildYamlFile, currentModifiedTime)) {
                    logger.lifecycle("检测到 build.yml 文件变化，重新加载配置...")
                    lastModifiedTime = currentModifiedTime
                }
                
                logger.lifecycle("正在解析 build.yml 配置文件...")
                val config = yamlParser.parse(buildYamlFile)
                yamlParser.validate(config)
                logger.lifecycle("build.yml 配置文件解析成功")
                config
            } catch (e: YamlParseException) {
                logger.error("解析 build.yml 文件失败: ${e.message}")
                throw e
            } catch (e: YamlValidationException) {
                logger.error("验证 build.yml 配置失败: ${e.message}")
                throw e
            } catch (e: Exception) {
                logger.error("处理 build.yml 文件时发生未知错误: ${e.message}", e)
                throw YamlParseException("处理配置文件时发生未知错误", e)
            }
        } else {
            logger.debug("未找到 build.yml 文件，使用默认配置")
            BuildConfig()
        }
    }
    
    /**
     * 检查文件是否有变化
     */
    private fun hasFileChanged(file: File, currentModifiedTime: Long): Boolean {
        return currentModifiedTime > lastModifiedTime
    }
    
    /**
     * 检查项目是否包含元配置
     * 
     * @param project Gradle项目对象
     * @return 是否包含build.yml文件
     */
    fun hasMetaConfig(project: Project): Boolean {
        return project.projectDir.resolve("build.yml").exists()
    }
    
    /**
     * 获取配置文件的摘要信息
     * 
     * @param config BuildConfig对象
     * @return 配置摘要字符串
     */
    fun getConfigSummary(config: BuildConfig): String {
        val summary = mutableListOf<String>()
        
        config.project?.let { project ->
            project.group?.let { summary.add("group: $it") }
            project.version?.let { summary.add("version: $it") }
        }
        
        if (config.plugins.isNotEmpty()) {
            summary.add("plugins: ${config.plugins.size}个")
        }
        
        if (config.repositories.isNotEmpty()) {
            summary.add("repositories: ${config.repositories.size}个")
        }
        
        config.dependencies?.let { deps ->
            val depCount = deps.implementation.size + deps.testImplementation.size + 
                         deps.api.size + deps.compileOnly.size + deps.runtimeOnly.size +
                         deps.testApi.size + deps.testCompileOnly.size + deps.testRuntimeOnly.size
            if (depCount > 0) {
                summary.add("dependencies: ${depCount}个")
            }
        }
        
        return if (summary.isNotEmpty()) {
            summary.joinToString(", ")
        } else {
            "无配置"
        }
    }
    
    /**
     * 验证配置文件路径
     * 
     * @param project Gradle项目对象
     * @return 配置文件是否存在且可读
     */
    fun validateConfigFile(project: Project): Boolean {
        val buildYamlFile = project.projectDir.resolve("build.yml")
        return buildYamlFile.exists() && buildYamlFile.canRead()
    }
}