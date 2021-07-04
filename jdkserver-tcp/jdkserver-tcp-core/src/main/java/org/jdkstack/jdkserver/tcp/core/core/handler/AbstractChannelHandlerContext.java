package org.jdkstack.jdkserver.tcp.core.core.handler;

import java.nio.channels.SocketChannel;
import org.jdkstack.jdkserver.tcp.core.core.buffer.ChannelOutboundBuffer;
import org.jdkstack.jdkserver.tcp.core.core.codecs.Message;
import org.jdkstack.jdkserver.tcp.core.future.Handler;

public abstract class AbstractChannelHandlerContext implements ChannelHandlerContext {
  protected ChannelOutboundBuffer<String> outboundBuffer = new ChannelOutboundBuffer<>();

  protected ChannelHandler channelHandler;

  protected SocketChannel socketChannel;

  protected Handler<Message> handler;
  protected Handler<Message> setWriteHandler;

  protected AbstractChannelHandlerContext(
      SocketChannel socketChannel, ChannelHandler channelHandler) {
    this.socketChannel = socketChannel;
    this.channelHandler = channelHandler;
  }

  @Override
  public void setHandler(Handler<Message> handler) {
    this.handler = handler;
  }

  @Override
  public void setWriteHandler(Handler<Message> setWriteHandler) {
    this.setWriteHandler = setWriteHandler;
  }
}
