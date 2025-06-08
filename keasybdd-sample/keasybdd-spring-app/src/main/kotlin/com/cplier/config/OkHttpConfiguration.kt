package com.cplier.config

import com.cplier.config.property.OkHttpProperties
import io.micrometer.core.instrument.binder.okhttp3.OkHttpObservationInterceptor
import io.micrometer.observation.ObservationRegistry
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
public open class OkHttpConfiguration(
  private val okHttpProperties: OkHttpProperties,
  private val observationRegistry: ObservationRegistry,
) {
  internal companion object {
    private const val OKHTTP_CLIENT_REQUESTS_METRIC_NAME = "http.client.requests"
  }

  @Bean
  public open fun okHttpClient(): OkHttpClient {
    val observationInterceptor =
      OkHttpObservationInterceptor.builder(observationRegistry, OKHTTP_CLIENT_REQUESTS_METRIC_NAME).build()
    val loggingInterceptor = HttpLoggingInterceptor()
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
    return OkHttpClient
      .Builder()
      .connectionPool(
        ConnectionPool(
          okHttpProperties.maxIdleConnections,
          okHttpProperties.keepAliveDuration,
          TimeUnit.MILLISECONDS,
        ),
      ).connectTimeout(okHttpProperties.connectTimeoutMills, TimeUnit.MILLISECONDS)
      .readTimeout(okHttpProperties.readTimeoutMills, TimeUnit.MILLISECONDS)
      .addInterceptor(loggingInterceptor)
      .addInterceptor(observationInterceptor)
      .build()
  }
}
