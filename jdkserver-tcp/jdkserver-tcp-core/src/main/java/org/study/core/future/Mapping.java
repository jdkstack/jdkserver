package org.study.core.future;

import java.util.function.Function;

class Mapping<T, U> extends Operation<U> implements Listener<T> {

  private final Function<T, U> successMapper;

  Mapping(Function<T, U> successMapper) {
    super();
    this.successMapper = successMapper;
  }

  @Override
  public void onSuccess(T value) {
    U result;
    try {
      result = successMapper.apply(value);
    } catch (Throwable e) {
      tryFail(e);
      return;
    }
    tryComplete(result);
  }

  @Override
  public void onFailure(Throwable failure) {
    tryFail(failure);
  }
}
