import plugins.ConfigAndroidLibrary
import plugins.ConfigPublish
import utils.artifactIdProperty
import utils.artifactPrefix
import utils.integrationPrefix
import utils.versionProperty

plugins {
    id("com.android.library")
    id("kotlin-android")
    //id("org.jetbrains.dokka")
}

apply<ConfigAndroidLibrary>()
apply<ConfigPublish>()

ext {
    set(artifactIdProperty, "$artifactPrefix${integrationPrefix}agent-${project.name}")
    set(versionProperty, Configurations.sdkVersionName)
}

android {
    namespace = "com.cisco.android.rum.integration.agent.api"
}

dependencies {
    api(project(":common:otel:api"))
    api(project(":integration:agent:module"))

    implementation(project(":common:otel:internal"))
    // TODO implementation(project(":common:logger"))
    // TODO implementation(project(":common:storage"))
    implementation(project(":integration:agent:internal"))
    // TODO implementation(project(":common:utils"))
}