package org.jdkstack.jdkserver.tcp.core;

import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicLong;
import org.jdkstack.jdkserver.tcp.core.channel.queue.StudyQueue;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public interface Handler {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param logRecord 日志对象.
   * @author admin
   */
  void publish(SelectionKey logRecord);

  int size();
}
