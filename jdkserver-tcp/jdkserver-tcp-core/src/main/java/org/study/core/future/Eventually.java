package org.study.core.future;

import java.util.function.Function;

class Eventually<T, U> extends Operation<T> implements Listener<T> {

  private final Function<Void, StudyFuture<U>> mapper;

  Eventually(Function<Void, StudyFuture<U>> mapper) {
    super();
    this.mapper = mapper;
  }

  @Override
  public void onSuccess(T value) {
    StudyFutureInternal<U> future;
    try {
      future = (StudyFutureInternal<U>) mapper.apply(null);
    } catch (Throwable e) {
      tryFail(e);
      return;
    }
    future.addListener(
        new Listener<U>() {
          @Override
          public void onSuccess(U ignore) {
            tryComplete(value);
          }

          @Override
          public void onFailure(Throwable ignore) {
            tryComplete(value);
          }
        });
  }

  @Override
  public void onFailure(Throwable failure) {
    StudyFutureInternal<U> future;
    try {
      future = (StudyFutureInternal<U>) mapper.apply(null);
    } catch (Throwable e) {
      tryFail(e);
      return;
    }
    future.addListener(
        new Listener<U>() {
          @Override
          public void onSuccess(U ignore) {
            tryFail(failure);
          }

          @Override
          public void onFailure(Throwable ignore) {
            tryFail(failure);
          }
        });
  }
}
