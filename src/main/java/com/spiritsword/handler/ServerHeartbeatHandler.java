package com.spiritsword.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHeartbeatHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ServerHeartbeatHandler.class);

    private ChannelClosePostProcessor channelClosePostProcessor;

    public ServerHeartbeatHandler() {

    }

    public ServerHeartbeatHandler(ChannelClosePostProcessor channelClosePostProcessor) {
        this.channelClosePostProcessor = channelClosePostProcessor;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent event){
            if(event.state() == IdleState.READER_IDLE) {
                logger.info("Channel has been disconnected");

                Channel channel = ctx.channel();

                if(channelClosePostProcessor != null){
                    channelClosePostProcessor.channelBeforeClose(channel);
                }

                channel.close();

                if(channelClosePostProcessor != null){
                    channelClosePostProcessor.channelDidClose();
                }
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
