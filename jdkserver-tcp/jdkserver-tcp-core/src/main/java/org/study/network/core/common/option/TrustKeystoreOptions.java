package org.study.network.core.common.option;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-03-03 14:05
 * @since 2021-03-03 14:05:00
 */
public class TrustKeystoreOptions implements Options {

  private final String trustKeyStorePassword;
  private final String trustKeyStoreType;
  private final String trustKeyStorePath;

  public TrustKeystoreOptions(
      final String trustKeyStorePassword,
      final String trustKeyStoreType,
      final String trustKeyStorePath) {
    this.trustKeyStorePassword = trustKeyStorePassword;
    this.trustKeyStoreType = trustKeyStoreType;
    this.trustKeyStorePath = trustKeyStorePath;
  }

  public String getTrustKeyStorePassword() {
    return trustKeyStorePassword;
  }

  public String getTrustKeyStoreType() {
    return trustKeyStoreType;
  }

  public String getTrustKeyStorePath() {
    return trustKeyStorePath;
  }
}
