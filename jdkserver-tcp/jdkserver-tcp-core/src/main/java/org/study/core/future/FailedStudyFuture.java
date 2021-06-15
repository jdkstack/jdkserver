package org.study.core.future;

import java.util.function.Function;

public final class FailedStudyFuture<T> extends StudyFutureBase<T> {

  private final Throwable cause;

  public FailedStudyFuture(Throwable t) {
    this.cause = t != null ? t : new NoStackTraceThrowable(null);
  }

  public FailedStudyFuture(String failureMessage) {

    this.cause = new NoStackTraceThrowable(failureMessage);
  }

  @Override
  public boolean isComplete() {
    return true;
  }

  @Override
  public StudyFuture<T> onComplete(Handler<AsyncResult<T>> handler) {
    if (handler instanceof Listener) {
      emitFailure(cause, (Listener<T>) handler);
    } else {
      emit(this, handler);
    }
    return this;
  }

  @Override
  public StudyFuture<T> onSuccess(Handler<T> handler) {
    return this;
  }

  @Override
  public StudyFuture<T> onFailure(Handler<Throwable> handler) {
    emit(cause, handler);
    return this;
  }

  @Override
  public void addListener(Listener<T> listener) {
    emitFailure(cause, listener);
  }

  @Override
  public T result() {
    return null;
  }

  @Override
  public Throwable cause() {
    return cause;
  }

  @Override
  public boolean succeeded() {
    return false;
  }

  @Override
  public boolean failed() {
    return true;
  }

  @Override
  public <U> StudyFuture<U> map(Function<T, U> mapper) {
    return (StudyFuture<U>) this;
  }

  @Override
  public <V> StudyFuture<V> map(V value) {
    return (StudyFuture<V>) this;
  }

  @Override
  public StudyFuture<T> otherwise(T value) {
    return new SucceededStudyFuture<>(value);
  }

  @Override
  public String toString() {
    return "Future{cause=" + cause.getMessage() + "}";
  }
}
