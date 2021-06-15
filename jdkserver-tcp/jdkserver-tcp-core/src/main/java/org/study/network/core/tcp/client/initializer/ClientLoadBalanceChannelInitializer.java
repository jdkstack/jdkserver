package org.study.network.core.tcp.client.initializer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import io.netty.util.AttributeKey;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.study.core.future.Handler;

/**
 * This is a method description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public class ClientLoadBalanceChannelInitializer extends ChannelInitializer<Channel> {

  private int remotePort;
  private String remoteHost;
  private String clientName;
  private String serverName;

  private final LoadBalanceWorkerEventLoopGroup workers;
  private final ConcurrentMap<EventLoop, WorkerList> workerMap = new ConcurrentHashMap<>();
  private volatile boolean hasHandlers;

  public ClientLoadBalanceChannelInitializer() {
    this.workers = new LoadBalanceWorkerEventLoopGroup();
  }

  public LoadBalanceWorkerEventLoopGroup workers() {
    return workers;
  }

  public int getRemotePort() {
    return remotePort;
  }

  public void setRemotePort(int remotePort) {
    this.remotePort = remotePort;
  }

  public String getRemoteHost() {
    return remoteHost;
  }

  public void setRemoteHost(String remoteHost) {
    this.remoteHost = remoteHost;
  }

  public boolean hasHandlers() {
    return hasHandlers;
  }

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  @Override
  public void initChannel(Channel channel) {
    Handler<Channel> handler = chooseInitializer(channel.eventLoop());
    if (handler == null) {
      channel.close();
    } else {
      AttributeKey<String> host = AttributeKey.valueOf("host");
      channel.attr(host).set(this.remoteHost);
      AttributeKey<Integer> port = AttributeKey.valueOf("port");
      channel.attr(port).set(this.remotePort);
      AttributeKey<Boolean> domainSocket = AttributeKey.valueOf("domainSocket");
      channel.attr(domainSocket).set(false);
      final AttributeKey<String> clientName = AttributeKey.valueOf("clientName");
      channel.attr(clientName).set(this.clientName);
      final AttributeKey<String> serverName = AttributeKey.valueOf("serverName");
      channel.attr(serverName).set(this.serverName);
      handler.handleSsl(channel);
    }
  }

  public void init(Channel ch) {
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
