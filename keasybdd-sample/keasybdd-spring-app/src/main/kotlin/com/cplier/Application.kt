package com.cplier

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableScheduling

@EnableConfigurationProperties
@SpringBootApplication(
  exclude = [
    DataSourceAutoConfiguration::class,
    HibernateJpaAutoConfiguration::class,
    JpaRepositoriesAutoConfiguration::class,
    JmsAutoConfiguration::class,
    SecurityAutoConfiguration::class,
  ],
)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
public open class Application : CommandLineRunner {
  override fun run(vararg args: String?) {
    log.info("Spring boot application start.")
  }

  public companion object {
    private val log = LoggerFactory.getLogger(Application::class.java)

    @JvmStatic
    public fun main(vararg args: String) {
      SpringApplication.run(Application::class.java, *args)
    }
  }
}
