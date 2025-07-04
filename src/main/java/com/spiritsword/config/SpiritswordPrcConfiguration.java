package com.spiritsword.config;

import com.spiritsword.server.RpcServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpiritswordPrcConfiguration {

    @Value("${spiritsword.rpc.server.port}")
    private int port;

    @Value("${spiritsword.rpc.server.boss}")
    private int bossGroupSize;

    @Value("${spiritsword.rpc.server.worker}")
    private int workerGroupSize;

    @Value("${spiritsword.rpc.server.backlog}")
    private int backLogSize;

    @Bean
    public RpcServer rpcServer() {
        RpcServer rpcServer = new RpcServer();
        rpcServer.setPort(port);
        rpcServer.setBossGroupSize(bossGroupSize);
        rpcServer.setWorkerGroupSize(workerGroupSize);
        rpcServer.setBackLogSize(backLogSize);
        rpcServer.startServer();
        return rpcServer;
    };
}
