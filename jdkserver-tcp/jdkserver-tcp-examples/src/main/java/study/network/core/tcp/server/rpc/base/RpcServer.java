package study.network.core.tcp.server.rpc.base;

import study.network.core.tcp.server.manager.ServerResourceManager;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-03-03 15:22
 * @since 2021-03-03 15:22:00
 */
public interface RpcServer {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  void shutdown();

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return ServerResourceManager.
   * @author admin
   */
  ServerResourceManager getServerResourceManager();
}
