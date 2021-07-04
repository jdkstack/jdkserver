package study.network.core.tcp.server.example;

import study.network.core.tcp.server.option.RpcServerOptions;
import study.network.core.tcp.server.rpc.AbstractRpcServer;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-23 13:43
 * @since 2021-01-23 13:43:00
 */
public class ServerExamples extends AbstractRpcServer {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param args args
   * @author admin
   */
  public static void main(final String[] args) throws Exception {
    ServerExamples serverExamples = new ServerExamples();
    RpcServerOptions rpcServerOptions = new RpcServerOptions();
    rpcServerOptions.setLocalHost("127.0.0.1");
    rpcServerOptions.setLocalPort(20000);
    serverExamples.init(rpcServerOptions);
    serverExamples.initServer(rpcServerOptions);

    Thread.sleep(999999999);
  }
}
