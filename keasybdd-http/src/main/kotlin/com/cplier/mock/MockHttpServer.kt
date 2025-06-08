package com.cplier.mock

import com.cplier.logger
import com.cplier.mock.http.MockDefaultHttpRequest
import com.cplier.mock.http.MockHttpRequest
import com.cplier.mock.http.MockHttpServerConfig
import com.github.dreamhead.moco.*
import com.github.dreamhead.moco.monitor.AbstractMonitor
import com.google.common.collect.ImmutableMap
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.system.exitProcess

public object MockHttpServer {
  private val log = logger()
  private val LOCK = ReentrantLock()

  public val RECEIVED_HEADER_MESSAGES: MutableMap<String, MutableList<MutableMap<String, Array<String>>>> =
    mutableMapOf()
  public val RECEIVED_REQUESTS: MutableList<String> = mutableListOf()
  public val RECEIVED_HEADERS: MutableList<MutableMap<String, String>> = mutableListOf()
  public val SERVER_RECEIVED_REQUESTS: MutableMap<Int, BlockingQueue<MockHttpRequest>> = mutableMapOf()
  public val RECEIVED_MESSAGES: MutableMap<String, MutableList<String>> = mutableMapOf()

  @JvmStatic
  public fun clearMockHttpServerConfigData() {
    RECEIVED_REQUESTS.clear()
    RECEIVED_HEADERS.clear()
    SERVER_RECEIVED_REQUESTS.clear()
    RECEIVED_MESSAGES.clear()
  }

  @JvmStatic
  public fun getServerReceivedRequests(serverPort: Int): BlockingQueue<MockHttpRequest>? =
    SERVER_RECEIVED_REQUESTS.also {
      if (!it.containsKey(serverPort)) {
        SERVER_RECEIVED_REQUESTS[serverPort] = LinkedBlockingQueue()
      }
    }[serverPort]

  private fun runHttpServer(httpServer: HttpServer): Runner? {
    val runner = Runner.runner(httpServer)
    var exceptionFlag = true
    var count = 0
    while (count++ < 10 && exceptionFlag) {
      exceptionFlag = startServerThrowException(runner)
      try {
        TimeUnit.MILLISECONDS.sleep(600)
      } catch (_: InterruptedException) {
        Thread.currentThread().interrupt()
      }
    }
    return if (exceptionFlag) null else runner
  }

  private fun startServerThrowException(runner: Runner): Boolean {
    var exceptionFlag = false
    try {
      runner.start()
    } catch (e: Exception) {
      log.debug("{}, wait more time.................", e.message)
      exceptionFlag = true
    }
    return exceptionFlag
  }

  @JvmStatic
  public fun startMocoServer(
    port: Int,
    logFilePath: String,
    vararg configs: MockHttpServerConfig,
  ): MockServer {
    val receiver = Receiver(port)
    val server = Moco.httpServer(port, receiver, Moco.log(logFilePath))
    // add default serverConfig to each mock server
    configs.forEach {
      val httpResponseSetting = it.configRequest(server)
      it.configResponse(httpResponseSetting)
    }
    val runner = runHttpServer(server)
    if (runner != null) {
      log.debug("==== Moco HttpServer start on port: {} ====", port)
    } else {
      log.error("==== Moco HttpServer failed to start on port: {} ====", port)
      exitProcess(-1)
    }
    return MockServer(runner, port)
  }

  @JvmStatic
  public fun stopServer(runner: Runner): Unit = runner.stop()

  internal class Receiver(
    private val serverPort: Int,
  ) : AbstractMonitor() {
    override fun isQuiet(): Boolean = false

    override fun onMessageArrived(request: Request?) {
      if (request !is HttpRequest) return
      super.onMessageArrived(request)
      val queueName = "%s-%s".format(request.method, request.uri)
      if (!request.content.hasContent()) return
      val body = request.content.content.toString(Charsets.UTF_8)
      val headers = request.headers
      val destMap = mutableMapOf<String, String>()
      headers.forEach { (key, value) -> destMap[key] = value[0] }
      val mockDefaultHttpRequest =
        MockDefaultHttpRequest(HttpMethod.valueOf(request.method.name), request.uri, destMap, body)
      SERVER_RECEIVED_REQUESTS.computeIfAbsent(serverPort) { LinkedBlockingQueue() }.add(mockDefaultHttpRequest)
      storageHeaders(destMap, queueName, headers)
      storageBody(queueName, body)
    }

    private fun storageHeaders(
      destMap: MutableMap<String, String>,
      queueName: String,
      headers: ImmutableMap<String, Array<String>>,
    ) {
      try {
        LOCK.lock()
        RECEIVED_HEADERS.add(destMap)
        if (RECEIVED_HEADER_MESSAGES[queueName] == null) {
          val temp = mutableListOf<MutableMap<String, Array<String>>>()
          temp.add(headers)
          RECEIVED_HEADER_MESSAGES[queueName] = temp
        } else {
          RECEIVED_HEADER_MESSAGES[queueName]?.add(headers)
        }
      } finally {
        LOCK.unlock()
      }
    }

    private fun storageBody(
      queueName: String,
      body: String,
    ) {
      try {
        LOCK.lock()
        RECEIVED_REQUESTS.add(queueName)
        log.debug("receive http request message, body: {}", body)
        if (RECEIVED_MESSAGES[queueName] == null) {
          val temp = mutableListOf<String>()
          temp.add(body)
          RECEIVED_MESSAGES[queueName] = temp
        } else {
          RECEIVED_MESSAGES[queueName]?.add(body)
        }
      } finally {
        LOCK.unlock()
      }
    }
  }

  public data class MockServer(
    val runner: Runner,
    val port: Int,
  )
}
