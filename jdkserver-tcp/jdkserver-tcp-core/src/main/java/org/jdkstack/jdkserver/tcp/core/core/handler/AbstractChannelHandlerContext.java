package org.jdkstack.jdkserver.tcp.core.core.handler;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelHandlerContext;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.Handler;
import org.jdkstack.jdkserver.tcp.core.core.buffer.ChannelInboundBuffer;
import org.jdkstack.jdkserver.tcp.core.core.buffer.ChannelOutboundBuffer;

public abstract class AbstractChannelHandlerContext implements ChannelHandlerContext {
  // protected ChannelHandler channelHandler;
  protected SocketChannel socketChannel;
  protected Handler<Message> readHandler;
  protected Handler<ByteBuffer> writeHandler;

  protected Handler<ByteBuffer> writeHandler2;

  protected ChannelInboundBuffer<Object> channelInboundBuffer = new ChannelInboundBuffer<>();

  protected ChannelInboundBuffer<Object> channelInboundBuffer2 = new ChannelInboundBuffer<>();

  protected ChannelOutboundBuffer<ByteBuffer> channelOutboundBuffer = new ChannelOutboundBuffer<>();

  protected ChannelOutboundBuffer<ByteBuffer> channelOutboundBuffer2 =
      new ChannelOutboundBuffer<>();

  public ChannelInboundBuffer<Object> getChannelInboundBuffer2() {
    return channelInboundBuffer2;
  }

  public ChannelInboundBuffer<Object> getChannelInboundBuffer() {
    return channelInboundBuffer;
  }

  public ChannelOutboundBuffer<ByteBuffer> getChannelOutboundBuffer() {
    return channelOutboundBuffer;
  }

  public ChannelOutboundBuffer<ByteBuffer> getChannelOutboundBuffer2() {
    return channelOutboundBuffer2;
  }

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

  @Override
  public void setWriteHandler2(Handler<ByteBuffer> handler) {
    this.writeHandler2 = handler;
  }
}
