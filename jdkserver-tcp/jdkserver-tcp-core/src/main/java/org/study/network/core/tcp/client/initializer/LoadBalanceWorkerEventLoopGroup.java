package org.study.network.core.tcp.client.initializer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.AbstractEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This is a method description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public final class LoadBalanceWorkerEventLoopGroup extends AbstractEventExecutorGroup
    implements EventLoopGroup {
  /** . */
  private final List<EventLoopHolder> workers = new ArrayList<>(16);
  /** . */
  private final Set<EventExecutor> children =
      new Set<>() {
        @Override
        public Iterator<EventExecutor> iterator() {
          return new EventLoopIterator(workers.iterator());
        }

        @Override
        public int size() {
          return workers.size();
        }

        @Override
        public boolean isEmpty() {
          return workers.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
          return workers.contains(o);
        }

        @Override
        public Object[] toArray() {
          return workers.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
          return workers.toArray(a);
        }

        @Override
        public boolean add(EventExecutor eventExecutor) {
          throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
          throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
          return workers.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends EventExecutor> c) {
          throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
          throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
          throw new UnsupportedOperationException();
        }
      };

  private int pos;

  @Override
  public synchronized EventLoop next() {
    if (workers.isEmpty()) {
      throw new IllegalStateException();
    } else {
      EventLoop worker = workers.get(pos).worker;
      pos++;
      checkPos();
      return worker;
    }
  }

  @Override
  public Iterator<EventExecutor> iterator() {
    return children.iterator();
  }

  @Override
  public ChannelFuture register(Channel channel) {
    return next().register(channel);
  }

  @Override
  public ChannelFuture register(Channel channel, ChannelPromise promise) {
    return next().register(channel, promise);
  }

  @Override
  public ChannelFuture register(ChannelPromise promise) {
    return next().register(promise);
  }

  @Override
  public boolean isShutdown() {
    return false;
  }

  @Override
  public boolean isTerminated() {
    return isShutdown();
  }

  @Override
  public synchronized boolean awaitTermination(long timeout, TimeUnit unit) {
    return false;
  }

  public synchronized void addWorker(EventLoop worker) {
    EventLoopHolder holder = findHolder(worker);
    if (holder == null) {
      workers.add(new EventLoopHolder(worker));
    } else {
      holder.count++;
    }
  }

  @Override
  public synchronized void shutdown() {
    throw new UnsupportedOperationException("Should never be called");
  }

  @Override
  public boolean isShuttingDown() {
    return false;
  }

  @Override
  public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
    throw new UnsupportedOperationException("Should never be called");
  }

  @Override
  public Future<?> terminationFuture() {
    throw new UnsupportedOperationException("Should never be called");
  }

  private EventLoopHolder findHolder(EventLoop worker) {
    EventLoopHolder wh = new EventLoopHolder(worker);
    for (EventLoopHolder holder : workers) {
      if (holder.equals(wh)) {
        return holder;
      }
    }
    return null;
  }

  public synchronized void removeWorker(EventLoop worker) {
    EventLoopHolder holder = findHolder(worker);
    if (holder != null) {
      holder.count--;
      if (holder.count == 0) {
        workers.remove(holder);
      }
      checkPos();
    } else {
      throw new IllegalStateException("Can't find worker to remove");
    }
  }

  public synchronized int workerCount() {
    return workers.size();
  }

  private void checkPos() {
    if (pos == workers.size()) {
      pos = 0;
    }
  }

  private static class EventLoopHolder {

    final EventLoop worker;
    int count = 1;

    EventLoopHolder(EventLoop worker) {
      this.worker = worker;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      EventLoopHolder that = (EventLoopHolder) o;
      return Objects.equals(worker, that.worker);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(worker);
    }
  }

  private static final class EventLoopIterator implements Iterator<EventExecutor> {

    private final Iterator<EventLoopHolder> holderIt;

    public EventLoopIterator(Iterator<EventLoopHolder> holderIt) {
      this.holderIt = holderIt;
    }

    @Override
    public boolean hasNext() {
      return holderIt.hasNext();
    }

    @Override
    public EventExecutor next() {
      return holderIt.next().worker;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("read-only");
    }
  }
}
