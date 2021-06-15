package org.jdkstack.jdkserver.tcp.core.channel.queue;

import java.nio.channels.SelectionKey;

/**
 * This is a method description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public class ProducerWorker implements StudyWorker<SelectionKey> {
  /** . */
  private final StudyQueue<? super SelectionKey> studyQueue;

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param studyQueue 放入队列中的元素.
   * @author admin
   */
  public ProducerWorker(final StudyQueue<? super SelectionKey> studyQueue) {
    this.studyQueue = studyQueue;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param logRecord 放入队列中的元素.
   * @author admin
   */
  @Override
  public final void handle(final SelectionKey logRecord) {
    this.studyQueue.enqueue(logRecord);
  }
}
