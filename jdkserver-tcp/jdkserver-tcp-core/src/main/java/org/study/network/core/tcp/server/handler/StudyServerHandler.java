package org.study.network.core.tcp.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.function.Function;
import org.study.core.future.Handler;
import org.study.network.codecs.Message;
import org.study.network.codecs.NetworkMessage;
import org.study.network.codecs.NetworkMessageType;
import org.study.network.core.common.pool.Connection;
import org.study.network.core.socket.ConnectionBase;
import org.study.network.core.tcp.client.pool.TcpClientConnection;
import org.study.network.core.tcp.server.manager.ServerConnectionPoolManager;
import org.study.network.core.tool.RemotingUtil;

public final class StudyServerHandler<C extends ConnectionBase> extends ChannelDuplexHandler {

  private final Function<ChannelHandlerContext, C> connectionFactory;
  private C conn;
  private Handler<C> addHandler;
  private Handler<C> removeHandler;

  private StudyServerHandler(Function<ChannelHandlerContext, C> connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  public static <C extends ConnectionBase> StudyServerHandler<C> create(
      Function<ChannelHandlerContext, C> connectionFactory) {
    return new StudyServerHandler<>(connectionFactory);
  }

  public static ByteBuf safeBuffer(ByteBufHolder holder, ByteBufAllocator allocator) {
    return safeBuffer(holder.content(), allocator);
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

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

    Channel channel = ctx.channel();
    if (channel != null) {
      System.out.println(
          "channel> channelRegistered" + channel.localAddress() + channel.remoteAddress());
    } else {
      System.out.println("channel> channelRegistered!");
    }
    super.channelRegistered(ctx);
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    Channel channel = ctx.channel();
    if (channel != null) {
      System.out.println(
          "channel> channelUnregistered" + channel.localAddress() + channel.remoteAddress());
    } else {
      System.out.println("channel> channelUnregistered!");
    }
    super.channelUnregistered(ctx);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    Channel channel = ctx.channel();
    if (channel != null) {
      System.out.println("channel> 上线" + channel.localAddress() + channel.remoteAddress());
    } else {
      System.out.println("channel> 上线!");
    }
    ServerConnectionPoolManager dscm = ServerConnectionPoolManager.getInstance();
    // /127.0.0.1:19999
    String s = RemotingUtil.parseRemoteAddress(channel);
    String[] split = s.split(":");
    Connection connection = new TcpClientConnection(channel);
    dscm.createTcpClientConnection(split[0], connection);
    super.channelActive(ctx);
  }

  private NetworkMessage buildLoginReq() {
    NetworkMessage message = new NetworkMessage();
    message.setPriority((byte) 0);
    message.setSessionId(UUID.randomUUID().getMostSignificantBits());
    message.setType(NetworkMessageType.LOGIN_REQUEST.value());
    message.setBody("{xxxxx:ddddd}");
    message.setLength(19999);
    return message;
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    Channel channel = ctx.channel();
    if (channel != null) {
      System.out.println(
          "channel> handlerRemoved" + channel.localAddress() + channel.remoteAddress());
    } else {
      System.out.println("channel> handlerRemoved!");
    }
    super.handlerRemoved(ctx);
  }

  @Override
  public void connect(
      ChannelHandlerContext ctx,
      SocketAddress remoteAddress,
      SocketAddress localAddress,
      ChannelPromise promise)
      throws Exception {
    Channel channel = ctx.channel();
    if (channel != null) {
      System.out.println("channel> connect" + channel.localAddress() + channel.remoteAddress());
    } else {
      System.out.println("channel> connect!");
    }
    super.connect(ctx, remoteAddress, localAddress, promise);
  }

  @Override
  public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    Channel channel = ctx.channel();
    if (channel != null) {
      System.out.println("channel> disconnect" + channel.localAddress() + channel.remoteAddress());
    } else {
      System.out.println("channel> disconnect!");
    }
    super.disconnect(ctx, promise);
  }

  @Override
  public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    Channel channel = ctx.channel();
    if (channel != null) {
      System.out.println("channel> deregister" + channel.localAddress() + channel.remoteAddress());
    } else {
      System.out.println("channel> deregister!");
    }
    super.deregister(ctx, promise);
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    Channel channel = ctx.channel();
    if (channel != null) {
      System.out.println(
          "channel> handlerAdded" + channel.localAddress() + channel.remoteAddress());
    } else {
      System.out.println("channel> handlerAdded!");
    }
    setConnection(connectionFactory.apply(ctx));
  }

  /**
   * Set an handler to be called when the connection is set on this handler.
   *
   * @param handler the handler to be notified
   * @return this
   */
  public StudyServerHandler<C> addHandler(Handler<C> handler) {
    this.addHandler = handler;
    return this;
  }

  /**
   * Set an handler to be called when the connection is unset from this handler.
   *
   * @param handler the handler to be notified
   * @return this
   */
  public StudyServerHandler<C> removeHandler(Handler<C> handler) {
    this.removeHandler = handler;
    return this;
  }

  public C getConnection() {
    return conn;
  }

  /**
   * Set the connection, this is called when the channel is added to the pipeline.
   *
   * @param connection the connection
   */
  private void setConnection(C connection) {
    conn = connection;
    if (addHandler != null) {
      addHandler.handle(connection);
    }
  }

  @Override
  public void channelWritabilityChanged(ChannelHandlerContext ctx) {
    C conn = getConnection();
    conn.handleInterestedOpsChanged();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext chctx, final Throwable t) {
    t.printStackTrace();
    C connection = getConnection();
    if (connection != null) {
      connection.handleException(t);
    }
    chctx.close();
  }

  @Override
  public void channelInactive(ChannelHandlerContext chctx) {
    Channel channel = chctx.channel();
    if (channel != null) {
      System.out.println("channel> 离线" + channel.localAddress() + channel.remoteAddress());
    } else {
      System.out.println("channel> 离线!");
    }
    if (removeHandler != null) {
      removeHandler.handle(conn);
    }
    ServerConnectionPoolManager dscm = ServerConnectionPoolManager.getInstance();
    // /127.0.0.1:19999
    String s = RemotingUtil.parseRemoteAddress(channel);
    String[] split = s.split(":");
    Connection connection = new TcpClientConnection(channel);
    dscm.removeTcpClientConnection(split[0], connection);

    // if (connectionPool != null) {
    //  connectionPool.remove(connection);
    // }
    conn.handleClosed();
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    conn.endReadAndFlush();
  }

  @Override
  public void channelRead(ChannelHandlerContext chctx, Object msg) {
    if (msg instanceof Message) {
      final Message message = (Message) msg;
      // 握手成功，主动发送心跳消息
      final int type = message.getType();
      if (type == 121) {
        conn.read(msg);
      } else {
        chctx.fireChannelRead(msg);
      }
    } else {
      chctx.fireChannelRead(msg);
    }
  }

  @Override
  public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    Channel channel = ctx.channel();
    if (channel != null) {
      System.out.println("channel> close!" + channel.localAddress() + channel.remoteAddress());
    } else {
      System.out.println("channel> close!");
    }
    conn.close(promise);
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent && ((IdleStateEvent) evt).state() == IdleState.ALL_IDLE) {
      // conn.handleIdle();
      // 需要统计，不能直接关闭.
    } else {
      ctx.fireUserEventTriggered(evt);
    }
    Channel channel = ctx.channel();
    if (channel != null) {
      System.out.println(
          "channel> userEventTriggered!" + channel.localAddress() + channel.remoteAddress());
    } else {
      System.out.println("channel> userEventTriggered!");
    }
  }
}