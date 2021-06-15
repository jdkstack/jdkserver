package org.study.network.core.socket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCounted;
import io.netty.util.concurrent.FutureListener;
import java.nio.charset.Charset;
import java.util.UUID;
import org.study.core.context.WorkerContext;
import org.study.core.context.WorkerStudyContextImpl;
import org.study.core.future.AsyncResult;
import org.study.core.future.Handler;
import org.study.core.future.StudyFuture;
import org.study.core.promise.StudyPromise;
import org.study.core.promise.StudyPromiseImpl;
import org.study.network.codecs.NetworkMessage;

public class NetSocketImpl extends ConnectionBase implements NetSocketInternal {

  private static final Handler<Object> NULL_MSG_HANDLER =
      event -> {
        if (event instanceof ReferenceCounted) {
          ReferenceCounted refCounter = (ReferenceCounted) event;
          refCounter.release();
        }
      };
  protected final WorkerContext context;
  private final String writeHandlerID;
  private final InboundBuffer<Object> pending;
  private Handler<Void> endHandler;
  private Handler<Void> drainHandler;
  private Handler<Object> messageHandler;

  public NetSocketImpl(ChannelHandlerContext channel, WorkerContext context) {
    super(channel);
    this.context = context;
    this.writeHandlerID = "__vertx.net." + UUID.randomUUID().toString();
    this.messageHandler = NULL_MSG_HANDLER;
    pending = new InboundBuffer<>();
    pending.drainHandler(v -> doResume());
    // pending.exceptionHandler(context::reportException);
    pending.handler(
        obj -> {
          if (obj == InboundBuffer.END_SENTINEL) {
            Handler<Void> handler = endHandler();
            if (handler != null) {
              handler.handle(null);
            }
          } else {
            Handler<Object> handler = messageHandler();
            if (handler != null) {
              handler.handle(obj);
            }
          }
        });
  }

  public static ByteBuf safeBuffer(ByteBuf buf, ByteBufAllocator allocator) {
    if (buf == Unpooled.EMPTY_BUFFER) {
      return buf;
    }
    if (buf.isDirect() || buf instanceof CompositeByteBuf) {
      try {
        if (buf.isReadable()) {
          ByteBuf buffer = allocator.heapBuffer(buf.readableBytes());
          buffer.writeBytes(buf);
          return buffer;
        } else {
          return Unpooled.EMPTY_BUFFER;
        }
      } finally {
        buf.release();
      }
    }
    return buf;
  }

  public synchronized void registerEventBusHandler() {
    // Handler<Message<Buffer>> writeHandler = msg -> write(msg.body());
    // registration = vertx.eventBus().<Buffer>localConsumer(writeHandlerID).handler(writeHandler);
  }

  @Override
  public String writeHandlerID() {
    return writeHandlerID;
  }

  @Override
  public void writeObject(Object str, Handler<AsyncResult<Void>> handler) {
    writeMessage(str, handler);
  }

  @Override
  public StudyFuture<Void> writeObject(Object str) {
    return writeMessage(str);
  }

  @Override
  public synchronized StudyFuture<Void> writeMessage(Object message) {
    StudyPromise promise = new StudyPromiseImpl<>();
    writeMessage(message, promise);
    return promise.future();
  }

  @Override
  public NetSocketInternal writeMessage(Object message, Handler<AsyncResult<Void>> handler) {
    writeToChannel(message, (FutureListener) future -> {
    });
    return this;
  }

  @Override
  protected void reportsBytesWritten(Object msg) {
    if (msg instanceof ByteBuf) {
      reportBytesWritten(((ByteBuf) msg).readableBytes());
    }
  }

  @Override
  public void reportBytesRead(Object msg) {
    if (msg instanceof ByteBuf) {
      reportBytesRead(((ByteBuf) msg).readableBytes());
    }
  }

  @Override
  public StudyFuture<Void> write(Buffer data) {
    return writeMessage(data.getByteBuf());
  }

  @Override
  public void write(String str, Handler<AsyncResult<Void>> handler) {
    write(Unpooled.copiedBuffer(str, CharsetUtil.UTF_8), handler);
  }

  @Override
  public StudyFuture<Void> write(String str) {
    return writeMessage(Unpooled.copiedBuffer(str, CharsetUtil.UTF_8));
  }

  @Override
  public StudyFuture<Void> writeNetworkMessage(NetworkMessage str) {
    return writeMessage(str);
  }

  @Override
  public StudyFuture<Void> writeHAProxyMessage(HAProxyMessage str) {
    return writeMessage(str);
  }

  @Override
  public StudyFuture<Void> write(String str, String enc) {
    return writeMessage(Unpooled.copiedBuffer(str, Charset.forName(enc)));
  }

  @Override
  public void write(String str, String enc, Handler<AsyncResult<Void>> handler) {
    Charset cs = enc != null ? Charset.forName(enc) : CharsetUtil.UTF_8;
    write(Unpooled.copiedBuffer(str, cs), handler);
  }

  @Override
  public void write(Buffer message, Handler<AsyncResult<Void>> handler) {
    write(message.getByteBuf(), handler);
  }

