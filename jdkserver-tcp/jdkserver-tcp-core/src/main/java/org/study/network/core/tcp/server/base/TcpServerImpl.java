package org.study.network.core.tcp.server.base;

import io.netty.channel.ChannelFuture;
import org.study.network.core.tcp.server.option.TcpServerOptions;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2020-12-24 10:13
 * @since 2020-12-24 10:13:00
 */
public class TcpServerImpl extends AbstractTcpServerBase {

  public TcpServerImpl(TcpServerOptions networkOptions) {
    super(networkOptions);
  }

  public ChannelFuture listenServer(int port, String host) {
    return listenTcpServer(port, host);
  }
}
