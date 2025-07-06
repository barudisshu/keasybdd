package com.cplier.mock.tcp

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.mockito.Mock
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import kotlin.test.Test

internal class TcpSocketTest {
  @Mock
  private lateinit var socket: Socket

  @Test
  fun readAndWrite() {
    val data = "Hello, World!".toByteArray()
    val `in` = ByteArrayInputStream(ByteArray(data.size))
    val `out` = ByteArrayOutputStream()
    val tcpSocket = TcpSocket(socket, DataInputStream(`in`), DataOutputStream(`out`))
    tcpSocket.write(data)
    val readData = tcpSocket.read()
    assertArrayEquals(data, readData)
  }
}
