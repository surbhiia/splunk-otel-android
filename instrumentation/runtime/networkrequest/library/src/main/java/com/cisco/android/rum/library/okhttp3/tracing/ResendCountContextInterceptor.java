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

package com.cisco.android.rum.library.okhttp3.tracing;

import java.io.IOException;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpClientRequestResendCount;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class ResendCountContextInterceptor implements Interceptor {

    private static final String TAG = "ResendCountInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (!OkHttpInterceptorUtils.isTracingEnabledAtInitiation(TAG, request)) {
            return chain.proceed(request);
        }

        // include the resend counter
        Context context = HttpClientRequestResendCount.initialize(Context.current());
        try (Scope ignored = context.makeCurrent()) {
            return chain.proceed(request);
        }
    }


}
