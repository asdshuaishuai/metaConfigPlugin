package io.github.asdshuaishuai.gradle.service

import io.github.asdshuaishuai.gradle.config.BuildConfig
import io.github.asdshuaishuai.gradle.config.CopyTaskConfig
import io.github.asdshuaishuai.gradle.config.DeleteTaskConfig
import io.github.asdshuaishuai.gradle.config.ExecTaskConfig
import io.github.asdshuaishuai.gradle.config.GenericTaskConfig
import io.github.asdshuaishuai.gradle.config.JavaExecTaskConfig
import io.github.asdshuaishuai.gradle.config.SyncTaskConfig
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Sync
import java.net.URI

/**
 * 项目配置器
 *
 * 负责将BuildConfig中的配置应用到Gradle项目中
 */
class ProjectConfigurator(private val logger: Logger) {

    /**
     * 应用所有配置到项目
     *
     * @param project Gradle项目对象
     * @param config 解析后的配置对象
     */
    fun applyConfiguration(project: Project, config: BuildConfig) {
        logger.lifecycle("开始应用元配置到项目: ${project.name}")

        // 1. 应用项目基本信息
        applyProjectInfo(project, config)

        // 2. 应用仓库配置
        applyRepositories(project, config)

        // 3. 应用依赖配置
        applyDependencies(project, config)

//        // 4. 应用任务配置
//        applyTasks(project, config)

        logger.lifecycle("元配置应用完成")
    }

    /**
     * 应用项目基本信息（group和version）
     */
    private fun applyProjectInfo(project: Project, config: BuildConfig) {
        config.project?.let { projectConfig ->
            projectConfig.group?.let { group ->
                if (project.group == null || project.group.toString().isBlank()) {
                    project.group = group
                    logger.debug("设置项目group: $group")
                } else {
                    logger.debug("项目group已存在，跳过设置: ${project.group}")
                }
            }

            projectConfig.version?.let { version ->
                if (project.version == null || project.version.toString().isBlank()) {
                    project.version = version
                    logger.debug("设置项目version: $version")
                } else {
                    logger.debug("项目version已存在，跳过设置: ${project.version}")
                }
            }
        }
    }

    /**
     * 应用仓库配置
     */
    private fun applyRepositories(project: Project, config: BuildConfig) {
        if (config.repositories.isNotEmpty()) {
            logger.lifecycle("应用 ${config.repositories.size} 个仓库")

            config.repositories.forEach { repository ->
                try {
                    when (repository.lowercase()) {
                        "mavencentral", "mavencentral" -> project.repositories.mavenCentral()
                        "google" -> project.repositories.google()
                        "gradlepluginportal", "gradle-plugin-portal" -> project.repositories.gradlePluginPortal()
                        "local", "mavenlocal", "maven-local" -> project.repositories.mavenLocal()
                        else -> {
                            // 检查是否是有效的URL格式
                            if (repository.startsWith("http://") || repository.startsWith("https://") ||
                                repository.startsWith("file://") || repository.startsWith("s3://") ||
                                repository.startsWith("gcs://") || repository.startsWith("sftp://")) {
                                // 自定义Maven仓库
                                project.repositories.maven {
                                    it.name = repository
                                    it.url = URI(repository)
                                }
                                logger.debug("应用自定义仓库: $repository")
                            } else {
                                logger.warn("跳过无效的仓库配置: $repository (不是预定义仓库类型，也不是有效的URL)")
                            }
                        }
                    }
                    logger.debug("应用仓库: $repository")
                } catch (e: Exception) {
                    logger.error("应用仓库失败: $repository - ${e.message}")
                    throw e
                }
            }
        }
    }

    /**
     * 应用依赖配置
     */
    private fun applyDependencies(project: Project, config: BuildConfig) {
        config.dependencies?.let { deps ->
            // 确保Java插件已应用，这样依赖配置才会存在
            if (!project.plugins.hasPlugin("java")) {
                project.pluginManager.apply("java")
                logger.lifecycle("应用Java插件以确保依赖配置存在")
            }

            val dependencyHandler = project.dependencies
            var totalDependencies = 0

            // 应用各种配置的依赖
            totalDependencies += applyDependencyConfiguration(dependencyHandler, "implementation", deps.implementation)
            totalDependencies += applyDependencyConfiguration(dependencyHandler, "api", deps.api)
            totalDependencies += applyDependencyConfiguration(dependencyHandler, "compileOnly", deps.compileOnly)
            totalDependencies += applyDependencyConfiguration(dependencyHandler, "runtimeOnly", deps.runtimeOnly)
            totalDependencies += applyDependencyConfiguration(dependencyHandler, "testImplementation", deps.testImplementation)
            totalDependencies += applyDependencyConfiguration(dependencyHandler, "testApi", deps.testApi)
            totalDependencies += applyDependencyConfiguration(dependencyHandler, "testCompileOnly", deps.testCompileOnly)
            totalDependencies += applyDependencyConfiguration(dependencyHandler, "testRuntimeOnly", deps.testRuntimeOnly)

            if (totalDependencies > 0) {
                logger.lifecycle("应用了 $totalDependencies 个依赖")
            }
        }
    }

    /**
     * 应用特定配置的依赖
     */
    private fun applyDependencyConfiguration(
        dependencyHandler: DependencyHandler,
        configuration: String,
        dependencies: List<String>
    ): Int {
        if (dependencies.isEmpty()) return 0

        dependencies.forEach { dependency ->
            try {
                dependencyHandler.add(configuration, dependency)
                logger.debug("添加$configuration  依赖: $dependency")
            } catch (e: Exception) {
                logger.error("Add $configuration dependency failed: $dependency - ${e.message}")
                throw e
            }
        }

        return dependencies.size
    }

