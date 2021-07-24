package org.jdkstack.jdkserver.tcp.core.api.core.channel;

import java.util.List;
import java.util.Map;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelHandler;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelHandlerContext;

public interface ChannelPipeline {

  ChannelPipeline addFirst(ChannelHandler... handlers);

  ChannelPipeline addFirst(String name, ChannelHandler handler);

  ChannelPipeline addLast(String name, ChannelHandler handler);

  ChannelPipeline addLast(ChannelHandler... handlers);

  ChannelPipeline remove(ChannelHandler handler);

  ChannelHandler remove(String name);

  <T extends ChannelHandler> T remove(Class<T> handlerType);

  ChannelHandler removeFirst();

  ChannelHandler removeLast();

  ChannelHandler first();

  ChannelHandlerContext firstContext();

  ChannelHandler last();

  ChannelHandlerContext lastContext();

  ChannelHandler get(String name);

  <T extends ChannelHandler> T get(Class<T> handlerType);

  ChannelHandlerContext context(ChannelHandler handler);

  ChannelHandlerContext context(String name);

  ChannelHandlerContext context(Class<? extends ChannelHandler> handlerType);

  JdkChannel channel();

  List<String> names();

  Map<String, ChannelHandler> toMap();

  ChannelPipeline fireChannelRegistered();

  ChannelPipeline fireChannelUnregistered();

  ChannelPipeline fireChannelActive();

  ChannelPipeline fireChannelInactive();

  ChannelPipeline fireExceptionCaught(Throwable cause);

  ChannelPipeline fireUserEventTriggered(Object event);

  ChannelPipeline fireChannelRead(Object msg);

  ChannelPipeline fireChannelReadComplete();

  ChannelPipeline fireChannelWritabilityChanged();

  ChannelPipeline flush();
}