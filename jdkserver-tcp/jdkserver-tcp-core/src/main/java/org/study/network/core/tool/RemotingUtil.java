package org.study.network.core.tool;

import io.netty.channel.Channel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class RemotingUtil {

  public static String parseRemoteAddress(final Channel channel) {
    if (null == channel) {
      return StringUtils.EMPTY;
    }
    final SocketAddress remote = channel.remoteAddress();
    return doParse(remote != null ? remote.toString().trim() : StringUtils.EMPTY);
  }

  public static String parseLocalAddress(final Channel channel) {
    if (null == channel) {
      return StringUtils.EMPTY;
    }
    final SocketAddress local = channel.localAddress();
    return doParse(local != null ? local.toString().trim() : StringUtils.EMPTY);
  }

  public static String parseRemoteIP(final Channel channel) {
    if (null == channel) {
      return StringUtils.EMPTY;
    }
    final InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
    if (remote != null) {
      return remote.getAddress().getHostAddress();
    }
    return StringUtils.EMPTY;
  }

  public static String parseRemoteHostName(final Channel channel) {
    if (null == channel) {
      return StringUtils.EMPTY;
    }
    final InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
    if (remote != null) {
      return remote.getAddress().getHostName();
    }
    return StringUtils.EMPTY;
  }

  public static String parseLocalIP(final Channel channel) {
    if (null == channel) {
      return StringUtils.EMPTY;
    }
    final InetSocketAddress local = (InetSocketAddress) channel.localAddress();
    if (local != null) {
      return local.getAddress().getHostAddress();
    }
    return StringUtils.EMPTY;
  }

  public static int parseRemotePort(final Channel channel) {
    if (null == channel) {
      return -1;
    }
    final InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
    if (remote != null) {
      return remote.getPort();
    }
    return -1;
  }

  public static int parseLocalPort(final Channel channel) {
    if (null == channel) {
      return -1;
    }
    final InetSocketAddress local = (InetSocketAddress) channel.localAddress();
    if (local != null) {
      return local.getPort();
    }
    return -1;
  }

  public static String parseSocketAddressToString(SocketAddress socketAddress) {
    if (socketAddress != null) {
      return doParse(socketAddress.toString().trim());
    }
    return StringUtils.EMPTY;
  }

  public static String parseSocketAddressToHostIp(SocketAddress socketAddress) {
    final InetSocketAddress addrs = (InetSocketAddress) socketAddress;
    if (addrs != null) {
      InetAddress addr = addrs.getAddress();
      if (null != addr) {
        return addr.getHostAddress();
      }
    }
    return StringUtils.EMPTY;
  }

  private static String doParse(String addr) {
    if (StringUtils.isBlank(addr)) {
      return StringUtils.EMPTY;
    }
    if (addr.charAt(0) == '/') {
      return addr.substring(1);
    } else {
      int len = addr.length();
      for (int i = 1; i < len; ++i) {
        if (addr.charAt(i) == '/') {
          return addr.substring(i + 1);
        }
      }
      return addr;
    }
  }
}
