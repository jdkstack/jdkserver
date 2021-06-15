package org.study.core.context;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-03 19:24
 * @since 2021-01-03 19:24:00
 */
public class Example {

  public static void main(String[] args) {
    ThreadMonitor checker = new ThreadMonitor(20);
    StudyThreadFactory studyThreadFactory =
        new StudyThreadFactory("vert.x-eventloop-thread-", checker);
    int cpu = 2 * Runtime.getRuntime().availableProcessors();
    NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(cpu, studyThreadFactory);
    nioEventLoopGroup.setIoRatio(100);
    EventLoopGroup eventLoopGroup = nioEventLoopGroup;

    for (int i = 0; i < 10; i++) {
      EventLoop eventLoop = eventLoopGroup.next();
      /*     EventLoopContext eventLoopContext = new EventLoopContext(eventLoop);
      eventLoopContext.runOnContext(
          v -> {
            System.out.println("===========>" + Thread.currentThread());
          });*/
    }
  }
}
