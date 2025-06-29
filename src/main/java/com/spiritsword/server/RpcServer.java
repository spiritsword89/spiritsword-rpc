package com.spiritsword.server;

import com.spiritsword.handler.JsonMessageDecodeHandler;
import com.spiritsword.handler.jsonRequestForwardEncoder;
import com.spiritsword.handler.RpcServerMessageHandler;
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

    @Value("${spiritsword.rpc.server.boss}")
    private int bossGroupSize;

    @Value("${spiritsword.rpc.server.worker}")
    private int workerGroupSize;

    @Value("${spiritsword.rpc.server.backlog}")
    private int backLogSize;

    public void start(){
        NioEventLoopGroup boss = new NioEventLoopGroup(bossGroupSize);
        NioEventLoopGroup worker = new NioEventLoopGroup(workerGroupSize);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, backLogSize)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new JsonMessageDecodeHandler());
                            socketChannel.pipeline().addLast(new jsonRequestForwardEncoder());
                            socketChannel.pipeline().addLast(new RpcServerMessageHandler());
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().close().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
