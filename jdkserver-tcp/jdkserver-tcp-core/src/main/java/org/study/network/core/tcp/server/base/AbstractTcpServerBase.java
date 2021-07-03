package org.study.network.core.tcp.server.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.ServerChannel;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueServerDomainSocketChannel;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.study.network.core.tcp.server.initializer.ServerLoadBalanceChannelInitializer;
import org.study.network.core.tcp.server.option.TcpServerOptions;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2020-12-24 12:17
 * @since 2020-12-24 12:17:00
 */
public abstract class AbstractTcpServerBase implements TcpServer {
  protected ServerBootstrap bootstrap;
  protected TcpServerOptions networkOptions;

  protected AbstractTcpServerBase(TcpServerOptions networkOptions) {
    this.networkOptions = networkOptions;
  }

  public ChannelFactory<ServerChannel> serverChannelFactory(boolean domainSocket) {
    if (Epoll.isAvailable()) {
      if (domainSocket) {
        return EpollServerDomainSocketChannel::new;
      } else {
        return EpollServerSocketChannel::new;
      }
    }
    if (KQueue.isAvailable()) {
      if (domainSocket) {
        return KQueueServerDomainSocketChannel::new;
      } else {
        return KQueueServerSocketChannel::new;
      }
    }
    if (domainSocket) {
      throw new RuntimeException("Domain socket in window os not available!");
    } else {
      return NioServerSocketChannel::new;
    }
  }

  @Override
  public void initTcpServer(
      ServerLoadBalanceChannelInitializer channelBalance, EventLoopGroup eventLoopGroup) {
    bootstrap = new ServerBootstrap();
    bootstrap.option(ChannelOption.SO_REUSEADDR, networkOptions.isReuseAddress());
    bootstrap.childOption(ChannelOption.SO_SNDBUF, networkOptions.getSendBufferSize());
    bootstrap.childOption(ChannelOption.SO_RCVBUF, networkOptions.getReceiveBufferSize());
    bootstrap.childOption(
        ChannelOption.RCVBUF_ALLOCATOR,
        new FixedRecvByteBufAllocator(networkOptions.getReceiveBufferSize()));
    bootstrap.childOption(ChannelOption.SO_LINGER, networkOptions.getSoLinger());
    bootstrap.childOption(ChannelOption.IP_TOS, networkOptions.getTrafficClass());
    bootstrap
        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    bootstrap.option(ChannelOption.SO_BACKLOG, networkOptions.getAcceptBacklog());
    bootstrap.childOption(
        ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024));
    boolean domainSocket = networkOptions.isDomainSocket();
    if (domainSocket) {
      bootstrap.channelFactory(serverChannelFactory(true));
    } else {
      bootstrap.channelFactory(serverChannelFactory(false));
      bootstrap.childOption(ChannelOption.SO_KEEPALIVE, networkOptions.isTcpKeepAlive());
      bootstrap.childOption(ChannelOption.TCP_NODELAY, networkOptions.isTcpNoDelay());
    }
    bootstrap.group(eventLoopGroup, channelBalance.workers());
    bootstrap.childHandler(channelBalance);
    bootstrap.validate();
  }

  /**
   * This is a method description.
   *
   * <p>This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return future
   */
  public ChannelFuture listenTcpServer(int port, String host) {
    boolean domainSocket = networkOptions.isDomainSocket();
    SocketAddress socketAddress;
    if (domainSocket) {
      socketAddress = new DomainSocketAddress(host + ":" + port);
    } else {
      socketAddress = new InetSocketAddress(host, port);
    }
    return bootstrap.bind(socketAddress);
  }
}
