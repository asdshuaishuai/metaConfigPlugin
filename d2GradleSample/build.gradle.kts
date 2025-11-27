plugins {
    kotlin("jvm") version "2.2.20"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = "com.d2rabbit"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation(gradleApi())
    testImplementation(kotlin("test"))
}

// 配置Gradle插件发布
gradlePlugin {
    website = "https://github.com/asdshuaishuai/metaConfigPlugin"
    vcsUrl = "https://github.com/asdshuaishuai/metaConfigPlugin.git"
    
    plugins {
        create("metaConfigPlugin") {
            id = "com.d2rabbit.meta-config"
            displayName = "Meta Config Plugin"
            description = "A Gradle plugin that enhances build.gradle.kts by moving dependencies, basic project information, and repository configurations into a build.yml file."
            tags = listOf("yaml", "configuration", "dependencies", "repositories", "meta-config")
            implementationClass = "com.d2rabbit.gradle.MetaConfigPlugin"
        }
    }
}

// 配置发布信息
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            
            // 配置POM信息
            pom {
                name.set("Meta Config Plugin")
                description.set("A Gradle plugin that enhances build.gradle.kts by moving dependencies, basic project information, and repository configurations into a build.yml file.")
                url.set("https://github.com/asdshuaishuai/metaConfigPlugin")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("kelthas")
                        name.set("kelthas huo")
                        email.set("asdshuaishuai@outlook.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com:asdshuaishuai/metaConfigPlugin.git")
                    developerConnection.set("scm:git:ssh://github.com:asdshuaishuai/metaConfigPlugin.git")
                    url.set("https://github.com/asdshuaishuai/metaConfigPlugin/tree/master")
                }
            }
        }
    }
    
    repositories {
        mavenLocal()  // 发布到本地 Maven 仓库
    }
}

// 配置JavaDoc和源码发布任务
java {
    withSourcesJar()
    withJavadocJar()
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

// 配置处理重复文件
tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// 配置Javadoc
tasks.withType<Javadoc> {
    options {
        encoding = "UTF-8"
        (this as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}