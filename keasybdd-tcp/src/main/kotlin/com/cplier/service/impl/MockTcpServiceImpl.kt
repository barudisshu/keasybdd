package com.cplier.service.impl

import com.cplier.logger
import com.cplier.mock.tcp.TcpClient
import com.cplier.mock.tcp.TcpServer
import com.cplier.service.MockTcpService
import com.cplier.util.Highlight
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

@Component
public open class MockTcpServiceImpl : MockTcpService {
  private companion object {
    private val log = logger()
    private val tcpServerRef = AtomicReference<TcpServer>()
    private val clientConnections = ConcurrentHashMap<String, TcpClient>()
  }

  @Throws(IOException::class)
  private fun getOrCreateTcpClient(
    endpoint: String,
    localServerPort: Int?,
  ): TcpClient {
    if (clientConnections.containsKey(endpoint)) {
      return clientConnections[endpoint]!!
    } else {
      if (localServerPort != null) {
        val tcpClient = TcpClient(endpoint, "0.0.0.0", localServerPort)
        tcpClient.start()
        clientConnections[endpoint] = tcpClient
        log.debug(
          "{}New TCP CLIENT {}ESTABLISHED{}: {}{}{}",
          Highlight.GREEN,
          Highlight.GREEN_BOLD,
          Highlight.RESET,
          Highlight.YELLOW_BOLD,
          endpoint,
          Highlight.RESET,
        )
        return tcpClient
      } else {
        throw AssertionError("TcpClient not initialized on local server port: $localServerPort")
      }
    }
  }

  override fun cleanup() {
    clientConnections.values.forEach { it.close() }
    clientConnections.clear()
    log.debug("Destroy TCP Client connections: {}", clientConnections.values.joinToString())
  }

  override fun establishServerSocket(
    endpoint: String,
    port: Int,
  ) {
    if (tcpServerRef.get() == null) {
      tcpServerRef.set(TcpServer(endpoint, port))
      tcpServerRef.get().start()
    }
  }

  override fun establishClientSocket(
    endpoint: String,
    port: Int,
  ) {
    getOrCreateTcpClient(endpoint, port)
  }

  override fun clientSendMessage(
    endpoint: String,
    message: ByteArray?,
  ) {
    if (message != null && message.isNotEmpty()) {
      getOrCreateTcpClient(endpoint, null).send(message)
    }
  }

  override fun clientReceiveMessage(
    endpoint: String,
    message: ByteArray?,
  ) {
    if (message != null && message.isNotEmpty()) {
      val receive = getOrCreateTcpClient(endpoint, null).receive()
      assertArrayEquals(message, receive) { "TCP Client received mismatch byte array message." }
    }
  }
}
