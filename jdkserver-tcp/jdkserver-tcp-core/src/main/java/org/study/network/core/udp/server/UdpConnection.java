package org.study.network.core.udp.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import org.study.core.future.Handler;
import org.study.network.core.socket.Buffer;
import org.study.network.core.socket.ConnectionBase;
import org.study.network.core.tcp.client.handler.StudyClientHandler;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-19 20:58
 * @since 2021-01-19 20:58:00
 */
public class UdpConnection extends ConnectionBase {
  private long demand;
  private Handler<Object> messageHandler;

  public UdpConnection(ChannelHandlerContext channel) {
    super(channel);
    this.demand = Long.MAX_VALUE;
  }

  @Override
  public void handleInterestedOpsChanged() {}

  @Override
  public void handleException(Throwable t) {
    super.handleException(t);
    Handler<Throwable> handler = null;
    synchronized (UdpConnection.this) {
      // handler = exceptionHandler;
    }
    if (handler != null) {
      handler.handle(t);
    }
  }

  @Override
  public void handleClosed() {
    super.handleClosed();
    Handler<Void> handler = null;
    synchronized (UdpConnection.this) {
      // handler = endHandler;
    }
    if (handler != null) {
      handler.handle(null);
    }
  }

  public void handler(Handler<Buffer> dataHandler) {
    messageHandler = new DataMessageHandler(channelHandlerContext().alloc(), dataHandler);
  }

  public void handleMessage(Object msg) {
    Handler<Object> handler = messageHandler;
    if (handler != null) {
      handler.handle(msg);
    }

    /*    if (msg instanceof DatagramPacket) {
    DatagramPacket packet = (DatagramPacket) msg;
    ByteBuf buf = packet.content();
    // ByteBuf buf = (ByteBuf) msg;
    String s = buf.toString(CharsetUtil.UTF_8);
    System.out.println(s);
    DatagramPacket datagramPacket =
        new DatagramPacket(
            Unpooled.copiedBuffer(
                "Hello，我是Server，我的时间戳是" + System.currentTimeMillis(), CharsetUtil.UTF_8),
            packet.sender());

    this.writeToChannel(datagramPacket); // 只能向客户端写一次.

    */
    /* if (buf.isDirect()) {
      buf = StudyHandler.safeBuffer(buf, chctx.alloc());
    }
    packet.sender();
    Buffer.buffer(buf);*/
    /*
     */
    /*
    }*/
    /*
    }*/
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
        byteBuf = StudyClientHandler.safeBuffer(byteBuf, allocator);
        Buffer data = Buffer.buffer(byteBuf);
        dataHandler.handle(data);
      }
    }
  }
}
