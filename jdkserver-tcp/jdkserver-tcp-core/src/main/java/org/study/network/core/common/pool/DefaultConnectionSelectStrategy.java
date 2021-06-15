package org.study.network.core.common.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-22 13:56
 * @since 2021-01-22 13:56:00
 */
public class DefaultConnectionSelectStrategy implements ConnectionSelectStrategy {

  private final Random random = new Random();

  @Override
  public Connection selectConnection(List<Connection> connections) {
    List<Connection> serviceStatusOnConnections = new ArrayList<>();
    for (Connection conn : connections) {
      serviceStatusOnConnections.add(conn);
      // ChannelState channelState = conn.getChannelState();
      // if ("1".equals(channelState.name())) {
      // } else {
      // 触发状态事件
      // }
    }
    return randomGet(serviceStatusOnConnections);
  }

  private Connection randomGet(final List<Connection> connections) {
    int size = connections.size();
    Connection connection = connections.get(this.random.nextInt(size));
    // 检查连接是否正常
    if (connection == null || !connection.isActive()) {
      throw new RuntimeException("当前连接不可用.");
    }
    return connection;
  }
}
