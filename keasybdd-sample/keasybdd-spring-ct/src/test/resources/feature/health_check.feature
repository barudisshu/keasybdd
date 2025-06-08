Feature: A1 - Health Check

  Scenario: 01 - readiness indicator
    Given HTTP_CLIENT "K8S" set the http headers
      | Content-TYpe                 | application/json        |
      | X-CPL-Request-Correlation-Id | @Request_Correlation_Id |
    Given HTTP_CLIENT "K8S" set the http method: "GET", uri: "/v1/api/actuator/health/readiness"
    When HTTP_CLIENT "K8S" send the http request
    Then HTTP_CLIENT "K8S" receive response with code: 200, Content-Type: "application/vnd.spring-boot.actuator.v3+json"

  Scenario: 02 - liveness indicator
    Given HTTP_CLIENT "K8S" set the http headers
      | Content-TYpe                 | application/json        |
      | X-CPL-Request-Correlation-Id | @Request_Correlation_Id |
    Given HTTP_CLIENT "K8S" set the http method: "GET", uri: "/v1/api/actuator/health/liveness"
    When HTTP_CLIENT "K8S" send the http request
    Then HTTP_CLIENT "K8S" receive response with code: 200, Content-Type: "application/vnd.spring-boot.actuator.v3+json"
