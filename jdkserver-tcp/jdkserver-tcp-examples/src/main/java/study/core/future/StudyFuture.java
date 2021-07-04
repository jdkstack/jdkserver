package study.core.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public interface StudyFuture<T> extends AsyncResult<T> {

  static <T> StudyFuture<T> succeededFuture() {
    return (StudyFuture<T>) SucceededStudyFuture.EMPTY;
  }

  static <T> StudyFuture<T> succeededFuture(T result) {
    if (result == null) {
      return succeededFuture();
    } else {
      return new SucceededStudyFuture<>(result);
    }
  }

  static <T> StudyFuture<T> failedFuture(Throwable t) {
    return new FailedStudyFuture<>(t);
  }

  static <T> StudyFuture<T> failedFuture(String failureMessage) {
    return new FailedStudyFuture<>(failureMessage);
  }

  boolean tryComplete(T result);

  boolean tryFail(Throwable cause);

  boolean isComplete();

  StudyFuture<T> onComplete(Handler<AsyncResult<T>> handler);

  default StudyFuture<T> onSuccess(Handler<T> handler) {
    return onComplete(
        ar -> {
          if (ar.succeeded()) {
            handler.handle(ar.result());
          }
        });
  }

  default StudyFuture<T> onFailure(Handler<Throwable> handler) {
    return onComplete(
        ar -> {
          if (ar.failed()) {
            handler.handle(ar.cause());
          }
        });
  }

  @Override
  T result();

  @Override
  Throwable cause();

  @Override
  boolean succeeded();

  @Override
  boolean failed();

  default <U> StudyFuture<U> flatMap(Function<T, StudyFuture<U>> mapper) {
    return compose(mapper);
  }

  default <U> StudyFuture<U> compose(Function<T, StudyFuture<U>> mapper) {
    return compose(mapper, StudyFuture::failedFuture);
  }

  default StudyFuture<T> recover(Function<Throwable, StudyFuture<T>> mapper) {
    return compose(StudyFuture::succeededFuture, mapper);
  }

  <U> StudyFuture<U> compose(
      Function<T, StudyFuture<U>> successMapper, Function<Throwable, StudyFuture<U>> failureMapper);

  <U> StudyFuture<U> transform(Function<AsyncResult<T>, StudyFuture<U>> mapper);

  <U> StudyFuture<T> eventually(Function<Void, StudyFuture<U>> mapper);

  <U> StudyFuture<U> map(Function<T, U> mapper);

  <V> StudyFuture<V> map(V value);

  @Override
  default <V> StudyFuture<V> mapEmpty() {
    return (StudyFuture<V>) AsyncResult.super.mapEmpty();
  }

  StudyFuture<T> otherwise(Function<Throwable, T> mapper);

  StudyFuture<T> otherwise(T value);

  default StudyFuture<T> otherwiseEmpty() {
    return (StudyFuture<T>) AsyncResult.super.otherwiseEmpty();
  }

  default CompletionStage<T> toCompletionStage() {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    onComplete(
        ar -> {
          if (ar.succeeded()) {
            completableFuture.complete(ar.result());
          } else {
            completableFuture.completeExceptionally(ar.cause());
          }
        });
    return completableFuture;
  }
}
