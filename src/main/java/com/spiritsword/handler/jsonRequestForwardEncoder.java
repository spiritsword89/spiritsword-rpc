package com.spiritsword.handler;

import com.alibaba.fastjson.JSON;
import com.spiritsword.model.MessagePayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class jsonRequestForwardEncoder extends MessageToByteEncoder<MessagePayload.RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MessagePayload.RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {
        byte[] jsonBytes = JSON.toJSONBytes(rpcRequest);

        byteBuf.writeInt(jsonBytes.length);
        byteBuf.writeBytes(jsonBytes);
    }
}
