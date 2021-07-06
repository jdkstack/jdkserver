package org.jdkstack.jdkserver.tcp.core.core.water;

import org.jdkstack.jdkserver.tcp.core.api.core.water.WaterBufferMark;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public abstract class AbstractWaterBufferMark implements WaterBufferMark {
  protected static final int DEFAULT_LOW_WATER_MARK = 32 << 10;
  protected static final int DEFAULT_HIGH_WATER_MARK = 64 << 10;
  protected static final int DEFAULT_TYPE = 1;
  protected int low;
  protected int high;
  protected int type;

  protected AbstractWaterBufferMark() {
    this.low = DEFAULT_LOW_WATER_MARK;
    this.high = DEFAULT_HIGH_WATER_MARK;
    this.type = DEFAULT_TYPE;
  }

  protected AbstractWaterBufferMark(int low, int high, int type) {
    this.low = low;
    this.high = high;
    this.type = type;
  }

  public abstract int low();

  public abstract int high();
}
