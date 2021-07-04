package study.network.core.socket;

import io.netty.util.concurrent.FastThreadLocalThread;
import java.util.ArrayDeque;
import study.core.future.Handler;

public class InboundBuffer<E> {
  public static final Object END_SENTINEL = new Object();
  private final ArrayDeque<E> pending;
  private final long highWaterMark;
  private long demand;
  private Handler<E> handler;
  private boolean overflow;
  private Handler<Void> drainHandler;
  private Handler<Void> emptyHandler;
  private Handler<Throwable> exceptionHandler;
  private boolean emitting;

  public InboundBuffer() {
    this(16L);
  }

  public InboundBuffer(long highWaterMark) {
    if (highWaterMark < 0) {
      throw new IllegalArgumentException("highWaterMark " + highWaterMark + " >= 0");
    }
    this.highWaterMark = highWaterMark;
    this.demand = Long.MAX_VALUE;
    this.pending = new ArrayDeque<>();
  }

  private void checkThread() {
    Thread thread = Thread.currentThread();
    if (!(thread instanceof FastThreadLocalThread)) {
      throw new IllegalStateException("This operation must be called from a Vert.x thread");
    }
  }

  public boolean write(E element) {
    checkThread();
    Handler<E> handler;
    synchronized (this) {
      if (demand == 0L || emitting) {
        pending.add(element);
        return checkWritable();
      } else {
        if (demand != Long.MAX_VALUE) {
          --demand;
        }
        emitting = true;
        handler = this.handler;
      }
    }
    handleEvent(handler, element);
    return emitPending();
  }

  private boolean checkWritable() {
    if (demand == Long.MAX_VALUE) {
      return true;
    } else {
      long actual = pending.size() - demand;
      boolean writable = actual < highWaterMark;
      overflow |= !writable;
      return writable;
    }
  }

  public boolean write(Iterable<E> elements) {
    checkThread();
    synchronized (this) {
      for (E element : elements) {
        pending.add(element);
      }
      if (demand == 0L || emitting) {
        return checkWritable();
      } else {
        emitting = true;
      }
    }
    return emitPending();
  }

  private boolean emitPending() {
    E element;
    Handler<E> h;
    while (true) {
      synchronized (this) {
        int size = pending.size();
        if (demand == 0L) {
          emitting = false;
          boolean writable = size < highWaterMark;
          overflow |= !writable;
          return writable;
        } else if (size == 0) {
          emitting = false;
          return true;
        }
        if (demand != Long.MAX_VALUE) {
          demand--;
        }
        element = pending.poll();
        h = this.handler;
      }
      handleEvent(h, element);
    }
  }

  private void drain() {
    int emitted = 0;
    Handler<Void> drainHandler;
    Handler<Void> emptyHandler;
    while (true) {
      E element;
      Handler<E> handler;
      synchronized (this) {
        int size = pending.size();
        if (size == 0) {
          emitting = false;
          if (overflow) {
            overflow = false;
            drainHandler = this.drainHandler;
          } else {
            drainHandler = null;
          }
          emptyHandler = emitted > 0 ? this.emptyHandler : null;
          break;
        } else if (demand == 0L) {
          emitting = false;
          return;
        }
        emitted++;
        if (demand != Long.MAX_VALUE) {
          demand--;
        }
        element = pending.poll();
        handler = this.handler;
      }
      handleEvent(handler, element);
    }
    if (drainHandler != null) {
      handleEvent(drainHandler, null);
    }
    if (emptyHandler != null) {
      handleEvent(emptyHandler, null);
    }
  }

  private <T> void handleEvent(Handler<T> handler, T element) {
    if (handler != null) {
      try {
        handler.handle(element);
      } catch (Throwable t) {
        t.printStackTrace();
        handleException(t);
      }
    }
  }

  private void handleException(Throwable err) {
    Handler<Throwable> handler;
    synchronized (this) {
      if ((handler = exceptionHandler) == null) {
        return;
      }
    }
    handler.handle(err);
  }

  public boolean fetch(long amount) {
    if (amount < 0L) {
      throw new IllegalArgumentException();
    }
    synchronized (this) {
      demand += amount;
      if (demand < 0L) {
        demand = Long.MAX_VALUE;
      }
      if (emitting || (pending.isEmpty() && !overflow)) {
        return false;
      }
      emitting = true;
    }
    // context.runOnContext(v -> drain());
    return true;
  }

  public E read() {
    synchronized (this) {
      return pending.poll();
    }
  }

  public synchronized InboundBuffer<E> clear() {
    pending.clear();
    return this;
  }

  public synchronized InboundBuffer<E> pause() {
    demand = 0L;
    return this;
  }

  public boolean resume() {
    return fetch(Long.MAX_VALUE);
  }

  public synchronized InboundBuffer<E> handler(Handler<E> handler) {
    this.handler = handler;
    return this;
  }

  public synchronized InboundBuffer<E> drainHandler(Handler<Void> handler) {
    drainHandler = handler;
    return this;
  }

  public synchronized InboundBuffer<E> emptyHandler(Handler<Void> handler) {
    emptyHandler = handler;
    return this;
  }

  public synchronized InboundBuffer<E> exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  public synchronized boolean isEmpty() {
    return pending.isEmpty();
  }

  public synchronized boolean isWritable() {
    return pending.size() < highWaterMark;
  }

  public synchronized boolean isPaused() {
    return demand == 0L;
  }

  public synchronized int size() {
    return pending.size();
  }
}
