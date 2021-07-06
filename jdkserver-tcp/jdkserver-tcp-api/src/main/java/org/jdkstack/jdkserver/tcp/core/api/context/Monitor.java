package org.jdkstack.jdkserver.tcp.core.api.context;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-02-24 11:04
 * @since 2021-02-24 11:04:00
 */
public interface Monitor {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  void monitor(final WorkerContext context);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param thread 线程.
   * @author admin
   */
  default void registerThread(final StudyThreadImpl thread) {
    //
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  void close();
}
