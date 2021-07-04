package study.network.core.tcp.client.pool;

import study.network.core.common.pool.AbstractConnectionPool;
import study.network.core.common.pool.ConnectionSelectStrategy;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-22 13:53
 * @since 2021-01-22 13:53:00
 */
public class TcpClientConnectionPool extends AbstractConnectionPool {
  public TcpClientConnectionPool() {}

  public TcpClientConnectionPool(ConnectionSelectStrategy strategy) {
    super(strategy);
  }
}
