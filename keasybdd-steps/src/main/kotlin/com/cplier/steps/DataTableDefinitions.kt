package com.cplier.steps

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.cucumber.java.DefaultDataTableCellTransformer
import io.cucumber.java.DefaultDataTableEntryTransformer
import io.cucumber.java.DefaultParameterTransformer
import io.cucumber.java.DocStringType
import jakarta.annotation.Resource
import java.lang.reflect.Type

public class DataTableDefinitions {
  @Resource
  private lateinit var objectMapper: ObjectMapper

  @DefaultParameterTransformer
  @DefaultDataTableCellTransformer
  @DefaultDataTableEntryTransformer
  public fun defaultTransformer(
    fromValue: Any?,
    toValueType: Type,
  ): Any? {
    val javaType = objectMapper.typeFactory.constructType(toValueType)
    return objectMapper.convertValue(fromValue, javaType)
  }

  // register DataTable
  @DocStringType(contentType = "json")
  @Throws(JsonProcessingException::class)
  public fun json(json: String): JsonNode = objectMapper.readTree(json)
}
