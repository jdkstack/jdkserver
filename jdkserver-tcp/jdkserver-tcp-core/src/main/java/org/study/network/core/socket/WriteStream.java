package org.study.network.core.socket;

import org.study.core.future.AsyncResult;
import org.study.core.future.Handler;
import org.study.core.future.StudyFuture;
import org.study.core.promise.StudyPromise;
import org.study.core.promise.StudyPromiseImpl;

/**
 * 写流.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public interface WriteStream<T> {

  WriteStream<T> exceptionHandler(Handler<Throwable> handler);

  StudyFuture<Void> write(T data);

  void write(T data, Handler<AsyncResult<Void>> handler);

  default StudyFuture<Void> end() {
    StudyPromise<Void> provide = new StudyPromiseImpl();
    end(provide);
    return provide.future();
  }

  void end(Handler<AsyncResult<Void>> handler);

  default StudyFuture<Void> end(T data) {
    StudyPromise<Void> provide = new StudyPromiseImpl();
    end(data, provide);
    return provide.future();
  }

  default void end(T data, Handler<AsyncResult<Void>> handler) {
    if (handler != null) {
      write(
          data,
          ar -> {
            if (ar.succeeded()) {
              end(handler);
            } else {
              handler.handle(ar);
            }
          });
    } else {
      end(data);
    }
  }

  WriteStream<T> setWriteQueueMaxSize(int maxSize);

  boolean writeQueueFull();

  WriteStream<T> drainHandler(Handler<Void> handler);
}
