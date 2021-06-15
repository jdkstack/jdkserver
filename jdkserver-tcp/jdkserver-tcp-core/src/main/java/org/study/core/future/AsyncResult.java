package org.study.core.future;

import java.util.function.Function;

/** 包装异步的结果 */
public interface AsyncResult<T> {

  /** 如果为空,代表操作失败. */
  T result();

  /** 如果为空,代表操作成功. */
  Throwable cause();

  /** 成功. */
  boolean succeeded();

  /** 失败. */
  boolean failed();

  default <U> AsyncResult<U> map(Function<T, U> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    return new AsyncResult<U>() {
      @Override
      public U result() {
        if (succeeded()) {
          return mapper.apply(AsyncResult.this.result());
        } else {
          return null;
        }
      }

      @Override
      public Throwable cause() {
        return AsyncResult.this.cause();
      }

      @Override
      public boolean succeeded() {
        return AsyncResult.this.succeeded();
      }

      @Override
      public boolean failed() {
        return AsyncResult.this.failed();
      }
    };
  }

  default <V> AsyncResult<V> map(V value) {
    return map(t -> value);
  }

  default <V> AsyncResult<V> mapEmpty() {
    return map((V) null);
  }

  default AsyncResult<T> otherwise(Function<Throwable, T> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    return new AsyncResult<T>() {
      @Override
      public T result() {
        if (AsyncResult.this.succeeded()) {
          return AsyncResult.this.result();
        } else if (AsyncResult.this.failed()) {
          return mapper.apply(AsyncResult.this.cause());
        } else {
          return null;
        }
      }

      @Override
      public Throwable cause() {
        return null;
      }

      @Override
      public boolean succeeded() {
        return AsyncResult.this.succeeded() || AsyncResult.this.failed();
      }

      @Override
      public boolean failed() {
        return false;
      }
    };
  }

  default AsyncResult<T> otherwise(T value) {
    return otherwise(err -> value);
  }

  default AsyncResult<T> otherwiseEmpty() {
    return otherwise(err -> null);
  }
}
