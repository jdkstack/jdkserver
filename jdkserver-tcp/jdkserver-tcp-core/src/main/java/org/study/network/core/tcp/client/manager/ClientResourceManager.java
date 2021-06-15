package org.study.network.core.tcp.client.manager;

import io.netty.channel.EventLoopGroup;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import org.study.network.core.common.manager.ConnectionPoolManager;
import org.study.network.core.common.manager.ResourceManager;
import org.study.network.core.tcp.client.initializer.ClientLoadBalanceChannelInitializer;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-24 20:07
 * @since 2021-01-24 20:07:00
 */
public class ClientResourceManager implements ResourceManager {

  /** Group管理. */
  public final Map<String, List<EventLoopGroup>> eventLoopGroups = new ConcurrentHashMap<>(16);

  /** 线程池管理. */
  public final Map<String, List<ExecutorService>> threadPoolExecutors = new ConcurrentHashMap<>(16);

  /** 连接池管理. */
  public final Map<String, ConnectionPoolManager> connectionPoolManagers = new ConcurrentHashMap<>(16);

  /** 服务Worker. */
  public final Map<String, ClientLoadBalanceChannelInitializer> lb = new ConcurrentHashMap<>(16);

}
