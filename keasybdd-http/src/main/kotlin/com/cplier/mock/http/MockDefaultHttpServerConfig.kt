package com.cplier.mock.http

import com.github.dreamhead.moco.*
import com.github.dreamhead.moco.Moco.*
import com.github.dreamhead.moco.handler.HttpHeaderResponseHandler
import com.github.dreamhead.moco.internal.ActualHttpServer
import com.github.dreamhead.moco.internal.MocoHttpServer
import com.github.dreamhead.moco.server.ServerRunner
import com.github.dreamhead.moco.setting.HttpSetting

public class MockDefaultHttpServerConfig
  @JvmOverloads
  constructor(
    public var mockHttpRequest: MockHttpRequest? = null,
    public var mockHttpResponse: MockHttpResponse,
    public var serverRunner: ServerRunner,
    public var httpSetting: HttpSetting? = null,
    /**
     * Directly send response if config not matches.
     */
    public var isUnmatchedDirectSend: Boolean = false,
  ) : MockHttpServerConfig {
    override fun configRequest(httpServer: HttpServer?): HttpResponseSetting? {
      requireNotNull(httpServer) { "httpServer is null" }
      requireNotNull(mockHttpRequest) { "mockHttpRequest is null" }
      val matcherList = arrayListOf<RequestMatcher>()
      val httpMethod = mockHttpRequest!!.method
      val uri = mockHttpRequest!!.uri
      matcherList.add(by(mockHttpRequest!!.body))
      mockHttpRequest?.headers?.forEach { (key, value) ->
        matcherList.add(contain(header(key), value))
      }
      return when (httpMethod) {
        HttpMethod.GET ->
          httpServer[
            and(
              by(uri(uri)),
              *matcherList.toTypedArray(),
            ),
          ]
        HttpMethod.POST ->
          httpServer.post(
            and(by(uri(uri)), *matcherList.toTypedArray()),
          )
        HttpMethod.PUT ->
          httpServer.put(
            and(by(uri(uri)), *matcherList.toTypedArray()),
          )
        HttpMethod.DELETE ->
          httpServer.delete(
            and(by(uri(uri)), *matcherList.toTypedArray()),
          )
        else -> throw IllegalArgumentException(
          "Unsupported http method: $httpMethod",
        )
      }
    }

    override fun configResponse(responseSetting: HttpResponseSetting?) {
      responseSetting?.response(status(mockHttpResponse.statusCode))
      if (mockHttpResponse.body != null) {
        responseSetting?.response(mockHttpResponse.body)
      }
      mockHttpResponse.headers?.forEach { (key, value) ->
        responseSetting?.response(
          HttpHeaderResponseHandler(HttpHeader(key, text(value))),
        )
      }
    }

    /**
     * Apply this request / response.
     */
    public fun apply(): MockDefaultHttpServerConfig {
      if (isUnmatchedDirectSend) {
        return this
      }
      try {
        val field = serverRunner.javaClass.getDeclaredField("configuration")
        field.isAccessible = true
        val serverConfiguration = field[serverRunner] as MocoHttpServer
        val actualHttpServer = serverConfiguration.serverSetting() as ActualHttpServer
        val httpResponseSetting = configRequest(actualHttpServer)
        configResponse(httpResponseSetting)
      } catch (_: NoSuchMethodException) {
        // noop
      } catch (_: IllegalAccessException) {
        // noop
      }
      return this
    }
  }
