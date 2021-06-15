package org.study.network.core.tcp.server.base;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import org.study.network.core.tcp.server.initializer.ServerLoadBalanceChannelInitializer;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-05 09:57
 * @since 2021-01-05 09:57:00
 */
public interface TcpServer {

  void initTcpServer(ServerLoadBalanceChannelInitializer channelBalance, EventLoopGroup eventLoopGroup);

  ChannelFuture listenServer(int port, String host);
}
