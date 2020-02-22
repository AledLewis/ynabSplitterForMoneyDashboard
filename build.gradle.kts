plugins {
    kotlin("jvm") version("1.3.61")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.7.3")
}

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin"){
    kotlinOptions.jvmTarget = "1.8"
}
tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileTestKotlin") {
    kotlinOptions.jvmTarget = "1.8"
}

application{
    mainClassName = "dev.aledlewis.ynabsplitterformoneydashboard.SplitterKt"
}

tasks.named<CreateStartScripts>("startScripts"){

}

