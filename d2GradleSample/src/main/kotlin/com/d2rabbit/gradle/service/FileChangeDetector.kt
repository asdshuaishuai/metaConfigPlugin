package com.d2rabbit.gradle.service

import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * 文件变化检测器
 * 
 * 用于监控build.yml文件的变化
 */
object FileChangeDetector {
    
    private val fileTimestamps = ConcurrentHashMap<String, Long>()
    
    /**
     * 检查文件是否有变化
     * 
     * @param file 要检查的文件
     * @return 文件是否有变化
     */
    fun hasFileChanged(file: File): Boolean {
        val filePath = file.absolutePath
        val currentTimestamp = file.lastModified()
        
        val lastTimestamp = fileTimestamps.getOrDefault(filePath, 0L)
        
        if (currentTimestamp > lastTimestamp) {
            fileTimestamps[filePath] = currentTimestamp
            return true
        }
        
        return false
    }
    
    /**
     * 注册要监控的文件
     * 
     * @param file 要监控的文件
     */
    fun registerFile(file: File) {
        val filePath = file.absolutePath
        if (file.exists()) {
            fileTimestamps[filePath] = file.lastModified()
        }
    }
    
    /**
     * 清除文件监控记录
     * 
     * @param file 要清除的文件
     */
    fun clearFile(file: File) {
        val filePath = file.absolutePath
        fileTimestamps.remove(filePath)
    }
}