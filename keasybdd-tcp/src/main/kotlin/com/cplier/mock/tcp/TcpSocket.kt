package com.cplier.mock.tcp

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.EOFException
import java.io.IOException
import java.net.Socket
import java.net.SocketAddress

public class TcpSocket(
  private val socket: Socket,
  private val `in`: BufferedInputStream = BufferedInputStream(socket.inputStream),
  private val `out`: BufferedOutputStream = BufferedOutputStream(socket.outputStream),
) {
  @Throws(IOException::class)
  public fun read(): ByteArray {
    val header = ByteArray(Short.SIZE_BYTES)
    var read = `in`.read(header)
    return if (read == -1) {
      ByteArray(0)
    } else {
      var len = (header[0].toInt() and 0xFF) shl 8
      len = len or (header[1].toInt() and 0xFF)
      val data = ByteArray(len)
      read = `in`.read(data)
      if (read == -1) {
        throw EOFException("End of Stream on reading data len: ${data.size}")
      }
      data
    }
  }

  @Throws(IOException::class)
  public fun write(data: ByteArray) {
    writeHeader(data)
    writeData(data)
    `out`.flush()
  }

  @Throws(IOException::class)
  private fun writeHeader(data: ByteArray) {
    val header = ByteArray(Short.SIZE_BYTES)
    header[0] = ((data.size shr 8) and 0xFF).toByte()
    header[1] = (data.size and 0xFF).toByte()
    `out`.write(header)
  }

  @Throws(IOException::class)
  private fun writeData(data: ByteArray) {
    `out`.write(data)
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
