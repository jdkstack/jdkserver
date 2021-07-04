package study.core.promise;

import study.core.future.AsyncResult;
import study.core.future.Listener;
import study.core.future.StudyFuture;
import study.core.future.StudyFutureImpl;

public final class StudyPromiseImpl<T> extends StudyFutureImpl<T>
    implements StudyPromiseInternal<T>, Listener<T> {

  @Override
  public void handle(AsyncResult<T> ar) {
    if (ar.succeeded()) {
      onSuccess(ar.result());
    } else {
      onFailure(ar.cause());
    }
  }

  @Override
  public void onSuccess(T value) {
    tryComplete(value);
  }

  @Override
  public void onFailure(Throwable failure) {
    tryFail(failure);
  }

  @Override
  public StudyFuture<T> future() {
    return this;
  }

  /*  @Override
  public void operationComplete(io.netty.util.concurrent.Future<T> future) throws Exception {
    if (future.isSuccess()) {
      complete(future.getNow());
    } else {
      fail(future.cause());
    }
  }*/
}
