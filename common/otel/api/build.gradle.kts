import plugins.ConfigAndroidLibrary
import plugins.ConfigPublish
import utils.artifactIdProperty
import utils.artifactPrefix
import utils.commonPrefix
import utils.versionProperty

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
}

apply<ConfigAndroidLibrary>()
apply<ConfigPublish>()

ext {
    set(artifactIdProperty, "$artifactPrefix${commonPrefix}otel-${project.name}")
    set(versionProperty, Configurations.sdkVersionName)
}

android {
    namespace = "com.cisco.mrum.common.otel.api"
}

dependencies {
    compileOnly(Dependencies.Android.annotation)

    implementation(project(":common:otel:internal"))
    // TODO implementation(project(":common:job"))
    // TODO implementation(project(":common:http"))
    // TODO implementation(project(":common:storage"))
    // TODO implementation(project(":common:logger"))
    // TODO implementation(project(":common:utils"))

    api(Dependencies.Otel.sdk)
    api(Dependencies.Otel.api)
    api(Dependencies.Otel.extensionIncubator)
    api(Dependencies.Otel.exporterOtlpCommon)
    api(Dependencies.Otel.exporterOtlp) {
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    api(Dependencies.Otel.semConv)
}