  private void write(ByteBuf buff, Handler<AsyncResult<Void>> handler) {
    reportBytesWritten(buff.readableBytes());
    writeMessage(buff, handler);
  }

  @Override
  public synchronized NetSocket handler(Handler<Buffer> dataHandler) {
    if (dataHandler != null) {
      messageHandler(new DataMessageHandler(channelHandlerContext().alloc(), dataHandler));
    } else {
      messageHandler(null);
    }
    return this;
  }

  @Override
  public synchronized NetSocket handlerNetworkMessage(Handler<NetworkMessage> dataHandler) {
    if (dataHandler != null) {
      messageHandler(new DataNetworkMessageHandler(channelHandlerContext().alloc(), dataHandler));
    } else {
      messageHandler(null);
    }
    return this;
  }

  @Override
  public synchronized NetSocket handlerObject(Handler<Object> dataHandler) {
    if (dataHandler != null) {
      messageHandler(new DataObjectHandler(channelHandlerContext().alloc(), dataHandler));
    } else {
      messageHandler(null);
    }
    return this;
  }

  @Override
  public synchronized NetSocket handlerHAProxyMessage(Handler<HAProxyMessage> dataHandler) {
    if (dataHandler != null) {
      messageHandler(new DataHAProxyMessageHandler(channelHandlerContext().alloc(), dataHandler));
    } else {
      messageHandler(null);
    }
    return this;
  }

  private synchronized Handler<Object> messageHandler() {
    return messageHandler;
  }

  @Override
  public synchronized NetSocketInternal messageHandler(Handler<Object> handler) {
    messageHandler = handler;
    return this;
  }

  @Override
  public synchronized NetSocket pause() {
    pending.pause();
    return this;
  }

  @Override
  public NetSocket fetch(long amount) {
    pending.fetch(amount);
    return this;
  }

  @Override
  public synchronized NetSocket resume() {
    return fetch(Long.MAX_VALUE);
  }

  @Override
  public NetSocket setWriteQueueMaxSize(int maxSize) {
    doSetWriteQueueMaxSize(maxSize);
    return this;
  }

  @Override
  public boolean writeQueueFull() {
    return isNotWritable();
  }

  private synchronized Handler<Void> endHandler() {
    return endHandler;
  }

  @Override
  public synchronized NetSocket endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  @Override
  public synchronized NetSocket drainHandler(Handler<Void> drainHandler) {
    this.drainHandler = drainHandler;
    callDrainHandler();
    return this;
  }

  @Override
  public StudyFuture<Void> sendFile(String filename, long offset, long length) {
    //  Promise<Void> promise = context.promise();
    // sendFile(filename, offset, length, promise);
    return null; // promise.future();
  }

  @Override
  public NetSocket sendFile(
      String filename, long offset, long length, final Handler<AsyncResult<Void>> resultHandler) {
    /*    File f = vertx.resolveFile(filename);
    if (f.isDirectory()) {
      throw new IllegalArgumentException("filename must point to a file and not to a directory");
    }
    RandomAccessFile raf = null;
    try {
      raf = new RandomAccessFile(f, "r");
      ChannelFuture future = super.sendFile(raf, Math.min(offset, f.length()), Math.min(length, f.length() - offset));
      if (resultHandler != null) {
        future.addListener(fut -> {
          final AsyncResult<Void> res;
          if (future.isSuccess()) {
            res = Future.succeededFuture();
          } else {
            res = Future.failedFuture(future.cause());
          }
          vertx.runOnContext(v -> resultHandler.handle(res));
        });
      }
    } catch (IOException e) {
      try {
        if (raf != null) {
          raf.close();
        }
      } catch (IOException ignore) {
      }
      if (resultHandler != null) {
        vertx.runOnContext(v -> resultHandler.handle(Future.failedFuture(e)));
      } else {
        log.error("Failed to send file", e);
      }
    }*/
    return this;
  }

  @Override
  public NetSocketImpl exceptionHandler(Handler<Throwable> handler) {
    return (NetSocketImpl) super.exceptionHandler(handler);
  }

  @Override
  public NetSocketImpl closeHandler(Handler<Void> handler) {
    return (NetSocketImpl) super.closeHandler(handler);
  }

  @Override
  public StudyFuture<Void> upgradeToSsl() {
    // Promise<Void> promise = context.promise();
    // upgradeToSsl(promise);
    return null; // promise.future();
  }

  @Override
  public StudyFuture<Void> upgradeToSsl(String serverName) {
    // Promise<Void> promise = context.promise();
    // upgradeToSsl(serverName, promise);
    return null; // promise.future();
  }

  @Override
  public NetSocket upgradeToSsl(Handler<AsyncResult<Void>> handler) {
    return upgradeToSsl(null, handler);
  }

