package org.study.network.core.common.pool;

import java.net.SocketAddress;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-22 14:36
 * @since 2021-01-22 14:36:00
 */
public abstract class AbstractConnectionFactory implements ConnectionFactory {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   * @author admin
   */
  public Connection createConnection(final SocketAddress remoteAddress) throws Exception {

    return null;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public Connection createConnection(
      final SocketAddress localAddress, final SocketAddress remoteAddress) throws Exception {

    return null;
  }
}
