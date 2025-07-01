package com.spiritsword.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodDescriptor {
    private String methodId;
    private String className;
    private Integer numOfParams;
    private String methodName;
    private List<String> parameterTypes;
    private boolean hasReturnValue;
    private String returnValueType;

    public static String generateMethodId(String methodName, int paramsCounts, String[] paramTypeSimpleNames, String returnValueTypeSimpleName) {
        String id = String.join(".", methodName, String.valueOf(paramsCounts));

        if(paramTypeSimpleNames != null) {
            for(String paramTypeSimpleName : paramTypeSimpleNames) {
                id = String.join(".", id, paramTypeSimpleName);
            }
        }

        if(returnValueTypeSimpleName != null ) {
            id = String.join(".", id, returnValueTypeSimpleName);
        }
        return id;
    }

    public static MethodDescriptor build(Method  method) {
        MethodDescriptor methodDescriptor = new MethodDescriptor();
        Class<?>[] paramTypes = method.getParameterTypes();
        methodDescriptor.setClassName(method.getClass().getName());
        methodDescriptor.setNumOfParams(method.getParameterCount());
        methodDescriptor.setMethodName(method.getName());

        String id = String.join(".", method.getName(), String.valueOf(method.getParameterCount()));
        List<String> parameterTypes = new ArrayList<>();
        for (Class<?> paramType : paramTypes) {
            parameterTypes.add(paramType.getName());
            id = String.join(".", id, paramType.getSimpleName());
        }

        methodDescriptor.setHasReturnValue(false);
        if(!method.getReturnType().equals(void.class)) {
            id = String.join(".", id,  method.getReturnType().getSimpleName());
            methodDescriptor.setHasReturnValue(true);
            methodDescriptor.setReturnValueType(method.getReturnType().getName());
        }

        methodDescriptor.setMethodId(id);

        methodDescriptor.setParameterTypes(parameterTypes);

        return methodDescriptor;
    }

    public String getMethodId() {
        return methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getNumOfParams() {
        return numOfParams;
    }

    public void setNumOfParams(Integer numOfParams) {
        this.numOfParams = numOfParams;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public boolean isHasReturnValue() {
        return hasReturnValue;
    }

    public void setHasReturnValue(boolean hasReturnValue) {
        this.hasReturnValue = hasReturnValue;
    }

    public String getReturnValueType() {
        return returnValueType;
    }

    public void setReturnValueType(String returnValueType) {
        this.returnValueType = returnValueType;
    }
}
