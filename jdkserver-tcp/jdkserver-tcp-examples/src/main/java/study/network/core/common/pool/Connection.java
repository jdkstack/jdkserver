package study.network.core.common.pool;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-22 13:30
 * @since 2021-01-22 13:30:00
 */
public interface Connection {

  boolean isActive();

  ChannelState getChannelState();

  void increase();

  void decrease();
}
