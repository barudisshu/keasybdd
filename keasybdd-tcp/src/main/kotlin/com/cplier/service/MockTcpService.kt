package com.cplier.service

import java.io.IOException

public interface MockTcpService {
  public fun cleanup()

  @Throws(IOException::class)
  public fun establishServerSocket(
    endpoint: String,
    port: Int,
  )

  @Throws(IOException::class)
  public fun establishClientSocket(
    endpoint: String,
    port: Int,
  )

  @Throws(IOException::class)
  public fun clientSendMessage(
    endpoint: String,
    message: ByteArray?,
  )

  @Throws(IOException::class)
  public fun clientReceiveMessage(
    endpoint: String,
    message: ByteArray?,
  )
}
