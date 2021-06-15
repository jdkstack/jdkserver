package org.study.core.future;

import java.util.Objects;
import java.util.function.Function;

public final class SucceededStudyFuture<T> extends StudyFutureBase<T> {

  public static final SucceededStudyFuture EMPTY = new SucceededStudyFuture(null);

  private final T result;

  public SucceededStudyFuture(T result) {
    this.result = result;
  }

  @Override
  public boolean isComplete() {
    return true;
  }

  @Override
  public StudyFuture<T> onSuccess(Handler<T> handler) {
    emit(result, handler);
    return this;
  }

  @Override
  public StudyFuture<T> onFailure(Handler<Throwable> handler) {
    return this;
  }

  @Override
  public StudyFuture<T> onComplete(Handler<AsyncResult<T>> handler) {
    if (handler instanceof Listener) {
      emitSuccess(result, (Listener<T>) handler);
    } else {
      emit(this, handler);
    }
    return this;
  }

  @Override
  public void addListener(Listener<T> listener) {
    emitSuccess(result, listener);
  }

  @Override
  public T result() {
    return result;
  }

  @Override
  public Throwable cause() {
    return null;
  }

  @Override
  public boolean succeeded() {
    return true;
  }

  @Override
  public boolean failed() {
    return false;
  }

  @Override
  public <V> StudyFuture<V> map(V value) {
    return new SucceededStudyFuture<>(value);
  }

  @Override
  public StudyFuture<T> otherwise(Function<Throwable, T> mapper) {
    Objects.requireNonNull(mapper, "No null mapper accepted");
    return this;
  }

  @Override
  public StudyFuture<T> otherwise(T value) {
    return this;
  }

  @Override
  public String toString() {
    return "Future{result=" + result + "}";
  }
}
