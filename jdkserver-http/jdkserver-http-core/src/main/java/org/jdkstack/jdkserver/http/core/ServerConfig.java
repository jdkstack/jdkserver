package org.jdkstack.jdkserver.http.core;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.security.PrivilegedAction;

/** Parameters that users will not likely need to set but are useful for debugging */
class ServerConfig {

  private static final int DEFAULT_CLOCK_TICK = 10000; // 10 sec.

  /* These values must be a reasonable multiple of clockTick */
  private static final long DEFAULT_IDLE_INTERVAL = 30; // 5 min
  private static final int DEFAULT_MAX_IDLE_CONNECTIONS = 200;

  private static final long DEFAULT_MAX_REQ_TIME = -1; // default: forever
  private static final long DEFAULT_MAX_RSP_TIME = -1; // default: forever
  private static final long DEFAULT_TIMER_MILLIS = 1000;
  private static final int DEFAULT_MAX_REQ_HEADERS = 200;
  private static final long DEFAULT_DRAIN_AMOUNT = 64 * 1024;

  private static int clockTick;
  private static long idleInterval;
  // The maximum number of bytes to drain from an inputstream
  private static long drainAmount;
  private static int maxIdleConnections;
  // The maximum number of request headers allowable
  private static int maxReqHeaders;
  // max time a request or response is allowed to take
  private static long maxReqTime;
  private static long maxRspTime;
  private static long timerMillis;
  private static boolean debug;

  // the value of the TCP_NODELAY socket-level option
  private static boolean noDelay;

  static {
    java.security.AccessController.doPrivileged(
        new PrivilegedAction<Void>() {
          @Override
          public Void run() {
            idleInterval = Long.getLong("idleInterval", DEFAULT_IDLE_INTERVAL) * 1000;

            clockTick = Integer.getInteger("clockTick", DEFAULT_CLOCK_TICK);

            maxIdleConnections =
                Integer.getInteger("maxIdleConnections", DEFAULT_MAX_IDLE_CONNECTIONS);

            drainAmount = Long.getLong("drainAmount", DEFAULT_DRAIN_AMOUNT);

            maxReqHeaders = Integer.getInteger("maxReqHeaders", DEFAULT_MAX_REQ_HEADERS);

            maxReqTime = Long.getLong("maxReqTime", DEFAULT_MAX_REQ_TIME);

            maxRspTime = Long.getLong("maxRspTime", DEFAULT_MAX_RSP_TIME);

            timerMillis = Long.getLong("timerMillis", DEFAULT_TIMER_MILLIS);

            debug = Boolean.getBoolean("debug");

            noDelay = Boolean.getBoolean("nodelay");

            return null;
          }
        });
  }

  static void checkLegacyProperties(final Logger logger) {

    // legacy properties that are no longer used
    // print a warning to logger if they are set.

    java.security.AccessController.doPrivileged(
        new PrivilegedAction<Void>() {
          public Void run() {
            if (System.getProperty("readTimeout") != null) {
              logger.log(
                  Level.WARNING,
                  "readTimeout " + "property is no longer used. " + "Use maxReqTime instead.");
            }
            if (System.getProperty("writeTimeout") != null) {
              logger.log(
                  Level.WARNING,
                  "writeTimeout " + "property is no longer used. Use " + "maxRspTime instead.");
            }
            if (System.getProperty("selCacheTimeout") != null) {
              logger.log(Level.WARNING, "selCacheTimeout " + "property is no longer used.");
            }
            return null;
          }
        });
  }

  static boolean debugEnabled() {
    return debug;
  }

  static long getIdleInterval() {
    return idleInterval;
  }

  static int getClockTick() {
    return clockTick;
  }

  static int getMaxIdleConnections() {
    return maxIdleConnections;
  }

  static long getDrainAmount() {
    return drainAmount;
  }

  static int getMaxReqHeaders() {
    return maxReqHeaders;
  }

  static long getMaxReqTime() {
    return maxReqTime;
  }

  static long getMaxRspTime() {
    return maxRspTime;
  }

  static long getTimerMillis() {
    return timerMillis;
  }

  static boolean noDelay() {
    return noDelay;
  }
}
