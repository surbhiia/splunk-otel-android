/*
 * Copyright 2024 Splunk Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cisco.mrum.common.otel.api

import android.app.Application
import com.cisco.mrum.common.otel.api.internal.Resources
import com.cisco.mrum.common.otel.api.logRecord.AndroidLogRecordExporter
import com.cisco.mrum.common.otel.api.span.AndroidSpanExporter
import com.cisco.mrum.common.otel.internal.storage.OtelStorage
import com.smartlook.sdk.common.job.JobIdStorage
import com.smartlook.sdk.common.job.JobManager
import com.smartlook.sdk.common.storage.Storage
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import java.util.UUID

class OpenTelemetryInitializer(application: Application) {

    private var resource: Resource

    private val spanProcessors: MutableList<SpanProcessor> = mutableListOf()
    private val logRecordProcessors: MutableList<LogRecordProcessor> = mutableListOf()

    init {
        val storage = Storage.attach(application)
        val jobManager = JobManager.attach(application)
        val jobIdStorage = JobIdStorage.init(storage)
        val otelStorage = OtelStorage.obtainInstance(storage.preferences)

        val deviceId = getDeviceId(otelStorage)

        resource = Resources.createDefault(deviceId)

        spanProcessors += BatchSpanProcessor.builder(
            AndroidSpanExporter(
                storage = storage,
                jobManager = jobManager,
                jobIdStorage = jobIdStorage
            )
        ).build()

        logRecordProcessors += BatchLogRecordProcessor.builder(
            AndroidLogRecordExporter(
                storage = storage,
                jobManager = jobManager,
                jobIdStorage = jobIdStorage
            )
        ).build()
    }

    fun build(global: Boolean = false): OpenTelemetrySdk {
        val instance = OpenTelemetrySdk.builder()
            .setTracerProvider(createTracerProvider())
            .setLoggerProvider(createLoggerProvider())
            .setPropagators(createPropagators())

        val sdk = if (global) instance.buildAndRegisterGlobal() else instance.build()

        OpenTelemetry.instance = sdk

        return sdk
    }

    fun addSpanProcessor(spanProcessor: SpanProcessor): OpenTelemetryInitializer {
        spanProcessors += spanProcessor
        return this
    }

    fun addLogRecordProcessor(logRecordProcessor: LogRecordProcessor): OpenTelemetryInitializer {
        logRecordProcessors += logRecordProcessor
        return this
    }

    fun joinResources(resource: Resource): OpenTelemetryInitializer {
        this.resource = this.resource.merge(resource)
        return this
    }

    private fun createTracerProvider(): SdkTracerProvider {
        val builder = SdkTracerProvider.builder()
            .setResource(resource)

        spanProcessors.forEach { builder.addSpanProcessor(it) }

        return builder.build()
    }

    private fun createLoggerProvider(): SdkLoggerProvider {
        val builder = SdkLoggerProvider.builder()
            .setResource(resource)

        logRecordProcessors.forEach { builder.addLogRecordProcessor(it) }

        return builder.build()
    }

    private fun createPropagators(): ContextPropagators {
        val propagator = TextMapPropagator.composite(
            W3CTraceContextPropagator.getInstance(),
            W3CBaggagePropagator.getInstance()
        )
        return ContextPropagators.create(propagator)
    }

    private fun getDeviceId(otelStorage: OtelStorage): String {
        return otelStorage.readDeviceId() ?: run {
            val deviceId = UUID.randomUUID().toString()
            otelStorage.writeDeviceId(deviceId)
            deviceId
        }
    }
}
