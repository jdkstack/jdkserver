package org.jdkstack.jdkserver.tcp.core.context;

import java.util.concurrent.ScheduledExecutorService;
import org.jdkstack.jdkserver.tcp.core.future.Handler;
import org.jdkstack.jdkserver.tcp.core.future.StudyFuture;

/**
 * This is a method description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public interface WorkerContext {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param task 任务.
   * @author admin
   */
  void executeInExecutorService(Runnable task);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param event 处理对象.
   * @param handler 处理器.
   * @return StudyFuture.
   * @author admin
   */
  <T> StudyFuture<T> executeInExecutorService(T event, Handler<T> handler);

  <T> StudyFuture<T> executeInExecutorServiceWorker(T event, StudyWorker<T> handler);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return ScheduledExecutorService.
   * @author admin
   */
  ScheduledExecutorService getScheduledExecutorService();
}
