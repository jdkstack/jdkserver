package study.network.core.socket;

import io.netty.handler.codec.haproxy.HAProxyMessage;
import javax.net.ssl.SSLSession;
import study.core.future.AsyncResult;
import study.core.future.Handler;
import study.core.future.StudyFuture;
import study.network.codecs.NetworkMessage;

/** @author admin */
public interface NetSocket extends ReadStream<Buffer>, WriteStream<Buffer> {

  @Override
  NetSocket exceptionHandler(Handler<Throwable> handler);

  @Override
  NetSocket handler(Handler<Buffer> handler);

  NetSocket handlerNetworkMessage(Handler<NetworkMessage> handler);

  NetSocket handlerHAProxyMessage(Handler<HAProxyMessage> handler);

  NetSocket handlerObject(Handler<Object> handler);

  @Override
  NetSocket pause();

  @Override
  NetSocket resume();

  @Override
  NetSocket fetch(long amount);

  @Override
  NetSocket endHandler(Handler<Void> endHandler);

  @Override
  NetSocket setWriteQueueMaxSize(int maxSize);

  @Override
  NetSocket drainHandler(Handler<Void> handler);

  String writeHandlerID();

  void writeObject(Object str, Handler<AsyncResult<Void>> handler);

  StudyFuture<Void> writeObject(Object str);

  void write(String str, Handler<AsyncResult<Void>> handler);

  StudyFuture<Void> write(String str);

  StudyFuture<Void> writeNetworkMessage(NetworkMessage str);

  StudyFuture<Void> writeHAProxyMessage(HAProxyMessage str);

  void write(String str, String enc, Handler<AsyncResult<Void>> handler);

  StudyFuture<Void> write(String str, String enc);

  void write(Buffer message, Handler<AsyncResult<Void>> handler);

  default StudyFuture<Void> sendFile(String filename) {
    return sendFile(filename, 0, Long.MAX_VALUE);
  }

  default StudyFuture<Void> sendFile(String filename, long offset) {
    return sendFile(filename, offset, Long.MAX_VALUE);
  }

  StudyFuture<Void> sendFile(String filename, long offset, long length);

  default NetSocket sendFile(String filename, Handler<AsyncResult<Void>> resultHandler) {
    return sendFile(filename, 0, Long.MAX_VALUE, resultHandler);
  }

  default NetSocket sendFile(
      String filename, long offset, Handler<AsyncResult<Void>> resultHandler) {
    return sendFile(filename, offset, Long.MAX_VALUE, resultHandler);
  }

  NetSocket sendFile(
      String filename, long offset, long length, Handler<AsyncResult<Void>> resultHandler);

  @Override
  StudyFuture<Void> end();

  @Override
  void end(Handler<AsyncResult<Void>> handler);

  StudyFuture<Void> close();

  void close(Handler<AsyncResult<Void>> handler);

  NetSocket closeHandler(Handler<Void> handler);

  NetSocket upgradeToSsl(Handler<AsyncResult<Void>> handler);

  StudyFuture<Void> upgradeToSsl();

  NetSocket upgradeToSsl(String serverName, Handler<AsyncResult<Void>> handler);

  StudyFuture<Void> upgradeToSsl(String serverName);

  boolean isSsl();

  SSLSession sslSession();

  String indicatedServerName();
}
