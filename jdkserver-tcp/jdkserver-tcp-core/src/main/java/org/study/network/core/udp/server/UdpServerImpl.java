package org.study.network.core.udp.server;

import io.netty.channel.Channel;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.util.concurrent.Future;
import org.study.core.future.AsyncResult;
import org.study.core.future.Handler;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-19 20:23
 * @since 2021-01-19 20:23:00
 */
public class UdpServerImpl extends AbstractUdpServerBase implements UdpServer {
  private io.netty.util.concurrent.Future<Channel> channelFuture;

  public UdpServerImpl(String a) {
    super(a);
  }

  public Future<UdpServer> listenServer(
      int port, String host, Handler<AsyncResult<UdpServer>> handler) {
    channelFuture = listenUdpServer(port, host, InternetProtocolFamily.IPv4);

    handler.handle((AsyncResult<UdpServer>) this);
    /*    StudyPromise<UdpServer> promise = new StudyPromiseImpl();
    channelFuture.addListener(
        res -> {
          if (res.isSuccess()) {
            promise.complete(this);
          } else {
            promise.fail(res.cause());
          }
        });
    Channel channel = channelFuture.getNow();
    if (channel != null) {
      channel
          .closeFuture()
          .addListener(
              future -> {
                System.out.println("channel close!");
              });
    }
    return promise.future();*/

    return (Future<UdpServer>) this;
  }
}
