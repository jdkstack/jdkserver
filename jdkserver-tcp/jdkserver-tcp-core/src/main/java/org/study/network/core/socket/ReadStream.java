package org.study.network.core.socket;

import org.study.core.future.AsyncResult;
import org.study.core.future.Handler;
import org.study.core.future.StudyFuture;
import org.study.core.promise.StudyPromise;
import org.study.core.promise.StudyPromiseImpl;

public interface ReadStream<T> {

  ReadStream<T> exceptionHandler(Handler<Throwable> handler);

  ReadStream<T> handler(Handler<T> handler);

  ReadStream<T> pause();

  ReadStream<T> resume();

  ReadStream<T> fetch(long amount);

  ReadStream<T> endHandler(Handler<Void> endHandler);

  default Pipe<T> pipe() {
    pause();
    return new PipeImpl<>(this);
  }

  default StudyFuture<Void> pipeTo(WriteStream<T> dst) {
    StudyPromise<Void> provide = new StudyPromiseImpl();
    new PipeImpl<>(this).to(dst, provide);
    return provide.future();
  }

  default void pipeTo(WriteStream<T> dst, Handler<AsyncResult<Void>> handler) {
    new PipeImpl<>(this).to(dst, handler);
  }
}
