package org.jdkstack.jdkserver.tcp.core.channel.water;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public class ReadBufferWaterMark extends AbstractWaterBufferMark {
  private static final int DEFAULT_LOW_WATER_MARK = 32 * 1024;
  private static final int DEFAULT_HIGH_WATER_MARK = 64 * 1024;
  private static final int DEFAULT_TYPE = 1;

  private final int low;
  private final int high;
  private final int type;
  /** 未来可以考虑使用AtomicLongFieldUpdater. */
  private final AtomicLong count = new AtomicLong(0L);
  /** 未来可以考虑使用AtomicLongFieldUpdater. */
  private final AtomicLong size = new AtomicLong(0L);
  /** 控制写水位的锁. */
  private final ReentrantLock lock = new ReentrantLock();
  /** Condition for waiting takes */
  private final Condition countLimitLockCondition = lock.newCondition();

  private final AtomicBoolean isWritable = new AtomicBoolean(true);
  private final AtomicBoolean isReadable = new AtomicBoolean(true);

  public ReadBufferWaterMark() {
    this.low = DEFAULT_LOW_WATER_MARK;
    this.high = DEFAULT_HIGH_WATER_MARK;
    this.type = DEFAULT_TYPE;
  }

  ReadBufferWaterMark(int low, int high, int type) {
    this.low = low;
    this.high = high;
    this.type = type;
  }

  public int low() {
    return low;
  }

  public int high() {
    return high;
  }

  public synchronized boolean isWritable() {
    return isWritable.get();
  }

  public AtomicLong getCount() {
    return count;
  }

  public boolean checkHigh() {
    int countHigh = COUNT_WATER_MARK.high();
    long l = count.get();
    if (l >= countHigh) {
      isWritable.set(false);
      return false;
    }
    return true;
  }

  public boolean checkLow() {
    int countLow = COUNT_WATER_MARK.low();
    long l = count.get();
    if (l < countLow) {
      isWritable.set(true);
      return true;
    }
    return false;
  }

  public void await(String message) {
    // 按照条件阻塞生产.
    lock.lock();
    try {
      while (!isReadable()) {
        countLimitLockCondition.await();
      }
      count.incrementAndGet();
      byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
      int i = bytes.length;
      size.addAndGet(i);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      lock.unlock();
    }
  }

  public boolean isReadable() {
    int countHigh = COUNT_WATER_MARK.high();
    int sizeHigh = SIZE_WATER_MARK.high();
    long l = count.get();
    long l1 = size.get();
    if (l >= countHigh || l1 >= sizeHigh) {
      isReadable.set(false);
      return false;
    } else {
      isReadable.set(true);
      return true;
    }
  }

  @Override
  public String toString() {
    StringBuilder builder =
        new StringBuilder(20)
            .append("ReadBufferWaterMark(low: ")
            .append(low)
            .append(", high: ")
            .append(high)
            .append(", type: ")
            .append(type)
            .append(")");
    return builder.toString();
  }
}
