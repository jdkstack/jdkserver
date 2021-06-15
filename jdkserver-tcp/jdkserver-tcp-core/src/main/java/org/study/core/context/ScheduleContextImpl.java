package org.study.core.context;

import java.util.concurrent.ScheduledExecutorService;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-02-28 11:14
 * @since 2021-02-28 11:14:00
 */
public class ScheduleContextImpl extends AbstractStudyContext {

  private final ScheduledExecutorService executorService;

  public ScheduleContextImpl(ScheduledExecutorService executorService) {
    this.executorService = executorService;
  }

  public ScheduledExecutorService getExecutorService() {
    return executorService;
  }
}
