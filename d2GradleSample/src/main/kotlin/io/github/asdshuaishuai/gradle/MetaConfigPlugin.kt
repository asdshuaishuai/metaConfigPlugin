package io.github.asdshuaishuai.gradle

import io.github.asdshuaishuai.gradle.config.BuildConfig
import io.github.asdshuaishuai.gradle.config.ConfigService
import io.github.asdshuaishuai.gradle.service.ProjectConfigurator
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

/**
 * Gradle Meta Config Plugin
 * 
 * This plugin allows managing project plugins, repositories, dependencies, etc. 
 * through build.yml file, simplifying the complexity of build.gradle.kts.
 * 
 * Plugin ID: io.github.asdshuaishuai.meta-config
 */
class MetaConfigPlugin : Plugin<Settings> {
    
    private lateinit var configService: ConfigService
    private lateinit var projectConfigurator: ProjectConfigurator
    
    override fun apply(settings: Settings) {
        // Register afterProject hook, execute after build.gradle.kts is evaluated
        settings.gradle.afterProject { project ->
            // Initialize service components here so we can use project.logger
            configService = ConfigService(project.logger)
            projectConfigurator = ProjectConfigurator(project.logger)
            
            try {
                applyMetaConfiguration(project)
            } catch (e: Exception) {
                project.logger.error("Error applying meta configuration: ${e.message}", e)
                // Configuration failure should not block build, just log error
            }
        }
    }
    
    /**
     * Apply meta configuration to project
     */
    private fun applyMetaConfiguration(project: Project) {
        // Check if project contains meta configuration file
        if (!configService.hasMetaConfig(project)) {
            project.logger.debug("Project ${project.name} does not contain build.yml file, skipping meta configuration")
            return
        }
        
        project.logger.lifecycle("Starting to apply meta configuration for project ${project.name}...")
        
        try {
            // 1. Load and parse configuration file
            val config = configService.loadConfig(project)
            
            // 2. Display configuration summary
            val summary = configService.getConfigSummary(config)
            project.logger.lifecycle("Configuration summary: $summary")
            
            // 3. Apply configuration to project
            projectConfigurator.applyConfiguration(project, config)
            
            project.logger.lifecycle("Meta configuration applied successfully for project ${project.name}")
            
        } catch (e: Exception) {
            project.logger.error("Meta configuration application failed for project ${project.name}: ${e.message}")
            throw e
        }
    }
}