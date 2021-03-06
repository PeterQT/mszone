package com.mszone.core.io

import java.net.{InetAddress, InetSocketAddress}
import java.nio.channels.spi.SelectorProvider
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel, SocketChannel}

import com.mszone.core.log.Log

class NioServer(hostAddress:InetAddress, port:Int) extends Thread with Log {
  var active = false
  override def run(): Unit = {
    val channel = ServerSocketChannel.open()
    val selector = SelectorProvider.provider().openSelector()
    channel.configureBlocking(false)
    channel.socket().bind(new InetSocketAddress(this.hostAddress, this.port))
    channel.register(selector, SelectionKey.OP_ACCEPT)
    active = true
    do {
      try {
        selector.select()
        val iterator = selector.selectedKeys().iterator()
        while (iterator.hasNext) {
          val key = iterator.next()
          iterator.remove()
          if (key.isAcceptable) {
            accept(key, selector)
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

  private[this] def accept(key: SelectionKey, selector: Selector) : Unit = {
    val serverSocketChannel = key.channel().asInstanceOf[ServerSocketChannel]
    val socketChannel = serverSocketChannel.accept()
    socketChannel.configureBlocking(false)
    socketChannel.register(selector, socketChannel.validOps(), new NioTransporter())
  }

  private[this] def write(key: SelectionKey, selector: Selector) : Unit = {
    if (key.attachment() != null) {
      key.attachment().asInstanceOf[NioTransporter].send(key.channel().asInstanceOf[SocketChannel])
    }
  }

  private[this] def read(key: SelectionKey, selector: Selector) : Unit = {
    if (key.attachment() != null) {
      key.attachment().asInstanceOf[NioTransporter].receive(key.channel().asInstanceOf[SocketChannel])
    }
  }
}