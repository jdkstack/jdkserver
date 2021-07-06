package org.jdkstack.jdkserver.tcp.core.core.handler;

import java.nio.channels.SocketChannel;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelHandler;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelHandlerContext;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.Handler;

public abstract class AbstractChannelHandlerContext implements ChannelHandlerContext {
  protected ChannelHandler channelHandler;
  protected SocketChannel socketChannel;
  protected Handler<Message> readHandler;
  protected Handler<Message> writeHandler;

  protected AbstractChannelHandlerContext(
      SocketChannel socketChannel, ChannelHandler channelHandler) {
    this.socketChannel = socketChannel;
    this.channelHandler = channelHandler;
  }

  @Override
  public void setReadHandler(Handler<Message> handler) {
    this.readHandler = handler;
  }

  @Override
  public void setWriteHandler(Handler<Message> handler) {
    this.writeHandler = handler;
  }
}
