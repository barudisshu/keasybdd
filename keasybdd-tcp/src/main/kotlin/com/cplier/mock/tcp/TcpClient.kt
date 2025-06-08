package com.cplier.mock.tcp

import com.cplier.logger
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue

public class TcpClient(
  public val cid: String,
  public val host: String = "0.0.0.0",
  public val port: Int,
) {
  private companion object {
    private val log = logger()
  }

  private var socket: TcpSocket? = null
  private var task: Runnable? = null
  private var thread: Thread? = null

  private val receivedMessage = ConcurrentLinkedQueue<ByteArray>()

  @Throws(IOException::class)
  public fun start() {
    val serverAddress = InetAddress.getByName(host)
    val rawSocket = Socket(serverAddress, port)
    this.socket = TcpSocket(rawSocket)
    this.task =
      Runnable {
        try {
          while (socket?.isBound ?: false) {
            val buf = this.socket?.read()
            if (buf != null && buf.isNotEmpty()) {
              handleDataReceived(buf)
            }
          }
        } catch (e: IOException) {
          handleException(e)
        }
      }
    this.thread = Thread.ofVirtual().name("TCP-CLIENT-", 0).unstarted(this.task)
    this.thread!!.start()
  }

  public fun handleDataReceived(data: ByteArray): Boolean = receivedMessage.add(data)

  public fun handleException(e: Exception) {
    if (thread != null && !thread!!.isInterrupted) {
      thread!!.interrupt()
    }
    thread = null
    task = null
    socket = null
    log.error(e.message, e)
  }

  public fun receive(): ByteArray? = receivedMessage.poll()

  @Throws(IOException::class)
  public fun send(data: ByteArray): Unit? = socket?.write(data)

  @Throws(IOException::class)
  public fun close() {
    if (socket != null) {
      try {
        socket!!.close()
      } catch (e: IOException) {
        log.error(e.message, e)
      }
    }
  }
}
