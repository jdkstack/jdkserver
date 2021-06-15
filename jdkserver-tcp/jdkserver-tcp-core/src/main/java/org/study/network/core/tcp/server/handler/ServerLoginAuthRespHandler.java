package org.study.network.core.tcp.server.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.study.network.codecs.Message;
import org.study.network.codecs.NetworkMessage;
import org.study.network.codecs.NetworkMessageType;

/**
 * 登录身份验证职责处理程序
 *
 * @author admin
 */
public class ServerLoginAuthRespHandler extends ChannelDuplexHandler {
  private static final Map<String, Boolean> nodeCheck = new ConcurrentHashMap<>(16);
  private static final List<String> LIST = new ArrayList<>(16);

  static {
    LIST.add("127.0.0.1");
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof Message) {
    NetworkMessage message = (NetworkMessage) msg;
    // 如果是握手请求消息，处理，其它消息透传
    int type = message.getType();
    if (type == NetworkMessageType.LOGIN_REQUEST.value()) {
      InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
      String nodeIndex = address.toString();
      NetworkMessage loginResp;
      // 重复登陆，拒绝
      if (nodeCheck.containsKey(nodeIndex)) {
        loginResp = buildResponse((byte) -1);
      } else {
        loginResp = buildResponse((byte) 0);
        String ip = address.getAddress().getHostAddress();
        boolean contains = LIST.contains(ip);
        nodeCheck.put(nodeIndex, contains);
      }
      //LOG.info("The login response is :  {}  body [  {}  ]", loginResp, loginResp.getBody());
      ctx.writeAndFlush(loginResp);
      ctx.fireChannelRead(msg);

    } else {
      ctx.fireChannelRead(msg);
    }
    }else {
      ctx.fireChannelRead(msg);
    }
  }

  private NetworkMessage buildResponse(byte result) {
    NetworkMessage message = new NetworkMessage();
    message.setType(NetworkMessageType.LOGIN_RESPONSE.value());
    message.setBody(String.valueOf(result));
    return message;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    // 删除缓存
    nodeCheck.remove(ctx.channel().remoteAddress().toString());
    ctx.close();
    ctx.fireExceptionCaught(cause);
    cause.printStackTrace();
  }

  private static class HeartBeatTask implements Runnable {
    private final ChannelHandlerContext ctx;

    public HeartBeatTask(final ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }

    @Override
    public void run() {
      NetworkMessage heatBeat = buildHeatBeat();
    //  LOG.info("Server send heart beat messsage to client : ---> {}", heatBeat);
      ctx.writeAndFlush(heatBeat);
    }

    private NetworkMessage buildHeatBeat() {
      NetworkMessage message = new NetworkMessage();
      message.setType(NetworkMessageType.PING.value());
      return message;
    }
  }
}
