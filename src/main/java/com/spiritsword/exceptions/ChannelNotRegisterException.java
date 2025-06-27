package com.spiritsword.exceptions;

public class ChannelNotRegisterException extends RuntimeException{
    public ChannelNotRegisterException(String clientName){
        super("The requested service : " + clientName + " is not registered");
    }
}
