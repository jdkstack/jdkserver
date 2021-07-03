package org.jdkstack.jdkserver.tcp.core.channel.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.FileRegion;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import org.jdkstack.jdkserver.tcp.core.channel.queue.MessageQueue;
import org.jdkstack.jdkserver.tcp.core.channel.queue.StudyQueue;

public final class ChannelOutboundBuffer implements OutboundBuffer {

  private static final int MESSAGE_OVERHEAD = 96;

  private static final StudyQueue<Object> MESSAGE_QUEUE = new MessageQueue("MessageQueue");
  private static final AtomicLongFieldUpdater<ChannelOutboundBuffer> TOTAL_PENDING_SIZE_UPDATER =
      AtomicLongFieldUpdater.newUpdater(ChannelOutboundBuffer.class, "totalPendingSize");
  private static final AtomicIntegerFieldUpdater<ChannelOutboundBuffer> UNWRITABLE_UPDATER =
      AtomicIntegerFieldUpdater.newUpdater(ChannelOutboundBuffer.class, "unwritable");
  private int flushed;
  private int nioBufferCount;
  private long nioBufferSize;
  private boolean inFail;
  private volatile long totalPendingSize;
  private volatile int unwritable;

  private volatile Runnable fireChannelWritabilityChangedTask;

  private static long total(Object msg) {
    if (msg instanceof ByteBuf) {
      return ((ByteBuf) msg).readableBytes();
    }
    if (msg instanceof FileRegion) {
      return ((FileRegion) msg).count();
    }
    if (msg instanceof ByteBufHolder) {
      return ((ByteBufHolder) msg).content().readableBytes();
    }
    return -1;
  }

  private static ByteBuffer[] expandNioBufferArray(ByteBuffer[] array, int neededSpace, int size) {
    int newCapacity = array.length;
    do {
      // double capacity until it is big enough
      // See https://github.com/netty/netty/issues/1890
      newCapacity <<= 1;

      if (newCapacity < 0) {
        throw new IllegalStateException();
      }

    } while (neededSpace > newCapacity);

    ByteBuffer[] newArray = new ByteBuffer[newCapacity];
    System.arraycopy(array, 0, newArray, 0, size);

    return newArray;
  }

  private static int writabilityMask(int index) {
    if (index < 1 || index > 31) {
      throw new IllegalArgumentException("index: " + index + " (expected: 1~31)");
    }
    return 1 << index;
  }

  public void addMessage(Object msg, int size) {
    MESSAGE_QUEUE.enqueue(msg);
    incrementPendingOutboundBytes(1, false);
  }

  void incrementPendingOutboundBytes(long size) {
    incrementPendingOutboundBytes(size, true);
  }

