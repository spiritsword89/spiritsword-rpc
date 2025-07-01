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

public class RpcServer {
    private int port;
    private int bossGroupSize;
    private int workerGroupSize;
    private int backLogSize;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBossGroupSize() {
        return bossGroupSize;
    }

    public void setBossGroupSize(int bossGroupSize) {
        this.bossGroupSize = bossGroupSize;
    }

    public int getWorkerGroupSize() {
        return workerGroupSize;
    }

    public void setWorkerGroupSize(int workerGroupSize) {
        this.workerGroupSize = workerGroupSize;
    }

    public int getBackLogSize() {
        return backLogSize;
    }

    public void setBackLogSize(int backLogSize) {
        this.backLogSize = backLogSize;
    }

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
