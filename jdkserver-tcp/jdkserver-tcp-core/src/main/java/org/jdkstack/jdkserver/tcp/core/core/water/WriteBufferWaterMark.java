package org.jdkstack.jdkserver.tcp.core.core.water;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public class WriteBufferWaterMark extends AbstractWaterBufferMark {

  public WriteBufferWaterMark() {
    //
  }

  public WriteBufferWaterMark(int low, int high, int type) {
    super(low, high, type);
  }

  @Override
  public int low() {
    return low;
  }

  @Override
  public int high() {
    return high;
  }
}
