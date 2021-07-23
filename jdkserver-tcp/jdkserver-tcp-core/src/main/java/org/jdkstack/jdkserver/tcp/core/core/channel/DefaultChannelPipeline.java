package org.jdkstack.jdkserver.tcp.core.core.channel;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jdkstack.jdkserver.tcp.core.api.core.channel.ChannelPipeline;
import org.jdkstack.jdkserver.tcp.core.api.core.channel.JdkChannel;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelHandler;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelHandlerContext;

public class DefaultChannelPipeline implements ChannelPipeline {

  private LinkedList<ChannelHandler> channelHandlerList = new LinkedList<>();

  @Override
  public ChannelPipeline addFirst(ChannelHandler... handlers) {
    return null;
  }

  @Override
  public ChannelPipeline addFirst(String name, ChannelHandler handler) {
    channelHandlerList.addFirst(handler);
    return this;
  }

  @Override
  public ChannelPipeline addLast(String name, ChannelHandler handler) {
    channelHandlerList.addLast(handler);
    return null;
  }

  @Override
  public ChannelPipeline addLast(ChannelHandler... handlers) {
    return null;
  }

  @Override
  public ChannelPipeline remove(ChannelHandler handler) {
    return null;
  }

  @Override
  public ChannelHandler remove(String name) {
    return null;
  }

  @Override
  public <T extends ChannelHandler> T remove(Class<T> handlerType) {
    return null;
  }

  @Override
  public ChannelHandler removeFirst() {
    return null;
  }

  @Override
  public ChannelHandler removeLast() {
    return null;
  }

  @Override
  public ChannelHandler first() {
    return null;
  }

  @Override
  public ChannelHandlerContext firstContext() {
    return null;
  }

  @Override
  public ChannelHandler last() {
    return null;
  }

  @Override
  public ChannelHandlerContext lastContext() {
    return null;
  }

  @Override
  public ChannelHandler get(String name) {
    return null;
  }

  @Override
  public <T extends ChannelHandler> T get(Class<T> handlerType) {
    return null;
  }

  @Override
  public ChannelHandlerContext context(ChannelHandler handler) {
    return null;
  }

  @Override
  public ChannelHandlerContext context(String name) {
    return null;
  }

  @Override
  public ChannelHandlerContext context(Class<? extends ChannelHandler> handlerType) {
    return null;
  }

  @Override
  public JdkChannel channel() {
    return null;
  }

  @Override
  public List<String> names() {
    return null;
  }

  @Override
  public Map<String, ChannelHandler> toMap() {
    return null;
  }

  @Override
  public ChannelPipeline fireChannelRegistered() {
    return null;
  }

  @Override
  public ChannelPipeline fireChannelUnregistered() {
    return null;
  }

  @Override
  public ChannelPipeline fireChannelActive() {
    return null;
  }

  @Override
  public ChannelPipeline fireChannelInactive() {
    return null;
  }

  @Override
  public ChannelPipeline fireExceptionCaught(Throwable cause) {
    return null;
  }

  @Override
  public ChannelPipeline fireUserEventTriggered(Object event) {
    return null;
  }

  @Override
  public ChannelPipeline fireChannelRead(Object msg) {
    return null;
  }

  @Override
  public ChannelPipeline fireChannelReadComplete() {
    return null;
  }

  @Override
  public ChannelPipeline fireChannelWritabilityChanged() {
    return null;
  }

  @Override
  public ChannelPipeline flush() {
    return null;
  }
}
