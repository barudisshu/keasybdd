package com.cplier

import org.slf4j.Logger
import org.slf4j.LoggerFactory

public inline fun <reified T> T.logger(): Logger {
  if (T::class.isCompanion) {
    return LoggerFactory.getLogger(T::class.java.enclosingClass)
  }
  return LoggerFactory.getLogger(T::class.java)
}

public const val CI_TEST_RESOURCES_MOCO_LOG: String = "target/moco.log"
public const val IGNORE_CHECK_FLAG: String = "\${json-unit.ignore}"
