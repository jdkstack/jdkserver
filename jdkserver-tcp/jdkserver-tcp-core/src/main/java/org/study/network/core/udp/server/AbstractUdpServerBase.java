package org.study.network.core.udp.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.MaxMessagesRecvByteBufAllocator;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramChannelConfig;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import org.study.core.future.AsyncResult;
import org.study.core.future.Handler;
import org.study.network.core.socket.Buffer;
import org.study.network.core.tcp.client.handler.StudyClientHandler;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-19 20:43
 * @since 2021-01-19 20:43:00
 */
public class AbstractUdpServerBase implements UdpServer {
  private DatagramChannel channel;
  private Handler<UdpServer> packetHandler;

  public AbstractUdpServerBase(String studyImpl) {}

  @Override
  public io.netty.util.concurrent.Future<Channel> listenUdpServer(
      int port, String host, InternetProtocolFamily family) {
    UdpServerOptions options = null; // (UdpServerOptions) studyImpl.getNetworkOptions();
    EventLoop studyEventLoop = null; // studyImpl.getStudyEventLoop();
    EventLoopGroup masterEventLoopGroup = null; // studyImpl.getMasterEventLoopGroup();
    // DatagramChannel channel = null;
    if (Epoll.isAvailable()) {
      channel = new EpollDatagramChannel();
    }
    if (channel == null && KQueue.isAvailable()) {
      channel = new KQueueDatagramChannel();
    }
    if (channel == null) {
      channel = new NioDatagramChannel();
    }
    DatagramChannelConfig config = channel.config();
    // config.setAllocator(PartialPooledByteBufAllocator.INSTANCE);
    config.setSendBufferSize(options.getSendBufferSize());
    config.setReceiveBufferSize(options.getReceiveBufferSize());
    config.setRecvByteBufAllocator(new FixedRecvByteBufAllocator(options.getReceiveBufferSize()));
    config.setOption(ChannelOption.SO_REUSEADDR, options.isReuseAddress());
    config.setTrafficClass(options.getTrafficClass());
    config.setBroadcast(options.isBroadcast());
    config.setOption(ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION, true);
    MaxMessagesRecvByteBufAllocator bufAllocator = config.getRecvByteBufAllocator();
    bufAllocator.maxMessagesPerRead(1);
    studyEventLoop.register(channel);

    channel.pipeline().addLast("logging", new LoggingHandler());
    channel.pipeline().addLast("handler", StudyClientHandler.create(UdpConnection::new));

    Promise<Channel> promise = masterEventLoopGroup.next().newPromise();
    SocketAddress socketAddress = new InetSocketAddress(host, port);
    ChannelFuture cf = channel.bind(socketAddress);
    cf.addListener(
        f -> {
          if (f.isSuccess()) {
            promise.setSuccess(cf.channel());
          } else {
            promise.setFailure(cf.cause());
          }
        });
    return promise;
  }

  public Future<Void> send(
      Buffer packet, int port, String host, Handler<AsyncResult<Void>> handler) {
    Objects.requireNonNull(packet, "no null packet accepted");
    Objects.requireNonNull(host, "no null host accepted");
    InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
    DatagramPacket datagramPacket = new DatagramPacket(packet.getByteBuf(), inetSocketAddress);
    ChannelFuture channelFuture = channel.writeAndFlush(datagramPacket);
    channelFuture.addListener(
        fut -> {
          if (fut.isSuccess()) {
            System.out.println("发送成功.");
          }
        });
    return channelFuture;
  }

  public synchronized UdpServer handler(Handler<UdpServer> handler) {
    this.packetHandler = handler;
    return this;
  }

  @Override
  public SocketAddress sender() {
    return null;
  }

  @Override
  public Buffer data() {
    return null;
  }
}
