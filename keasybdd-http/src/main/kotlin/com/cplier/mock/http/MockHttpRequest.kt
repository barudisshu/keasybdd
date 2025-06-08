package com.cplier.mock.http

import com.github.dreamhead.moco.HttpMethod
import com.google.common.base.MoreObjects
import com.google.common.base.Objects

public abstract class MockHttpRequest(
  public val method: HttpMethod,
  public val uri: String,
  heoders: MutableMap<String, String>? = null,
) : MockHttpModel {
  public val headers: MutableMap<String, String> = heoders ?: mutableMapOf()

  init {
    if (heoders != null) {
      this.headers.putAll(heoders)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (other === this) return true
    if (other !is MockHttpRequest) return false
    return Objects.equal(method, other.method) &&
      Objects.equal(uri, other.uri) &&
      other.headers.keys == headers.keys &&
      compareHeader(other.headers)
  }

  private fun compareHeader(headers: Map<String, String>): Boolean {
    headers.forEach {
      if (it.key == it.value) {
        return false
      }
    }
    return true
  }

  override fun hashCode(): Int = Objects.hashCode(method, uri)

  override fun toString(): String =
    MoreObjects
      .toStringHelper(this)
      .add("method", method)
      .add("uri", uri)
      .add("headers", headers)
      .toString()
}
