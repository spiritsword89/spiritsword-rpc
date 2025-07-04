package com.spiritsword.handler;

import com.spiritsword.exceptions.ChannelNotRegisterException;
import com.spiritsword.exceptions.RemoteCallException;
import com.spiritsword.model.MessagePayload;
import com.spiritsword.model.MessageType;
import com.spiritsword.server.ClientSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RpcServerMessageHandler extends SimpleChannelInboundHandler<MessagePayload> {

    private static final Logger logger = LoggerFactory.getLogger(RpcServerMessageHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessagePayload message) {
        try {
            MessageType requestType = message.getMessageType();

            if(requestType.equals(MessageType.REGISTER)) {
                registerClientIntoSession(message, ctx.channel());
            }

            if(requestType.equals(MessageType.CALL)) {
                MessagePayload.RpcRequest request = (MessagePayload.RpcRequest) message.getPayload();

                if(request.getRequestClientId().equals(message.getClientId())) {
                    throw new RemoteCallException("Client Id and Request Client Id are the same.");
                }

                Channel channel = ClientSessionManager.getClientChannel(request.getRequestClientId());

                if(channel == null) {
                    throw new ChannelNotRegisterException(request.getRequestClientId());
                }

                forwardRequestToClient(message, channel);
            }

            if(requestType.equals(MessageType.RESPONSE)) {
                returnRequestToClient(message);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
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
        channel.writeAndFlush(message);
    }

    private void forwardRequestToClient(MessagePayload message, Channel channel) {
        ClientSessionManager.putRequest(message);
        message.setMessageType(MessageType.FORWARD);
        channel.writeAndFlush(message);
    }

    private void registerClientIntoSession(MessagePayload message, Channel channel) {
        ClientSessionManager.register(message.getClientId(), channel);
        channel.attr(AttributeKey.valueOf("clientId")).set(message.getClientId());
    }
}
