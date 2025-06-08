package com.cplier.service

public interface MockHttpService {
  public fun cleanup()

  public fun mockHttpServerStart(
    endpoint: String,
    port: Int,
  )

  public fun stop(endpoint: String)

  public fun sendTheHttpRequest(endpoint: String)

  public fun prepareClientRequestScheme(
    endpoint: String,
    scheme: String,
  )

  public fun prepareClientRequestMethodAndBody(
    endpoint: String,
    method: String,
    body: String?,
  )

  public fun prepareClientRequestHost(
    endpoint: String,
    host: String,
  )

  public fun prepareClientRequestPort(
    endpoint: String,
    port: Int,
  )

  public fun prepareClientRequestMethodAndUri(
    endpoint: String,
    method: String,
    uri: String,
    body: String?,
  )

  public fun receivedClientResponse(
    endpoint: String,
    expectedCode: Int,
    expectedContentType: String,
    body: String?,
  )

  public fun receivedClientResponseCode(
    endpoint: String,
    code: Int,
  )

  public fun prepareClientRequestHeaders(
    endpoint: String,
    headers: MutableMap<String, String?>?,
  )

  public fun mockHttpServerResponse(
    endpoint: String,
    statusCode: Int,
    contentType: String,
    body: String?,
  )

  public fun mockHttpServerReceiveRequest(
    endpoint: String,
    expectedRequest: String?,
  )

  public fun mockHttpServerNotReceiveRequest(
    endpoint: String,
    expectedRequest: String?,
  )

  public fun prepareClientRequestUri(
    endpoint: String,
    uri: String,
  )

  public fun prepareClientRequestUrl(
    endpoint: String,
    url: String,
  )

  public fun prepareClientRequestBody(
    endpoint: String,
    body: String?,
  )
}
