package com.cplier.mock.http

import com.cplier.*
import com.cplier.util.JsonUtil.asJsonNode
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.dreamhead.moco.HttpMethod
import com.google.common.base.Objects

public class MockDefaultHttpRequest(
  method: HttpMethod,
  uri: String,
  headers: MutableMap<String, String>? = null,
  override var body: String? = null,
) : MockHttpRequest(method, uri, headers) {
  override fun equals(other: Any?): Boolean {
    if (other == null || other !is MockDefaultHttpRequest) return false
    return if (body != null && other.body != null) {
      val thisNode = body!!.asJsonNode()
      val otherNode = other.body!!.asJsonNode()
      removeIgnoreField(thisNode, otherNode)
      Objects.equal(thisNode, otherNode)
    } else {
      Objects.equal(this, body)
    }
  }

  override fun hashCode(): Int = Objects.hashCode(super.hashCode(), body)

  private fun removeIgnoreField(
    expected: JsonNode,
    real: JsonNode,
  ) {
    if (expected.isObject && real.isObject) {
      val fieldNames =
        (
          expected.fieldNames().asSequence() +
            real.fieldNames().asSequence()
        ).toSet()
      fieldNames.forEach { fieldName ->
        val expectedChild = expected[fieldName]
        val realChild = real[fieldName]
        if (expectedChild != null && realChild != null) {
          if (isIgnore(expectedChild) || isIgnore(realChild)) {
            (expected as ObjectNode).remove(fieldName)
            (real as ObjectNode).remove(fieldName)
          } else {
            removeIgnoreField(expectedChild, realChild)
          }
        } else {
          (expected as ObjectNode).remove(fieldName)
          (real as ObjectNode).remove(fieldName)
        }
      }
    }
  }

  private fun isIgnore(jsonNode: JsonNode): Boolean = jsonNode.isTextual && jsonNode.textValue() == IGNORE_CHECK_FLAG
}
