/*
 * Copyright 2025 Splunk Inc.
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

package com.splunk.sdk.common.otel.internal

import io.opentelemetry.api.common.AttributeKey

object RumConstants {
    const val RUM_TRACER_NAME: String = "SplunkRum"
    const val COMPONENT_ERROR: String = "error"
    const val COMPONENT_CRASH: String = "crash"
    val WORKFLOW_NAME_KEY: AttributeKey<String> = AttributeKey.stringKey("workflow.name")
    val COMPONENT_KEY: AttributeKey<String> = AttributeKey.stringKey("component")

    val APPLICATION_ID_KEY: AttributeKey<String> = AttributeKey.stringKey("service.application_id")
    val APP_VERSION_CODE_KEY: AttributeKey<String> = AttributeKey.stringKey("service.version_code")
    val SPLUNK_OLLY_UUID_KEY: AttributeKey<String> = AttributeKey.stringKey("service.o11y.key")
}