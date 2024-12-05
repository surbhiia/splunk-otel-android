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
import com.smartlook.sdk.common.logger.Logger
import com.smartlook.sdk.log.LogAspect
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.ReadWriteLogRecord

internal class SessionIdLogProcessor(private val sessionManager: SessionManager) : LogRecordProcessor {
    override fun onEmit(context: Context, logRecord: ReadWriteLogRecord) {
        val sessionId = sessionManager.sessionId
        Logger.privateD(LogAspect.EXPORT, "SessionIdLogProcessor", message = { "onEmit sessionId: $sessionId, ${logRecord.toLogRecordData().attributes}" })
        logRecord.setAttribute(SESSION_ID_KEY, sessionId)
    }
}
