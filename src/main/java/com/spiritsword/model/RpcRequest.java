package com.spiritsword.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class RpcRequest implements Serializable {
    private String clientName;
    private String requestClientName;
    private String requestId;
    private MethodDescriptor methodDescriptors;
    @JSONField(serializeUsing = RequestTypeSerializer.class, deserializeUsing = RequestTypeDeserializer.class)
    private RequestType requestType;
    private Object returnValue;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getRequestClientName() {
        return requestClientName;
    }

    public void setRequestClientName(String requestClientName) {
        this.requestClientName = requestClientName;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public MethodDescriptor getMethodDescriptors() {
        return methodDescriptors;
    }

    public void setMethodDescriptors(MethodDescriptor methodDescriptors) {
        this.methodDescriptors = methodDescriptors;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }
}
