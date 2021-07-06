package org.jdkstack.jdkserver.tcp.core.core.channel;

import java.nio.channels.spi.SelectorProvider;
import org.jdkstack.jdkserver.tcp.core.api.core.channel.ChannelPipeline;
import org.jdkstack.jdkserver.tcp.core.api.core.channel.JdkChannel;

public abstract class AbstractJdkChannel implements JdkChannel {
  protected static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
  protected final ChannelPipeline pipeline = new DefaultChannelPipeline();
}
