package org.study.network.core.tcp.client.base;

import io.netty.channel.ChannelFuture;
import org.study.network.core.tcp.client.option.TcpClientOptions;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-20 14:37
 * @since 2021-01-20 14:37:00
 */
public class TcpClientImpl extends AbstractTcpClientBase implements TcpClient {

  public TcpClientImpl(TcpClientOptions networkOptions) {
    super(networkOptions);
  }

  public ChannelFuture connectServer(
      int remotePort, String remoteHost, int localPort, String localHost) {
    return super.connectTcpServer(remotePort, remoteHost, localPort, localHost);
  }

  public ChannelFuture connectServer(int remotePort, String remoteHost) {
    return connectTcpServer(remotePort, remoteHost);
  }
}
