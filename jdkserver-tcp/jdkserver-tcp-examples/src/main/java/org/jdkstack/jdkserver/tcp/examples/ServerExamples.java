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
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkByteToMessageDecoderHandler;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkMessageToByteEncoderHandler;
import org.jdkstack.jdkserver.tcp.core.core.server.JdkServerSocketChannel;
import org.jdkstack.jdkserver.tcp.core.core.server.JdkServerSocketChannelEventRunnable;
import org.jdkstack.jdkserver.tcp.core.core.server.bridge.BridgeChannelHandler;
import org.jdkstack.jdkserver.tcp.core.core.server.bridge.BridgeChannelReadHandler;
import org.jdkstack.jdkserver.tcp.core.core.server.bridge.BridgeChannelReadWriteHandler;
import org.jdkstack.jdkserver.tcp.core.core.server.bridge.BridgeChannelWriteHandler;
import org.jdkstack.jdkserver.tcp.core.core.server.bridge.JdkBridgeSocketChannel;

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
    //  System.setProperty("javax.net.debug", "all");
    SocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 20000);
    //
    JdkServerSocketChannel serverSocketChannel = new JdkServerSocketChannel();
    // 编码器.
    NetworkMessageToByteEncoderHandler encoder = new NetworkMessageToByteEncoderHandler();
    serverSocketChannel.setEncoder(encoder);
    // 解码器.
    NetworkByteToMessageDecoderHandler decoder = new NetworkByteToMessageDecoderHandler();
    serverSocketChannel.setDecoder(decoder);
    // 设置非SSL读写处理器.
    Handler<JdkBridgeSocketChannel> handler = new BridgeChannelReadWriteHandler();
    serverSocketChannel.setHandler(handler);
    BridgeChannelHandler bridgeChannelHandler = new BridgeChannelHandler(handler);
    serverSocketChannel.setBridgeChannelHandler(bridgeChannelHandler);
    serverSocketChannel.setHandlerRead(new BridgeChannelReadHandler());
    serverSocketChannel.setHandlerWrite(new BridgeChannelWriteHandler());
    // 设置SSL读写处理器.
    // serverSocketChannel.setHandlerReadSsl(new BridgeChannelReadSslHandler());
    // serverSocketChannel.setHandlerWriteSsl(new BridgeChannelWriteSslHandler());
    // 服务端事件处理任务.
    JdkServerSocketChannelEventRunnable jdkServerSocketChannelEventRunnable =
        new JdkServerSocketChannelEventRunnable(serverSocketChannel);
    // 启动服务端,等待客户端连接.backlog=50.
    jdkServerSocketChannelEventRunnable.bind(remoteAddress, 50);
    // 注册accept事件.
    jdkServerSocketChannelEventRunnable.acceptEvent();
    // 将任务提交到线程池.
    Future<?> submit = LOG_PRODUCER.submit(jdkServerSocketChannelEventRunnable);
    // 阻塞线程池退出.
    submit.get();
  }
}
