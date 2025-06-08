package com.cplier.service.impl

import com.cplier.CI_TEST_RESOURCES_MOCO_LOG
import com.cplier.mock.MockHttpServer
import com.cplier.mock.http.MockDefaultHttpServerConfig
import com.cplier.mock.http.MockHttpRequest
import com.cplier.mock.http.MockHttpResponse
import com.cplier.mock.http.MockHttpServerConfig
import com.cplier.service.MockAbstractHttpService
import com.cplier.util.DreamheadUtil.asHttpRequest
import com.github.dreamhead.moco.*
import com.github.dreamhead.moco.internal.SessionContext
import com.github.dreamhead.moco.model.MessageContent
import com.github.dreamhead.moco.server.ServerRunner
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.BDDAssertions
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedTransferQueue
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Component
public open class MockHttpServiceImpl : MockAbstractHttpService() {
  override fun cleanup() {
    cleanHttpClients()
  }

  private fun cleanHttpClients() {
    httpClientRequests.clear()
    httpClientUrls.clear()
    httpClientResponses.clear()
    sessionIds.clear()
    requestIds.clear()
  }

  private fun clearExpireMocoConfigData() {
    cleanHttpClients()
    MockHttpServer.clearMockHttpServerConfigData()
    mockHttpServerConfigs.clear()
  }

