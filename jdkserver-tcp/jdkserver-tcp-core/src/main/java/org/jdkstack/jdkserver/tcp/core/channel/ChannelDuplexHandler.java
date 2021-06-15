package org.jdkstack.jdkserver.tcp.core.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.net.SocketAddress;

public class ChannelDuplexHandler implements ChannelInboundHandler, ChannelOutboundHandler {

  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {}

  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {}

  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {}

  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {}

  public void channelActive(ChannelHandlerContext ctx) throws Exception {}

  public void channelInactive(ChannelHandlerContext ctx) throws Exception {}

  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {}

  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {}

  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {}

  public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {}

  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {}

  public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
      throws Exception {
    ctx.bind(localAddress, promise);
  }

  public void connect(
      ChannelHandlerContext ctx,
      SocketAddress remoteAddress,
      SocketAddress localAddress,
      ChannelPromise promise)
      throws Exception {
    ctx.connect(remoteAddress, localAddress, promise);
  }

  public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    ctx.disconnect(promise);
  }

  public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    ctx.close(promise);
  }

  public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    ctx.deregister(promise);
  }

  public void read(ChannelHandlerContext ctx) throws Exception {
    ctx.read();
  }

  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
      throws Exception {
    ctx.write(msg, promise);
  }

  public void flush(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
  }
}
