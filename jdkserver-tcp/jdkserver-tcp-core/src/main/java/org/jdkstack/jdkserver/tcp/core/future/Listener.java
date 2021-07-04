package org.jdkstack.jdkserver.tcp.core.future;

public interface Listener<T> extends Handler<AsyncResult<T>> {

  default void handle(AsyncResult<T> ar) {
    if (ar.succeeded()) {
      onSuccess(ar.result());
    } else {
      onFailure(ar.cause());
    }
  }

  void onSuccess(T value);

  void onFailure(Throwable failure);
}
