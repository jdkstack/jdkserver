package org.jdkstack.jdkserver.tcp.core.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorChooserFactory;
import io.netty.util.concurrent.MultithreadEventExecutorGroup;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public abstract class MultithreadEventLoopGroup extends MultithreadEventExecutorGroup
    implements EventLoopGroup {

  private static final InternalLogger logger =
      InternalLoggerFactory.getInstance(io.netty.channel.MultithreadEventLoopGroup.class);

  private static final int DEFAULT_EVENT_LOOP_THREADS;

  static {
    DEFAULT_EVENT_LOOP_THREADS =
        Math.max(
            1,
            SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));

    if (logger.isDebugEnabled()) {
      logger.debug("-Dio.netty.eventLoopThreads: {}", DEFAULT_EVENT_LOOP_THREADS);
    }
  }

  /** @see MultithreadEventExecutorGroup#MultithreadEventExecutorGroup(int, Executor, Object...) */
  protected MultithreadEventLoopGroup(int nThreads, Executor executor, Object... args) {
    super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, args);
  }

  /**
   * @see MultithreadEventExecutorGroup#MultithreadEventExecutorGroup(int, ThreadFactory, Object...)
   */
  protected MultithreadEventLoopGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
    super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, threadFactory, args);
  }

  /**
   * @see MultithreadEventExecutorGroup#MultithreadEventExecutorGroup(int, Executor,
   *     EventExecutorChooserFactory, Object...)
   */
  protected MultithreadEventLoopGroup(
      int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, Object... args) {
    super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, chooserFactory, args);
  }

  @Override
  protected ThreadFactory newDefaultThreadFactory() {
    return new DefaultThreadFactory(getClass(), Thread.MAX_PRIORITY);
  }

  @Override
  public EventLoop next() {
    return (EventLoop) super.next();
  }

  @Override
  protected abstract EventLoop newChild(Executor executor, Object... args) throws Exception;

  @Override
  public ChannelFuture register(Channel channel) {
    return next().register(channel);
  }

  @Override
  public ChannelFuture register(ChannelPromise promise) {
    return next().register(promise);
  }

  @Deprecated
  @Override
  public ChannelFuture register(Channel channel, ChannelPromise promise) {
    return next().register(channel, promise);
  }
}
