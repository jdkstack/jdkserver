package org.jdkstack.jdkserver.tcp.core;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jdkstack.jdkserver.tcp.core.tcp.client.JdkClientSocketChannel;
import org.study.core.context.Monitor;
import org.study.core.context.StudyThreadFactory;
import org.study.core.context.ThreadMonitor;
import org.study.network.codecs.NetworkMessage;

public class ClientExamples {
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
    SocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 20000);
    SocketAddress localAddress = new InetSocketAddress("127.0.0.1", 18000);
    JdkClientSocketChannel jdkClientSocketChannel = new JdkClientSocketChannel();

    JdkClientSocketChannelEventRunnable jdkClientSocketChannelWorker =
        new JdkClientSocketChannelEventRunnable(jdkClientSocketChannel, null);
    jdkClientSocketChannelWorker.connectEvent();
    LOG_PRODUCER.submit(jdkClientSocketChannelWorker);
    jdkClientSocketChannelWorker.connect(remoteAddress, localAddress);
    Thread.sleep(9999999);
  }
}
