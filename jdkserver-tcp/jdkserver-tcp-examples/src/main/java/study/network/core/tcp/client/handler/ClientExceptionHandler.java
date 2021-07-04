package study.network.core.tcp.client.handler;

import study.core.future.Handler;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-03-03 12:28
 * @since 2021-03-03 12:28:00
 */
public class ClientExceptionHandler implements Handler<Throwable> {

  @Override
  public void handle(Throwable e) {
    System.out.println(e.getMessage());
  }
}
