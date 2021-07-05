package org.jdkstack.jdkserver.tcp.core.examples;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jdkstack.jdkserver.tcp.core.context.Monitor;
import org.jdkstack.jdkserver.tcp.core.context.StudyRejectedPolicy;
import org.jdkstack.jdkserver.tcp.core.context.StudyThreadFactory;
import org.jdkstack.jdkserver.tcp.core.context.ThreadMonitor;
import org.jdkstack.jdkserver.tcp.core.core.server.JdkServerSocketChannel;
import org.jdkstack.jdkserver.tcp.core.core.server.JdkServerSocketChannelEventRunnable;

public class ServerExamples {
  /** 线程阻塞的最大时间时10秒.如果不超过15秒,打印warn.如果超过15秒打印异常堆栈. */
  private static final Monitor CHECKER = new ThreadMonitor(15000L);
  /** 线程池. */
  private static final ExecutorService LOG_PRODUCER =
      new ThreadPoolExecutor(
          1,
          1,
          0,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(5000),
          new StudyThreadFactory("log-producer", CHECKER),
          new StudyRejectedPolicy());

  /** 线程池. CallerRunsPolicy 拒绝策略不丢数据,因为在主线程上执行. */
  private static final ExecutorService LOG_CONSUMER =
      new ThreadPoolExecutor(
          1,
          1,
          0,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(5000),
          new StudyThreadFactory("log-consumer", CHECKER),
          new StudyRejectedPolicy());

  public static void main(String[] args) throws Exception {
    System.setProperty("javax.net.debug", "all");
    SocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 20000);
    JdkServerSocketChannel serverSocketChannel = new JdkServerSocketChannel();
    JdkServerSocketChannelEventRunnable jdkServerSocketChannelEventRunnable =
        new JdkServerSocketChannelEventRunnable(serverSocketChannel);
    LOG_PRODUCER.submit(jdkServerSocketChannelEventRunnable);
    jdkServerSocketChannelEventRunnable.acceptEvent();
    jdkServerSocketChannelEventRunnable.bind(remoteAddress, 100);
    Thread.sleep(9999999);
  }
}
