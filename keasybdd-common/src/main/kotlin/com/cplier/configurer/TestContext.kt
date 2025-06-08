package com.cplier.configurer

public object TestContext {
  public const val SESSION_CORRELATION_ID: String = "X-CPL-Session-Correlation-Id"
  public const val REQUEST_CORRELATION_ID: String = "X-CPL-Request-Correlation-Id"
  public const val SESSION_CORRELATION_ID_FLAG: String = "@Session_Correlation_Id"
  public const val REQUEST_CORRELATION_ID_FLAG: String = "@Request_Correlation_Id"
}
