package org.jdkstack.jdkserver.tcp.core.channel;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.study.network.codecs.Message;

public class DefaultChannelHandlerContext extends AbstractChannelHandlerContext {

  public DefaultChannelHandlerContext(
      SocketChannel socketChannel,
      org.jdkstack.jdkserver.tcp.core.channel.ChannelHandler channelHandler) {
    super(socketChannel, channelHandler);
  }

  @Override
  public Channel channel() {
    return null;
  }

  @Override
  public EventExecutor executor() {
    return null;
  }

  @Override
  public String name() {
    return null;
  }

  @Override
  public ChannelHandler handler() {
    return null;
  }

  @Override
  public boolean isRemoved() {
    return false;
  }

  @Override
  public ChannelHandlerContext fireChannelRegistered() {
    return null;
  }

  @Override
  public ChannelHandlerContext fireChannelUnregistered() {
    return null;
  }

  @Override
  public ChannelHandlerContext fireChannelActive() {
    return null;
  }

  @Override
  public ChannelHandlerContext fireChannelInactive() {
    return null;
  }

  @Override
  public ChannelHandlerContext fireExceptionCaught(Throwable cause) {
    return null;
  }

  @Override
  public ChannelHandlerContext fireUserEventTriggered(Object evt) {
    return null;
  }

  @Override
  public ChannelHandlerContext fireChannelRead(Object msg) {
    if (msg instanceof Message) {
      Message message = (Message) msg;
      handler.handle(message);
    }
    // 调用业务处理器.
    return null;
  }

  @Override
  public ChannelHandlerContext fireChannelReadComplete() {
    return null;
  }

  @Override
  public ChannelHandlerContext fireChannelWritabilityChanged() {
    return null;
  }

  @Override
  public ChannelHandlerContext read() {
    return null;
  }

  @Override
  public ChannelHandlerContext flush() {
    return null;
  }

  @Override
  public ChannelPipeline pipeline() {
    return null;
  }

  @Override
  public ByteBufAllocator alloc() {
    return null;
  }

  @Override
  public <T> Attribute<T> attr(AttributeKey<T> key) {
    return null;
  }

  @Override
  public <T> boolean hasAttr(AttributeKey<T> key) {
    return false;
  }

  @Override
  public void write(final ByteBuffer buffer) throws Exception {
    outboundBuffer.decrementPendingOutboundBytes(1024);
    socketChannel.write(ByteBuffer.wrap(buffer.array()));
  }
}
