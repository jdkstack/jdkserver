package org.jdkstack.jdkserver.tcp.core.ssl.handler;

import java.nio.ByteBuffer;
import javax.net.ssl.SSLEngineResult;

public class WrapperResult {
  public SSLEngineResult result;

  /* if passed in buffer was not big enough then the
   * a reallocated buffer is returned here
   */
  public ByteBuffer buf;
}
