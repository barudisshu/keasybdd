package com.cplier.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "spring.okhttp")
public open class OkHttpProperties(
  public var connectTimeoutMills: Long = 300_000L,
  public var readTimeoutMills: Long = 30_000L,
  public var writeTimeoutMills: Long = 15_000L,
  public var maxIdleConnections: Int = Runtime.getRuntime().availableProcessors() * 2 + 1,
  public var keepAliveDuration: Long = 60_000L,
)
