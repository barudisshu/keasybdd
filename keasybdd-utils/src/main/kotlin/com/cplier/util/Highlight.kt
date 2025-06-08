package com.cplier.util

public enum class Highlight(
  internal val code: String,
) {
  // color end
  RESET("\u001B[0m"),

  // regular
  BLACK("\u001B[0;30m"),
  RED("\u001B[0;31m"),
  GREEN("\u001B[0;32m"),
  YELLOW("\u001B[0;33m"),
  BLUE("\u001B[0;34m"),
  PURPLE("\u001B[0;35m"),
  CYAN("\u001B[0;36m"),
  WHITE("\u001B[0;37m"),

  // bold
  BLACK_BOLD("\u001B[1;30m"),
  RED_BOLD("\u001B[1;31m"),
  GREEN_BOLD("\u001B[1;32m"),
  YELLOW_BOLD("\u001B[1;33m"),
  BLUE_BOLD("\u001B[1;34m"),
  PURPLE_BOLD("\u001B[1;35m"),
  CYAN_BOLD("\u001B[1;36m"),
  WHITE_BOLD("\u001B[1;37m"),

  // underline
  BLACK_UNDERLINE("\u001B[4;30m"),
  RED_UNDERLINE("\u001B[4;31m"),
  GREEN_UNDERLINE("\u001B[4;32m"),
  YELLOW_UNDERLINE("\u001B[4;33m"),
  BLUE_UNDERLINE("\u001B[4;34m"),
  PURPLE_UNDERLINE("\u001B[4;35m"),
  CYAN_UNDERLINE("\u001B[4;36m"),
  WHITE_UNDERLINE("\u001B[4;37m"),

  // strike through
  BLACK_STRIKETHROUGH("\u001B[9;30m"),
  RED_STRIKETHROUGH("\u001B[9;31m"),
  GREEN_STRIKETHROUGH("\u001B[9;32m"),
  YELLOW_STRIKETHROUGH("\u001B[9;33m"),
  BLUE_STRIKETHROUGH("\u001B[9;34m"),
  PURPLE_STRIKETHROUGH("\u001B[9;35m"),
  CYAN_STRIKETHROUGH("\u001B[9;36m"),
  WHITE_STRIKETHROUGH("\u001B[9;37m"),

  // background
  BLACK_BACKGROUND("\u001B[40m"),
  RED_BACKGROUND("\u001B[41m"),
  GREEN_BACKGROUND("\u001B[42m"),
  YELLOW_BACKGROUND("\u001B[43m"),
  BLUE_BACKGROUND("\u001B[44m"),
  PURPLE_BACKGROUND("\u001B[45m"),
  CYAN_BACKGROUND("\u001B[46m"),
  WHITE_BACKGROUND("\u001B[47m"),

  // high intensity
  BLACK_BRIGHT("\u001B[0;90m"),
  RED_BRIGHT("\u001B[0;91m"),
  GREEN_BRIGHT("\u001B[0;92m"),
  YELLOW_BRIGHT("\u001B[0;93m"),
  BLUE_BRIGHT("\u001B[0;94m"),
  PURPLE_BRIGHT("\u001B[0;95m"),
  CYAN_BRIGHT("\u001B[0;96m"),
  WHITE_BRIGHT("\u001B[0;97m"),

  // bold  high intensity
  BLACK_BOLD_BRIGHT("\u001B[1;90m"),
  RED_BOLD_BRIGHT("\u001B[1;91m"),
  GREEN_BOLD_BRIGHT("\u001B[1;92m"),
  YELLOW_BOLD_BRIGHT("\u001B[1;93m"),
  BLUE_BOLD_BRIGHT("\u001B[1;94m"),
  PURPLE_BOLD_BRIGHT("\u001B[1;95m"),
  CYAN_BOLD_BRIGHT("\u001B[1;96m"),
  WHITE_BOLD_BRIGHT("\u001B[1;97m"),

  // high  intensity backgrounds
  BLACK_BACKGROUND_BRIGHT("\u001B[0;100m"),
  RED_BACKGROUND_BRIGHT("\u001B[0;101m"),
  GREEN_BACKGROUND_BRIGHT("\u001B[0;102m"),
  YELLOW_BACKGROUND_BRIGHT("\u001B[0;103m"),
  BLUE_BACKGROUND_BRIGHT("\u001B[0;104m"),
  PURPLE_BACKGROUND_BRIGHT("\u001B[0;105m"),
  CYAN_BACKGROUND_BRIGHT("\u001B[0;106m"),
  WHITE_BACKGROUND_BRIGHT("\u001B[0;107m"),
  ;

  override fun toString(): String = code

  public companion object {
    @JvmStatic
    @get:JvmName("isAnsiSupport")
    public val ansiSupport: Boolean
      get() = System.console() != null && System.getenv()["TERM"] != null
  }
}
