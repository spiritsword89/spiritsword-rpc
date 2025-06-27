package com.spiritsword.handler;

import com.alibaba.fastjson2.JSON;
import com.spiritsword.model.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class JsonDecodeHandler extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        byteBuf.markReaderIndex();
        int length = byteBuf.readInt();

        if(byteBuf.readableBytes() < length){
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        RpcRequest rpcRequest = JSON.parseObject(bytes, RpcRequest.class);

        list.add(rpcRequest);
    }
}
