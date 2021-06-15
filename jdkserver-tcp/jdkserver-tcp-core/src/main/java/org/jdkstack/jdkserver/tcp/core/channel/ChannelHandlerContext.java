package org.jdkstack.jdkserver.tcp.core.channel;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.study.core.future.Handler;
import org.study.network.codecs.Message;

public interface ChannelHandlerContext {

  Channel channel();

  EventExecutor executor();

  String name();

  void setHandler(Handler<Message> handler);

  void setWriteHandler(Handler<Message> handler);

  io.netty.channel.ChannelHandler handler();

  boolean isRemoved();

  io.netty.channel.ChannelHandlerContext fireChannelRegistered();

  io.netty.channel.ChannelHandlerContext fireChannelUnregistered();

  io.netty.channel.ChannelHandlerContext fireChannelActive();

  io.netty.channel.ChannelHandlerContext fireChannelInactive();

  io.netty.channel.ChannelHandlerContext fireExceptionCaught(Throwable cause);

  io.netty.channel.ChannelHandlerContext fireUserEventTriggered(Object evt);

  io.netty.channel.ChannelHandlerContext fireChannelRead(Object msg);

  io.netty.channel.ChannelHandlerContext fireChannelReadComplete();

  io.netty.channel.ChannelHandlerContext fireChannelWritabilityChanged();

  io.netty.channel.ChannelHandlerContext read();

  io.netty.channel.ChannelHandlerContext flush();

  ChannelPipeline pipeline();

  ByteBufAllocator alloc();

  @Deprecated
  <T> Attribute<T> attr(AttributeKey<T> key);

  @Deprecated
  <T> boolean hasAttr(AttributeKey<T> key);

  void write(final ByteBuffer buffer) throws Exception;
}
