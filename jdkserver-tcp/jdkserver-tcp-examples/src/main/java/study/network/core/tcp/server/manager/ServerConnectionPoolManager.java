package study.network.core.tcp.server.manager;

import study.network.core.common.manager.AbstractConnectionPoolManager;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-22 08:51
 * @since 2021-01-22 08:51:00
 */
public class ServerConnectionPoolManager extends AbstractConnectionPoolManager {

  private static ServerConnectionPoolManager instance = new ServerConnectionPoolManager();

  private ServerConnectionPoolManager() {}

  public static ServerConnectionPoolManager getInstance() {
    return instance;
  }
}
