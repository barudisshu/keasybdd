package com.cplier.util

import com.cplier.util.JsonUtil.asJsonNode
import com.cplier.util.JsonUtil.asObject
import com.cplier.util.JsonUtil.asString
import com.fasterxml.jackson.annotation.JsonCreator
import kotlin.test.*

public class JsonUtilTest {
  @Test
  public fun `JsonUtil SHALL Be able to serialization pojo`() {
    val person = Person("cplier", 18)
    val json = person.asString()
    assertNotNull(json)
    assertEquals(person, json.asObject(Person::class.java))
    val jsonNode = json.asJsonNode()
    assertTrue(jsonNode.has("name"))
    assertNull("{}".asObject(Person::class.java))
  }

  internal data class Person
    @JsonCreator
    constructor(
      var name: String,
      var age: Int,
    )
}
