package com.spiritsword.server;

import com.spiritsword.model.MessagePayload;
import io.netty.channel.Channel;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSessionManager {
    private static final Map<String, Channel> registerClients = new ConcurrentHashMap<>();
    private static final Map<String, MessagePayload> requestMap = new ConcurrentHashMap<>();

    public static void register(String clientId, Channel channel) {
        registerClients.put(clientId, channel);
    }

    public static boolean isClientRegistered(String clientId) {
        return registerClients.containsKey(clientId);
    }

    public static Channel getClientChannel(String clientId) {
        return registerClients.get(clientId);
    }

    public static void signOut(String clientId) {
        registerClients.remove(clientId);
    }

    public static void putRequest(MessagePayload message) {
        MessagePayload.RpcRequest payload = (MessagePayload.RpcRequest) message.getPayload();
        requestMap.put(payload.getRequestId(), message);
    }

    public static MessagePayload getRequestClient(String requestId) {
        return requestMap.get(requestId);
    }

    public static void deleteRequest(String requestId) {
        requestMap.remove(requestId);
    }

    public static void clear(String clientName, MessagePayload.RpcRequest request) {
        signOut(clientName);
        deleteRequest(request.getRequestId());
    }

    public static void clearByClientId(String clientId) {
        registerClients.remove(clientId);

        Set<Map.Entry<String, MessagePayload>> entries = requestMap.entrySet();
        Iterator<Map.Entry<String, MessagePayload>> iterator = entries.iterator();

        while(iterator.hasNext()) {
            Map.Entry<String, MessagePayload> next = iterator.next();
            MessagePayload messagePayload = next.getValue();

            if(messagePayload.getClientId().equals(clientId)) {
                iterator.remove();
            }
        }
    }
}
