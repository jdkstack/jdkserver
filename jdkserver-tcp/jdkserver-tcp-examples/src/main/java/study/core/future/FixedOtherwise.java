package study.core.future;

class FixedOtherwise<T> extends Operation<T> implements Listener<T> {

  private final T value;

  FixedOtherwise(T value) {
    super();
    this.value = value;
  }

  @Override
  public void onSuccess(T value) {
    tryComplete(value);
  }

  @Override
  public void onFailure(Throwable failure) {
    tryComplete(value);
  }
}
