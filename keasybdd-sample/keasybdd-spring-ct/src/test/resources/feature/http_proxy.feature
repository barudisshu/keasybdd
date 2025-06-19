Feature: 01 - HTTP Proxy

  Background:
    Given HTTP_SERVER "service-proxy" is started in port: 8081

  Scenario: 01 - Service will proxy request
    Given HTTP_SERVER "service-proxy" response with code: 200, Content-Type: "application/json"
    """
    Dam
    """
    Given HTTP_CLIENT "service-client" set the http method: "POST", uri: "/v1/api/exchange", body
    """json
    {
    "secret": "I'm handsome"
    }
    """
    Given HTTP_CLIENT "service-client" send the http request
    Then HTTP_SERVER "service-proxy" receive request
    """json
    {
      "uri": "/v1/api/exchange",
      "method": "POST",
      "request": {
        "secret": "I'm handsome"
      }
    }
    """