  @Override
  public NetSocket upgradeToSsl(String serverName, Handler<AsyncResult<Void>> handler) {
    ChannelOutboundHandler sslHandler = (ChannelOutboundHandler) chctx.pipeline().get("ssl");
    /*  if (sslHandler == null) {
      ChannelPromise p = chctx.newPromise();
      chctx.pipeline().addFirst("handshaker", new SslHandshakeCompletionHandler(p));
      p.addListener(future -> {
        if (handler != null) {
          AsyncResult<Void> res;
          if (future.isSuccess()) {
            res = Future.succeededFuture();
          } else {
            res = Future.failedFuture(future.cause());
          }
          context.emit(res, handler);
        }
      });
      if (remoteAddress != null) {
        sslHandler = new SslHandler(helper.createEngine(vertx, remoteAddress, serverName));
        ((SslHandler) sslHandler).setHandshakeTimeout(helper.getSslHandshakeTimeout(), helper.getSslHandshakeTimeoutUnit());
      } else {
        if (helper.isSNI()) {
          sslHandler = new SniHandler(helper.serverNameMapper(vertx));
        } else {
          sslHandler = new SslHandler(helper.createEngine(vertx));
          ((SslHandler) sslHandler).setHandshakeTimeout(helper.getSslHandshakeTimeout(), helper.getSslHandshakeTimeoutUnit());
        }
      }
      chctx.pipeline().addFirst("ssl", sslHandler);
    }*/
    return this;
  }

  @Override
  public void handleInterestedOpsChanged() {
    callDrainHandler();
  }

  @Override
  public void end(Handler<AsyncResult<Void>> handler) {
    close(handler);
  }

  @Override
  public StudyFuture<Void> end() {
    return close();
  }

  @Override
  public void handleClosed() {
    pending.write(InboundBuffer.END_SENTINEL);
    super.handleClosed();
  }

  @Override
  public void handleMessage(Object msg) {
    context.executeInExecutorService(
        msg,
        o -> {
          if (!pending.write(msg)) {
            doPause();
          }
        });
  }

  private synchronized void callDrainHandler() {
    if (drainHandler != null) {
      if (!writeQueueFull()) {
        drainHandler.handle(null);
      }
    }
  }

  private class DataMessageHandler implements Handler<Object> {

    private final Handler<Buffer> dataHandler;
    private final ByteBufAllocator allocator;

    DataMessageHandler(ByteBufAllocator allocator, Handler<Buffer> dataHandler) {
      this.allocator = allocator;
      this.dataHandler = dataHandler;
    }

    @Override
    public void handle(Object event) {
      if (event instanceof ByteBuf) {
        ByteBuf byteBuf = (ByteBuf) event;
        byteBuf = safeBuffer(byteBuf, allocator);
        Buffer data = Buffer.buffer(byteBuf);
        dataHandler.handle(data);
        System.out.println("bytebuf message: " + ByteBufUtil.prettyHexDump(byteBuf));
      } else {
        throw new RuntimeException("未知的序列化消息.");
      }
    }
  }

  private class DataNetworkMessageHandler implements Handler<Object> {

    private final Handler<NetworkMessage> dataHandlerNetworkMessage;
    private final ByteBufAllocator allocator;

    DataNetworkMessageHandler(
        ByteBufAllocator allocator, Handler<NetworkMessage> dataHandlerNetworkMessage) {
      this.allocator = allocator;
      this.dataHandlerNetworkMessage = dataHandlerNetworkMessage;
    }

    @Override
    public void handle(Object event) {
      if (event instanceof NetworkMessage) {
        NetworkMessage message = (NetworkMessage) event;
        dataHandlerNetworkMessage.handle(message);
      } else if (event instanceof ByteBuf) {
        System.out.println("bytebuf message: " + ByteBufUtil.prettyHexDump((ByteBuf) event));
      } else {
        throw new RuntimeException("未知的序列化消息.");
      }
    }
  }

  private class DataObjectHandler implements Handler<Object> {

    private final Handler<Object> dataHandlerHAProxyMessage;
    private final ByteBufAllocator allocator;

    DataObjectHandler(
        ByteBufAllocator allocator, Handler<Object> dataHandlerHAProxyMessage) {
      this.allocator = allocator;
      this.dataHandlerHAProxyMessage = dataHandlerHAProxyMessage;
    }

    @Override
    public void handle(Object event) {
        dataHandlerHAProxyMessage.handle(event);
    }
  }
  private class DataHAProxyMessageHandler implements Handler<Object> {

    private final Handler<HAProxyMessage> dataHandlerHAProxyMessage;
    private final ByteBufAllocator allocator;

    DataHAProxyMessageHandler(
        ByteBufAllocator allocator, Handler<HAProxyMessage> dataHandlerHAProxyMessage) {
      this.allocator = allocator;
      this.dataHandlerHAProxyMessage = dataHandlerHAProxyMessage;
    }

    @Override
    public void handle(Object event) {
      if (event instanceof HAProxyMessage) {
        HAProxyMessage message = (HAProxyMessage) event;
        dataHandlerHAProxyMessage.handle(message);
      } else if (event instanceof ByteBuf) {
        System.out.println("bytebuf message: " + ByteBufUtil.prettyHexDump((ByteBuf) event));
      } else {
        throw new RuntimeException("未知的序列化消息.");
      }
    }
  }
}
