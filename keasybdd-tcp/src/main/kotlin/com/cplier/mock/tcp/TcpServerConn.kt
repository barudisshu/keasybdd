package com.cplier.mock.tcp

import com.cplier.logger
import java.io.IOException
import java.net.Socket
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

public class TcpServerConn {
  private companion object {
    private val log = logger()
  }

  public var cid: String? = null
  public var onConnectionClosed: ((String?) -> Unit)? = null
  public var socket: TcpSocket? = null
  public var task: Runnable? = null
  public var thread: Thread? = null

  @Volatile
  private var lastRunningNanoTime: Long = 0L

  private val receivedMessage = ConcurrentLinkedQueue<ByteArray>()

  @Throws(IOException::class)
  public fun start(socket: Socket) {
    if (task != null) {
      throw IOException("Connection is already running")
    }
    this.cid = UUID.randomUUID().toString().replace("-", "")
    this.socket = TcpSocket(socket)
    this.task =
      Runnable {
        try {
          while (true) {
            val buf = this.socket!!.read()
            if (buf.isNotEmpty()) {
              handleDataReceived(buf)
            }
          }
        } catch (e: IOException) {
          handleException(e)
        }
      }
    this.thread = Thread.ofVirtual().name("TCP-CONN-", 0).start(this.task)
  }

  public fun idle(): Long = System.nanoTime() - lastRunningNanoTime

  public fun handleDataReceived(data: ByteArray) {
    receivedMessage.add(data)
    lastRunningNanoTime = System.nanoTime()
  }

  public fun handleException(e: Exception) {
    if (thread != null && !thread!!.isInterrupted) {
      thread!!.interrupt()
    }
    this.thread = null
    this.task = null
    this.socket = null
    if (onConnectionClosed != null) {
      onConnectionClosed!!.invoke(cid)
    }
    log.error(e.message, e)
  }

  public fun receive(): ByteArray? = receivedMessage.poll()

  @Throws(IOException::class)
  public fun send(data: ByteArray) {
    socket?.write(data)
    lastRunningNanoTime = System.nanoTime()
  }

  @Throws(IOException::class)
  public fun stop() {
    try {
      socket?.close()
      lastRunningNanoTime = System.nanoTime()
    } catch (e: IOException) {
      throw IOException("Failed to close socket", e)
    }
  }

  public val isBound: Boolean
    get() = socket?.isBound ?: false
}
