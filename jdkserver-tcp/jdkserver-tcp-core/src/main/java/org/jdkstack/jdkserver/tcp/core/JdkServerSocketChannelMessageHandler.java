package org.jdkstack.jdkserver.tcp.core;

import java.nio.channels.SelectionKey;
import org.jdkstack.jdkserver.tcp.core.channel.queue.MessageQueue;
import org.jdkstack.jdkserver.tcp.core.channel.queue.ProducerWorker;
import org.jdkstack.jdkserver.tcp.core.channel.queue.SelectionKeyQueue;
import org.jdkstack.jdkserver.tcp.core.channel.queue.StudyQueue;
import org.jdkstack.jdkserver.tcp.core.channel.queue.StudyWorker;
import org.jdkstack.jdkserver.tcp.core.tcp.server.JdkServerSocketChannelWorker;

/**
 * This is a method description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public class JdkServerSocketChannelMessageHandler extends AbstractHandler {
  /** . */
  private Runnable consumerRunnable;
  /** . */
  private final StudyQueue<SelectionKey> fileQueue;

  /** 生产日志处理器. */
  private final StudyWorker<SelectionKey> producerWorker;

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public JdkServerSocketChannelMessageHandler() {
    // 动态配置队列属性.
    this.fileQueue = new SelectionKeyQueue();
    this.producerWorker = new ProducerWorker(this.fileQueue);
    // this.consumerRunnable = new JdkServerSocketChannelWorker();
  }

  /**
   * JDK会调用这个方法.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public final void publish(final SelectionKey logRecord) {
    // 启动一个线程,开始生产日志.(考虑将LogRecord预先格式化成字符串消息,LogRecord对象生命周期结束.)
    LOG_PRODUCER_CONTEXT.executeInExecutorServiceWorker(logRecord, this.producerWorker);
    // 具体业务逻辑.
    LOG_CONSUMER_CONTEXT.executeInExecutorService(this.consumerRunnable);
  }

  @Override
  public int size() {
    return this.fileQueue.size();
  }

  public final void enqueue(final SelectionKey logRecord) {
    this.fileQueue.enqueue(logRecord);
  }

  public final SelectionKey poll() {
    return this.fileQueue.poll();
  }
}
