package study.core.context;

import io.netty.channel.EventLoop;
import study.core.future.Handler;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-04 11:27
 * @since 2021-01-04 11:27:00
 */
public class StudyContextImpl extends AbstractStudyContext implements StudyContext {

  protected EventLoop eventLoop;

  public StudyContextImpl(EventLoop eventLoop) {
    this.eventLoop = eventLoop;
  }

  public void executeInEventLoop(Runnable task) {
    if (eventLoop.inEventLoop()) {
      task.run();
    } else {
      eventLoop.execute(() -> this.dispatch(task));
    }
  }

  public <T> void executeInEventLoop(T argument, Handler<T> task) {
    if (eventLoop.inEventLoop()) {
      task.handle(argument);
    } else {
      eventLoop.execute(() -> this.dispatch(argument, task));
    }
  }
}
