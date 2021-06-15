package org.study.network.core.udp.server;

import io.netty.util.CharsetUtil;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.study.core.context.StudyThreadFactory;
import org.study.core.context.ThreadMonitor;
import org.study.network.core.socket.Buffer;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-25 12:07
 * @since 2021-01-25 12:07:00
 */
public class Examples {
  public static void main(String[] args) {
    UdpServerOptions networkOptions = new UdpServerOptions();
    ThreadMonitor checker = new ThreadMonitor( 3L);
    ThreadFactory masterThreadFactory =
        new StudyThreadFactory("study-master-", checker, 0, 50, TimeUnit.SECONDS);
    ThreadFactory workerThreadFactory =
        new StudyThreadFactory("study-", checker, 1, 200, TimeUnit.SECONDS);

  /*  UdpServerBasketImpl udpServerBasket =
        new UdpServerBasketImpl(networkOptions, masterThreadFactory, workerThreadFactory);*/
    UdpServerImpl udpServer = new UdpServerImpl(null);

    /*
        udpServer.listenServer(
            17000,
            "127.0.0.1",
            asyncResult -> {
              if (asyncResult.succeeded()) {
                System.out.println("Listen succeeded " + asyncResult.succeeded());
              } else {
                System.out.println("Listen failed" + asyncResult.cause());
              }
            });
    */

    Buffer buffer = Buffer.buffer("content");
    udpServer.send(
        buffer,
        17000,
        "127.0.0.1",
        asyncResult -> {
          System.out.println("Send succeeded? " + asyncResult.succeeded());
        });

    udpServer.handler(
        packet -> {
          Buffer data = packet.data();
          String s = data.toString(CharsetUtil.UTF_8);
          System.out.println("我是服务器端>>>>" + s);
          SocketAddress sender = packet.sender();
          // System.out.println(sender.host() + ":" + sender.port());
        });
  }
}
