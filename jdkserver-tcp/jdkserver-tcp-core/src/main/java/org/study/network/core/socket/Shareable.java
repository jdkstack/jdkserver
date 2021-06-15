package org.study.network.core.socket;

public interface Shareable {

  /**
   * Returns a copy of the object. Only mutable objects should provide a custom implementation of
   * the method.
   */
  default Shareable copy() {
    return this;
  }
}
