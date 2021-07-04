package study.network.core.tcp.client.pool;

import io.netty.channel.Channel;
import study.network.core.common.pool.AbstractConnection;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-22 13:50
 * @since 2021-01-22 13:50:00
 */
public class TcpClientConnection extends AbstractConnection {

  public TcpClientConnection(Channel channel) {
    super(channel);
  }
}
