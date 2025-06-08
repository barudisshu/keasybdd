package com.cplier.mock.tcp

import com.cplier.logger
import java.io.IOException
import java.net.ServerSocket

public class TcpServer(
  private val sid: String,
  private var port: Int,
) {
  private companion object {
    private val log = logger()
  }

  private val connManager: TcpServerConnManager = TcpServerConnManager()
  private var serverSocket: ServerSocket? = null
  private var task: Runnable? = null
  private var thread: Thread? = null

  @Throws(IOException::class)
  public fun start() {
    log.debug("Server: {} starting", sid)
    try {
      serverSocket = ServerSocket(port, 128)
      this.port = serverSocket!!.localPort
      this.task =
        Runnable {
          try {
            while (serverSocket != null) {
              val socket = serverSocket!!.accept() // blocking
              if (socket != null && socket.isConnected) {
                val cid = connManager.create()
                connManager.start(cid, socket)
              }
            }
          } catch (_: IOException) {
            if (this.thread != null && !thread!!.isInterrupted) {
              thread!!.interrupt()
            }
            this.thread = null
            this.task = null
            this.serverSocket = null
          }
        }
    } catch (e: IOException) {
      throw IOException("Port $port is already in use.", e)
    }
  }

  @Throws(IOException::class)
  public fun stop() {
    try {
      this.serverSocket?.close()
    } catch (e: IOException) {
      throw IOException("Failed to close server socket.", e)
    }
  }

  @Throws(IOException::class)
  public fun send(
    cid: String,
    message: ByteArray,
  ) {
    val conn = connManager.getConnection(cid)
    if (conn == null) {
      throw IOException("Unknown connection: $cid")
    }
    conn.send(message)
  }

  @Throws(IOException::class)
  public fun send(message: ByteArray) {
    val conn = connManager.idleConn()
    if (conn == null) {
      throw IOException("Unknown connection: <null>")
    }
    conn.send(message)
  }

  @Throws(IOException::class)
  public fun closeConn(cid: String) {
    val conn = connManager.getConnection(cid)
    if (conn == null) {
      throw IOException("Unknown connection: $cid")
    }
    conn.stop()
  }
}
