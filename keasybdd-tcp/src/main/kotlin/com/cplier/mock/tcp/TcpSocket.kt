package com.cplier.mock.tcp

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.net.SocketAddress

public class TcpSocket(
  private val socket: Socket,
  private val `in`: DataInputStream = DataInputStream(socket.inputStream),
  private val `out`: DataOutputStream = DataOutputStream(socket.outputStream),
) {
  @Throws(IOException::class)
  public fun read(): ByteArray {
    val len = `in`.readUnsignedShort()
    val data = ByteArray(len)
    `in`.readFully(data)
    return data
  }

  @Throws(IOException::class)
  public fun write(data: ByteArray) {
    `out`.writeShort(data.size)
    `out`.write(data)
    `out`.flush()
  }

  @Throws(IOException::class)
  public fun close() {
    `in`.close()
    `out`.close()
    socket.close()
  }

  public val remoteSocketAddress: SocketAddress
    get() = socket.remoteSocketAddress

  public val localPort: Int
    get() = socket.localPort

  public val port: Int
    get() = socket.port

  public val isConnected: Boolean
    get() = socket.isConnected

  public val isClosed: Boolean
    get() = socket.isClosed

  public val isBound: Boolean
    get() = socket.isBound
}
