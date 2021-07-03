package org.jdkstack.jdkserver.tcp.core.channel.queue;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public final class Constants {
  /** . */
  public static final String TRUE = "true";
  /** . */
  public static final String FALSE = "false";
  /** . */
  public static final String LEVEL = ".level";
  /** . */
  public static final String UNIQUE = ".unique";
  /** . */
  public static final String HANDLERS = ".handlers";
  /** . */
  public static final String HANDLERS_SINGLE = "handlers";
  /** . */
  public static final String DEFAULT_LEVEL = "ALL";
  /** . */
  public static final String DEFAULT_HANDLERS_SINGLE =
      "org.jdklog.logging.handler.FileHandler,org.jdklog.logging.handler.ConsoleHandler";
  /** 日志消息中有多个大括号对. */
  public static final String BRACE = "{}";
  /** . */
  public static final String FORMATTER =
      "org.jdklog.logging.formatter.StudyJuliMessageTextFormatter";
  /** . */
  public static final String FILTER = "org.jdklog.logging.filter.StudyJuliFilter";
  /** . */
  public static final String FORMATTER_NAME = "org.jdklog.logging.handler.ConsoleHandler.formatter";
  /** . */
  public static final String CONFIG_FILE = "org.jdklog.logging.config.file";
  /** . */
  public static final String LOG_MANAGER = "org.jdklog.logging.manager";
  /** . */
  public static final String STUDY_JULI_LOG_MANAGER =
      "org.jdklog.logging.manager.StudyJuliLogManager";
  /** . */
  public static final int STACK_TRACE_ELEMENT = 3;
  /** . */
  public static final int LOOP_COUNT = 5;
  /** . */
  public static final String DATETIME_FORMAT_NAME = ".dateTimeFormat";
  /** . */
  public static final String DATETIME_FORMAT_VALUE = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  /** . */
  public static final int BATCH_SIZE = 500;
  /** . */
  public static final String JULI_CONFIG_EXCEPTION_MESSAGE =
      "SPI服务没有读取到任何实现,调试查看BuiltinClassLoader类1045行checkURL方法.";
  /** . */
  public static final long INITIAL_DELAY = 5000L;
  /** . */
  public static final long PERIOD = 5000L;
  /** . */
  public static final long MAX_EXEC_TIME = 10000L;
  /** . */
  public static final long MAX_FREE_TIME = 2000L;
  /** . */
  public static final int BATCH_BUF_SIZE = 8192;
  /** . */
  public static final int FLUSH_COUNT = 100;
  /** . */
  public static final int LEN_COUNT = 100;
  /** . */
  public static final int CAPACITY = 5000;
  /** . */
  public static final int LIMIT = 200000;
  /** . */
  public static final int MAP_CAPACITY = 16;

  private Constants() {
    //
  }
}
