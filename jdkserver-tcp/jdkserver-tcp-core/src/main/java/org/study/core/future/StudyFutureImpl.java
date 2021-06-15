package org.study.core.future;

import java.util.ArrayList;
import java.util.Objects;

public class StudyFutureImpl<T> extends StudyFutureBase<T> {

  private static final Object NULL_VALUE = new Object();

  private Object value;
  private Listener<T> listener;

  public StudyFutureImpl() {
    super();
  }

  public synchronized T result() {
    return value instanceof Throwable ? null : value == NULL_VALUE ? null : (T) value;
  }

  public synchronized Throwable cause() {
    return value instanceof Throwable ? (Throwable) value : null;
  }

  public synchronized boolean succeeded() {
    return value != null && !(value instanceof Throwable);
  }

  public synchronized boolean failed() {
    return value instanceof Throwable;
  }

  public synchronized boolean isComplete() {
    return value != null;
  }

  @Override
  public StudyFuture<T> onSuccess(Handler<T> handler) {
    Objects.requireNonNull(handler, "No null handler accepted");
    addListener(
        new Listener<T>() {
          @Override
          public void onSuccess(T value) {
            handler.handle(value);
          }

          @Override
          public void onFailure(Throwable failure) {}
        });
    return this;
  }

  @Override
  public StudyFuture<T> onFailure(Handler<Throwable> handler) {
    Objects.requireNonNull(handler, "No null handler accepted");
    addListener(
        new Listener<T>() {
          @Override
          public void onSuccess(T value) {}

          @Override
          public void onFailure(Throwable failure) {
            handler.handle(failure);
          }
        });
    return this;
  }

  @Override
  public StudyFuture<T> onComplete(Handler<AsyncResult<T>> handler) {
    Objects.requireNonNull(handler, "No null handler accepted");
    Listener<T> listener;
    if (handler instanceof Listener) {
      listener = (Listener<T>) handler;
    } else {
      listener =
          new Listener<T>() {
            @Override
            public void onSuccess(T value) {
              handler.handle(StudyFutureImpl.this);
            }

            @Override
            public void onFailure(Throwable failure) {
              handler.handle(StudyFutureImpl.this);
            }
          };
    }
    addListener(listener);
    return this;
  }

  @Override
  public void addListener(Listener<T> listener) {
    Object v;
    synchronized (this) {
      v = value;
      if (v == null) {
        if (this.listener == null) {
          this.listener = listener;
        } else {
          ListenerArray<T> listeners;
          if (this.listener instanceof StudyFutureImpl.ListenerArray) {
            listeners = (ListenerArray<T>) this.listener;
          } else {
            listeners = new ListenerArray<>();
            listeners.add(this.listener);
            this.listener = listeners;
          }
          listeners.add(listener);
        }
        return;
      }
    }
    if (v instanceof Throwable) {
      emitFailure((Throwable) v, listener);
    } else {
      if (v == NULL_VALUE) {
        v = null;
      }
      emitSuccess((T) v, listener);
    }
  }

  public boolean tryComplete(T result) {
    Listener<T> l;
    synchronized (this) {
      if (value != null) {
        return false;
      }
      value = result == null ? NULL_VALUE : result;
      l = listener;
      listener = null;
    }
    if (l != null) {
      emitSuccess(result, l);
    }
    return true;
  }

  public boolean tryFail(Throwable cause) {
    if (cause == null) {
      cause = new NoStackTraceThrowable(null);
    }
    Listener<T> l;
    synchronized (this) {
      if (value != null) {
        return false;
      }
      value = cause;
      l = listener;
      listener = null;
    }
    if (l != null) {
      emitFailure(cause, l);
    }
    return true;
  }

  @Override
  public String toString() {
    synchronized (this) {
      if (value instanceof Throwable) {
        return "Future{cause=" + ((Throwable) value).getMessage() + "}";
      }
      if (value != null) {
        return "Future{result=" + (value == NULL_VALUE ? "null" : value) + "}";
      }
      return "Future{unresolved}";
    }
  }

  private static class ListenerArray<T> extends ArrayList<Listener<T>> implements Listener<T> {

    @Override
    public void onSuccess(T value) {
      for (Listener<T> handler : this) {
        handler.onSuccess(value);
      }
    }

    @Override
    public void onFailure(Throwable failure) {
      for (Listener<T> handler : this) {
        handler.onFailure(failure);
      }
    }
  }
}
