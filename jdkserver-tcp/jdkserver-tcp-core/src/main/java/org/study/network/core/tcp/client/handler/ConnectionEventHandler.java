package org.study.network.core.tcp.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.net.SocketAddress;
import org.study.network.core.tcp.client.base.ConnectionEventType;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-03-04 09:50
 * @since 2021-03-04 09:50:00
 */
@Sharable
public class ConnectionEventHandler extends ChannelDuplexHandler {
  @Override
  public void connect(
      ChannelHandlerContext ctx,
      SocketAddress remoteAddress,
      SocketAddress localAddress,
      ChannelPromise promise)
      throws Exception {
    super.connect(ctx, remoteAddress, localAddress, promise);
  }

  @Override
  public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    super.disconnect(ctx, promise);
  }

  @Override
  public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    super.close(ctx, promise);
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    super.channelRegistered(ctx);
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    super.channelUnregistered(ctx);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    userEventTriggered(ctx, ConnectionEventType.CLOSE);
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
    if (event instanceof ConnectionEventType) {
      ConnectionEventType eventType = (ConnectionEventType) event;
      Channel channel = ctx.channel();
      if (channel == null) {
        return;
      }
      switch (eventType) {
        case CONNECT:
          break;
        case CONNECT_FAILED:
        case CLOSE:
        case EXCEPTION:
          break;
        default:
          break;
      }
    } else {
      super.userEventTriggered(ctx, event);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    ctx.channel().close();
  }
}
