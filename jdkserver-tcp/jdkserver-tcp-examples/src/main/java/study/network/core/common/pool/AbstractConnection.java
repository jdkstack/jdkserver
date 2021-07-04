package study.network.core.common.pool;

import io.netty.channel.Channel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-22 13:30
 * @since 2021-01-22 13:30:00
 */
@SuppressWarnings({"java:S1068"})
public abstract class AbstractConnection implements Connection {
  /** . */
  private final AtomicInteger referenceCount = new AtomicInteger(0);
  /** . */
  private final AtomicLong pingCount = new AtomicLong(0L);
  /** . */
  private final AtomicLong pongCount = new AtomicLong(0L);
  /** . */
  private Channel channel;
  /** . */
  private ChannelState channelState;

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  protected AbstractConnection(final Channel channel) {
    this.channel = channel;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public boolean isActive() {
    return this.channel.isActive();
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public ChannelState getChannelState() {
    return channelState;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public void increase() {
    this.referenceCount.getAndIncrement();
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public void decrease() {
    this.referenceCount.getAndDecrement();
  }

  public Channel getChannel() {
    return channel;
  }
}
