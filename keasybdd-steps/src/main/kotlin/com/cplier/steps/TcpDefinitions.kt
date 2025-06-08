package com.cplier.steps

import com.cplier.service.MockTcpService
import io.cucumber.java.After
import io.cucumber.java.Scenario
import io.cucumber.java.en.Given
import jakarta.annotation.Resource
import java.io.IOException

public class TcpDefinitions {
  @Resource
  private lateinit var mockTcpService: MockTcpService

  @After
  public fun tearDown(scenario: Scenario) {
    mockTcpService.cleanup()
  }

  // a stoppable mock client.
  @Given("TCP_SERVER {string} bound in port: {int}")
  @Throws(IOException::class)
  public fun startedMockTcpServer(
    endpoint: String,
    port: Int,
  ) {
    mockTcpService.establishServerSocket(endpoint, port)
  }

  // multi clients
  @Given("TCP_CLIENT {string} connected in port: {int}")
  @Throws(IOException::class)
  public fun startedMockTcpClient(
    endpoint: String,
    port: Int,
  ) {
    mockTcpService.establishClientSocket(endpoint, port)
  }

  @Given("TCP_CLIENT {string} send binary message")
  @Throws(IOException::class)
  public fun clientSendBinFormat(
    endpoint: String,
    hex: String?,
  ) {
    mockTcpService.clientSendMessage(endpoint, hex?.toByteArray() ?: ByteArray(0))
  }

  @Given("TCP_CLIENT {string} receive binary message")
  @Throws(IOException::class)
  public fun clientReceiveBinFormat(
    endpoint: String,
    hex: String?,
  ) {
    mockTcpService.clientReceiveMessage(endpoint, hex?.toByteArray() ?: ByteArray(0))
  }

  @Given("TCP_SERVER {string} shutdown")
  public fun stoppedMockTcpServer(endpoint: String?) {
    // noop
  }

  @Given("TCP_SERVER {string} disconnected")
  public fun stoppedMockTcpClient(endpoint: String?) {
    // noop
  }
}
