package com.cplier.mock.http

public abstract class MockHttpResponse
  @JvmOverloads
  constructor(
    public val statusCode: Int,
    public val headers: MutableMap<String, String>? = null,
  ) : MockHttpModel
