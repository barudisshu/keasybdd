package com.cplier.util

import com.cplier.logger
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

public object JsonUtil {
  private val MAPPER = ObjectMapper()
  private val log = logger()

  init {
    MAPPER.findAndRegisterModules()
  }

  public fun toString(obj: Any?): String = MAPPER.writeValueAsString(obj)

  public fun Any?.asString(): String = toString(this)

  public fun <T> fromString(
    json: String,
    clazz: Class<T>,
  ): T? {
    try {
      return MAPPER.readValue(json, clazz)
    } catch (e: Exception) {
      log.error("Failed to parse json: $json", e)
      return null
    }
  }

  public fun <T> String.asObject(clazz: Class<T>): T? = fromString(this, clazz)

  public fun readTree(json: String): JsonNode = MAPPER.readTree(json)

  public fun String.asJsonNode(): JsonNode = readTree(this)
}
