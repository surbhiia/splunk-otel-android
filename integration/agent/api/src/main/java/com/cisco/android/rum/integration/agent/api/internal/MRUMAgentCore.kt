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

package com.cisco.android.rum.integration.agent.api.internal

import android.app.Application
import com.cisco.mrum.common.otel.api.OpenTelemetryInitializer
import com.cisco.mrum.common.otel.internal.storage.OtelStorage
import com.cisco.android.rum.integration.agent.api.AgentConfiguration
import com.cisco.android.rum.integration.agent.api.attributes.GenericAttributesLogProcessor
import com.cisco.android.rum.integration.agent.api.configuration.ConfigurationManager
import com.cisco.android.rum.integration.agent.api.extension.toResource
import com.cisco.android.rum.integration.agent.api.sessionId.SessionIdLogProcessor
import com.cisco.android.rum.integration.agent.api.sessionId.SessionIdSpanProcessor
import com.cisco.android.rum.integration.agent.api.sessionId.SessionStartEventManager
import com.cisco.android.rum.integration.agent.api.sessionPulse.SessionPulseEventManager
import com.cisco.android.rum.integration.agent.api.state.StateLogRecordProcessor
import com.cisco.android.rum.integration.agent.internal.AgentIntegration
import com.cisco.android.rum.integration.agent.internal.BuildConfig
import com.cisco.android.rum.integration.agent.internal.state.StateManager
import com.cisco.android.rum.integration.agent.module.ModuleConfiguration
import com.smartlook.sdk.common.logger.Logger
import com.smartlook.sdk.common.storage.Storage
import com.smartlook.sdk.common.utils.HashCalculationUtils
import com.smartlook.sdk.log.LogAspect
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.resources.ResourceBuilder
import java.io.File

internal object MRUMAgentCore {

    private const val TAG = "MRUMAgentCore"
    private const val SERVICE_HASH_RESOURCE_KEY = "service.hash"

    fun install(application: Application, agentConfiguration: AgentConfiguration, moduleConfigurations: List<ModuleConfiguration>) {
        Logger.privateD(LogAspect.SDK_METHODS, TAG, { "install(agentConfiguration: $agentConfiguration, moduleConfigurations: $moduleConfigurations)" })

        val storage = Storage.attach(application)
        val otelStorage = OtelStorage.obtainInstance(storage.preferences)

        val finalConfiguration = ConfigurationManager
            .obtainInstance(otelStorage)
            .preProcessConfiguration(application, agentConfiguration)

        val agentIntegration = AgentIntegration
            .obtainInstance(application)
            .setup(
                appName = requireNotNull(finalConfiguration.appName),
                agentVersion = requireNotNull(BuildConfig.VERSION_NAME),
                moduleConfigurations = moduleConfigurations
            )

        val stateManager = StateManager.obtainInstance(application)
        SessionStartEventManager.obtainInstance(agentIntegration.sessionManager)
        SessionPulseEventManager.obtainInstance(agentIntegration.sessionManager)

        val openTelemetryInitializer = OpenTelemetryInitializer(application)
        openTelemetryInitializer
            .joinResources(finalConfiguration.toResource())
            .addSpanProcessor(SessionIdSpanProcessor(agentIntegration.sessionManager))
            .addLogRecordProcessor(GenericAttributesLogProcessor())
            .addLogRecordProcessor(StateLogRecordProcessor(stateManager))
            .addLogRecordProcessor(SessionIdLogProcessor(agentIntegration.sessionManager))

        val hash = obtainServiceHashResource(application)
        if (hash != null) {
            openTelemetryInitializer.joinResources(hash)
        }

        openTelemetryInitializer.build()

        agentIntegration.install(application)
    }

    fun obtainServiceHashResource(application: Application): Resource? {
        val sourceDir = application.applicationInfo.sourceDir
        if (sourceDir == null) {
            Logger.privateD(LogAspect.SDK_METHODS, TAG, { "Unable to calculate service hash, application source directory null" })
            return null
        }

        return ResourceBuilder().put(AttributeKey.stringKey(SERVICE_HASH_RESOURCE_KEY),
            HashCalculationUtils.calculateSha256(File(sourceDir)))
            .build()
    }
}
