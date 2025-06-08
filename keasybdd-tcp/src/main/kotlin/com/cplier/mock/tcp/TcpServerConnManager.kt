package com.cplier.mock.tcp

import com.cplier.logger
import java.io.IOException
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

public class TcpServerConnManager {
  private companion object {
    private val log = logger()
  }

  private val connections = ConcurrentHashMap<String?, TcpServerConn?>()

  public fun create(): String? {
    val conn = TcpServerConn()
    return conn.cid?.apply {
      connections[this] = conn
    }
  }

  @Throws(IOException::class)
  public fun start(
    cid: String?,
    socket: Socket,
  ) {
    val tcpServerConn = connections[cid]
    if (tcpServerConn == null) {
      throw IOException("Unknown connection id: $cid")
    }
    tcpServerConn.onConnectionClosed = this::onConnectionClosed
    tcpServerConn.start(socket)
  }

  public fun onConnectionClosed(cid: String?) {
    connections.remove(cid)
  }

  public fun getConnection(cid: String?): TcpServerConn? = connections[cid]

  public fun idleConn(): TcpServerConn? =
    connections.values.reduce { a, b -> if ((a?.idle() ?: 0) > (b?.idle() ?: 0)) a else b }

  public fun clear() {
    connections.forEach { cid, conn ->
      if (conn != null && conn.isBound) {
        try {
          conn.stop()
        } catch (e: IOException) {
          log.trace("Failed to stop connection: {}", cid, e)
        }
      }
    }
    connections.clear()
  }
}
