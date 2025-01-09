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

package com.cisco.android.rum.integration.startup

import android.content.Context
import com.cisco.android.common.logger.Logger
import com.cisco.android.rum.integration.agent.internal.AgentIntegration
import com.cisco.android.rum.integration.agent.internal.config.ModuleConfigurationManager
import com.cisco.android.rum.integration.agent.internal.config.RemoteModuleConfiguration
import com.cisco.android.rum.integration.agent.internal.extension.find
import com.cisco.android.rum.startup.ApplicationStartupTimekeeper
import com.smartlook.sdk.common.utils.extensions.optBooleanNull

internal object StartupConfigurator {

    private const val TAG = "StartupConfigurator"
    private const val MODULE_NAME = "startup"

    init {
        AgentIntegration.registerModule(MODULE_NAME)
    }

    fun attach(context: Context) {
        AgentIntegration.obtainInstance(context).listeners += installationListener

        ApplicationStartupTimekeeper.listeners += applicationStartupTimekeeperListener
    }

    private val applicationStartupTimekeeperListener = object : ApplicationStartupTimekeeper.Listener {
        override fun onColdStarted(duration: Long) {
            Logger.d(TAG, "onColdStarted(duration: $duration ms)")
            // TODO send data
        }

        override fun onWarmStarted(duration: Long) {
            Logger.d(TAG, "onWarmStarted(duration: $duration ms)")
            // TODO send data
        }

        override fun onHotStarted(duration: Long) {
            Logger.d(TAG, "onHotStarted(duration: $duration ms)")
            // TODO send data
        }
    }

    private val configManagerListener = object : ModuleConfigurationManager.Listener {
        override fun onRemoteModuleConfigurationsChanged(manager: ModuleConfigurationManager, remoteConfigurations: List<RemoteModuleConfiguration>) {
            Logger.d(TAG, "onRemoteModuleConfigurationsChanged(remoteConfigurations: $remoteConfigurations)")
            setModuleConfiguration(remoteConfigurations)
        }
    }

    private fun setModuleConfiguration(remoteConfigurations: List<RemoteModuleConfiguration>) {
        Logger.d(TAG, "setModuleConfiguration(remoteConfigurations: $remoteConfigurations)")

        val remoteConfig = remoteConfigurations.find(MODULE_NAME)?.config

        ApplicationStartupTimekeeper.isEnabled = remoteConfig?.optBooleanNull("enabled") ?: true
    }

    private val installationListener = object : AgentIntegration.Listener {
        override fun onInstall(context: Context) {
            Logger.d(TAG, "onInstall()")

            val integration = AgentIntegration.obtainInstance(context)
            integration.moduleConfigurationManager.listeners += configManagerListener

            setModuleConfiguration(integration.moduleConfigurationManager.remoteConfigurations)
        }
    }
}
