package org.study.network.core.tcp.client.ssl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.concurrent.Promise;
import javax.net.ssl.SSLHandshakeException;

/**
 * 等待SSL握手完成.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public class ClientSslHandshakeHandler extends ChannelInboundHandlerAdapter {
  private final Promise<Void> promise;

  public ClientSslHandshakeHandler(Promise<Void> promise) {
    this.promise = promise;
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
    if (evt instanceof SslHandshakeCompletionEvent) {
      SslHandshakeCompletionEvent completion = (SslHandshakeCompletionEvent) evt;
      if (completion.isSuccess()) {
        ctx.pipeline().remove(this);
        promise.setSuccess(null);
      } else {
        promise.tryFailure(completion.cause());
        SSLHandshakeException sslException =
            new SSLHandshakeException("Failed to create SSL connection");
        sslException.initCause(completion.cause());
      }
    }
    ctx.fireUserEventTriggered(evt);
  }
}
