package org.sultans.sultancyborg.utils;

import com.google.common.util.concurrent.RateLimiter;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RateLimitInterceptor implements Interceptor {

    private RateLimiter rateLimiter = RateLimiter.create(1);

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        rateLimiter.acquire();
        return chain.proceed(chain.request());
    }
}
