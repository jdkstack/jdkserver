package study.core.promise;

import study.core.future.AsyncResult;
import study.core.future.Handler;
import study.core.future.NoStackTraceThrowable;
import study.core.future.StudyFuture;

public interface StudyPromise<T> extends Handler<AsyncResult<T>> {

  static <T> StudyPromise<T> promise() {
    return new StudyPromiseImpl<>();
  }

  @Override
  default void handle(AsyncResult<T> asyncResult) {
    if (asyncResult.succeeded()) {
      complete(asyncResult.result());
    } else {
      fail(asyncResult.cause());
    }
  }

  default void complete(T result) {
    if (!tryComplete(result)) {
      throw new IllegalStateException("Result is already complete");
    }
  }

  default void complete() {
    if (!tryComplete()) {
      throw new IllegalStateException("Result is already complete");
    }
  }

  default void fail(Throwable cause) {
    if (!tryFail(cause)) {
      throw new IllegalStateException("Result is already complete");
    }
  }

  default void fail(String message) {
    if (!tryFail(message)) {
      throw new IllegalStateException("Result is already complete");
    }
  }

  boolean tryComplete(T result);

  default boolean tryComplete() {
    return tryComplete(null);
  }

  boolean tryFail(Throwable cause);

  default boolean tryFail(String message) {
    return tryFail(new NoStackTraceThrowable(message));
  }

  StudyFuture<T> future();
}
