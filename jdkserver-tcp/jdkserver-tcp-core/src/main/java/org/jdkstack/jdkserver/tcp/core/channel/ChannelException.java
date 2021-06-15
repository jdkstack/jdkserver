package org.jdkstack.jdkserver.tcp.core.channel;

public class ChannelException extends RuntimeException {

  public ChannelException() {
    //
  }

  public ChannelException(String message, Throwable cause) {
    super(message, cause);
  }

  public ChannelException(String message) {
    super(message);
  }

  public ChannelException(Throwable cause) {
    super(cause);
  }

  protected ChannelException(String message, Throwable cause, boolean shared) {
    super(message, cause, false, true);
    assert shared;
  }
}
