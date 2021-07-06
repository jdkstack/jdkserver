package org.jdkstack.jdkserver.tcp.core.core.codecs;

import java.util.HashMap;
import java.util.Map;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-02-22 19:21
 * @since 2021-02-22 19:21:00
 */
public abstract class AbstractMessage implements Message {
  /** CRC校验码. */
  private final Map<String, Object> attachments = new HashMap<>(16);
  /** CRC校验码. */
  private int crcCode = 0xabef0101;
  /** CRC校验码. */
  private int length;
  /** CRC校验码. */
  private long sessionId;
  /** CRC校验码. */
  private int type;
  /** CRC校验码. */
  private int priority;
  /** CRC校验码. */
  private long createTime;
  /** CRC校验码. */
  private int timeout;
  /** CRC校验码. */
  private String body;
  /** CRC校验码. */
  private String customMsg = "{}";

  @Override
  public long getCreateTime() {
    return this.createTime;
  }

  @Override
  public Message setCreateTime(final long createTime) {
    this.createTime = createTime;
    return this;
  }

  @Override
  public int getCrcCode() {
    return this.crcCode;
  }

  @Override
  public Message setCrcCode(final int crcCodeParam) {
    this.crcCode = crcCodeParam;
    return this;
  }

  @Override
  public int getLength() {
    return this.length;
  }

  @Override
  public Message setLength(final int lengthParam) {
    this.length = lengthParam;
    return this;
  }

  @Override
  public long getSessionId() {
    return this.sessionId;
  }

  @Override
  public Message setSessionId(final long sessionIdParam) {
    this.sessionId = sessionIdParam;
    return this;
  }

  @Override
  public int getType() {
    return this.type;
  }

  @Override
  public Message setType(final int typeParam) {
    this.type = typeParam;
    return this;
  }

  @Override
  public int getPriority() {
    return this.priority;
  }

  @Override
  public Message setPriority(final int priorityParam) {
    this.priority = priorityParam;
    return this;
  }

  @Override
  public int getTimeout() {
    return this.timeout;
  }

  @Override
  public Message setTimeout(final int timeoutParam) {
    this.timeout = timeoutParam;
    return this;
  }

  @Override
  public String getCustomMsg() {
    return this.customMsg;
  }

  @Override
  public Message setCustomMsg(final String customMsgParam) {
    this.customMsg = customMsgParam;
    return this;
  }

  @Override
  public Object getAttachment(final String keyParam) {
    return this.attachments.get(keyParam);
  }

  @Override
  public Map<String, Object> getAttachments() {
    // 直接引用,可能存在bug,新创建一个对象,包装数据.
    return new HashMap<>(this.attachments);
  }

  @Override
  public Message attachment(final String keyParam, final Object attachmentParam) {
    this.attachments.put(keyParam, attachmentParam);
    return this;
  }

  @Override
  public String getBody() {
    return this.body;
  }

  @Override
  public Message setBody(final String bodyParam) {
    this.body = bodyParam;
    return this;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @return String.
   * @author admin
   */
  @Override
  public String toString() {
    return "AbstractMessage{"
        + "crcCode="
        + this.crcCode
        + ", length="
        + this.length
        + ", sessionId="
        + this.sessionId
        + ", type="
        + this.type
        + ", priority="
        + this.priority
        + ", timeout="
        + this.timeout
        + ", body="
        + this.body
        + ", customMsg="
        + this.customMsg
        + ", attachments="
        + this.attachments
        + "}";
  }
}
