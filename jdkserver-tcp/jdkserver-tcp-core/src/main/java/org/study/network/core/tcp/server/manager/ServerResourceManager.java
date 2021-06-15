package org.study.network.core.tcp.server.manager;

import io.netty.channel.EventLoopGroup;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import org.study.network.core.common.manager.ConnectionPoolManager;
import org.study.network.core.common.manager.ResourceManager;
import org.study.network.core.tcp.server.initializer.ServerLoadBalanceChannelInitializer;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-24 19:39
 * @since 2021-01-24 19:39:00
 */
public class ServerResourceManager implements ResourceManager {
  // Group管理
  public Map<String, List<EventLoopGroup>> eventLoopGroups = new ConcurrentHashMap<>();

  // 线程池管理
  public Map<String, List<ThreadPoolExecutor>> threadPoolExecutors = new ConcurrentHashMap<>();

  // 连接池管理
  public Map<String, ConnectionPoolManager> connectionPoolManagers = new ConcurrentHashMap<>();

  // 服务Worker
  public Map<String, ServerLoadBalanceChannelInitializer> lb = new ConcurrentHashMap<>();

  public ServerLoadBalanceChannelInitializer getLoadBalanceChannelInitializer(String id) {
    return lb.get(id);
  }

  public void setLoadBalanceChannelInitializer(String id, ServerLoadBalanceChannelInitializer lbc) {
    lb.putIfAbsent(id, lbc);
  }
}
