package com.cplier.service

import com.cplier.configurer.TestContext.REQUEST_CORRELATION_ID
import com.cplier.configurer.TestContext.REQUEST_CORRELATION_ID_FLAG
import com.cplier.configurer.TestContext.SESSION_CORRELATION_ID
import com.cplier.configurer.TestContext.SESSION_CORRELATION_ID_FLAG
import com.cplier.logger
import com.cplier.mock.MockHttpServer
import com.cplier.mock.http.MockDefaultHttpRequest
import com.cplier.mock.http.MockDefaultHttpServerConfig
import com.cplier.mock.http.MockHttpRequest
import com.cplier.util.Highlight
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

public abstract class MockAbstractHttpService : MockHttpService {
  internal companion object {
    val log = logger()

    fun String.isJson(): Boolean =
      try {
        JSONObject(this)
        true
      } catch (_: Exception) {
        try {
          JSONArray(this)
          true
        } catch (_: Exception) {
          false
        }
      }
  }

  @LocalServerPort
  private var randomServerPort: Int = 0

  protected val httpClient: OkHttpClient by lazy {
    val loggingInterceptor =
      HttpLoggingInterceptor { message ->
        if (message.isJson()) {
          log.debug("{}==>{}", Highlight.YELLOW, Highlight.RESET)
          log.debug("{}{}{}", Highlight.GREEN, Gson().toJson(JsonParser.parseString(message)), Highlight.RESET)
          log.debug("{}<=={}", Highlight.YELLOW, Highlight.RESET)
        }
      }
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
    OkHttpClient
      .Builder()
      .retryOnConnectionFailure(true)
      .followRedirects(false)
      .dispatcher(Dispatcher(Executors.newVirtualThreadPerTaskExecutor()))
      .connectionPool(ConnectionPool(5, 1L, TimeUnit.MINUTES))
      .addInterceptor(loggingInterceptor)
      .build()
  }

  protected val mockHttpServers: MutableMap<String, MockHttpServer.MockServer> = mutableMapOf()
  protected val mockHttpServerPorts: MutableMap<String, Int> = mutableMapOf()
  protected val mockHttpServerConfigs: MutableMap<String, BlockingQueue<MockDefaultHttpServerConfig>> = mutableMapOf()

  // Sender
  protected val httpClientRequests: ConcurrentHashMap<String, Request.Builder> = ConcurrentHashMap()
  protected val httpClientUrls: ConcurrentHashMap<String, HttpUrl.Builder> =
    ConcurrentHashMap<String, HttpUrl.Builder>()

  //
  protected val httpClientResponses: ConcurrentHashMap<String, Response?> = ConcurrentHashMap()

  protected val sessionIds: ConcurrentHashMap<String, String?> = ConcurrentHashMap<String, String?>()
  protected val requestIds: MutableMap<String, String?> = mutableMapOf()

  protected val nextId: String
    get() {
      return UUID.randomUUID().toString().replace("-", "")
    }

  protected fun updateCorrelationId(
    endpoint: String,
    originalHeaders: MutableMap<String, String?>?,
  ) {
    fun updateEndpointSessionId(endpoint: String) {
      originalHeaders?.forEach { entry ->
        val value = entry.value ?: return@forEach
        if (SESSION_CORRELATION_ID_FLAG == value) {
          originalHeaders[entry.key] = sessionIds[endpoint]
        }
      }
    }

    fun updateEndpointRequestId(endpoint: String) {
      originalHeaders?.forEach { entry ->
        val value = entry.value ?: return@forEach
        if (REQUEST_CORRELATION_ID_FLAG == value) {
          originalHeaders[entry.key] = requestIds[endpoint]
        }
      }
    }

    if (originalHeaders?.containsKey(SESSION_CORRELATION_ID) ?: false) {
      sessionIds.computeIfAbsent(endpoint) { nextId }
      updateEndpointSessionId(endpoint)
    } else {
      if (sessionIds.containsKey(endpoint)) {
        updateEndpointSessionId(endpoint)
      }
    }
    if (originalHeaders?.containsKey(REQUEST_CORRELATION_ID) ?: false) {
      requestIds.computeIfAbsent(endpoint) { nextId }
      updateEndpointRequestId(endpoint)
    } else {
      if (requestIds.containsKey(endpoint)) {
        updateEndpointRequestId(endpoint)
      }
    }
  }

