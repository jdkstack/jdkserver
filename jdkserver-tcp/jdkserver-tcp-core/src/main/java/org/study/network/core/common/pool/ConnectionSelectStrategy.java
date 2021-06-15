package org.study.network.core.common.pool;

import java.util.List;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-22 13:55
 * @since 2021-01-22 13:55:00
 */
public interface ConnectionSelectStrategy {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param connections 连接池.
   * @return Connection 选择一个连接.
   * @author admin
   */
  Connection selectConnection(final List<Connection> connections);
}
