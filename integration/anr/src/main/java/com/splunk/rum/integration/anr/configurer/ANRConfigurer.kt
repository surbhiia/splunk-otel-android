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

package com.splunk.rum.integration.anr.configurer

import android.annotation.SuppressLint
import android.content.Context
import com.cisco.android.common.logger.Logger
import com.splunk.rum.anr.AnrReportingHandler
import com.splunk.rum.integration.agent.internal.AgentIntegration
import com.splunk.rum.integration.agent.internal.config.ModuleConfigurationManager
import com.splunk.rum.integration.agent.internal.config.RemoteModuleConfiguration
import com.splunk.rum.integration.agent.internal.extension.find
import com.splunk.sdk.common.utils.extensions.optBooleanNull
import com.splunk.sdk.common.utils.extensions.optLongNull

@SuppressLint("LongLogTag")
object ANRConfigurer {

    private const val TAG = "ANRReportingConfigurer"
    private const val MODULE_NAME = "anrReporting"
    private const val DEFAULT_IS_ENABLED = true
    private const val DEFAULT_TIMEOUT = 5L
    private lateinit var anrHandler: AnrReportingHandler

    @JvmField
    var isANRReportingEnabled: Boolean = DEFAULT_IS_ENABLED

    @JvmField
    var thresholdSeconds: Long = DEFAULT_TIMEOUT

    init {
        Logger.d(TAG, "init()")
        AgentIntegration.registerModule(MODULE_NAME)
    }

    fun attach(context: Context) {
        Logger.d(TAG, "attach()")
        AgentIntegration.obtainInstance(context).listeners += installationListener
    }

    private val configManagerListener = object : ModuleConfigurationManager.Listener {
        override fun onRemoteModuleConfigurationsChanged(manager: ModuleConfigurationManager, remoteConfigurations: List<RemoteModuleConfiguration>) {
            Logger.d(TAG, "onRemoteModuleConfigurationsChanged(remoteConfigurations: $remoteConfigurations)")
            setModuleConfiguration(remoteConfigurations)

            if (ANRConfigurer::anrHandler.isInitialized) {
                // We always unregister the ANR Handler. If ANR reporting is still enabled, we re-register
                // the ANR Handler so that it will have the most recently updated threshold value
                anrHandler.unregister()
                if (isANRReportingEnabled) {
                    anrHandler.register(thresholdSeconds)
                }
            }
        }
    }

    private fun setModuleConfiguration(remoteConfigurations: List<RemoteModuleConfiguration>) {
        Logger.d(TAG, "setModuleConfiguration(remoteConfigurations: $remoteConfigurations)")
        val remoteConfig = remoteConfigurations.find(MODULE_NAME)?.config
        isANRReportingEnabled = remoteConfig?.optBooleanNull("enabled") ?: DEFAULT_IS_ENABLED
        thresholdSeconds = remoteConfig?.optLongNull("thresholdAndroid") ?: DEFAULT_TIMEOUT
    }

    private val installationListener = object : AgentIntegration.Listener {
        override fun onInstall(context: Context) {
            Logger.d(TAG, "onInstall()")
            val integration = AgentIntegration.obtainInstance(context)
            integration.moduleConfigurationManager.listeners += configManagerListener

            setModuleConfiguration(integration.moduleConfigurationManager.remoteConfigurations)

            // Registers anr handler if anr reporting enabled
            if (!ANRConfigurer::anrHandler.isInitialized) {
                anrHandler = AnrReportingHandler(context)
            }

            if (isANRReportingEnabled) {
                anrHandler.register(thresholdSeconds)
            }
        }
    }
}