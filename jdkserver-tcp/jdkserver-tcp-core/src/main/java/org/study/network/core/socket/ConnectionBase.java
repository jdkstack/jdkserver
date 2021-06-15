package org.study.network.core.socket;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.channel.VoidChannelPromise;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.FutureListener;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import javax.net.ssl.SSLSession;
import org.study.core.future.AsyncResult;
import org.study.core.future.Handler;
import org.study.core.future.StudyFuture;
import org.study.core.promise.StudyPromiseImpl;
import org.study.core.promise.StudyPromiseInternal;

public abstract class ConnectionBase {
  private static final int MAX_REGION_SIZE = 1024 * 1024;
  public final VoidChannelPromise voidPromise;
  protected final ChannelHandlerContext chctx;
  private Handler<Throwable> exceptionHandler;
  private Handler<Void> closeHandler;
  private int writeInProgress;
  private Object metric;
  private SocketAddress remoteAddress;
  private SocketAddress localAddress;
  private ChannelPromise closePromise;
  private StudyFuture<Void> closeFuture;
  private long remainingBytesRead;
  private long remainingBytesWritten;
  private boolean read;
  private boolean needsFlush;
  private boolean closed;

  protected ConnectionBase(ChannelHandlerContext chctx) {
    this.chctx = chctx;
    this.voidPromise = new VoidChannelPromise(chctx.channel(), false);
    this.closePromise = chctx.newPromise();
    StudyPromiseInternal<Void> p = new StudyPromiseImpl<>();
    closeFuture = p.future();
    closeFuture.onComplete(this::checkCloseHandler);
  }

  public StudyFuture<Void> closeFuture() {
    return closeFuture;
  }

  public void fail(Throwable error) {
    chctx.pipeline().fireExceptionCaught(error);
  }

  public void close(ChannelPromise promise) {
    closePromise.addListener(
        l -> {
          if (l.isSuccess()) {
            promise.setSuccess();
          } else {
            promise.setFailure(l.cause());
          }
        });
    close();
  }

  public final void endReadAndFlush() {
    if (read) {
      read = false;
      if (needsFlush) {
        needsFlush = false;
        chctx.flush();
      }
    }
  }

  public final void read(Object msg) {
    read = true;
    if (!closed) {
      handleMessage(msg);
    }
  }

  private void write(Object msg, Boolean flush, ChannelPromise promise) {

    boolean writeAndFlush;
    if (flush == null) {
      writeAndFlush = !read;
    } else {
      writeAndFlush = flush;
    }
    needsFlush = !writeAndFlush;
    if (writeAndFlush) {
      chctx.writeAndFlush(msg, promise);
    } else {
      chctx.write(msg, promise);
    }
  }

  private ChannelPromise wrap(FutureListener<Void> handler) {
    ChannelPromise promise = chctx.newPromise();
    promise.addListener(handler);
    return promise;
  }

  public final void writeToChannel(Object msg, FutureListener<Void> listener) {
    writeToChannel(msg, listener == null ? voidPromise : wrap(listener));
  }

  public final void writeToChannel(Object msg, ChannelPromise promise) {
    writeToChannel(msg, false, promise);
  }

  public final void writeToChannel(Object msg, boolean forceFlush, ChannelPromise promise) {
    synchronized (this) {
      if (!chctx.executor().inEventLoop() || writeInProgress > 0) {
        queueForWrite(msg, forceFlush, promise);
        return;
      }
    }
    write(msg, forceFlush ? true : null, promise);
  }

  private void queueForWrite(Object msg, boolean forceFlush, ChannelPromise promise) {
    writeInProgress++;
    chctx
        .executor()
        .execute(
            () -> {
              boolean flush;
              if (forceFlush) {
                flush = true;
              } else {
                synchronized (this) {
                  flush = --writeInProgress == 0;
                }
              }
              write(msg, flush, promise);
            });
  }

  public void writeToChannel(Object obj) {
    writeToChannel(obj, voidPromise);
  }

  public final void flush() {
    flush(voidPromise);
  }

  public final void flush(ChannelPromise promise) {
    writeToChannel(Unpooled.EMPTY_BUFFER, true, promise);
  }

  public boolean isNotWritable() {
    return !chctx.channel().isWritable();
  }

  public StudyFuture<Void> close() {
    StudyPromiseInternal<Void> promise = new StudyPromiseImpl<>();
    EventExecutor exec = chctx.executor();
    if (exec.inEventLoop()) {
      writeClose(promise);
    } else {
      exec.execute(() -> writeClose(promise));
    }
    return promise.future();
  }

  private void writeClose(StudyPromiseInternal<Void> promise) {
    if (closed) {
      promise.complete();
      return;
    }
    closed = true;
    ChannelPromise channelPromise =
        chctx
            .newPromise()
            .addListener(
                (ChannelFutureListener)
                    f -> {
                      // chctx.close().addListener(promise)
                    });
    chctx.close();
    writeToChannel(Unpooled.EMPTY_BUFFER, true, channelPromise);
  }

  public final void close(Handler<AsyncResult<Void>> handler) {
    close().onComplete(handler);
  }

