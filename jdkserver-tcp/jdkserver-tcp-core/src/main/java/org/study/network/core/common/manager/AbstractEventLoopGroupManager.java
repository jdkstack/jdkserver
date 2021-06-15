package org.study.network.core.common.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import java.util.concurrent.ThreadFactory;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-24 20:53
 * @since 2021-01-24 20:53:00
 */
public class AbstractEventLoopGroupManager implements EventLoopGroupManager {
  private ThreadFactory threadFactory;

  public AbstractEventLoopGroupManager(ThreadFactory threadFactory) {
    this.threadFactory = threadFactory;
  }

  public EventLoopGroup createEventLoopGroup(int ioRatio, int poolSize) {
    // it only works on linux.
    if (Epoll.isAvailable()) {
      EpollEventLoopGroup epollEventLoopGroup = new EpollEventLoopGroup(poolSize, threadFactory);
      return epollEventLoopGroup;
    }
    // it only works on unix.
    if (KQueue.isAvailable()) {
      KQueueEventLoopGroup kQueueNioEventLoopGroup =
          new KQueueEventLoopGroup(poolSize, threadFactory);
      kQueueNioEventLoopGroup.setIoRatio(ioRatio);
      return kQueueNioEventLoopGroup;
    }
    // it only works on windows.
    NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(poolSize, threadFactory);
    nioEventLoopGroup.setIoRatio(ioRatio);
    return nioEventLoopGroup;
  }
}
