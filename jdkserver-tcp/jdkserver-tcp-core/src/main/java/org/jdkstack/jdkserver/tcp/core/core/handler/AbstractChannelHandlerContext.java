package org.jdkstack.jdkserver.tcp.core.core.handler;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelHandlerContext;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.Handler;

public abstract class AbstractChannelHandlerContext implements ChannelHandlerContext {
  // protected ChannelHandler channelHandler;
  protected SocketChannel socketChannel;
  protected Handler<Message> readHandler;
  protected Handler<ByteBuffer> writeHandler;

  protected AbstractChannelHandlerContext(SocketChannel socketChannel) {
    this.socketChannel = socketChannel;
    //  this.channelHandler = channelHandler;
  }

  @Override
  public void setReadHandler(Handler<Message> handler) {
    this.readHandler = handler;
  }

  @Override
  public void setWriteHandler(Handler<ByteBuffer> handler) {
    this.writeHandler = handler;
  }
}
