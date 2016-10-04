package com.mszone.core.io

import java.net.{InetAddress, InetSocketAddress}
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel, SocketChannel}
import java.nio.channels.spi.SelectorProvider

import com.mszone.core.log.Log

class NioClient (hostAddress:InetAddress, port:Int) extends Thread with Log {
  val nioTransporter: NioTransporter = new NioTransporter()
  var active = false

  override def run(): Unit = {
    val channel = ServerSocketChannel.open().asInstanceOf[SocketChannel]
    val selector = SelectorProvider.provider().openSelector()
    channel.configureBlocking(false)
    channel.connect(new InetSocketAddress(this.hostAddress, this.port))
    channel.register(selector, SelectionKey.OP_CONNECT)
    active = true
    do {
      try {
        selector.select()
        val iterator = selector.selectedKeys().iterator()
        while (iterator.hasNext) {
          val key = iterator.next()
          iterator.remove()
          if (key.isConnectable) {
            connect(key, selector)
          }
          if (key.isReadable) {
            read(key, selector)
          }
          if (key.isWritable) {
            write(key, selector)
          }
        }
      } catch {
        case e: Exception => println("exception caught: " + e)
      }
    } while (active)
    selector.close()
    channel.close()
  }

  def shutdown(): Unit = {
    active = false
  }

  private[this] def connect(key: SelectionKey, selector: Selector) : Unit = {
    try {
      key.channel().asInstanceOf[SocketChannel].finishConnect()
      key.interestOps(key.channel().validOps())
    } catch {
      case e: Exception =>
        println("exception caught: " + e)
        key.cancel()
    }
  }

  private[this] def write(key: SelectionKey, selector: Selector) : Unit = {
    nioTransporter.send(key.channel().asInstanceOf[SocketChannel])
  }

  private[this] def read(key: SelectionKey, selector: Selector) : Unit = {
    nioTransporter.receive(key.channel().asInstanceOf[SocketChannel])
  }
}