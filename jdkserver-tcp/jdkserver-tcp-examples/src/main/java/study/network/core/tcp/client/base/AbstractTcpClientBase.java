package study.network.core.tcp.client.base;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueDomainSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.resolver.DefaultAddressResolverGroup;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import study.network.core.tcp.client.initializer.ClientLoadBalanceChannelInitializer;
import study.network.core.tcp.client.option.TcpClientOptions;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-20 13:52
 * @since 2021-01-20 13:52:00
 */
public abstract class AbstractTcpClientBase implements TcpClient {
  /** 客户端启动对象. */
  protected Bootstrap bootstrap;
  /** 客户端配置对象. */
  protected TcpClientOptions networkOptions;

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  protected AbstractTcpClientBase(final TcpClientOptions networkOptions) {
    this.networkOptions = networkOptions;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public void initTcpClient(final ClientLoadBalanceChannelInitializer channelBalance) {
    bootstrap = new Bootstrap();
    bootstrap.option(ChannelOption.SO_SNDBUF, networkOptions.getSendBufferSize());
    bootstrap.option(ChannelOption.SO_RCVBUF, networkOptions.getReceiveBufferSize());
    bootstrap.option(
        ChannelOption.RCVBUF_ALLOCATOR,
        new FixedRecvByteBufAllocator(networkOptions.getReceiveBufferSize()));
    bootstrap.option(ChannelOption.SO_LINGER, networkOptions.getSoLinger());
    bootstrap.option(ChannelOption.IP_TOS, networkOptions.getTrafficClass());
    bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, networkOptions.getConnectTimeout());
    bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    // 配置客户端高低水位线,搭配流量整形,一起解决客户端的背压问题,防止客户端读写出现非常大的流量.
    bootstrap.option(
        ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024));
    // domainSocket连接,windows系统不支持.
    final boolean domainSocket = networkOptions.isDomainSocket();
    if (domainSocket) {
      // 如果采用domainSocket方式连接服务器.
      bootstrap.channelFactory(channelFactory(true));
    } else {
      // 如果采用非domainSocket方式连接服务器.
      bootstrap.channelFactory(channelFactory(false));
      bootstrap.option(ChannelOption.SO_REUSEADDR, networkOptions.isReuseAddress());
      bootstrap.option(ChannelOption.TCP_NODELAY, networkOptions.isTcpNoDelay());
      bootstrap.option(ChannelOption.SO_KEEPALIVE, networkOptions.isTcpKeepAlive());
    }
    // 客户端配置域名解析器,默认采用IP.
    bootstrap.resolver(DefaultAddressResolverGroup.INSTANCE);
    // bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);
    // bootstrap.resolver(new DnsAddressResolverGroup(new DnsNameResolverBuilder()));
    // DnsAddressResolverGroup dGroup = new DnsAddressResolverGroup(new DnsNameResolverBuilder());
    // RoundRobinDnsAddressResolverGroup roundGroup = new RoundRobinDnsAddressResolverGroup(new
    // DnsNameResolverBuilder());
    bootstrap.group(channelBalance.workers());
    // 客户端连接服务器之前,需要进行初始化的操作.
    bootstrap.handler(channelBalance);
    // 检查是否配置正确.
    bootstrap.validate();
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public ChannelFuture connectTcpServer(final int remotePort, final String remoteHost) {
    SocketAddress remoteAddress;
    final boolean domainSocket = networkOptions.isDomainSocket();
    if (domainSocket) {
      // 如果采用domainSocket方式连接服务器.
      remoteAddress = new DomainSocketAddress(remotePort + ":" + remoteHost);
    } else {
      // 如果采用非domainSocket方式连接服务器.
      remoteAddress = new InetSocketAddress(remoteHost, remotePort);
    }
    return bootstrap.connect(remoteAddress);
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public ChannelFuture connectTcpServer(
      final int remotePort, final String remoteHost, final int localPort, final String localHost) {
    SocketAddress localAddress;
    SocketAddress remoteAddress;
    final boolean domainSocket = networkOptions.isDomainSocket();
    if (domainSocket) {
      // 如果采用domainSocket方式连接服务器.
      remoteAddress = new DomainSocketAddress(remotePort + ":" + remoteHost);
      // 配置本地IP和端口,连接服务器时使用.
      localAddress = new DomainSocketAddress(localPort + ":" + localHost);
    } else {
      // 如果采用非domainSocket方式连接服务器.
      remoteAddress = new InetSocketAddress(remoteHost, remotePort);
      // 配置本地IP和端口,连接服务器时使用.
      localAddress = new InetSocketAddress(localHost, localPort);
    }
    return bootstrap.connect(remoteAddress, localAddress);
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public ChannelFactory<Channel> channelFactory(final boolean domainSocket) {
    if (Epoll.isAvailable()) {
      if (domainSocket) {
        return EpollDomainSocketChannel::new;
      } else {
        return EpollSocketChannel::new;
      }
    }
    if (KQueue.isAvailable()) {
      if (domainSocket) {
        return KQueueDomainSocketChannel::new;
      } else {
        return KQueueSocketChannel::new;
      }
    }
    if (domainSocket) {
      throw new RuntimeException("Domain socket in window os not available!");
    } else {
      return NioSocketChannel::new;
    }
  }
}