  protected fun updateCorrelationId(
    endpoint: String,
    body: String,
  ): String {
    sessionIds.computeIfAbsent(endpoint) { nextId }
    requestIds.computeIfAbsent(endpoint) { nextId }
    return body
      .replace(SESSION_CORRELATION_ID_FLAG, sessionIds[endpoint]!!)
      .replace(REQUEST_CORRELATION_ID_FLAG, requestIds[endpoint]!!)
  }

  protected fun updateCorrelationId(
    endpoint: String,
    bufferRequest: MockHttpRequest?,
    httpRequest: MockHttpRequest?,
  ) {
    if (bufferRequest == null) return
    val headers = bufferRequest.headers
    val sessionId = headers[SESSION_CORRELATION_ID]
    val requestId = headers[REQUEST_CORRELATION_ID]
    sessionId?.also {
      sessionIds[endpoint] = sessionId
    }
    requestId?.also {
      requestIds[endpoint] = requestId
    }
    if (httpRequest is MockDefaultHttpRequest) {
      val replacedBody = httpRequest.body
      if (replacedBody != null) {
        httpRequest.body =
          replacedBody
            .replace(SESSION_CORRELATION_ID_FLAG, sessionIds[endpoint]!!)
            .replace(REQUEST_CORRELATION_ID_FLAG, requestIds[endpoint]!!)
      }
    }
    httpRequest?.headers?.set(SESSION_CORRELATION_ID, sessionIds[endpoint]!!)
    httpRequest?.headers?.set(REQUEST_CORRELATION_ID, requestIds[endpoint]!!)

    replaceLocalHostWithRealIp(httpRequest)
    replaceLocalHostWithRealIp(bufferRequest)
  }

  private fun replaceLocalHostWithRealIp(httpRequest: MockHttpRequest?) {
    var replacedBody = httpRequest?.body
    if (replacedBody != null) {
      replacedBody = replaceLocalHostWithRealIp(replacedBody)
      if (httpRequest is MockDefaultHttpRequest) {
        httpRequest.body = replacedBody
      }
    }
  }

  protected fun replaceLocalHostWithRealIp(requestBody: String): String {
    val realIp = localIpv4Addr()
    return requestBody
      .replace("localhost", realIp)
      .replace("127.0.0.1", realIp)
      .replace("::1", realIp)
      .replace("::0", realIp)
  }

  protected fun localIpv4Addr(): String {
    try {
      for (intf in NetworkInterface.getNetworkInterfaces()) {
        if (!intf.isUp ||
          intf.isLoopback ||
          intf.name.startsWith("p2p") ||
          intf.name.startsWith("dummy") ||
          intf.name.startsWith("rmnet") ||
          intf.name.startsWith("docker")
        ) {
          continue
        }
        for (inetAddress in intf.inetAddresses) {
          if (inetAddress is InetAddress) {
            return inetAddress.hostAddress
          }
        }
      }
    } catch (ex: SocketException) {
      log.error("Inet4Address Lookup Failed", ex)
    }
    return "localhost"
  }

  protected fun newRequestBuilder(endpoint: String): Request.Builder {
    if (httpClientRequests.containsKey(endpoint)) {
      return httpClientRequests[endpoint]!!
    } else {
      log.debug("Create a new request for endpoint: {}", endpoint)
      httpClientRequests[endpoint] = Request.Builder()
    }
    return httpClientRequests[endpoint]!!
  }

  protected fun addHeaders(
    requestBuilder: Request.Builder,
    headers: Map<String, String?>?,
  ) {
    headers?.forEach { (key, value) -> if (value != null) requestBuilder.addHeader(key, value) }
  }

  protected fun newHttpUrlBuilder(endpoint: String): HttpUrl.Builder {
    httpClientUrls.computeIfAbsent(endpoint) {
      HttpUrl
        .Builder()
        .scheme("http")
        .host("localhost")
        .port(randomServerPort)
    }
    return httpClientUrls[endpoint]!!
  }

  protected fun getResponse(endpoint: String): Response? = httpClientResponses[endpoint]!!
}
