package study.network.codecs;

import java.util.Map;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-02-22 19:10
 * @since 2021-02-22 19:10:00
 */
public interface Message {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return int crcCode.
   * @author admin
   */
  int getCrcCode();

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param crcCode .
   * @return Message Message.
   * @author admin
   */
  Message setCrcCode(final int crcCode);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return int int.
   * @author admin
   */
  int getLength();

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param length .
   * @return Message Message.
   * @author admin
   */
  Message setLength(final int length);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return long long.
   * @author admin
   */
  long getSessionId();

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param sessionId .
   * @return Message Message.
   * @author admin
   */
  Message setSessionId(final long sessionId);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return int int.
   * @author admin
   */
  int getType();

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param type .
   * @return Message Message.
   * @author admin
   */
  Message setType(final int type);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return int int.
   * @author admin
   */
  int getPriority();

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param priority .
   * @return Message Message.
   * @author admin
   */
  Message setPriority(final int priority);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return int int.
   * @author admin
   */
  int getTimeout();

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param timeout .
   * @return Message Message.
   * @author admin
   */
  Message setTimeout(final int timeout);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return String String.
   * @author admin
   */
  String getCustomMsg();

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param customMsg .
   * @return Message Message.
   * @author admin
   */
  Message setCustomMsg(final String customMsg);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param key .
   * @return Object Message.
   * @author admin
   */
  Object getAttachment(final String key);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return Map.
   * @author admin
   */
  Map<String, Object> getAttachments();

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param key .
   * @param attachment .
   * @return Message.
   * @author admin
   */
  Message attachment(final String key, final Object attachment);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return String String.
   * @author admin
   */
  String getBody();

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param body .
   * @return Message.
   * @author admin
   */
  Message setBody(final String body);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return long long.
   * @author admin
   */
  long getCreateTime();

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param createTime .
   * @return Message.
   * @author admin
   */
  Message setCreateTime(final long createTime);
}
