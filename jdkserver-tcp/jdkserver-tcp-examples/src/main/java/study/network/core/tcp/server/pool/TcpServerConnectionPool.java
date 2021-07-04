package study.network.core.tcp.server.pool;

import study.network.core.common.pool.AbstractConnectionPool;
import study.network.core.common.pool.ConnectionSelectStrategy;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-22 13:54
 * @since 2021-01-22 13:54:00
 */
public class TcpServerConnectionPool extends AbstractConnectionPool {
  public TcpServerConnectionPool() {}

  public TcpServerConnectionPool(ConnectionSelectStrategy strategy) {
    super(strategy);
  }
}
