package study.network.core.tcp.client.example;

import study.network.core.tcp.client.option.RpcClientOptions;
import study.network.core.tcp.client.rpc.AbstractRpcClient;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-22 16:57
 * @since 2021-01-22 16:57:00
 */
public class ClientExamples extends AbstractRpcClient {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static void main(String[] args) throws Exception {
    ClientExamples clientExamples = new ClientExamples();
    RpcClientOptions rpcClientOptions = new RpcClientOptions();
    rpcClientOptions.setRemoteHost("127.0.0.1");
    rpcClientOptions.setRemotePort(20000);
    rpcClientOptions.setClientAlias("www.client2.com");
    rpcClientOptions.setServerAlias("www.server2.com");
    rpcClientOptions.setLocalHost("127.0.0.1");
    rpcClientOptions.setLocalPort(17999);
    rpcClientOptions.setCount(1);
    clientExamples.init(rpcClientOptions);
    clientExamples.initClient(rpcClientOptions);
    Thread.sleep(99999999);
  }
}
