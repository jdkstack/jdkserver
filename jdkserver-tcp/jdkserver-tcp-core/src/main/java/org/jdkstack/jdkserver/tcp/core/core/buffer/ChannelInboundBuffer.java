package org.jdkstack.jdkserver.tcp.core.core.buffer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import org.jdkstack.jdkserver.tcp.core.context.Constants;
import org.jdkstack.jdkserver.tcp.core.core.water.ReadBufferWaterMark;

/**
 * 日志消息阻塞队列,主要用于消息的短暂缓冲.
 *
 * <p>入站队列,从socket读取数据后,将读取的数据缓冲在队列中.
 *
 * @param <T> 泛型对象.
 * @author admin
 */
public class ChannelInboundBuffer<T> implements InboundBuffer<T> {
  /** 有界数组阻塞队列,为了避免垃圾回收,采用数组而非链表Queue/Dqueue,数组对处理器的缓存机制更加友好. */
  private final BlockingQueue<T> queue;
  /** 队列初始容量默认2000. */
  private int capacity = Constants.CAPACITY;

  private final ReadBufferWaterMark readBufferWaterMark = new ReadBufferWaterMark();
  private static final AtomicLongFieldUpdater<ChannelInboundBuffer> TOTAL_PENDING_SIZE_UPDATER =
      AtomicLongFieldUpdater.newUpdater(ChannelInboundBuffer.class, "totalPendingSize");
  private static final AtomicIntegerFieldUpdater<ChannelInboundBuffer> UNWRITABLE_UPDATER =
      AtomicIntegerFieldUpdater.newUpdater(ChannelInboundBuffer.class, "unreadable");
  private volatile long totalPendingSize;
  private volatile int unreadable;

  /**
   * 创建一个容量Integer.MAX_VALUE的队列.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public ChannelInboundBuffer() {
    this.queue = new ArrayBlockingQueue<>(this.capacity);
  }

  /**
   * 创建一个容量capacity的队列.
   *
   * <p>Another description after blank line.
   *
   * @param capacity 队列容量.
   * @author admin
   */
  public ChannelInboundBuffer(final int capacity) {
    this.capacity = capacity;
    this.queue = new ArrayBlockingQueue<>(capacity);
  }

  public void incrementPendingOutboundBytes(long size) {
    long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, size);
    if (newWriteBufferSize > readBufferWaterMark.high()) {
      setUnreadable();
    }
  }

  public void decrementPendingOutboundBytes(long size) {
    long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, -size);
    if (newWriteBufferSize < readBufferWaterMark.low()) {
      setReadable();
    }
  }

  public boolean isReadable() {
    return unreadable == 0;
  }

  private void setReadable() {
    for (; ; ) {
      final int oldValue = unreadable;
      final int newValue = oldValue & ~1;
      if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
        if (oldValue != 0 && newValue == 0) {
          //
        }
        break;
      }
    }
  }

  private void setUnreadable() {
    for (; ; ) {
      final int oldValue = unreadable;
      final int newValue = oldValue | 1;
      if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
        if (oldValue == 0) {
          // fireChannelWritabilityChanged();
        }
        break;
      }
    }
  }

  /**
   * 消费端采用poll方法,非阻塞,队列为空时返回空对象.
   *
   * <p>Another description after blank line.
   *
   * @return T .
   * @author admin
   */
  @Override
  public final T poll() {
    try {
      return this.queue.take();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 将数据快速放入列队中,采用阻塞方法put.
   *
   * <p>Another description after blank line.
   *
   * @param logRecord .
   * @author admin
   */
  @Override
  public final void enqueue(final T logRecord) {
    try {
      // 使用阻塞方法将元素插入队列. 天然的背压方式,当队列满后阻塞.
      this.queue.put(logRecord);
    } catch (final InterruptedException interruptedException) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * 实时获取队列元素数量.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public final int size() {
    return this.queue.size();
  }
}
