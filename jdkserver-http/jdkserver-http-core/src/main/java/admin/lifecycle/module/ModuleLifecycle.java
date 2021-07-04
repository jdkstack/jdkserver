package admin.lifecycle.module;

import admin.lifecycle.Lifecycle;
import admin.lifecycle.LifecycleListener;

/**
 * 模块的生命周期
 *
 * @author admin
 */
public interface ModuleLifecycle extends Lifecycle {

  ModuleLifecycleState getModuleLifecycleState();

  /**
   * 添加生命周期侦听器
   *
   * @param listener 侦听器
   */
  void addLifecycleListener(LifecycleListener listener);

  /**
   * 删除生命周期侦听器
   *
   * @param listener 侦听器
   */
  void removeLifecycleListener(LifecycleListener listener);

  LifecycleListener[] findLifecycleListeners();

  String getStateName();

  void fireLifecycleEvent(String type, Object data);

  void setLifecycleEvent(ModuleLifecycleState state);

  void setLifecycleEvent(ModuleLifecycleState state, Object data);
}
