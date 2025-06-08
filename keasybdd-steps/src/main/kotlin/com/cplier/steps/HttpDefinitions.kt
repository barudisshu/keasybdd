package com.cplier.steps

import com.cplier.service.MockHttpService
import io.cucumber.java.After
import io.cucumber.java.Scenario
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import jakarta.annotation.Resource

public class HttpDefinitions {
  @Resource
  private lateinit var mockHttpService: MockHttpService

  // hook for each scenario
  @After
  public fun tearDown(scenario: Scenario) {
    mockHttpService.cleanup()
  }

  @Given("HTTP_SERVER {string} is started in port: {int}")
  public fun startMockHttpServer(
    endpoint: String,
    mockPort: Int,
  ) {
    mockHttpService.mockHttpServerStart(endpoint, mockPort)
  }

  @Given("HTTP_CLIENT {string} set the http method: {string}")
  public fun prepareRequestMethod(
    endpoint: String,
    method: String,
    body: String?,
  ): Unit = mockHttpService.prepareClientRequestMethodAndBody(endpoint, method, body)

  @Given("HTTP_CLIENT {string} set the http scheme: {string}")
  public fun prepareRequestScheme(
    endpoint: String,
    scheme: String,
  ): Unit = mockHttpService.prepareClientRequestScheme(endpoint, scheme)

  @Given("HTTP_CLIENT {string} set the http host: {string}")
  public fun prepareRequestHost(
    endpoint: String,
    host: String,
  ): Unit = mockHttpService.prepareClientRequestHost(endpoint, host)

  @Given("HTTP_CLIENT {string} set the http port: {int}")
  public fun prepareRequestPort(
    endpoint: String,
    port: Int,
  ): Unit = mockHttpService.prepareClientRequestPort(endpoint, port)

  @Given("HTTP_CLIENT {string} set the http uri: {string}")
  public fun prepareRequestUri(
    endpoint: String,
    uri: String,
  ): Unit = mockHttpService.prepareClientRequestUri(endpoint, uri)

  @Given("HTTP_CLIENT {string} set the http url: {string}")
  public fun prepareRequestUrl(
    endpoint: String,
    url: String,
  ): Unit = mockHttpService.prepareClientRequestUrl(endpoint, url)

  @Given("HTTP_CLIENT {string} set the http body")
  public fun prepareRequestBody(
    endpoint: String,
    body: String?,
  ): Unit = mockHttpService.prepareClientRequestBody(endpoint, body)

  @Given("HTTP_CLIENT {string} set the http method: {string}, uri: {string}")
  public fun prepareRequestMethodAndBody(
    endpoint: String,
    method: String,
    uri: String,
  ) {
    mockHttpService.prepareClientRequestMethodAndUri(endpoint, method, uri, null)
  }

  @Given("HTTP_CLIENT {string} set the http method: {string}, uri: {string}, body")
  public fun prepareRequestWithinBody(
    endpoint: String,
    method: String,
    uri: String,
    body: String,
  ) {
    mockHttpService.prepareClientRequestMethodAndUri(endpoint, method, uri, body)
  }

  @Given("HTTP_CLIENT {string} set the http headers")
  public fun prepareRequestHeaders(
    endpoint: String,
    headers: Map<String, String?>?,
  ) {
    mockHttpService.prepareClientRequestHeaders(endpoint, headers?.toMutableMap())
  }

  @Given("HTTP_SERVER {string} response with code: {int}, Content-Type: {string}")
  public fun prepareMockResponse(
    endpoint: String,
    code: Int,
    contentType: String,
    body: String?,
  ) {
    mockHttpService.mockHttpServerResponse(endpoint, code, contentType, body)
  }

  @Given("HTTP_SERVER {string} receive request")
  public fun prepareMockRequest(
    endpoint: String,
    body: String?,
  ) {
    mockHttpService.mockHttpServerReceiveRequest(endpoint, body)
  }

  @When("HTTP_CLIENT {string} send the http request")
  public fun sendRequest(endpoint: String) {
    mockHttpService.sendTheHttpRequest(endpoint)
  }

  @Then("HTTP_CLIENT {string} receive response with code: {int}, Content-Type: {string}")
  public fun receivedResponseWithCode(
    endpoint: String,
    code: Int,
    contentType: String,
  ) {
    mockHttpService.receivedClientResponse(endpoint, code, contentType, null)
  }

  @Then("HTTP_CLIENT {string} receive response with code: {int}")
  public fun receivedResponseWithCode(
    endpoint: String,
    code: Int,
  ) {
    mockHttpService.receivedClientResponseCode(endpoint, code)
  }

  @Then("HTTP_CLIENT {string} receive response with code: {int}, Content-Type: {string}, body")
  public fun receivedResponseWithCode(
    endpoint: String,
    code: Int,
    contentType: String,
    body: String?,
  ) {
    mockHttpService.receivedClientResponse(endpoint, code, contentType, body)
  }
}
