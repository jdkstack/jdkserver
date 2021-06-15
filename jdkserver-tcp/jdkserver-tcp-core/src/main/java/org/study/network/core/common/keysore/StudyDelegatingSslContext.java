package org.study.network.core.common.keysore;

import io.netty.handler.ssl.DelegatingSslContext;
import io.netty.handler.ssl.SslContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-02-24 14:03
 * @since 2021-02-24 14:03:00
 */
public class StudyDelegatingSslContext extends DelegatingSslContext {

  private Collection<SNIMatcher> matchers;

  protected StudyDelegatingSslContext(SslContext ctx, Collection<SNIMatcher> matchers) {
    super(ctx);
    this.matchers = matchers;
  }

  @Override
  protected void initEngine(SSLEngine engine) {
    engine.setUseClientMode(false);
    engine.setNeedClientAuth(true);
    String[] protocols = {"TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3"};
    engine.setEnabledProtocols(protocols);
    SSLParameters sslParameters = engine.getSSLParameters();
    sslParameters.setSNIMatchers(matchers);
    engine.setSSLParameters(sslParameters);
  }
}
