package org.jdkstack.jdkserver.tcp.core.future;

import java.util.function.Function;

class Transformation<T, U> extends Operation<U> implements Listener<T> {

  private final StudyFuture<T> future;
  private final Function<AsyncResult<T>, StudyFuture<U>> mapper;

  Transformation(StudyFuture<T> future, Function<AsyncResult<T>, StudyFuture<U>> mapper) {
    super();
    this.future = future;
    this.mapper = mapper;
  }

  @Override
  public void onSuccess(T value) {
    StudyFutureInternal<U> future;
    try {
      future = (StudyFutureInternal<U>) mapper.apply(this.future);
    } catch (Throwable e) {
      tryFail(e);
      return;
    }
    future.addListener(newListener());
  }

  @Override
  public void onFailure(Throwable failure) {
    StudyFutureInternal<U> future;
    try {
      future = (StudyFutureInternal<U>) mapper.apply(this.future);
    } catch (Throwable e) {
      tryFail(e);
      return;
    }
    future.addListener(newListener());
  }

  private Listener<U> newListener() {
    return new Listener<U>() {
      @Override
      public void onSuccess(U value) {
        tryComplete(value);
      }

      @Override
      public void onFailure(Throwable failure) {
        tryFail(failure);
      }
    };
  }
}
