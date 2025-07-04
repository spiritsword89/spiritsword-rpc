package com.spiritsword.handler;

import com.spiritsword.client.RpcClient;
import com.spiritsword.model.MessagePayload;
import com.spiritsword.model.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RpcClientMessageHandler extends SimpleChannelInboundHandler<MessagePayload> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientMessageHandler.class);

    private final RpcClient rpcClient;

    public RpcClientMessageHandler(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessagePayload messagePayload) {
        try {
            if(messagePayload.getMessageType().equals(MessageType.FORWARD)) {
                processRequestAndGenerateResponse(messagePayload);
            } else if (messagePayload.getMessageType().equals(MessageType.RESPONSE)) {
                completeRequest(messagePayload);
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(!ctx.channel().isActive()){
            rpcClient.reconnect();
        }
    }

    private void processRequestAndGenerateResponse(MessagePayload messagePayload) throws Exception {
        rpcClient.processRequest(messagePayload);
    }

    private void completeRequest(MessagePayload messagePayload) {
        MessagePayload.RpcResponse response = (MessagePayload.RpcResponse) messagePayload.getPayload();
        rpcClient.completeRequest(response);
    }
}
