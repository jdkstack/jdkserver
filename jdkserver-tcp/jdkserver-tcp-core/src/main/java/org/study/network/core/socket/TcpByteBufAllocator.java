package org.study.network.core.socket;

import io.netty.buffer.AbstractByteBufAllocator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.internal.PlatformDependent;

abstract class TcpByteBufAllocator extends AbstractByteBufAllocator {

  private static TcpByteBufAllocator UNSAFE_IMPL =
      new TcpByteBufAllocator() {
        @Override
        protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
          return new TcpUnsafeHeapByteBuf(this, initialCapacity, maxCapacity);
        }
      };

  private static TcpByteBufAllocator IMPL =
      new TcpByteBufAllocator() {
        @Override
        protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
          return new TcpHeapByteBuf(this, initialCapacity, maxCapacity);
        }
      };

  static final TcpByteBufAllocator DEFAULT = PlatformDependent.hasUnsafe() ? UNSAFE_IMPL : IMPL;

  @Override
  protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity) {
    return UnpooledByteBufAllocator.DEFAULT.directBuffer(initialCapacity, maxCapacity);
  }

  @Override
  public boolean isDirectBufferPooled() {
    return false;
  }
}
