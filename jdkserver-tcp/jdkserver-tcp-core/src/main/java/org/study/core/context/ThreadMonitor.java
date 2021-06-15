package org.study.core.context;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 定时检查线程的运行时间.
 *
 * <p>当线程的run方法运行时间不超过组大阻塞时间blockTime,但是超过了线程运行的最大时间.
 *
 * <p>打印危险的消息,如果超过最大阻塞时间,打算线程堆栈信息,看看线程的run中运行的代码是什么.
 *
 * @author admin
 */
public class ThreadMonitor implements Monitor {
  /** 保存所有的线程,key是线程名字,value是线程. */
  private final Map<String, StudyThread> threads = new WeakHashMap<>();
  /** 最大阻塞时间. */
  private final long blockTime;

  private ScheduledFuture<?> scheduledFuture;

  /**
   * 参数需要加final修饰,避免被修改.
   *
   * <p>并且参数名和类的变量名不能是一样的,避免歧义.
   *
   * @param blockTimeParam 线程最大运行的阻塞时间.
   * @author admin
   */
  public ThreadMonitor(final long blockTimeParam) {
    this.blockTime = blockTimeParam;
  }

  /**
   * 添加要监控的线程,这个线程必须是StudyThread.
   *
   * <p>线程必须继承netty的fast thread.
   *
   * @param thread 要监控的线程.
   * @author admin
   */
  @Override
  public void registerThread(final StudyThread thread) {
    threads.put(thread.getName(), thread);
  }

  /**
   * 定时监控的线程的运行时间.
   *
   * <p>标志位用于检测是否停止timerTask,在关闭定时器之前,停止run方法的执行 .
   *
   * @author admin
   */
  @Override
  public void monitor(final WorkerContext context) {
    ScheduledExecutorService scheduledExecutorService = context.getScheduledExecutorService();
    Runnable runnable =
        () -> {
          try {
            // 当前系统时间毫秒数.
            long currentTimeMillis = System.currentTimeMillis();
            // 原打算使用异步的方式,但是感觉不太合理.
            for (Map.Entry<String, StudyThread> entry : threads.entrySet()) {
              StudyThread studyThread = entry.getValue();
              // 线程开始执行时间的毫秒数.
              long execStart = studyThread.startTime();
              // 线程执行的时间.
              long duration = currentTimeMillis - execStart;
              // 线程允许的最大执行时间.
              final long maxExecTime = studyThread.maxExecTime();
              // 线程开始时间不为0,表示线程运行.
              // 如果大于线程最大执行时间.
              if (execStart != 0 && duration >= maxExecTime) {
                if (duration <= blockTime) {
                  // 如果小于等于阻塞时间,打印线程异常的注意信息.
                  // LOGGER.info("线程{}锁定{}毫秒,限制{}毫秒", studyThread, duration, maxExecTime);
                } else {
                  // 如果大于阻塞时间,打印线程可能的异常信息.
                  StackTraceElement[] stackTraces = studyThread.getStackTrace();
                  for (StackTraceElement stackTrace : stackTraces) {
                    // LOGGER.error("线程运行异常?堆栈信息:{}", stackTrace);
                  }
                }
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        };
    scheduledFuture =
        scheduledExecutorService.scheduleAtFixedRate(runnable, 5000L, 5000L, TimeUnit.MILLISECONDS);
  }

  /**
   * 用于关闭定时器.
   *
   * <p>关闭定时器之前,先将标志位设置为true,停止timerTask的run方法.
   *
   * @author admin
   */
  @Override
  public void close() {
    if (scheduledFuture != null) {
      scheduledFuture.cancel(true);
    }
  }
}
