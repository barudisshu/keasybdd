package com.cplier.mock.http

import com.github.dreamhead.moco.HttpResponseSetting
import com.github.dreamhead.moco.HttpServer

public interface MockHttpServerConfig {
  public fun configRequest(httpServer: HttpServer?): HttpResponseSetting?

  public fun configResponse(responseSetting: HttpResponseSetting?)
}
