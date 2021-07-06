package org.jdkstack.jdkserver.tcp.core.context;

import org.jdkstack.jdkserver.tcp.core.api.context.StudyContext;
import org.jdkstack.jdkserver.tcp.core.api.context.StudyWorker;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.Handler;

public abstract class AbstractStudyContext implements StudyContext {

  public final <T> void dispatch(T event, Handler<T> handler) {
    try {
      beginDispatch();
      handler.handle(event);
    } catch (Exception t) {
      t.printStackTrace();
    } finally {
      endDispatch();
    }
  }

  public final void dispatch(Runnable handler) {
    try {
      beginDispatch();
      handler.run();
    } catch (Exception t) {
      t.printStackTrace();
    } finally {
      endDispatch();
    }
  }

  public final <T> void dispatch(T event, StudyWorker<T> handler) {
    try {
      beginDispatch();
      handler.handle(event);
    } catch (Exception t) {
      t.printStackTrace();
    } finally {
      endDispatch();
    }
  }

  public void beginDispatch() {
    StudyThread th = (StudyThread) Thread.currentThread();
    th.beginEmission(this);
  }

  public void endDispatch() {
    StudyThread th = (StudyThread) Thread.currentThread();
    th.endEmission();
  }
}
