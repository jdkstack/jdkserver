package org.jdkstack.jdkserver.tcp.core.core.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jdkstack.jdkserver.tcp.core.api.context.Monitor;
import org.jdkstack.jdkserver.tcp.core.api.context.WorkerContext;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelHandler;
import org.jdkstack.jdkserver.tcp.core.context.StudyRejectedPolicy;
import org.jdkstack.jdkserver.tcp.core.context.StudyThreadFactory;
import org.jdkstack.jdkserver.tcp.core.context.ThreadMonitor;
import org.jdkstack.jdkserver.tcp.core.context.WorkerStudyContextImpl;
import org.jdkstack.jdkserver.tcp.core.core.buffer.ChannelInboundBuffer;
import org.jdkstack.jdkserver.tcp.core.core.buffer.ChannelOutboundBuffer;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkMessage;

public class DefaultChannelHandlerContext extends AbstractChannelHandlerContext {
  /** 线程阻塞的最大时间时10秒.如果不超过15秒,打印warn.如果超过15秒打印异常堆栈. */
  private static final Monitor CHECKER = new ThreadMonitor(15000L);
  /** 线程池. */
  private static final ExecutorService READ_PRODUCER =
      new ThreadPoolExecutor(
          1,
          1,
          0,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(5000),
          new StudyThreadFactory("read_producer", CHECKER),
          new StudyRejectedPolicy());

  /** 线程池. CallerRunsPolicy 拒绝策略不丢数据,因为在主线程上执行. */
  private static final ExecutorService READ_CONSUMER =
      new ThreadPoolExecutor(
          1,
          1,
          0,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(5000),
          new StudyThreadFactory("read_consumer", CHECKER),
          new StudyRejectedPolicy());
  /** 线程池. */
  private static final ExecutorService WRITE_PRODUCER =
      new ThreadPoolExecutor(
          1,
          1,
          0,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(5000),
          new StudyThreadFactory("write_producer", CHECKER),
          new StudyRejectedPolicy());

  /** 线程池. CallerRunsPolicy 拒绝策略不丢数据,因为在主线程上执行. */
  private static final ExecutorService WRITE_CONSUMER =
      new ThreadPoolExecutor(
          1,
          1,
          0,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(5000),
          new StudyThreadFactory("write_consumer", CHECKER),
          new StudyRejectedPolicy());
  /** 服务器端的定时调度线程池. */
  private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE =
      new ScheduledThreadPoolExecutor(3, new StudyThreadFactory("study_scheduled", null));
  /** 工作任务上下文. */
  protected static final WorkerContext READ_PRODUCER_CONTEXT =
      new WorkerStudyContextImpl(READ_PRODUCER, SCHEDULED_EXECUTOR_SERVICE);
  /** 工作任务上下文. */
  protected static final WorkerContext READ_CONSUMER_CONTEXT =
      new WorkerStudyContextImpl(READ_CONSUMER, SCHEDULED_EXECUTOR_SERVICE);
  /** 工作任务上下文. */
  protected static final WorkerContext WRITE_PRODUCER_CONTEXT =
      new WorkerStudyContextImpl(WRITE_PRODUCER, SCHEDULED_EXECUTOR_SERVICE);
  /** 工作任务上下文. */
  protected static final WorkerContext WRITE_CONSUMER_CONTEXT =
      new WorkerStudyContextImpl(WRITE_CONSUMER, SCHEDULED_EXECUTOR_SERVICE);

  protected ChannelInboundBuffer<Object> channelInboundBuffer = new ChannelInboundBuffer<>();

  protected ChannelOutboundBuffer<ByteBuffer> channelOutboundBuffer = new ChannelOutboundBuffer<>();

  public DefaultChannelHandlerContext(SocketChannel socketChannel, ChannelHandler channelHandler) {
    super(socketChannel, channelHandler);
  }

  static {
    // 线程监控任务.
    CHECKER.monitor(READ_PRODUCER_CONTEXT);
    // 线程监控任务.
    CHECKER.monitor(READ_CONSUMER_CONTEXT);

    // 线程监控任务.
    CHECKER.monitor(WRITE_PRODUCER_CONTEXT);
    // 线程监控任务.
    CHECKER.monitor(WRITE_CONSUMER_CONTEXT);
  }

  @Override
  public boolean isReadable() {
    return channelInboundBuffer.isReadable();
  }

  @Override
  public void handleRead(final Object msg) throws Exception {
    // 生产.
    READ_PRODUCER_CONTEXT.executeInExecutorService(
        msg,
        event -> {
          // 向队列插入元素.
          NetworkMessage bf = (NetworkMessage) msg;
          channelInboundBuffer.incrementPendingOutboundBytes(bf.getLength());
          channelInboundBuffer.enqueue(msg);
        });
    // 消费.
    READ_CONSUMER_CONTEXT.executeInExecutorService(
        () -> {
          // 从队列获取元素.
          Object poll = channelInboundBuffer.poll();
          NetworkMessage bf = (NetworkMessage) poll;
          channelInboundBuffer.decrementPendingOutboundBytes(bf.getLength());
          if (poll instanceof Message) {
            Message message = (Message) poll;
            readHandler.handle(message);
          } else if (poll instanceof ByteBuffer) {
            //
            ByteBuffer byteBuffer = (ByteBuffer) poll;
            //
          }
        });
  }

  @Override
  public boolean isWritable() {
    return channelOutboundBuffer.isWritable();
  }

  @Override
  public void handleWrite(final ByteBuffer buffer) throws Exception {
    // 生产.
    WRITE_PRODUCER_CONTEXT.executeInExecutorService(
        buffer,
        event -> {
          // 向队列插入元素.
          channelOutboundBuffer.incrementPendingOutboundBytes(buffer.capacity());
          channelOutboundBuffer.enqueue(buffer);
        });
    // 消费.
    WRITE_CONSUMER_CONTEXT.executeInExecutorService(
        () -> {
          // 从队列获取元素.
          ByteBuffer poll = channelOutboundBuffer.poll();
          if (poll != null) {
            try {
              channelOutboundBuffer.decrementPendingOutboundBytes(poll.capacity());
              int write = socketChannel.write(ByteBuffer.wrap(poll.array()));
            } catch (IOException e) {
              //
            }
          }
        });
  }
}
