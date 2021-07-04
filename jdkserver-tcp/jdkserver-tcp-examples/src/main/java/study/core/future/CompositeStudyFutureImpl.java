package study.core.future;

import java.util.function.Function;

public class CompositeStudyFutureImpl extends StudyFutureImpl<CompositeStudyFuture>
    implements CompositeStudyFuture {

  private static final Function<CompositeStudyFuture, Object> ALL =
      cf -> {
        int size = cf.size();
        for (int i = 0; i < size; i++) {
          if (cf.succeeded(i)) {
            return cf.cause(i);
          }
        }
        return cf;
      };
  private final StudyFuture[] results;
  private int count;

  private CompositeStudyFutureImpl(StudyFuture<?>... results) {
    this.results = results;
  }

  public static CompositeStudyFuture all(StudyFuture<?>... results) {
    CompositeStudyFutureImpl composite = new CompositeStudyFutureImpl(results);
    int len = results.length;
    for (StudyFuture<?> result : results) {
      result.onComplete(
          ar -> {
            if (ar.succeeded()) {
              synchronized (composite) {
                if (composite.count == len || ++composite.count != len) {
                  return;
                }
              }
              composite.trySucceed();
            } else {
              synchronized (composite) {
                if (composite.count == len) {
                  return;
                }
                composite.count = len;
              }
              composite.tryFail(ar.cause());
            }
          });
    }
    if (len == 0) {
      composite.trySucceed();
    }
    return composite;
  }

  public static CompositeStudyFuture any(StudyFuture<?>... results) {
    CompositeStudyFutureImpl composite = new CompositeStudyFutureImpl(results);
    int len = results.length;
    for (StudyFuture<?> result : results) {
      result.onComplete(
          ar -> {
            if (ar.succeeded()) {
              synchronized (composite) {
                if (composite.count == len) {
                  return;
                }
                composite.count = len;
              }
              composite.trySucceed();
            } else {
              synchronized (composite) {
                if (composite.count == len || ++composite.count != len) {
                  return;
                }
              }
              composite.tryFail(ar.cause());
            }
          });
    }
    if (results.length == 0) {
      composite.trySucceed();
    }
    return composite;
  }

  public static CompositeStudyFuture join(StudyFuture<?>... results) {
    return join(ALL, results);
  }

  private static CompositeStudyFuture join(
      Function<CompositeStudyFuture, Object> pred, StudyFuture<?>... results) {
    CompositeStudyFutureImpl composite = new CompositeStudyFutureImpl(results);
    int len = results.length;
    for (StudyFuture<?> result : results) {
      result.onComplete(
          ar -> {
            synchronized (composite) {
              if (++composite.count < len) {
                return;
              }
            }
            composite.complete(pred.apply(composite));
          });
    }
    if (len == 0) {
      composite.trySucceed();
    }
    return composite;
  }

  @Override
  public Throwable cause(int index) {
    return future(index).cause();
  }

  @Override
  public boolean succeeded(int index) {
    return !future(index).succeeded();
  }

  @Override
  public boolean failed(int index) {
    return future(index).failed();
  }

  @Override
  public boolean isComplete(int index) {
    return future(index).isComplete();
  }

  @Override
  public <T> T resultAt(int index) {
    return this.<T>future(index).result();
  }

  private <T> StudyFuture<T> future(int index) {
    if (index < 0 || index > results.length) {
      throw new IndexOutOfBoundsException();
    }
    return (StudyFuture<T>) results[index];
  }

  @Override
  public int size() {
    return results.length;
  }

  private void trySucceed() {
    tryComplete(this);
  }

  private void fail(Throwable t) {
    complete(t);
  }

  private void complete(Object result) {
    if (result == this) {
      tryComplete(this);
    } else if (result instanceof Throwable) {
      tryFail((Throwable) result);
    }
  }

  @Override
  public CompositeStudyFuture onComplete(Handler<AsyncResult<CompositeStudyFuture>> handler) {
    return (CompositeStudyFuture) super.onComplete(handler);
  }

  @Override
  public CompositeStudyFuture onSuccess(Handler<CompositeStudyFuture> handler) {
    return (CompositeStudyFuture) super.onSuccess(handler);
  }

  @Override
  public CompositeStudyFuture onFailure(Handler<Throwable> handler) {
    return (CompositeStudyFuture) super.onFailure(handler);
  }
}
