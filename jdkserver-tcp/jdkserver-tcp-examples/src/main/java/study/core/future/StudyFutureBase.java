package study.core.future;

import java.util.Objects;
import java.util.function.Function;

abstract class StudyFutureBase<T> implements StudyFutureInternal<T> {

  StudyFutureBase() {}

  protected final void emitSuccess(T value, Listener<T> listener) {

    listener.onSuccess(value);
  }

  protected final void emitFailure(Throwable cause, Listener<T> listener) {
    listener.onFailure(cause);
  }

  protected final <U> void emit(U value, Handler<U> handler) {
    handler.handle(value);
  }

  @Override
  public <U> StudyFuture<U> compose(
      Function<T, StudyFuture<U>> successMapper,
      Function<Throwable, StudyFuture<U>> failureMapper) {
    Objects.requireNonNull(successMapper, "No null success mapper accepted");
    Objects.requireNonNull(failureMapper, "No null failure mapper accepted");
    Composition<T, U> operation = new Composition<>(successMapper, failureMapper);
    addListener(operation);
    return operation;
  }

  @Override
  public <U> StudyFuture<U> transform(Function<AsyncResult<T>, StudyFuture<U>> mapper) {
    Objects.requireNonNull(mapper, "No null mapper accepted");
    Transformation<T, U> operation = new Transformation<>(this, mapper);
    addListener(operation);
    return operation;
  }

  @Override
  public <U> StudyFuture<T> eventually(Function<Void, StudyFuture<U>> mapper) {
    Objects.requireNonNull(mapper, "No null mapper accepted");
    Eventually<T, U> operation = new Eventually<>(mapper);
    addListener(operation);
    return operation;
  }

  @Override
  public <U> StudyFuture<U> map(Function<T, U> mapper) {
    Objects.requireNonNull(mapper, "No null mapper accepted");
    Mapping<T, U> operation = new Mapping<>(mapper);
    addListener(operation);
    return operation;
  }

  @Override
  public <V> StudyFuture<V> map(V value) {
    FixedMapping<T, V> transformation = new FixedMapping<>(value);
    addListener(transformation);
    return transformation;
  }

  @Override
  public StudyFuture<T> otherwise(Function<Throwable, T> mapper) {
    Objects.requireNonNull(mapper, "No null mapper accepted");
    Otherwise<T> transformation = new Otherwise<>(mapper);
    addListener(transformation);
    return transformation;
  }

  @Override
  public StudyFuture<T> otherwise(T value) {
    FixedOtherwise<T> operation = new FixedOtherwise<>(value);
    addListener(operation);
    return operation;
  }

  @Override
  public boolean tryComplete(T result) {
    return false;
  }

  @Override
  public boolean tryFail(Throwable cause) {
    return false;
  }
}
