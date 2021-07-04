package org.jdkstack.jdkserver.tcp.core.common;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public final class SocketUtils {

  public static void connect(
      final Socket socket, final SocketAddress remoteAddress, final int timeout)
      throws IOException {
    socket.connect(remoteAddress, timeout);
  }

  public static void bind(final Socket socket, final SocketAddress bindpoint) throws IOException {
    socket.bind(bindpoint);
  }

  public static boolean connect(
      final SocketChannel socketChannel, final SocketAddress remoteAddress) throws IOException {
    return socketChannel.connect(remoteAddress);
  }

  public static void bind(final SocketChannel socketChannel, final SocketAddress address)
      throws IOException {
    socketChannel.bind(address);
  }

  public static SocketChannel accept(final ServerSocketChannel serverSocketChannel)
      throws IOException {
    return serverSocketChannel.accept();
  }

  public static void bind(final DatagramChannel networkChannel, final SocketAddress address)
      throws IOException {
    networkChannel.bind(address);
  }

  public static SocketAddress localSocketAddress(final ServerSocket socket) {
    return socket.getLocalSocketAddress();
  }
}
