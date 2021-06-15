package org.study.network.core.socket;

import org.study.core.future.AsyncResult;
import org.study.core.future.Handler;
import org.study.core.future.StudyFuture;
import org.study.core.promise.StudyPromise;
import org.study.core.promise.StudyPromiseImpl;

public interface Pipe<T> {

  Pipe<T> endOnFailure(boolean end);

  Pipe<T> endOnSuccess(boolean end);

  Pipe<T> endOnComplete(boolean end);

  default StudyFuture<Void> to(WriteStream<T> dst) {
    StudyPromise<Void> promise = new StudyPromiseImpl<>();
    to(dst, promise);
    return promise.future();
  }

  void to(WriteStream<T> dst, Handler<AsyncResult<Void>> completionHandler);

  void close();
}