  private void incrementPendingOutboundBytes(long size, boolean invokeLater) {
    if (size == 0) {
      return;
    }

    long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, size);
    /*  if (newWriteBufferSize > channel.config().getWriteBufferHighWaterMark()) {
      setUnwritable(invokeLater);
    }*/
  }

  public void decrementPendingOutboundBytes(long size) {
    decrementPendingOutboundBytes(size, true, true);
  }

  private void decrementPendingOutboundBytes(
      long size, boolean invokeLater, boolean notifyWritability) {
    if (size == 0) {
      return;
    }

    long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, -size);
    /* if (notifyWritability && newWriteBufferSize < channel.config().getWriteBufferLowWaterMark()) {
      setWritable(invokeLater);
    }*/
  }

  public Object current() {

    return null;
  }

  public long currentProgress() {

    return 0;
  }

  public void progress(long amount) {}

  public boolean remove() {

    return true;
  }

  public boolean remove(Throwable cause) {
    return remove0(cause, true);
  }

  private boolean remove0(Throwable cause, boolean notifyWritability) {

    return true;
  }

  public void removeBytes(long writtenBytes) {
    for (; ; ) {
      Object msg = current();
      if (!(msg instanceof ByteBuf)) {
        assert writtenBytes == 0;
        break;
      }

      final ByteBuf buf = (ByteBuf) msg;
      final int readerIndex = buf.readerIndex();
      final int readableBytes = buf.writerIndex() - readerIndex;

      if (readableBytes <= writtenBytes) {
        if (writtenBytes != 0) {
          progress(readableBytes);
          writtenBytes -= readableBytes;
        }
        remove();
      } else { // readableBytes > writtenBytes
        if (writtenBytes != 0) {
          buf.readerIndex(readerIndex + (int) writtenBytes);
          progress(writtenBytes);
        }
        break;
      }
    }
    clearNioBuffers();
  }

  // Clear all ByteBuffer from the array so these can be GC'ed.
  // See https://github.com/netty/netty/issues/3837
  private void clearNioBuffers() {
    int count = nioBufferCount;
    if (count > 0) {
      nioBufferCount = 0;
    }
  }

  public ByteBuffer[] nioBuffers() {
    return nioBuffers(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  public ByteBuffer[] nioBuffers(int maxCount, long maxBytes) {
    assert maxCount > 0;
    assert maxBytes > 0;
    long nioBufferSize = 0;
    int nioBufferCount = 0;

    this.nioBufferCount = nioBufferCount;
    this.nioBufferSize = nioBufferSize;

    return null;
  }

  public int nioBufferCount() {
    return nioBufferCount;
  }

  public long nioBufferSize() {
    return nioBufferSize;
  }

  public boolean isWritable() {
    return unwritable == 0;
  }

  private void setWritable(boolean invokeLater) {
    for (; ; ) {
      final int oldValue = unwritable;
      final int newValue = oldValue & ~1;
      if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
        if (oldValue != 0 && newValue == 0) {
          fireChannelWritabilityChanged(invokeLater);
        }
        break;
      }
    }
  }

  public boolean getUserDefinedWritability(int index) {
    return (unwritable & writabilityMask(index)) == 0;
  }

  public void setUserDefinedWritability(int index, boolean writable) {
    if (writable) {
      setUserDefinedWritability(index);
    } else {
      clearUserDefinedWritability(index);
    }
  }

  private void setUserDefinedWritability(int index) {
    final int mask = ~writabilityMask(index);
    for (; ; ) {
      final int oldValue = unwritable;
      final int newValue = oldValue & mask;
      if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
        if (oldValue != 0 && newValue == 0) {
          fireChannelWritabilityChanged(true);
        }
        break;
      }
    }
  }

  private void clearUserDefinedWritability(int index) {
    final int mask = writabilityMask(index);
    for (; ; ) {
      final int oldValue = unwritable;
      final int newValue = oldValue | mask;
      if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
        if (oldValue == 0 && newValue != 0) {
          fireChannelWritabilityChanged(true);
        }
        break;
      }
    }
  }

  private void setUnwritable(boolean invokeLater) {
    for (; ; ) {
      final int oldValue = unwritable;
      final int newValue = oldValue | 1;
      if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
        if (oldValue == 0) {
          fireChannelWritabilityChanged(invokeLater);
        }
        break;
      }
    }
  }

  private void fireChannelWritabilityChanged(boolean invokeLater) {}

  public int size() {
    return flushed;
  }

  public boolean isEmpty() {
    return flushed == 0;
  }

  void failFlushed(Throwable cause, boolean notify) {
    // Make sure that this method does not reenter.  A listener added to the current promise can be
    // notified by the
    // current thread in the tryFailure() call of the loop below, and the listener can trigger
    // another fail() call
    // indirectly (usually by closing the channel.)
    //
    // See https://github.com/netty/netty/issues/1501
    if (inFail) {
      return;
    }

    try {
      inFail = true;
      for (; ; ) {
        if (!remove0(cause, notify)) {
          break;
        }
      }
    } finally {
      inFail = false;
    }
  }

  public long totalPendingWriteBytes() {
    return totalPendingSize;
  }
}
