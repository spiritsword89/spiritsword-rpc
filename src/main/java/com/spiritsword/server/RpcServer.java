package com.spiritsword.server;

import com.spiritsword.handler.JsonDecodeHandler;
import com.spiritsword.handler.JsonEncodeHandler;
import com.spiritsword.handler.RpcRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Value;

public class RpcServer {

    @Value("${server.port}")
    private int port;

    @Value("${spiritsword.rpc.boss}")
    private int bossGroupSize;

    @Value("${spiritsword.rpc.worker}")
    private int workerGroupSize;

    @Value("${spiritsword.rpc.backlog}")
    private int backLogSize;

    public void start() throws Exception{
        NioEventLoopGroup boss = new NioEventLoopGroup(bossGroupSize);
        NioEventLoopGroup worker = new NioEventLoopGroup(workerGroupSize);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, backLogSize)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new JsonDecodeHandler());
                        socketChannel.pipeline().addLast(new JsonEncodeHandler());
                        socketChannel.pipeline().addLast(new RpcRequestHandler());
                    }
                });

        ChannelFuture future = serverBootstrap.bind(port).sync();
        future.channel().close().sync();
    }
}
