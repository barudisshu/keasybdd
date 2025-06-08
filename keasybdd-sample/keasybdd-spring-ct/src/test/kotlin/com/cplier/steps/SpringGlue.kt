package com.cplier.steps

import com.cplier.Application
import com.cplier.boot.MockContextLoader
import io.cucumber.java.Scenario
import io.cucumber.spring.CucumberContextConfiguration
import org.junit.jupiter.api.BeforeEach
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = [Application::class], loader = MockContextLoader::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@CucumberContextConfiguration
public class SpringGlue {
  internal companion object {
    private val log = LoggerFactory.getLogger(SpringGlue::class.java)
  }

  @LocalServerPort
  private var randomServerPort: Int? = null

  @BeforeEach
  public fun setup(scenario: Scenario) {
    log.info("Scenario \"{}\" started in port: {}", scenario.name, randomServerPort)
  }
}
