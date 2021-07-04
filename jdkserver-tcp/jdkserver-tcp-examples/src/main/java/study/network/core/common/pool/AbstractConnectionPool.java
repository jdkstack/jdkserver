package study.network.core.common.pool;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-22 13:52
 * @since 2021-01-22 13:52:00
 */
@SuppressWarnings({"java:S1068"})
public abstract class AbstractConnectionPool implements ConnectionPool {
  /** . */
  protected final List<Connection> connections = new CopyOnWriteArrayList<>();
  /** . */
  protected int minPoolSize = 1;
  /** . */
  protected int maxPoolSize = 100;
  /** . */
  protected String poolName;
  /** . */
  protected long lastGetTimestamp;
  /** . */
  protected ConnectionSelectStrategy connectionSelectStrategy =
      new DefaultConnectionSelectStrategy();

  public AbstractConnectionPool() {}

  public AbstractConnectionPool(ConnectionSelectStrategy strategy) {
    this.connectionSelectStrategy = strategy;
  }

  @Override
  public synchronized void add(final Connection connection) {
    int size = connections.size();
    if (size < maxPoolSize) {
      connections.add(connection);
    }
  }

  @Override
  public synchronized void remove(final Connection connection) {
    connections.remove(connection);
  }

  @Override
  public synchronized int size() {
    return connections.size();
  }

  public List<Connection> getConnections() {
    return connections;
  }

  public synchronized Connection selectConnection() {
    return connectionSelectStrategy.selectConnection(connections);
  }
}