    /**
     * 应用任务配置
     */
    private fun applyTasks(project: Project, config: BuildConfig) {
        if (config.tasks.isNotEmpty()) {
            logger.lifecycle("应用 ${config.tasks.size} 个任务")

            config.tasks.forEach { (taskName, taskConfig) ->
                try {
                    when (taskConfig) {
                        is CopyTaskConfig -> createCopyTask(project, taskName, taskConfig)
                        is ExecTaskConfig -> createExecTask(project, taskName, taskConfig)
                        is JavaExecTaskConfig -> createJavaExecTask(project, taskName, taskConfig)
                        is DeleteTaskConfig -> createDeleteTask(project, taskName, taskConfig)
                        is SyncTaskConfig -> createSyncTask(project, taskName, taskConfig)
                        is GenericTaskConfig -> createGenericTask(project, taskName, taskConfig)
                    }
                    logger.debug("应用任务: $taskName")
                } catch (e: Exception) {
                    logger.error("应用任务失败: $taskName - ${e.message}")
                    throw e
                }
            }
        }
    }

    /**
     * 创建Copy任务
     */
    private fun createCopyTask(project: Project, taskName: String, config: CopyTaskConfig) {
        project.tasks.register(taskName, Copy::class.java) { task ->
            config.description?.let { task.description = it }
            config.group?.let { task.group = it }
            config.dependsOn?.let { task.dependsOn(it) }
            
            config.from?.let { from ->
                task.from(project.file(from))
            }
            config.into?.let { into ->
                task.into(project.file(into))
            }
            config.include?.let { includes ->
                task.include(includes)
            }
            config.exclude?.let { excludes ->
                task.exclude(excludes)
            }
        }
    }

    /**
     * 创建Exec任务
     */
    private fun createExecTask(project: Project, taskName: String, config: ExecTaskConfig) {
        project.tasks.register(taskName, Exec::class.java) { task ->
            config.description?.let { task.description = it }
            config.group?.let { task.group = it }
            config.dependsOn?.let { task.dependsOn(it) }
            
            config.commandLine?.let { commandLine ->
                task.commandLine(commandLine)
            }
            config.workingDir?.let { workingDir ->
                task.workingDir(project.file(workingDir))
            }
            config.environment?.let { environment ->
                task.environment(environment)
            }
            config.ignoreExitValue?.let { ignoreExitValue ->
                task.isIgnoreExitValue = ignoreExitValue
            }
        }
    }

    /**
     * 创建JavaExec任务
     */
    private fun createJavaExecTask(project: Project, taskName: String, config: JavaExecTaskConfig) {
        project.tasks.register(taskName, JavaExec::class.java) { task ->
            config.description?.let { task.description = it }
            config.group?.let { task.group = it }
            config.dependsOn?.let { task.dependsOn(it) }
            
            config.mainClass?.let { mainClass ->
                task.mainClass.set(mainClass)
            }
            config.classpath?.let { classpath ->
                // 支持简单的classpath表达式
                when (classpath) {
                    "sourceSets.main.runtimeClasspath" -> {
                        task.classpath = project.objects.fileCollection()
                            .from(project.configurations.getByName("runtimeClasspath"))
                            .from(project.tasks.getByName("compileJava").outputs)
                    }
                    else -> {
                        // 尝试作为配置名称处理
                        try {
                            task.classpath = project.configurations.getByName(classpath)
                        } catch (e: Exception) {
                            logger.warn("无法解析classpath配置: $classpath")
                        }
                    }
                }
            }
            config.args?.let { args ->
                task.args(args)
            }
            config.jvmArgs?.let { jvmArgs ->
                task.jvmArgs(jvmArgs)
            }
            config.workingDir?.let { workingDir ->
                task.workingDir(project.file(workingDir))
            }
            config.environment?.let { environment ->
                task.environment(environment)
            }
            config.ignoreExitValue?.let { ignoreExitValue ->
                task.isIgnoreExitValue = ignoreExitValue
            }
        }
    }

    /**
     * 创建Delete任务
     */
    private fun createDeleteTask(project: Project, taskName: String, config: DeleteTaskConfig) {
        project.tasks.register(taskName, Delete::class.java) { task ->
            config.description?.let { task.description = it }
            config.group?.let { task.group = it }
            config.dependsOn?.let { task.dependsOn(it) }
            
            config.delete?.let { deleteList ->
                deleteList.forEach { deletePath ->
                    task.delete(project.file(deletePath))
                }
            }
        }
    }

    /**
     * 创建Sync任务
     */
    private fun createSyncTask(project: Project, taskName: String, config: SyncTaskConfig) {
        project.tasks.register(taskName, Sync::class.java) { task ->
            config.description?.let { task.description = it }
            config.group?.let { task.group = it }
            config.dependsOn?.let { task.dependsOn(it) }
            
            config.from?.let { from ->
                task.from(project.file(from))
            }
            config.into?.let { into ->
                task.into(project.file(into))
            }
            config.include?.let { includes ->
                task.include(includes)
            }
            config.exclude?.let { excludes ->
                task.exclude(excludes)
            }
        }
    }

    /**
     * 创建通用任务
     */
    private fun createGenericTask(project: Project, taskName: String, config: GenericTaskConfig) {
        project.tasks.register(taskName) { task ->
            config.description?.let { task.description = it }
            config.group?.let { task.group = it }
            config.dependsOn?.let { task.dependsOn(it) }
            
            // 对于通用任务，我们只设置基本属性
            // 具体的任务逻辑需要在build.gradle.kts中定义
            logger.debug("创建通用任务: $taskName (类型: ${config.type})")
        }
    }
}