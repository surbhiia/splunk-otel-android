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

package com.cisco.android.rum.integration.agent.api.sessionId

import com.cisco.android.rum.integration.agent.api.attributes.AttributeConstants.SESSION_ID_KEY
import com.cisco.android.rum.integration.agent.internal.session.SessionManager
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.ReadWriteSpan
import io.opentelemetry.sdk.trace.ReadableSpan
import io.opentelemetry.sdk.trace.SpanProcessor

internal class SessionIdSpanProcessor(private val sessionManager: SessionManager) : SpanProcessor {
    override fun onStart(parentContext: Context, span: ReadWriteSpan) {
        span.setAttribute(SESSION_ID_KEY, sessionManager.sessionId)
    }

    override fun isStartRequired(): Boolean = true

    override fun onEnd(span: ReadableSpan) = Unit

    override fun isEndRequired(): Boolean = false
}
