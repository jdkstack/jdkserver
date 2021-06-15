package org.study.network.core.tcp.server.ssl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SniCompletionEvent;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Promise;

/**
 * 等待SSL握手完成.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public class ServerSslHandshakeHandler extends ChannelInboundHandlerAdapter {

  static AttributeKey<String> SERVER_NAME_ATTR = AttributeKey.valueOf("sniServerName");
  private final Promise<Void> promise;

  public ServerSslHandshakeHandler(Promise<Void> promise) {
    this.promise = promise;
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
    if (evt instanceof SniCompletionEvent) {
      SniCompletionEvent completion = (SniCompletionEvent) evt;
      Attribute<String> val = ctx.channel().attr(SERVER_NAME_ATTR);
      val.set(completion.hostname());
    } else if (evt instanceof SslHandshakeCompletionEvent) {
      SslHandshakeCompletionEvent completion = (SslHandshakeCompletionEvent) evt;
      if (completion.isSuccess()) {
        ctx.pipeline().remove(this);
        promise.setSuccess(null);
      } else {
        promise.tryFailure(completion.cause());
      }
    } else {
      ctx.fireUserEventTriggered(evt);
    }
  }
}
