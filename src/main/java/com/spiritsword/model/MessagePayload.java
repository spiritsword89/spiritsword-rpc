package com.spiritsword.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class MessagePayload implements Serializable {
    private String clientId;
    @JSONField(serializeUsing = MessageTypeSerializer.class, deserializeUsing = MessageTypeDeserializer.class)
    private MessageType messageType;
    private Object payload;

    public MessagePayload(RequestMessageBuilder requestMessageBuilder) {
        this.clientId = requestMessageBuilder.clientId;
        this.messageType = requestMessageBuilder.messageType;
        if (messageType.equals(MessageType.CALL)) {
            this.payload = new RpcRequest(requestMessageBuilder);
        } else if (messageType.equals(MessageType.RESPONSE)) {
            this.payload = new RpcResponse(requestMessageBuilder);
        }
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public static class RequestMessageBuilder {
        private String clientId;
        private String requestClientId;
        private String requestId;
        private String requestMethodName;
        private String requestedClassName;
        private MessageType messageType;
        private String[] paramTypes;
        private Object[] params;
        private Object returnValue;

        public RequestMessageBuilder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public RequestMessageBuilder setRequestClientId(String requestClientId) {
            this.requestClientId = requestClientId;
            return this;
        }

        public RequestMessageBuilder setMessageType(MessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        public RequestMessageBuilder setRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public RequestMessageBuilder setRequestMethodName(String requestMethodName) {
            this.requestMethodName = requestMethodName;
            return this;
        }

        public RequestMessageBuilder setRequestedClassName(String requestedClassName) {
            this.requestedClassName = requestedClassName;
            return this;
        }

        public RequestMessageBuilder setParamTypes(String[] paramTypes) {
            this.paramTypes = paramTypes;
            return this;
        }

        public RequestMessageBuilder setParams(Object[] params) {
            this.params = params;
            return this;
        }

        public RequestMessageBuilder setReturnValue(Object returnValue) {
            this.returnValue = returnValue;
            return this;
        }

        public MessagePayload build() {
            return new MessagePayload(this);
        }
    }

    public static class RpcRequest implements Serializable {
        private String requestClientId;
        private String requestId;
        private String requestMethodName;
        private String requestedClassName;
        private String[] paramTypes;
        private Object[] params;

        public RpcRequest(MessagePayload.RequestMessageBuilder builder) {
            this.requestClientId = builder.requestClientId;
            this.requestId = builder.requestId;
            this.requestMethodName = builder.requestMethodName;
            this.requestedClassName = builder.requestedClassName;
            this.paramTypes = builder.paramTypes;
            this.params = builder.params;
        }

        public String getRequestClientId() {
            return requestClientId;
        }

        public void setRequestClientId(String requestClientId) {
            this.requestClientId = requestClientId;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getRequestMethodName() {
            return requestMethodName;
        }

        public void setRequestMethodName(String requestMethodName) {
            this.requestMethodName = requestMethodName;
        }

        public String getRequestedClassName() {
            return requestedClassName;
        }

        public void setRequestedClassName(String requestedClassName) {
            this.requestedClassName = requestedClassName;
        }

        public String[] getParamTypes() {
            return paramTypes;
        }

        public void setParamTypes(String[] paramTypes) {
            this.paramTypes = paramTypes;
        }

        public Object[] getParams() {
            return params;
        }

        public void setParams(Object[] params) {
            this.params = params;
        }
    }

    public static class RpcResponse implements Serializable {
        private String requestId;
        private Object result;

        public RpcResponse(RequestMessageBuilder requestMessageBuilder) {
            this.requestId = requestMessageBuilder.requestId;
            this.result = requestMessageBuilder.returnValue;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }
    }

}
