package com.cplier.util

import org.junit.jupiter.api.Test

public class HighlightTest {
  @Test
  public fun `ansi simple test`() {
    Highlight.entries.forEach {
      println("${it.code} I'm Okay!! ${Highlight.RESET}")
    }
  }
}
