package org.study.network.core.tcp.client.manager;

import org.study.network.core.common.manager.AbstractConnectionPoolManager;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-22 08:51
 * @since 2021-01-22 08:51:00
 */
public class ClientConnectionPoolManager extends AbstractConnectionPoolManager {

  private static ClientConnectionPoolManager instance = new ClientConnectionPoolManager();

  private ClientConnectionPoolManager() {}

  public static ClientConnectionPoolManager getInstance() {
    return instance;
  }
}
