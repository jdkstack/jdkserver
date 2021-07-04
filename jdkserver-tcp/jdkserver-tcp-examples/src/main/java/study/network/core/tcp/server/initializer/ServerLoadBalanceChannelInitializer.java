package study.network.core.tcp.server.initializer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import study.core.future.Handler;

public class ServerLoadBalanceChannelInitializer extends ChannelInitializer<Channel> {

  private final LoadBalanceWorkerEventLoopGroup workers;
  private final ConcurrentMap<EventLoop, WorkerList> workerMap = new ConcurrentHashMap<>();
  private volatile boolean hasHandlers;

  public ServerLoadBalanceChannelInitializer() {
    this.workers = new LoadBalanceWorkerEventLoopGroup();
  }

  public LoadBalanceWorkerEventLoopGroup workers() {
    return workers;
  }

  public boolean hasHandlers() {
    return hasHandlers;
  }

  @Override
  public void initChannel(Channel ch) {
    Handler<Channel> handler = chooseInitializer(ch.eventLoop());
    if (handler == null) {
      ch.close();
    } else {
      handler.handle(ch);
    }
  }

  private Handler<Channel> chooseInitializer(EventLoop worker) {
    WorkerList handlers = workerMap.get(worker);
    return handlers == null ? null : handlers.chooseHandler();
  }

  public synchronized void addWorker(EventLoop eventLoop, Handler<Channel> handler) {
    workers.addWorker(eventLoop);
    WorkerList handlers = new WorkerList();
    WorkerList prev = workerMap.putIfAbsent(eventLoop, handlers);
    if (prev != null) {
      handlers = prev;
    }
    handlers.addWorker(handler);
    hasHandlers = true;
  }

  public synchronized boolean removeWorker(EventLoop worker, Handler<Channel> handler) {
    WorkerList handlers = workerMap.get(worker);
    if (handlers == null || !handlers.removeWorker(handler)) {
      return false;
    }
    if (handlers.isEmpty()) {
      workerMap.remove(worker);
    }
    if (workerMap.isEmpty()) {
      hasHandlers = false;
    }
    workers.removeWorker(worker);
    return true;
  }

  private static final class WorkerList {

    private final List<Handler<Channel>> list = new CopyOnWriteArrayList<>();
    private int pos;

    Handler<Channel> chooseHandler() {
      Handler<Channel> handler = list.get(pos);
      pos++;
      checkPos();
      return handler;
    }

    void addWorker(Handler<Channel> handler) {
      list.add(handler);
    }

    boolean removeWorker(Handler<Channel> handler) {
      if (list.remove(handler)) {
        checkPos();
        return true;
      } else {
        return false;
      }
    }

    boolean isEmpty() {
      return list.isEmpty();
    }

    void checkPos() {
      if (pos == list.size()) {
        pos = 0;
      }
    }
  }
}
