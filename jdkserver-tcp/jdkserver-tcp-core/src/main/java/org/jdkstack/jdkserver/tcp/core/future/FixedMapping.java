package org.jdkstack.jdkserver.tcp.core.future;

class FixedMapping<T, U> extends Operation<U> implements Listener<T> {

  private final U value;

  FixedMapping(U value) {
    super();
    this.value = value;
  }

  @Override
  public void onSuccess(T value) {
    tryComplete(this.value);
  }

  @Override
  public void onFailure(Throwable failure) {
    tryFail(failure);
  }
}
