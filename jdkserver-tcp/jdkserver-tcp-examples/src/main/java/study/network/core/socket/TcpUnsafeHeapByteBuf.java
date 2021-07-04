package study.network.core.socket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledUnsafeHeapByteBuf;

final class TcpUnsafeHeapByteBuf extends UnpooledUnsafeHeapByteBuf {

  public TcpUnsafeHeapByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
    super(alloc, initialCapacity, maxCapacity);
  }

  @Override
  public ByteBuf retain(int increment) {
    return this;
  }

  @Override
  public ByteBuf retain() {
    return this;
  }

  @Override
  public ByteBuf touch() {
    return this;
  }

  @Override
  public ByteBuf touch(Object hint) {
    return this;
  }

  @Override
  public boolean release() {
    return false;
  }

  @Override
  public boolean release(int decrement) {
    return false;
  }
}
