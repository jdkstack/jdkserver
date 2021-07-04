package study.network.core.socket;

import study.core.future.AsyncResult;
import study.core.future.Handler;
import study.core.future.StudyFuture;
import study.core.promise.StudyPromise;
import study.core.promise.StudyPromiseImpl;

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
