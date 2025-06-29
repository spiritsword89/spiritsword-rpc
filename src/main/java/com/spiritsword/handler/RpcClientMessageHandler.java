package com.spiritsword.handler;

import com.spiritsword.client.RpcClient;
import com.spiritsword.model.MessagePayload;
import com.spiritsword.model.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcClientMessageHandler extends SimpleChannelInboundHandler<MessagePayload> {

    private RpcClient rpcClient;

    public RpcClientMessageHandler(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessagePayload messagePayload) throws Exception {
        if(messagePayload.getMessageType().equals(MessageType.FORWARD)) {
            processRequestAndGenerateResponse(messagePayload);
        } else if (messagePayload.getMessageType().equals(MessageType.RESPONSE)) {
            completeRequest(messagePayload);
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        rpcClient.registerChannel(ctx.channel());
    }

    private void processRequestAndGenerateResponse(MessagePayload messagePayload) {

    }

    private void completeRequest(MessagePayload messagePayload) {
        MessagePayload.RpcResponse response = (MessagePayload.RpcResponse) messagePayload.getPayload();
        rpcClient.completeRequest(response);
    }
}
