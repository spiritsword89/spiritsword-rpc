package com.spiritsword.handler;

import com.alibaba.fastjson.JSON;
import com.spiritsword.model.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class JsonEncodeHandler extends MessageToByteEncoder<RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {
        byte[] jsonBytes = JSON.toJSONBytes(rpcRequest);

        byteBuf.writeInt(jsonBytes.length);
        byteBuf.writeBytes(jsonBytes);
    }
}
