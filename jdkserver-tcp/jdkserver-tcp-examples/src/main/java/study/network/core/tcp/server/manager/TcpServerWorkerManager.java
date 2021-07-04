package study.network.core.tcp.server.manager;

import java.util.concurrent.ConcurrentHashMap;
import study.network.core.tcp.server.initializer.ServerLoadBalanceChannelInitializer;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-24 11:19
 * @since 2021-01-24 11:19:00
 */
public class TcpServerWorkerManager implements ServerWorkerManager {
  protected ConcurrentHashMap<String, ServerLoadBalanceChannelInitializer> lb =
      new ConcurrentHashMap<>();

  public ServerLoadBalanceChannelInitializer get(String id) {
    return lb.get(id);
  }

  public void setLoadBalanceChannelInitializer(String id, ServerLoadBalanceChannelInitializer lbc) {
    lb.putIfAbsent(id, lbc);
  }

  public int size() {
    return lb.size();
  }
}
