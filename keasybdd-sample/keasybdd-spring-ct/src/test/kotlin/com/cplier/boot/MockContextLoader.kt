package com.cplier.boot

import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootContextLoader
import java.util.*

public class MockContextLoader : SpringBootContextLoader {
  internal companion object {
    private val log = LoggerFactory.getLogger(MockContextLoader::class.java)
    val newEnvMap = mutableMapOf<String, String>()

    init {
      val properties = Properties()
      MockContextLoader::class.java.classLoader.getResourceAsStream("env.properties").use {
        properties.load(it)
      }
      properties.forEach { (key, value) -> newEnvMap[key.toString()] = value.toString() }
    }

    @Suppress("UNCHECK_CAST")
    fun setEnv(newenv: MutableMap<String, String>) {
      try {
        val processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment")
        val theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment")
        theEnvironmentField.isAccessible = true
        val env = theEnvironmentField.get(null) as MutableMap<String, String>
        env.putAll(newenv)
        val theCaseInsensitiveEnvironmentField =
          processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment")
        theCaseInsensitiveEnvironmentField.isAccessible = true
        val cienv = theCaseInsensitiveEnvironmentField.get(null) as MutableMap<String, String>
        cienv.putAll(newenv)
      } catch (e: NoSuchFieldException) {
        try {
          val classes = Collections::class.java.declaredClasses
          val env = System.getenv()
          for (clazz in classes) {
            if ("java.util.Collections\$UnmodifiableMap" == clazz.name) {
              val field = clazz.getDeclaredField("m")
              field.isAccessible = true
              val obj = field[env]
              val map = obj as MutableMap<String, String>
              map.clear()
              map.putAll(newenv)
            }
          }
        } catch (ee: Exception) {
          log.error(ee.message, ee)
        }
      } catch (ex: Exception) {
        log.error(ex.message, ex)
      }
    }
  }

  public constructor() {
    setEnv(newEnvMap)
  }
}
