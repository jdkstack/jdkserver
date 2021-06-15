package org.study.core.future;

import java.util.function.Function;

class Composition<T, U> extends Operation<U> implements Listener<T> {

  private final Function<T, StudyFuture<U>> successMapper;
  private final Function<Throwable, StudyFuture<U>> failureMapper;

  Composition(
      Function<T, StudyFuture<U>> successMapper,
      Function<Throwable, StudyFuture<U>> failureMapper) {
    super();
    this.successMapper = successMapper;
    this.failureMapper = failureMapper;
  }

  @Override
  public void onSuccess(T value) {
    StudyFutureInternal<U> future;
    try {
      future = (StudyFutureInternal<U>) successMapper.apply(value);
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
      future = (StudyFutureInternal<U>) failureMapper.apply(failure);
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
