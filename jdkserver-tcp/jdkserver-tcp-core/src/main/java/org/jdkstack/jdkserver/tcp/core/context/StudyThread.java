package org.jdkstack.jdkserver.tcp.core.context;

import java.util.concurrent.TimeUnit;

/**
 * 自定义线程,便于系统内线程的监控,比如设置自定义的线程名,线程计数等.
 *
 * <p>继承Netty的FastThreadLocalThread.
 *
 * @author admin
 */
public final class StudyThread extends Thread {

  /** 线程类型. */
  private final int threadType;
  /** 线程最大的执行时间. */
  private final long maxExecTime;
  /** 线程最大的执行单位. */
  private final TimeUnit maxExecTimeUnit;
  /** 线程开始运行的时间(毫秒). */
  private long execStart;
  /** 线程运行的上下文环境. */
  private StudyContext context;

  /**
   * 自定义线程.
   *
   * <p>参数需要加final修饰.
   *
   * @param targetParam 线程任务.
   * @param nameParam 线程名.
   * @param threadTypeParam 线程类型.
   * @param maxExecTimeParam 线程最大执行时间.
   * @param maxExecTimeUnitParam 线程最大执行时间单位.
   * @author admin
   */
  public StudyThread(
      final Runnable targetParam,
      final String nameParam,
      final int threadTypeParam,
      final long maxExecTimeParam,
      final TimeUnit maxExecTimeUnitParam) {
    super(targetParam, nameParam);
    this.threadType = threadTypeParam;
    this.maxExecTime = maxExecTimeParam;
    this.maxExecTimeUnit = maxExecTimeUnitParam;
  }

  /**
   * 返回线程的最大执行时间单位.
   *
   * @return 返回线程的最大执行时间单位.
   * @author admin
   */
  public TimeUnit maxExecTimeUnit() {
    return maxExecTimeUnit;
  }

  /**
   * 返回线程的类型.
   *
   * @return 返回线程的类型.
   * @author admin
   */
  public int threadType() {
    return threadType;
  }

  /**
   * 获取线程运行的开始时间.
   *
   * @return 返回线程的开始运行时间.
   * @author admin
   */
  public long startTime() {
    return execStart;
  }

  /**
   * 获取线程的最大运行时间.
   *
   * @return 返回线程的最大运行时间.
   * @author admin
   */
  public long maxExecTime() {
    return maxExecTime;
  }

  /**
   * 获取线程的上下文对象.
   *
   * @return 返回线程的上习武对象.
   * @author admin
   */
  StudyContext context() {
    return context;
  }

  /**
   * 当线程开始时,开始时间设置成当前系统的时间戳毫秒数.
   *
   * @author admin
   */
  private void executeStart() {
    if (context == null) {
      execStart = System.currentTimeMillis();
    }
  }

  private void executeEnd() {
    if (context == null) {
      execStart = 0;
    }
  }

  /**
   * 给线程设置一个上下文环境对象.
   *
   * <p>代表线程正在运行着.
   *
   * @param contextParam 上下文对象.
   * @author admin
   */
  void beginEmission(final StudyContext contextParam) {
    executeStart();
    this.context = contextParam;
  }

  /**
   * 将线程上下文环境对象设置为空.
   *
   * <p>代表线程运行完毕.
   *
   * @author admin
   */
  void endEmission() {
    context = null;
    executeEnd();
  }
}
