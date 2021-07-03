package org.jdkstack.jdkserver.tcp.core.channel;

import java.nio.channels.spi.SelectorProvider;

public abstract class AbstractJdkChannel implements JdkChannel {
  protected static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
  protected final ChannelPipeline pipeline = new DefaultChannelPipeline();
}
