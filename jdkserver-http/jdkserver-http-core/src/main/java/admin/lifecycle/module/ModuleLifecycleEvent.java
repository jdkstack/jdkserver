package admin.lifecycle.module;

import admin.lifecycle.Lifecycle;
import admin.lifecycle.LifecycleEvent;

/**
 * 模块生命周期事件
 *
 * @author admin
 */
public class ModuleLifecycleEvent extends LifecycleEvent {

  private static final long serialVersionUID = 1L;

  /**
   * 模块生命周期事件
   *
   * @param lifecycle 生命周期
   * @param type 类型
   * @param data 数据
   */
  public ModuleLifecycleEvent(Lifecycle lifecycle, String type, Object data) {
    super(lifecycle, type, data);
  }
}
