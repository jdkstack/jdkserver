package org.study.network.core.udp.server;

import io.netty.channel.Channel;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.util.concurrent.Future;
import java.net.SocketAddress;
import org.study.network.core.socket.Buffer;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-19 20:23
 * @since 2021-01-19 20:23:00
 */
public interface UdpServer {

  SocketAddress sender();

  Buffer data();

  Future<Channel> listenUdpServer(int port, String host, InternetProtocolFamily family);
}
