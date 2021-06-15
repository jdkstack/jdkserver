package org.jdkstack.jdkserver.tcp.core.channel;

import io.netty.channel.DefaultSelectStrategyFactory;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopTaskQueueFactory;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.SelectStrategyFactory;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorChooserFactory;
import io.netty.util.concurrent.RejectedExecutionHandler;
import io.netty.util.concurrent.RejectedExecutionHandlers;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public class JdkEventLoopGroup extends MultithreadEventLoopGroup {

  /**
   * Create a new instance using the default number of threads, the default {@link ThreadFactory}
   * and the {@link SelectorProvider} which is returned by {@link SelectorProvider#provider()}.
   */
  public JdkEventLoopGroup() {
    this(0);
  }

  /**
   * Create a new instance using the specified number of threads, {@link ThreadFactory} and the
   * {@link SelectorProvider} which is returned by {@link SelectorProvider#provider()}.
   */
  public JdkEventLoopGroup(int nThreads) {
    this(nThreads, (Executor) null);
  }

  /**
   * Create a new instance using the default number of threads, the given {@link ThreadFactory} and
   * the {@link SelectorProvider} which is returned by {@link SelectorProvider#provider()}.
   */
  public JdkEventLoopGroup(ThreadFactory threadFactory) {
    this(0, threadFactory, SelectorProvider.provider());
  }

  /**
   * Create a new instance using the specified number of threads, the given {@link ThreadFactory}
   * and the {@link SelectorProvider} which is returned by {@link SelectorProvider#provider()}.
   */
  public JdkEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
    this(nThreads, threadFactory, SelectorProvider.provider());
  }

  public JdkEventLoopGroup(int nThreads, Executor executor) {
    this(nThreads, executor, SelectorProvider.provider());
  }

  /**
   * Create a new instance using the specified number of threads, the given {@link ThreadFactory}
   * and the given {@link SelectorProvider}.
   */
  public JdkEventLoopGroup(
      int nThreads, ThreadFactory threadFactory, final SelectorProvider selectorProvider) {
    this(nThreads, threadFactory, selectorProvider, DefaultSelectStrategyFactory.INSTANCE);
  }

  public JdkEventLoopGroup(
      int nThreads,
      ThreadFactory threadFactory,
      final SelectorProvider selectorProvider,
      final SelectStrategyFactory selectStrategyFactory) {
    super(
        nThreads,
        threadFactory,
        selectorProvider,
        selectStrategyFactory,
        RejectedExecutionHandlers.reject());
  }

  public JdkEventLoopGroup(
      int nThreads, Executor executor, final SelectorProvider selectorProvider) {
    this(nThreads, executor, selectorProvider, DefaultSelectStrategyFactory.INSTANCE);
  }

  public JdkEventLoopGroup(
      int nThreads,
      Executor executor,
      final SelectorProvider selectorProvider,
      final SelectStrategyFactory selectStrategyFactory) {
    super(
        nThreads,
        executor,
        selectorProvider,
        selectStrategyFactory,
        RejectedExecutionHandlers.reject());
  }

  public JdkEventLoopGroup(
      int nThreads,
      Executor executor,
      EventExecutorChooserFactory chooserFactory,
      final SelectorProvider selectorProvider,
      final SelectStrategyFactory selectStrategyFactory) {
    super(
        nThreads,
        executor,
        chooserFactory,
        selectorProvider,
        selectStrategyFactory,
        RejectedExecutionHandlers.reject());
  }

  public JdkEventLoopGroup(
      int nThreads,
      Executor executor,
      EventExecutorChooserFactory chooserFactory,
      final SelectorProvider selectorProvider,
      final SelectStrategyFactory selectStrategyFactory,
      final RejectedExecutionHandler rejectedExecutionHandler) {
    super(
        nThreads,
        executor,
        chooserFactory,
        selectorProvider,
        selectStrategyFactory,
        rejectedExecutionHandler);
  }

  public JdkEventLoopGroup(
      int nThreads,
      Executor executor,
      EventExecutorChooserFactory chooserFactory,
      final SelectorProvider selectorProvider,
      final SelectStrategyFactory selectStrategyFactory,
      final RejectedExecutionHandler rejectedExecutionHandler,
      final EventLoopTaskQueueFactory taskQueueFactory) {
    super(
        nThreads,
        executor,
        chooserFactory,
        selectorProvider,
        selectStrategyFactory,
        rejectedExecutionHandler,
        taskQueueFactory);
  }

  /**
   * Sets the percentage of the desired amount of time spent for I/O in the child event loops. The
   * default value is {@code 50}, which means the event loop will try to spend the same amount of
   * time for I/O as for non-I/O tasks.
   */
  public void setIoRatio(int ioRatio) {
    for (EventExecutor e : this) {
      ((JdkEventLoop) e).setIoRatio(ioRatio);
    }
  }

  /**
   * Replaces the current {@link Selector}s of the child event loops with newly created {@link
   * Selector}s to work around the infamous epoll 100% CPU bug.
   */
  public void rebuildSelectors() {
    for (EventExecutor e : this) {
      ((JdkEventLoop) e).rebuildSelector();
    }
  }

  @Override
  protected EventLoop newChild(Executor executor, Object... args) throws Exception {
    EventLoopTaskQueueFactory queueFactory =
        args.length == 4 ? (EventLoopTaskQueueFactory) args[3] : null;
    return new JdkEventLoop(
        this,
        executor,
        (SelectorProvider) args[0],
        ((SelectStrategyFactory) args[1]).newSelectStrategy(),
        (RejectedExecutionHandler) args[2],
        queueFactory);
  }
}
