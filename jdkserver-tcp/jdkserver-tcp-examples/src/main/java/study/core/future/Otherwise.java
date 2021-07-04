package study.core.future;

import java.util.function.Function;

class Otherwise<T> extends Operation<T> implements Listener<T> {

  private final Function<Throwable, T> mapper;

  Otherwise(Function<Throwable, T> mapper) {
    super();
    this.mapper = mapper;
  }

  @Override
  public void onSuccess(T value) {
    tryComplete(value);
  }

  @Override
  public void onFailure(Throwable failure) {
    T result;
    try {
      result = mapper.apply(failure);
    } catch (Throwable e) {
      tryFail(e);
      return;
    }
    tryComplete(result);
  }
}
