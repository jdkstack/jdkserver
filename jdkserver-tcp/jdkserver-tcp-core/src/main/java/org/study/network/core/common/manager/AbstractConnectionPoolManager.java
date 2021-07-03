package org.study.network.core.common.manager;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.study.network.core.common.pool.Connection;
import org.study.network.core.common.pool.ConnectionPool;
import org.study.network.core.tcp.client.pool.TcpClientConnectionPool;
import org.study.network.core.tcp.server.pool.TcpServerConnectionPool;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-22 08:50
 * @since 2021-01-22 08:50:00
 */
public abstract class AbstractConnectionPoolManager implements ConnectionPoolManager {

  /**
   * 1连接池名---->连接池. 2客户端: 服务器IP:服务器端口---->客户端连接池. 3服务端: 服务器IP:服务器端口---->客户端连接池. 4服务器IP---->服务器端连接池.
   */
  protected final Map<String, ConnectionPool> connectionPools = new ConcurrentHashMap<>(16);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public int size() {
    return connectionPools.size();
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public Collection<ConnectionPool> getAll() {
    return connectionPools.values();
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public synchronized ConnectionPool createTcpClientConnection(
      final String id, final Connection connection) {
    ConnectionPool connectionPool = connectionPools.get(id);
    if (connectionPool == null) {
      connectionPool = new TcpClientConnectionPool();
      this.connectionPools.put(id, connectionPool);
    }
    connectionPool.add(connection);
    return connectionPool;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public synchronized ConnectionPool createTcpServerConnection(
      final String id, final Connection connection) {
    ConnectionPool connectionPool = connectionPools.get(id);
    if (connectionPool == null) {
      connectionPool = new TcpServerConnectionPool();
      this.connectionPools.putIfAbsent(id, connectionPool);
    }
    connectionPool.add(connection);
    return connectionPool;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public synchronized ConnectionPool removeTcpClientConnection(
      final String id, final Connection connection) {
    ConnectionPool connectionPool = connectionPools.get(id);
    if (connectionPool != null) {
      connectionPool.remove(connection);
    }
    return connectionPool;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public synchronized ConnectionPool removeTcpServerConnection(
      final String id, final Connection connection) {
    ConnectionPool connectionPool = connectionPools.get(id);
    if (connectionPool != null) {
      connectionPool.remove(connection);
    }
    return connectionPool;
  }

  public Map<String, ConnectionPool> getConnectionPools() {
    return connectionPools;
  }
}
