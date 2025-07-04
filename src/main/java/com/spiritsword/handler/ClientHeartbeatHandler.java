package com.spiritsword.handler;

import com.spiritsword.model.MessagePayload;
import com.spiritsword.model.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ClientHeartbeatHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent event) {
            if(event.state() == IdleState.WRITER_IDLE) {
                MessagePayload payload = new MessagePayload.RequestMessageBuilder()
                        .setMessageType(MessageType.HEART_BEAT)
                        .build();
                ctx.writeAndFlush(payload);
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
