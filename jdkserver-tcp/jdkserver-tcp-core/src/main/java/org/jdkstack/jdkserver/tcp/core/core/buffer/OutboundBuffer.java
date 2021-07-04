package org.jdkstack.jdkserver.tcp.core.core.buffer;

public interface OutboundBuffer<T> {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return 返回队列中元素的数量.
   * @author admin
   */
  int size();

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return T .
   * @author admin
   */
  T poll();

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param logRecord logRecord.
   * @author admin
   */
  void enqueue(T logRecord);
}
