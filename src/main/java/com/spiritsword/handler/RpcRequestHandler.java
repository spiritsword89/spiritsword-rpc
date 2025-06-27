package com.spiritsword.handler;

import com.spiritsword.exceptions.ChannelNotRegisterException;
import com.spiritsword.model.RequestType;
import com.spiritsword.model.RpcRequest;
import com.spiritsword.server.ClientSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

import java.util.UUID;

public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        RequestType requestType = rpcRequest.getRequestType();

        if(requestType.equals(RequestType.REGISTER)) {
            registerClientIntoSession(rpcRequest, ctx.channel());
        }

        if(requestType.equals(RequestType.CALL)) {

            Channel channel = ClientSessionManager.getClientChannel(rpcRequest.getRequestClientName());

            if(channel == null) {
                throw new ChannelNotRegisterException(rpcRequest.getRequestClientName());
            }

            forwardRequestToClient(rpcRequest, channel);
        }

        if(requestType.equals(RequestType.RESPONSE)) {
            returnRequestToClient(rpcRequest, null);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String clientId = ctx.channel().attr(AttributeKey.valueOf("clientId")).get().toString();
        ClientSessionManager.clearByClientId(clientId);
    }

    private void returnRequestToClient(RpcRequest rpcRequest, Channel channel) {

    }

    private void forwardRequestToClient(RpcRequest rpcRequest, Channel channel) {
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        ClientSessionManager.putRequest(rpcRequest);
        channel.writeAndFlush(rpcRequest);
    }

    private void registerClientIntoSession(RpcRequest rpcRequest, Channel channel) {
        ClientSessionManager.register(rpcRequest, channel);
        channel.attr(AttributeKey.valueOf("clientId")).set(rpcRequest.getClientName());
    }
}
