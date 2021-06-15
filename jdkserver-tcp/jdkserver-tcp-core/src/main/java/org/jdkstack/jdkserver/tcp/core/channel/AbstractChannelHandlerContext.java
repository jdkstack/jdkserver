package org.jdkstack.jdkserver.tcp.core.channel;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.jdkstack.jdkserver.tcp.core.channel.buffer.ChannelOutboundBuffer;
import org.study.core.future.Handler;
import org.study.network.codecs.Message;

public abstract class AbstractChannelHandlerContext implements ChannelHandlerContext {
  protected ChannelOutboundBuffer outboundBuffer = new ChannelOutboundBuffer();

  protected ChannelHandler channelHandler;

  protected SocketChannel socketChannel;

  protected Handler<Message> handler;
  protected Handler<Message> setWriteHandler;

  @Override
  public void setHandler(Handler<Message> handler) {
    this.handler = handler;
  }

  @Override
  public void setWriteHandler(Handler<Message> setWriteHandler) {
    this.setWriteHandler = setWriteHandler;
  }

  public AbstractChannelHandlerContext(SocketChannel socketChannel, ChannelHandler channelHandler) {
    this.socketChannel = socketChannel;
    this.channelHandler = channelHandler;
  }
}