  override fun mockHttpServerStart(
    endpoint: String,
    port: Int,
  ) {
    clearExpireMocoConfigData()
    mockHttpServerPorts[endpoint] = port
    log.debug("{}: HTTP SERVER start", endpoint)
    if (mockHttpServers.containsKey(endpoint)) {
      log.debug("{}: HTTP SERVER already started on port: {}", endpoint, port)
      return
    }
    val configList = arrayListOf<MockHttpServerConfig>()
    val defaultConfig =
      object : MockHttpServerConfig {
        override fun configRequest(httpServer: HttpServer?): HttpResponseSetting? =
          httpServer?.post(Moco.and(RequestMatcher.ANY_REQUEST_MATCHER))

        override fun configResponse(responseSetting: HttpResponseSetting?) {
          responseSetting?.response(
            object : ResponseHandler {
              override fun writeToResponse(context: SessionContext?) {
                val httpRequest = context?.request as HttpRequest?
                val configChecked = arrayListOf<MockDefaultHttpServerConfig>()
                var retryNumber = 0
                while (true) {
                  val config = mockHttpServerConfigs[endpoint]
                  if (retryNumber >= 20) {
                    log.warn(
                      "No mockserver config for {}'s http request, Please set the mockServer config first",
                      endpoint,
                    )
                    val httpResponse = context?.response as MutableHttpResponse?
                    httpResponse?.status = 500
                    httpResponse?.addHeader("Content-Type", "application/json")
                    httpResponse?.content =
                      MessageContent.content(
                        """
                        {
                        "detail": "No mockserver config for http request, Please set the mockServer config first"
                        }
                        }
                        """.trimIndent(),
                      )
                    break
                  }
                  if (config == null) {
                    try {
                      TimeUnit.MILLISECONDS.sleep(800)
                      log.debug("Mock server {} don't have any mock setting try again after 0.8 second later")
                      retryNumber++
                    } catch (_: InterruptedException) {
                      log.warn("Mock server {} is interrupted", endpoint)
                      Thread.currentThread().interrupt()
                    }
                    continue
                  }
                  try {
                    val mockHttpServerConfig = config.poll(800, TimeUnit.MILLISECONDS)
                    if (mockHttpServerConfig == null) {
                      log.debug(
                        "Doesn't receive http response setting for request {} from {}, try again after 0.8 seconds",
                      )
                      retryNumber++
                      continue
                    }
                    if (configChecked.contains(mockHttpServerConfig)) {
                      log.debug("This mock config have already check, ignore it: {}", mockHttpServerConfig)
                      retryNumber++
                      config.add(mockHttpServerConfig)
                      continue
                    }
                    if (mockHttpServerConfig.isUnmatchedDirectSend) {
                      convertToMutableHttpResponse(context?.response, mockHttpServerConfig.mockHttpResponse)
                      break
                    }
                    val httpSetting = mockHttpServerConfig.httpSetting
                    if (mockHttpServerConfig.httpSetting!!.match(httpRequest)) {
                      httpSetting?.writeToResponse(context)
                      break
                    } else {
                      retryNumber++
                      configChecked.add(mockHttpServerConfig)
                      config.add(mockHttpServerConfig)
                    }
                  } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                  }
                }
              }

              override fun apply(config: MocoConfig<*>?): ResponseHandler? = this
            },
          )
        }
      }
    configList.add(defaultConfig)
    mockHttpServers[endpoint] =
      MockHttpServer.startMocoServer(port, CI_TEST_RESOURCES_MOCO_LOG, *configList.toTypedArray())
  }

  private fun convertToMutableHttpResponse(
    response: Response?,
    mockHttpResponse: MockHttpResponse,
  ): MutableHttpResponse? {
    val httpResponse = response as MutableHttpResponse?
    val mockHeaders = mockHttpResponse.headers
    mockHeaders?.forEach { (key, value) -> httpResponse?.addHeader(key, value) }
    httpResponse?.status = mockHttpResponse.statusCode
    mockHttpResponse.body?.apply { httpResponse?.content = MessageContent.content(this) }
    return httpResponse
  }

  override fun stop(endpoint: String) {
    val mockServer = mockHttpServers[endpoint]
    if (mockServer != null) {
      MockHttpServer.stopServer(mockServer.runner)
    }
  }

  override fun sendTheHttpRequest(endpoint: String) {
    val requestBuilder = newRequestBuilder(endpoint)
    val httpUrlBuilder = newHttpUrlBuilder(endpoint)
    val httpRequest = requestBuilder.url(httpUrlBuilder.build()).build()
    try {
      val response = httpClient.newCall(httpRequest).execute()
      httpClientResponses[endpoint] = response
    } catch (e: IOException) {
      log.error("Failed to send the http request", e)
    }
  }

  override fun prepareClientRequestScheme(
    endpoint: String,
    scheme: String,
  ) {
    newHttpUrlBuilder(endpoint).scheme(scheme)
  }

  override fun prepareClientRequestMethodAndBody(
    endpoint: String,
    method: String,
    body: String?,
  ) {
    var requestBody: RequestBody? = null
    if (body != null) {
      requestBody = body.toRequestBody(contentType = "application/json; charset=utf-8".toMediaTypeOrNull())
    }
    newRequestBuilder(endpoint).method(method, requestBody)
  }

  override fun prepareClientRequestHost(
    endpoint: String,
    host: String,
  ) {
    newHttpUrlBuilder(endpoint).host(host)
  }

  override fun prepareClientRequestPort(
    endpoint: String,
    port: Int,
  ) {
    newHttpUrlBuilder(endpoint).port(port)
  }

  override fun prepareClientRequestMethodAndUri(
    endpoint: String,
    method: String,
    uri: String,
    body: String?,
  ) {
    val requestBuilder = newRequestBuilder(endpoint)
    val httpUrlBuilder = newHttpUrlBuilder(endpoint)
    requestBuilder
      .method(method, if (body.isNullOrBlank()) null else body.toRequestBody("application/json".toMediaTypeOrNull()))
      .url(httpUrlBuilder.encodedPath(uri).build())
  }

  override fun receivedClientResponse(
    endpoint: String,
    expectedCode: Int,
    expectedContentType: String,
    body: String?,
  ) {
    val response = getResponse(endpoint)
    if (response == null) {
      BDDAssertions.fail<Any>("Timeout for $endpoint request")
    } else {
      val code = response.code
      val contentType = response.headers["Content-Type"]
      val responseBody = response.body
      assertEquals(expectedCode, code)
      assertEquals(expectedContentType, contentType)
      if (!body.isNullOrBlank() && responseBody != null) {
        try {
          val updatedBody = updateCorrelationId(endpoint, body)
          JSONAssert.assertEquals(updatedBody, responseBody.string(), true)
        } catch (e: Exception) {
          throw AssertionError(e)
        }
        responseBody.close()
      }
      response.close()
    }
  }

  override fun receivedClientResponseCode(
    endpoint: String,
    code: Int,
  ) {
    // noop
  }

  override fun prepareClientRequestHeaders(
    endpoint: String,
    headers: MutableMap<String, String?>?,
  ) {
    val requestBuilder = newRequestBuilder(endpoint)
    updateCorrelationId(endpoint, originalHeaders = headers)
    addHeaders(requestBuilder, headers = headers)
  }

  override fun mockHttpServerResponse(
    endpoint: String,
    statusCode: Int,
    contentType: String,
    body: String?,
  ) {
    val headerMap: MutableMap<String, String>? = mutableMapOf()
    headerMap?.put("Content-Type", contentType)
    val mockHttpResponse =
      object : MockHttpResponse(statusCode, headerMap) {
        override var body: String? = null
          get() = body
      }
    val serverPort = mockHttpServerPorts[endpoint]
    assertNotNull(serverPort) {
      "Can't find mock server by endpoint: $endpoint, please make sure server $endpoint is configured"
    }
    val mockHttpServer = mockHttpServers[endpoint]
    mockHttpServerConfigs
      .computeIfAbsent(endpoint) {
        LinkedTransferQueue()
      }.add(
        MockDefaultHttpServerConfig(
          mockHttpResponse = mockHttpResponse,
          serverRunner = mockHttpServer?.runner as ServerRunner,
          isUnmatchedDirectSend = true,
        ).apply(),
      )
  }

  override fun mockHttpServerReceiveRequest(
    endpoint: String,
    expectedRequest: String?,
  ) {
    val httpRequest = expectedRequest.asHttpRequest()
    log.debug("check server receive: {} with detail: {}", endpoint, expectedRequest)
    val serverPort = mockHttpServerPorts[endpoint]
    assertNotNull(serverPort) {
      "Can't find mock server by endpoint: $endpoint, please make sure server $endpoint is configured"
    }
    var checkTimes = 3
    val requestChecked = arrayListOf<MockHttpRequest>()
    while (checkTimes-- > 0) {
      val httpRequestQueue = MockHttpServer.getServerReceivedRequests(serverPort)
      if (httpRequestQueue.isNullOrEmpty()) {
        try {
          TimeUnit.MILLISECONDS.sleep(500)
        } catch (_: InterruptedException) {
          Thread.currentThread().interrupt()
        }
        continue
      }
      try {
        val bufferRequest = httpRequestQueue.poll(500, TimeUnit.MILLISECONDS)
        updateCorrelationId(endpoint, bufferRequest, httpRequest)
        if (httpRequest == bufferRequest) {
          httpRequestQueue.addAll(requestChecked)
          return
        } else {
          if (bufferRequest != null) {
            log.debug("Receive http request: {} is not an expected request, so return it to queue", bufferRequest)
            requestChecked.add(bufferRequest)
          }
        }
      } catch (_: InterruptedException) {
        Thread.currentThread().interrupt()
      }
    }
    throw AssertionError("Do not receive request from $endpoint Expected request: $httpRequest")
  }

  override fun mockHttpServerNotReceiveRequest(
    endpoint: String,
    expectedRequest: String?,
  ) {
    val httpRequest = expectedRequest.asHttpRequest()
    log.debug("Check server not receive {} with detail: {}", endpoint, expectedRequest)
    var checkTImes = 3
    val serverPort = mockHttpServerPorts[endpoint]
    assertNotNull(serverPort) {
      "Can't find mock server by endpoint: $endpoint, please make sure server$endpoint is runing"
    }
    val requestChecked = arrayListOf<MockHttpRequest>()
    var httpRequestQueue: BlockingQueue<MockHttpRequest>? = null
    while (checkTImes-- > 0) {
      httpRequestQueue = MockHttpServer.getServerReceivedRequests(serverPort)
      if (httpRequestQueue == null) {
        try {
          TimeUnit.MILLISECONDS.sleep(500)
        } catch (_: InterruptedException) {
          Thread.currentThread().interrupt()
        }
        continue
      }
      try {
        val bufferRequest = httpRequestQueue.poll(500, TimeUnit.MILLISECONDS)
        if (httpRequest == bufferRequest) {
          throw AssertionError("Receive request from $endpoint not expected request: $expectedRequest")
        } else {
          if (bufferRequest != null) {
            log.debug("Receive http request: {} is a different request, so return it to queue", bufferRequest)
            requestChecked.add(bufferRequest)
          }
        }
      } catch (_: InterruptedException) {
        Thread.currentThread().interrupt()
      }
    }
    if (httpRequestQueue != null && httpRequestQueue.isNotEmpty()) {
      httpRequestQueue.addAll(httpRequestQueue)
    }
  }

  override fun prepareClientRequestUri(
    endpoint: String,
    uri: String,
  ) {
    newHttpUrlBuilder(endpoint).encodedPath(uri)
  }

  override fun prepareClientRequestUrl(
    endpoint: String,
    url: String,
  ) {
    val httpUrlBuilder = newHttpUrlBuilder(endpoint)
    val httpUrl = url.toHttpUrlOrNull()
    if (httpUrl != null) {
      httpUrlBuilder.scheme(httpUrl.scheme)
      httpUrlBuilder.host(httpUrl.host)
      httpUrlBuilder.port(httpUrl.port)
      httpUrlBuilder.encodedPath(httpUrl.encodedPath)
    } else {
      log.error("Invalid URL: $url")
    }
  }

  override fun prepareClientRequestBody(
    endpoint: String,
    body: String?,
  ) {
    val requestBuilder = newRequestBuilder(endpoint)
    val httpUrlBuilder = newHttpUrlBuilder(endpoint)
    val request = requestBuilder.url(httpUrlBuilder.build()).build()
    val method = request.method
    val contentType = request.header("Content-Type")
    requestBuilder.method(method, body?.toRequestBody(contentType?.toMediaTypeOrNull()))
  }
}
