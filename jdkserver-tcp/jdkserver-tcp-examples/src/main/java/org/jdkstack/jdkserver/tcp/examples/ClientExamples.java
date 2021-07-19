package org.jdkstack.jdkserver.tcp.examples;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jdkstack.jdkserver.tcp.core.api.context.Monitor;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.Handler;
import org.jdkstack.jdkserver.tcp.core.context.StudyRejectedPolicy;
import org.jdkstack.jdkserver.tcp.core.context.StudyThreadFactory;
import org.jdkstack.jdkserver.tcp.core.context.ThreadMonitor;
import org.jdkstack.jdkserver.tcp.core.core.client.ClientChannelReadHandler;
import org.jdkstack.jdkserver.tcp.core.core.client.ClientChannelReadWriteHandler;
import org.jdkstack.jdkserver.tcp.core.core.client.ClientChannelWriteHandler;
import org.jdkstack.jdkserver.tcp.core.core.client.JdkClientSocketChannel;
import org.jdkstack.jdkserver.tcp.core.core.client.JdkClientSocketChannelEventRunnable;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkByteToMessageDecoderHandler;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkMessageToByteEncoderHandler;

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
    // System.setProperty("javax.net.debug", "all");
    SocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 20000);
    //
    JdkClientSocketChannel jdkClientSocketChannel = new JdkClientSocketChannel();
    // 编码器.
    NetworkMessageToByteEncoderHandler encoder = new NetworkMessageToByteEncoderHandler();
    jdkClientSocketChannel.setEncoder(encoder);
    // 解码器.
    NetworkByteToMessageDecoderHandler decoder = new NetworkByteToMessageDecoderHandler();
    jdkClientSocketChannel.setDecoder(decoder);
    // 设置非SSL读写处理器.
    Handler<JdkClientSocketChannel> handler = new ClientChannelReadWriteHandler();
    jdkClientSocketChannel.setHandler(handler);
    jdkClientSocketChannel.setHandlerRead(new ClientChannelReadHandler());
    jdkClientSocketChannel.setHandlerWrite(new ClientChannelWriteHandler());
    // 设置SSL读写处理器.
    // jdkClientSocketChannel.setHandlerReadSsl(new ClientChannelReadSslHandler());
    // jdkClientSocketChannel.setHandlerWriteSsl(new ClientChannelWriteSslHandler());
    // 客户端事件处理任务.
    JdkClientSocketChannelEventRunnable jdkClientSocketChannelWorker =
        new JdkClientSocketChannelEventRunnable(jdkClientSocketChannel);
    // 注册客户端连接事件.
    jdkClientSocketChannelWorker.connectEvent();
    // 启动客户端,等待向服务端发起连接.
    jdkClientSocketChannelWorker.connect(remoteAddress);
    // 提交任务到线程池.
    Future<?> submit = LOG_PRODUCER.submit(jdkClientSocketChannelWorker);
    // 阻塞线程池退出.
    submit.get();
  }
}
