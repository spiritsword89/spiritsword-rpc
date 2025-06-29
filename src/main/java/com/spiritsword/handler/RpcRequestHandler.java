package com.spiritsword.handler;

import com.spiritsword.exceptions.ChannelNotRegisterException;
import com.spiritsword.model.MessagePayload;
import com.spiritsword.model.MessageType;
import com.spiritsword.server.ClientSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

import java.util.UUID;

public class RpcRequestHandler extends SimpleChannelInboundHandler<MessagePayload> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessagePayload message) throws Exception {
        MessageType requestType = message.getMessageType();

        if(requestType.equals(MessageType.REGISTER)) {
            registerClientIntoSession(message, ctx.channel());
        }

        if(requestType.equals(MessageType.CALL)) {

            Channel channel = ClientSessionManager.getClientChannel(message.getClientId());

            MessagePayload.RpcRequest request = (MessagePayload.RpcRequest) message.getPayload();

            if(channel == null) {
                throw new ChannelNotRegisterException(request.getRequestClientId());
            }

            forwardRequestToClient(message, channel);
        }

        if(requestType.equals(MessageType.RESPONSE)) {
            returnRequestToClient(message);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String clientId = ctx.channel().attr(AttributeKey.valueOf("clientId")).get().toString();
        ClientSessionManager.clearByClientId(clientId);
    }

    private void returnRequestToClient(MessagePayload message) {
        MessagePayload.RpcResponse response = (MessagePayload.RpcResponse) message.getPayload();
        String requestId = response.getRequestId();
        MessagePayload requestClient = ClientSessionManager.getRequestClient(requestId);
        Channel channel = ClientSessionManager.getClientChannel(requestClient.getClientId());

        channel.writeAndFlush(response);
    }

    private void forwardRequestToClient(MessagePayload message, Channel channel) {
        MessagePayload.RpcRequest request = (MessagePayload.RpcRequest) message.getPayload();
        request.setRequestId(UUID.randomUUID().toString());
        ClientSessionManager.putRequest(message);
        channel.writeAndFlush(request);
    }

    private void registerClientIntoSession(MessagePayload message, Channel channel) {
        ClientSessionManager.register(message.getClientId(), channel);
        channel.attr(AttributeKey.valueOf("clientId")).set(message.getClientId());
    }
}
