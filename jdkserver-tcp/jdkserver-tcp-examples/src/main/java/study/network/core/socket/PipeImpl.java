package study.network.core.socket;

import study.core.future.AsyncResult;
import study.core.future.Handler;
import study.core.future.StudyFuture;
import study.core.promise.StudyPromise;
import study.core.promise.StudyPromiseImpl;
import study.network.core.common.exception.StudyException;

public class PipeImpl<T> implements Pipe<T> {

  private final StudyPromise<Void> result;
  private final ReadStream<T> src;
  private boolean endOnSuccess = true;
  private boolean endOnFailure = true;
  private WriteStream<T> dst;

  public PipeImpl(ReadStream<T> src) {
    this.src = src;
    this.result = new StudyPromiseImpl();
    // Set handlers now
    src.endHandler(result::tryComplete);
    src.exceptionHandler(result::tryFail);
  }

  @Override
  public synchronized Pipe<T> endOnFailure(boolean end) {
    endOnFailure = end;
    return this;
  }

  @Override
  public synchronized Pipe<T> endOnSuccess(boolean end) {
    endOnSuccess = end;
    return this;
  }

  @Override
  public synchronized Pipe<T> endOnComplete(boolean end) {
    endOnSuccess = end;
    endOnFailure = end;
    return this;
  }

  private void handleWriteResult(AsyncResult<Void> ack) {
    if (ack.failed()) {
      result.tryFail(new WriteException(ack.cause()));
    }
  }

  @Override
  public void to(WriteStream<T> ws, Handler<AsyncResult<Void>> completionHandler) {
    if (ws == null) {
      throw new NullPointerException();
    }
    boolean endOnSuccess;
    boolean endOnFailure;
    synchronized (PipeImpl.this) {
      if (dst != null) {
        throw new IllegalStateException();
      }
      dst = ws;
      endOnSuccess = this.endOnSuccess;
      endOnFailure = this.endOnFailure;
    }
    Handler<Void> drainHandler = v -> src.resume();
    src.handler(
        item -> {
          ws.write(item, this::handleWriteResult);
          if (ws.writeQueueFull()) {
            src.pause();
            ws.drainHandler(drainHandler);
          }
        });
    src.resume();
    result
        .future()
        .onComplete(
            ar -> {
              try {
                src.handler(null);
              } catch (Exception ignore) {
              }
              try {
                src.exceptionHandler(null);
              } catch (Exception ignore) {
              }
              try {
                src.endHandler(null);
              } catch (Exception ignore) {
              }
              if (ar.succeeded()) {
                handleSuccess(completionHandler);
              } else {
                Throwable err = ar.cause();
                if (err instanceof WriteException) {
                  src.resume();
                  err = err.getCause();
                }
                handleFailure(err, completionHandler);
              }
            });
  }

  private void handleSuccess(Handler<AsyncResult<Void>> completionHandler) {
    if (endOnSuccess) {
      dst.end(completionHandler);
    } else {
      completionHandler.handle(StudyFuture.succeededFuture());
    }
  }

  private void handleFailure(Throwable cause, Handler<AsyncResult<Void>> completionHandler) {
    StudyFuture<Void> res = StudyFuture.failedFuture(cause);
    if (endOnFailure) {
      dst.end(
          ignore -> {
            completionHandler.handle(res);
          });
    } else {
      completionHandler.handle(res);
    }
  }

  @Override
  public void close() {
    synchronized (this) {
      src.exceptionHandler(null);
      src.handler(null);
      if (dst != null) {
        dst.drainHandler(null);
        dst.exceptionHandler(null);
      }
    }
    StudyException err = new StudyException("Pipe closed", true);
    if (result.tryFail(err)) {
      src.resume();
    }
  }

  private static class WriteException extends StudyException {
    private WriteException(Throwable cause) {
      super(cause, true);
    }
  }
}