  public synchronized ConnectionBase closeHandler(Handler<Void> handler) {
    closeHandler = handler;
    return this;
  }

  public synchronized ConnectionBase exceptionHandler(Handler<Throwable> handler) {
    this.exceptionHandler = handler;
    return this;
  }

  protected synchronized Handler<Throwable> exceptionHandler() {
    return exceptionHandler;
  }

  public void doPause() {
    chctx.channel().config().setAutoRead(false);
  }

  public void doResume() {
    chctx.channel().config().setAutoRead(true);
  }

  public void doSetWriteQueueMaxSize(int size) {
    ChannelConfig config = chctx.channel().config();
    config.setWriteBufferWaterMark(new WriteBufferWaterMark(size / 2, size));
  }

  public final Channel channel() {
    return chctx.channel();
  }

  public final ChannelHandlerContext channelHandlerContext() {
    return chctx;
  }

  public final synchronized void metric(Object metric) {
    this.metric = metric;
  }

  public final synchronized Object metric() {
    return metric;
  }

  public void handleException(Throwable t) {
    Handler<Throwable> handler;
    synchronized (ConnectionBase.this) {
      handler = exceptionHandler;
    }
    if (handler != null) {
      handler.handle(t);
    }
  }

  public void handleClosed() {
    closed = true;
    closePromise.setSuccess();
  }

  private void checkCloseHandler(AsyncResult<Void> ar) {
    Handler<Void> handler;
    synchronized (ConnectionBase.this) {
      handler = closeHandler;
    }
    if (handler != null) {
      handler.handle(null);
    }
  }

  public void handleIdle() {
    chctx.close();
  }

  public abstract void handleInterestedOpsChanged();

  public boolean supportsFileRegion() {
    return !isSsl();
  }

  public void reportBytesRead(Object msg) {}

  public void reportBytesRead(long numberOfBytes) {
    if (numberOfBytes < 0L) {
      throw new IllegalArgumentException();
    }
    long bytes = remainingBytesRead;
    bytes += numberOfBytes;
    remainingBytesRead = bytes;
  }

  protected void reportsBytesWritten(Object msg) {}

  public void reportBytesWritten(long numberOfBytes) {
    if (numberOfBytes < 0L) {
      throw new IllegalArgumentException();
    }
    long bytes = remainingBytesWritten;
    bytes += numberOfBytes;
    remainingBytesWritten = bytes;
  }

  public void flushBytesRead() {
    long val = remainingBytesRead;
    if (val > 0L) {
      remainingBytesRead = 0L;
    }
  }

  public void flushBytesWritten() {
    long val = remainingBytesWritten;
    if (val > 0L) {
      remainingBytesWritten = 0L;
    }
  }

  private void sendFileRegion(
      RandomAccessFile file, long offset, long length, ChannelPromise writeFuture) {
    if (length < MAX_REGION_SIZE) {
      writeToChannel(new DefaultFileRegion(file.getChannel(), offset, length), writeFuture);
    } else {
      ChannelPromise promise = chctx.newPromise();
      FileRegion region = new DefaultFileRegion(file.getChannel(), offset, MAX_REGION_SIZE);

      region.retain();
      writeToChannel(region, promise);
      promise.addListener(
          future -> {
            if (future.isSuccess()) {
              sendFileRegion(file, offset + MAX_REGION_SIZE, length - MAX_REGION_SIZE, writeFuture);
            } else {
              writeFuture.setFailure(future.cause());
            }
          });
    }
  }

  public final ChannelFuture sendFile(RandomAccessFile raf, long offset, long length)
      throws IOException {
    // Write the content.
    ChannelPromise writeFuture = chctx.newPromise();
    if (!supportsFileRegion()) {
      // Cannot use zero-copy
      writeToChannel(new ChunkedFile(raf, offset, length, 8192), writeFuture);
    } else {
      // No encryption - use zero-copy.
      sendFileRegion(raf, offset, length, writeFuture);
    }
    if (writeFuture != null) {
      writeFuture.addListener(fut -> raf.close());
    } else {
      raf.close();
    }
    return writeFuture;
  }

  public boolean isSsl() {
    return chctx.pipeline().get(SslHandler.class) != null;
  }

  public SSLSession sslSession() {
    ChannelHandlerContext sslHandlerContext = chctx.pipeline().context(SslHandler.class);
    if (sslHandlerContext != null) {
      SslHandler sslHandler = (SslHandler) sslHandlerContext.handler();
      return sslHandler.engine().getSession();
    } else {
      return null;
    }
  }

  public String indicatedServerName() {
    return null;
  }

  public ChannelPromise channelFuture() {
    return chctx.newPromise();
  }

  public String remoteName() {
    SocketAddress addr = chctx.channel().remoteAddress();
    if (addr instanceof InetSocketAddress) {
      return ((InetSocketAddress) addr).getHostString();
    }
    return null;
  }

  protected void handleMessage(Object msg) {}

  public SocketAddress remoteAddress() {
    return remoteAddress;
  }

  public SocketAddress localAddress() {
    return localAddress;
  }
}
