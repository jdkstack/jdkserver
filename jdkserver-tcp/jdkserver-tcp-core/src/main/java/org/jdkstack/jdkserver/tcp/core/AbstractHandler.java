package org.jdkstack.jdkserver.tcp.core;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jdkstack.jdkserver.tcp.core.channel.queue.StudyWorker;
import org.study.core.context.Monitor;
import org.study.core.context.StudyThreadFactory;
import org.study.core.context.ThreadMonitor;
import org.study.core.context.WorkerContext;
import org.study.core.context.WorkerStudyContextImpl;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public abstract class AbstractHandler implements Handler {
  /** 线程阻塞的最大时间时10秒.如果不超过15秒,打印warn.如果超过15秒打印异常堆栈. */
  private static final Monitor CHECKER = new ThreadMonitor(15000L);
  /** 线程池. */
  private static final ExecutorService LOG_PRODUCER =
      new ThreadPoolExecutor(
          1,
          1,
          0,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(5000),
          new StudyThreadFactory("log-producer", CHECKER),
          new StudyRejectedPolicy());


  /** 线程池. CallerRunsPolicy 拒绝策略不丢数据,因为在主线程上执行. */
  private static final ExecutorService LOG_CONSUMER =
      new ThreadPoolExecutor(
          1,
          1,
          0,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(5000),
          new StudyThreadFactory("log-consumer", CHECKER),
          new StudyRejectedPolicy());

  /** 服务器端的定时调度线程池. */
  private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE =
      new ScheduledThreadPoolExecutor(3, new StudyThreadFactory("study_scheduled", null));

  /** 工作任务上下文. */
  protected static final WorkerContext LOG_PRODUCER_CONTEXT =
      new WorkerStudyContextImpl(LOG_PRODUCER, SCHEDULED_EXECUTOR_SERVICE);
  /** 工作任务上下文. */
  protected static final WorkerContext LOG_CONSUMER_CONTEXT =
      new WorkerStudyContextImpl(LOG_CONSUMER, SCHEDULED_EXECUTOR_SERVICE);

  static {
    // 线程监控任务.
    CHECKER.monitor(LOG_PRODUCER_CONTEXT);
    // 线程监控任务.
    CHECKER.monitor(LOG_CONSUMER_CONTEXT);
  }
}
