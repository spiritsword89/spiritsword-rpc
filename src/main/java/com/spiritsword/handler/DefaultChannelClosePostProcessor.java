package com.spiritsword.handler;

import com.spiritsword.server.ClientSessionManager;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class DefaultChannelClosePostProcessor implements ChannelClosePostProcessor {

    @Override
    public void channelBeforeClose(Channel channel) {
        String clientId = channel.attr(AttributeKey.valueOf("clientId")).get().toString();
        ClientSessionManager.clearByClientId(clientId);
    }

    @Override
    public void channelDidClose() {

    }
}
