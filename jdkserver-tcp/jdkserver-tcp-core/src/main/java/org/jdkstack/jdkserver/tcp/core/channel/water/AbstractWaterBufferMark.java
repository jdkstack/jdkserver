package org.jdkstack.jdkserver.tcp.core.channel.water;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public abstract class AbstractWaterBufferMark implements WaterBufferMark {

  protected static final WriteBufferWaterMark COUNT_WATER_MARK =
      new WriteBufferWaterMark(1000, 1500, 0);
  protected static final WriteBufferWaterMark SIZE_WATER_MARK =
      new WriteBufferWaterMark(1 * 1024 * 1024, 2 * 1024 * 1024, 1);

  protected AbstractWaterBufferMark() {
    //
  }
}
