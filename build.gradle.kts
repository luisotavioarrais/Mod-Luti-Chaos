plugins { java }
group = "play.luti"
version = "1.0.0"
repositories { mavenCentral() }
val serverJar = providers.gradleProperty("hytaleServerJar").orElse(System.getenv("APPDATA") + "/Hytale/install/release/package/game/latest/Server/HytaleServer.jar")
dependencies {
    compileOnly(files(serverJar))
    testImplementation(files(serverJar))
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
java { toolchain { languageVersion.set(JavaLanguageVersion.of(25)) } }
tasks.test { useJUnitPlatform(); systemProperty("java.util.logging.manager", "com.hypixel.hytale.logger.backend.HytaleLogManager") }
tasks.jar { archiveBaseName.set("LutiChaos") }
tasks.register<Copy>("dist") { dependsOn(tasks.build); from(tasks.jar); into("dist") }

