package org.jdkstack.jdkserver.tcp.core.option;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-03-03 14:05
 * @since 2021-03-03 14:05:00
 */
public class KeystoreOptions implements Options {
  private final String keyStorePassword;
  private final String keyStoreType;
  private final String keyStorePath;

  public KeystoreOptions(
      final String keyStorePassword, final String keyStoreType, final String keyStorePath) {
    this.keyStorePassword = keyStorePassword;
    this.keyStorePath = keyStorePath;
    this.keyStoreType = keyStoreType;
  }

  public String getKeyStorePassword() {
    return keyStorePassword;
  }

  public String getKeyStoreType() {
    return keyStoreType;
  }

  public String getKeyStorePath() {
    return keyStorePath;
  }
}
