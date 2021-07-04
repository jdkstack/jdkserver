package study.network.codecs;

/**
 * 在网路中传输消息时,定义消息的类型.
 *
 * <p>这个类型没有明确的标准,可以按照需要定义即可.
 *
 * @author admin
 */
public enum NetworkMessageType {
  /** 请求消息. */
  REQUEST(0),
  /** 响应消息. */
  RESPONSE(1),
  /** 消息. */
  ONE_WAY(2),
  /** 登陆请求消息. */
  LOGIN_REQUEST(3),
  /** 登陆响应消息. */
  LOGIN_RESPONSE(4),
  /** PING消息. */
  PING(5),
  /** PONG消息. */
  PONG(6),
  /** 消息. */
  ELECTION_BEFORE_REQUEST(7),
  /** 消息. */
  ELECTION_REQUEST(8),
  /** 消息. */
  ELECTION_BEFORE_RESPONSE(9),
  /** 消息. */
  ELECTION_RESPONSE(10),
  /** 消息. */
  ELECTION_H_PING(11),
  /** 消息. */
  ELECTION_H_PONG(12);

  /** 消息类型的值. */
  private final int value;

  NetworkMessageType(final int valueParam) {
    this.value = valueParam;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return 返回消息类型的值.
   * @author admin
   */
  public int value() {
    return this.value;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return 重写类的默认toString.
   * @author admin
   */
  @Override
  public String toString() {
    return "NetworkMessageType{value=" + this.value + "}";
  }
}
