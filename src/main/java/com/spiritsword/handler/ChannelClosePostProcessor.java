package com.spiritsword.handler;

import io.netty.channel.Channel;

public interface ChannelClosePostProcessor {
    public void channelBeforeClose(Channel channel);
    public void channelDidClose();
}
