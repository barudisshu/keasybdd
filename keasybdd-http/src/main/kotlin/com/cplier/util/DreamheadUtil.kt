package com.cplier.util

import com.cplier.mock.http.MockDefaultHttpRequest
import com.cplier.mock.http.MockHttpRequest
import com.cplier.mock.http.MockHttpResponse
import com.cplier.util.JsonUtil.asObject
import com.cplier.util.JsonUtil.asString
import com.github.dreamhead.moco.HttpMethod
import org.junit.jupiter.api.assertNotNull
import kotlin.test.assertFalse

@Suppress("kotlin:S6524", "Unchecked_Cast")
public object DreamheadUtil {
  public fun checkAndBuildHttpRequest(detailRequestStr: String?): MockHttpRequest {
    assertNotNull(detailRequestStr)
    assertFalse(detailRequestStr.isBlank(), "request data can't be blank")
    val detailRequest = detailRequestStr.asObject(Map::class.java) as MutableMap<String, Any?>
    val uri = detailRequest["uri"] as String?
    val method = detailRequest["method"] as String?
    val request = detailRequest["request"]
    val headers = detailRequest["headers"] as MutableMap<String, String>?
    if (uri == null || method == null || request == null) {
      throw AssertionError("Uri or method or request must be specified")
    }
    return MockDefaultHttpRequest(HttpMethod.valueOf(method), uri, headers, request.asString())
  }

  public fun String?.asHttpRequest(): MockHttpRequest = checkAndBuildHttpRequest(this)

  public fun checkAndBuildHttpResponse(detailResponseStr: String?): MockHttpResponse {
    assertNotNull(detailResponseStr, "response data can't be null")
    assertFalse(detailResponseStr.isBlank(), "response data can't be blank")
    val parameterMap = detailResponseStr.asObject(Map::class.java) as MutableMap<String, Any?>
    val response = parameterMap["response"]
    val statusCodeStr = parameterMap["statusCode"] as String?
    val headers = parameterMap["headers"] as MutableMap<String, String>?
    if (statusCodeStr.isNullOrEmpty()) {
      throw AssertionError("Response status code must be specified")
    }
    val statusCode = statusCodeStr.toInt()
    return object : MockHttpResponse(statusCode, headers) {
      override var body: String? = response?.asString()
    }
  }

  public fun String?.asHttpResponse() = checkAndBuildHttpResponse(this)
}
