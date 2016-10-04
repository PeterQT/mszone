package com.mszone.core.io

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent._

import com.mszone.core.log.Log

class NioTransporter extends Log {
  private val readQueue = new ConcurrentLinkedQueue[String]()
  private val writeQueue = new ConcurrentLinkedQueue[String]()
  private val buffer = ByteBuffer.allocateDirect(50*1024)
  private val sizeBuffer = ByteBuffer.allocateDirect(4)
  private val maxMessageSize = 500*1024

  private var size: Int = 0
  private var readSize: Int = 0
  private var messageBuffer: Array[Byte] = null

  def receive(socketChannel: SocketChannel): Unit = {
    buffer.clear()
    socketChannel.read(buffer)
    buffer.flip()
    while (buffer.remaining() > 0) {
      if (size == 0) {
        size = buffer.getInt()
        readSize = 0
      }
      if (buffer.remaining() > 0 && size > 0 && size <= maxMessageSize) {
        if (messageBuffer == null) {
          messageBuffer = new Array[Byte](size)
        }
        val next = Math.min(size - readSize, buffer.remaining())
        buffer.get(messageBuffer, readSize, next)
        readSize += next
        if (readSize == size) {
          pack()
        }
      }
    }
  }

  def send(socketChannel: SocketChannel): Unit = {
    var message = writeQueue.poll()
    while (message != null) {
      sizeBuffer.clear()
      sizeBuffer.putInt(message.length())
      sizeBuffer.flip()
      socketChannel.write(sizeBuffer)
      socketChannel.write(ByteBuffer.wrap(message.getBytes("utf-8")))
      message = writeQueue.poll()
    }
  }

  def send(message: String): Unit = {
    writeQueue.add(message)
  }

  def receive() : String = {
    readQueue.poll()
  }

  private [this] def pack() : Unit = {
    size = 0
    readSize = 0
    readQueue.add(new String(messageBuffer, "utf-8"))
    messageBuffer = null
  }

}
