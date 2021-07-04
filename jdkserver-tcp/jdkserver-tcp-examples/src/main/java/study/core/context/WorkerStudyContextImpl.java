package study.core.context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import study.core.future.Handler;
import study.core.future.StudyFuture;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-04 11:29
 * @since 2021-01-04 11:29:00
 */
public class WorkerStudyContextImpl extends AbstractStudyContext implements WorkerContext {
  /** . */
  private final ExecutorService executorService;
  /** . */
  private final ScheduledExecutorService scheduledExecutorService;

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public WorkerStudyContextImpl(
      ExecutorService executorService, final ScheduledExecutorService scheduledExecutorService) {
    this.executorService = executorService;
    this.scheduledExecutorService = scheduledExecutorService;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public void executeInExecutorService(Runnable task) {
    executorService.execute(task);
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public <T> StudyFuture<T> executeInExecutorService(T event, Handler<T> handler) {
    Runnable task = () -> dispatch(event, handler);
    executorService.execute(task);
    return null;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public <T> StudyFuture<T> executeInExecutorServiceWorker(T event, StudyWorker<T> handler) {
    Runnable task = () -> dispatch(event, handler);
    executorService.execute(task);
    return null;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public ScheduledExecutorService getScheduledExecutorService() {
    return scheduledExecutorService;
  }
}
