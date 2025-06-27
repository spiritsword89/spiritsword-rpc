package com.spiritsword.server;

import com.spiritsword.model.RpcRequest;
import io.netty.channel.Channel;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSessionManager {
    private static final Map<String, Channel> registerClients = new ConcurrentHashMap<>();
    private static final Map<String, RpcRequest> requestMap = new ConcurrentHashMap<>();

    public static void register(RpcRequest request, Channel channel) {
        registerClients.put(request.getClientName(), channel);
    }

    public static boolean isClientRegistered(String clientName) {
        return registerClients.containsKey(clientName);
    }

    public static Channel getClientChannel(String clientName) {
        return registerClients.get(clientName);
    }

    public static void signOut(String clientName) {
        registerClients.remove(clientName);
    }

    public static void putRequest(RpcRequest request) {
        requestMap.put(request.getRequestId(), request);
    }

    public static RpcRequest getRequestClient(String requestId) {
        return requestMap.get(requestId);
    }

    public static Channel getClientChannelFromRequest(String requestId) {
        RpcRequest request = getRequestClient(requestId);
        return getClientChannel(request.getClientName());
    }

    public static void deleteRequest(String requestId) {
        requestMap.remove(requestId);
    }

    public static void clear(String clientName, RpcRequest request) {
        signOut(clientName);
        deleteRequest(request.getRequestId());
    }

    public static void clearByClientId(String clientId) {
        registerClients.remove(clientId);

        Set<Map.Entry<String, RpcRequest>> entries = requestMap.entrySet();
        Iterator<Map.Entry<String, RpcRequest>> iterator = entries.iterator();

        while(iterator.hasNext()) {
            Map.Entry<String, RpcRequest> next = iterator.next();
            RpcRequest request = next.getValue();

            if(request.getClientName().equals(clientId)) {
                iterator.remove();
            }
        }
    }
}
